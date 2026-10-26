package org.pih.warehouse.regression.platform.core

import spock.lang.PendingFeature

import org.pih.warehouse.core.*
import grails.gorm.PagedResultList
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.hibernate.Cache
import org.hibernate.SessionFactory
import org.hibernate.stat.Statistics
import spock.lang.Specification
import org.pih.warehouse.admin.AdminController
import org.pih.warehouse.data.DataService
import org.pih.warehouse.order.OrderService
import org.pih.warehouse.product.Product

class SupplierControllerSpec extends Specification implements ControllerUnitTest<SupplierController>, DataTest {

    Organization acme
    Product aspirin

    void setupSpec() {
        mockDomains(Organization, PartyType, Product, Location)
    }

    void setup() {
        acme = new Organization(code: "ACME", name: "Acme").save(validate: false)
        aspirin = new Product(name: "Aspirin", productCode: "AS01").save(validate: false)
        controller.orderService = Mock(OrderService)
    }

    void "list pages through the suppliers matching the query"() {
        given:
        controller.locationService = Mock(LocationService)
        PagedResultList result = new PagedResultList(null)
        result.totalCount = 21

        when:
        params.q = "ac"
        params.offset = "20"
        def model = controller.list()

        then:
        1 * controller.locationService.getSuppliers("ac", 10, 20) >> result
        model.suppliers.is(result)
        model.suppliersTotal == 21
    }

    void "show renders the supplier with its documents"() {
        given:
        controller.documentService = Mock(DocumentService)

        when:
        params.id = acme.id
        def model = controller.show()

        then:
        1 * controller.documentService.getAllDocumentsBySupplierOrganization(acme) >> []
        model.supplier == acme
        model.documents == []
    }

    void "getPriceHistory renders the order lines as JSON"() {
        when:
        params.supplierId = acme.id
        params.productId = aspirin.id
        params.q = "PO"
        controller.getPriceHistory()

        then:
        1 * controller.orderService.getOrderItemsForPriceHistory(acme, aspirin, "PO") >> [[orderNumber: "PO-1"]]
        response.json.aaData[0].orderNumber == "PO-1"
    }

    void "getPriceHistory downloads unit prices per unit of measure as CSV"() {
        given:
        Map row = [orderNumber: "PO-1", dateCreated: Date.parse("yyyy-MM-dd", "2024-02-03"), description: "Order",
                   productCode: "AS01", productName: "Aspirin", unitPrice: 20, quantityPerUom: 10]

        when:
        params.supplierId = acme.id
        params.format = "text/csv"
        controller.getPriceHistory()
        List<String> lines = response.text.readLines()

        then:
        1 * controller.orderService.getOrderItemsForPriceHistory(acme, null, null) >> [row]
        response.getHeader("Content-disposition") == 'attachment; filename="Price-History.csv"'
        lines[0].startsWith('"Order Number","Date Created"')
        lines[1].startsWith('"PO-1","02/03/2024","Order","AS01","Aspirin","","","",""')
        lines[1].endsWith('"2"')
    }
}

class AdminControllerSpec extends Specification implements ControllerUnitTest<AdminController>, DataTest {

    void "evictDomainCache evicts a known domain class and reports unknown ones"() {
        given:
        controller.sessionFactory = Mock(SessionFactory)

        when:
        params.name = "missing.Domain"
        controller.evictDomainCache()

        then:
        0 * controller.sessionFactory.evict(_)
        flash.message == "Domain cache 'missing.Domain' does not exist"
        response.redirectedUrl == "/admin/showSettings"
    }

    @PendingFeature(reason = 'controllerActions reads Grails 2 controller.reference closures and throws MissingPropertyException')
    void "controllerActions lists the actions of every controller"() {
        when:
        def model = controller.controllerActions()

        then:
        model.actionNames.contains("admin.cache.label = cache")
        model.actionNames.contains("admin.evictQueryCache.label = evictQueryCache")
    }

    @PendingFeature(reason = 'evictDomainCache calls sessionFactory.evict, which Hibernate 5 SessionFactory lacks')
    void "evictDomainCache evicts the cached entities of a known domain class"() {
        given:
        Cache cache = Mock()
        mockDomain(Product)
        controller.sessionFactory = Stub(SessionFactory) { getCache() >> cache }

        when:
        params.name = "org.pih.warehouse.product.Product"
        controller.evictDomainCache()

        then:
        1 * cache.evictEntityRegion(Product)
        flash.message == "Domain cache 'org.pih.warehouse.product.Product' was invalidated"
        response.redirectedUrl == "/admin/showSettings"
    }

    @PendingFeature(reason = 'evictQueryCache calls sessionFactory.evictQueries, which Hibernate 5 SessionFactory lacks')
    void "evictQueryCache evicts one named query cache or all of them"() {
        given:
        Cache cache = Mock()
        controller.sessionFactory = Stub(SessionFactory) { getCache() >> cache }

        when:
        params.name = "reports"
        controller.evictQueryCache()

        then:
        1 * cache.evictQueryRegion("reports")
        0 * cache.evictQueryRegions()
        flash.message == "Query cache 'reports' was invalidated"
        response.redirectedUrl == "/admin/showSettings"

        when:
        response.reset()
        params.remove("name")
        controller.evictQueryCache()

        then:
        1 * cache.evictQueryRegions()
        0 * cache.evictQueryRegion(_)
        flash.message == "All query caches were invalidated"
    }

    void "the database status pages render the query results"() {
        given:
        controller.dataService = Mock(DataService)

        when:
        controller.showDatabaseStatus()

        then:
        1 * controller.dataService.executeQuery("show engine innodb status") >> [[Status: "all good"]]
        response.text == "<pre>all good</pre>"

        when:
        response.reset()
        controller.showDatabaseProcessList()

        then:
        1 * controller.dataService.executeQuery("show processlist") >> ["p1", "p2"]
        response.text == "<pre>p1<br/>p2</pre>"
    }

    void "sendMail shows the form on GET and rejects a POST without a valid form token"() {
        when:
        controller.sendMail()

        then:
        view == "/admin/sendMail"
        flash.message == null

        when:
        response.reset()
        request.method = "POST"
        controller.sendMail()

        then:
        flash.message == "Invalid token"
        view == "/admin/sendMail"
    }

    void "showSettings reports the mail configuration"() {
        given:
        config.grails.mail.enabled = "true"

        when:
        def model = controller.showSettings()

        then:
        model.enabled == true
        model.containsKey("printServices")
        model.containsKey("host")
    }

}
