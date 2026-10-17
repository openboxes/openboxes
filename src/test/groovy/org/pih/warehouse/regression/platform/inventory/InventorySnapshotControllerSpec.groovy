package org.pih.warehouse.regression.platform.inventory

import spock.lang.PendingFeature

import spock.lang.Ignore

import org.pih.warehouse.inventory.*
import grails.converters.JSON
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.data.DataService
import org.pih.warehouse.jobs.RefreshInventorySnapshotJob
import org.pih.warehouse.product.Product

class InventorySnapshotControllerSpec extends Specification implements ControllerUnitTest<InventorySnapshotController>, DataTest {

    Location depot
    Product product
    User user

    void setupSpec() {
        mockDomains(Location, Product, User)
    }

    void setup() {
        depot = new Location(name: 'Main depot').save(validate: false)
        product = new Product(name: 'Paracetamol', productCode: 'PC1').save(validate: false)
        user = new User(username: 'manager', email: 'manager@example.com').save(validate: false)
        session.warehouse = depot
        session.user = user

        controller.inventorySnapshotService = Mock(InventorySnapshotService)
        controller.dataService = Mock(DataService)
    }

    void 'index redirects to list and the plain pages have no model'() {
        when:
        controller.index()

        then:
        response.redirectedUrl == '/inventorySnapshot/list'

        expect:
        controller.list() == null
        controller.show() == null
        controller.edit() == null
    }

    void 'update triggers the snapshot job for the given day'() {
        given:
        GroovySpy(RefreshInventorySnapshotJob, global: true)
        params['location.id'] = depot.id
        params.date = '03/15/2026'

        when:
        controller.update()

        then:
        1 * RefreshInventorySnapshotJob.triggerNow({ it.locationId == depot.id && it.date == new Date(126, 2, 15) }) >> 'scheduled'
        JSON.parse(response.text).results == 'scheduled'
    }

    void 'triggerRefreshInventorySnapshotJob forces a refresh for the product'() {
        given:
        GroovySpy(RefreshInventorySnapshotJob, global: true)
        params['product.id'] = product.id
        params['location.id'] = depot.id

        when:
        controller.triggerRefreshInventorySnapshotJob()

        then:
        1 * RefreshInventorySnapshotJob.triggerNow([productId: product.id, locationId: depot.id, forceRefresh: true]) >> 'ok'
        JSON.parse(response.text).started == true
    }

    void 'refresh triggers the job for the user and tells them about the email'() {
        given:
        GroovySpy(RefreshInventorySnapshotJob, global: true)
        params['location.id'] = depot.id
        params.date = '01/02/2026'

        when:
        controller.refresh()

        then:
        1 * RefreshInventorySnapshotJob.triggerNow({ it.userId == user.id && it.locationId == depot.id && it.date == new Date(126, 0, 2) }) >> null
        JSON.parse(response.text).message.contains("manager@example.com")
    }

    void 'dates come from the service'() {
        when:
        controller.dates()

        then:
        1 * controller.inventorySnapshotService.getTransactionDates() >> ['2026-01-01']
        JSON.parse(response.text) == ['2026-01-01']
    }

    void 'download exports the snapshot of the current location as CSV'() {
        given:
        params.date = '01/02/2026'

        when:
        controller.download()

        then:
        1 * controller.inventorySnapshotService.findInventorySnapshotByDateAndLocation(new Date(126, 0, 2), depot) >> [[code: 'PC1']]
        1 * controller.dataService.generateCsv([[code: 'PC1']]) >> 'code\nPC1'
        response.getHeader('Content-disposition').contains('Stock-Main depot-02 Jan 2026.csv')
        response.text == 'code\nPC1'
    }

    void 'transactionDates are product specific when a product is given'() {
        when:
        params.id = product.id
        controller.transactionDates()

        then:
        1 * controller.inventorySnapshotService.getTransactionDates(depot, product) >> ['d1']
        JSON.parse(response.text).dates == ['d1']

        when:
        response.reset()
        params.id = 'missing'
        controller.transactionDates()

        then:
        1 * controller.inventorySnapshotService.getTransactionDates() >> ['d2']
        JSON.parse(response.text).dates == ['d2']
    }

    @PendingFeature(reason = 'OBAF refresh has no return after rendering the error, so it renders twice')
    void 'refresh reports a failed trigger once'() {
        given:
        GroovySpy(RefreshInventorySnapshotJob, global: true)
        params['location.id'] = depot.id
        List rendered = []
        controller.metaClass.render = { JSON payload -> rendered << payload.toString() }

        when:
        controller.refresh()

        then:
        1 * RefreshInventorySnapshotJob.triggerNow(_) >> { throw new IllegalStateException('scheduler down') }
        rendered == ['{"message":"An error occurred: scheduler down"}']
    }

    interface LocationServiceContract {
        List getDepots()
    }
}
