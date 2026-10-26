package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import spock.lang.Specification

/**
 * After printing a label the user goes back to the page they printed from, when that page is in the application.
 */
class DocumentControllerPrintRedirectSpec extends Specification implements ControllerUnitTest<DocumentController>, DataTest {

    Product product

    void setupSpec() {
        mockDomains(Document, DocumentType, Location, InventoryItem, Product)
    }

    void setup() {
        controller.beanPropertyTemplateService = new BeanPropertyTemplateService()
        DocumentType zebraType = new DocumentType(name: "Zebra", documentCode: DocumentCode.ZEBRA_TEMPLATE).save(validate: false)
        Document template = new Document(name: "label", filename: "label.zpl", fileContents: '^XA^FD${product.productCode}^FS^XZ'.bytes,
                documentType: zebraType).save(validate: false)
        Location facility = new Location(name: "Main Warehouse").save(validate: false)
        product = new Product(productCode: "AB123", name: "Gauze").save(validate: false)
        session.warehouse = [id: facility.id]
        request.serverName = 'obnav.example.org'
        request.contextPath = '/openboxes'
        params.id = template.id
        params.product = [id: product.id]
        // No printer protocol: the print fails before anything is sent, and the action still redirects
        params.protocol = 'none'
    }

    void "printZebraTemplate returns to the application page the label was printed from"() {
        given:
        String stockCard = "https://obnav.example.org/openboxes/inventoryItem/showStockCard/${product.id}?tab=2"
        request.addHeader('Referer', stockCard)

        when:
        controller.printZebraTemplate()

        then:
        response.redirectedUrl == stockCard
    }

    void "printZebraTemplate goes to the product's stock card when the Referer is another site"() {
        given:
        request.addHeader('Referer', 'https://elsewhere.example.com/openboxes/inventoryItem/showStockCard/x')

        when:
        controller.printZebraTemplate()

        then:
        response.redirectedUrl.endsWith("/inventoryItem/showStockCard/${product.id}")
        !response.redirectedUrl.contains('elsewhere')
    }

    void "printZebraTemplate goes to the location list for a bin label when the Referer is another site"() {
        given:
        params.remove('product')
        params.location = [id: new Location(name: "A-01", locationNumber: "A-01").save(validate: false).id]
        request.addHeader('Referer', '//elsewhere.example.com/openboxes/location/list')

        when:
        controller.printZebraTemplate()

        then:
        response.redirectedUrl.endsWith("/location/list")
        !response.redirectedUrl.contains('elsewhere')
    }
}
