package org.pih.warehouse.regression.platform.inventory

import spock.lang.PendingFeature

import org.pih.warehouse.inventory.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.stockTransfer.StockTransferService

class StockTransferControllerSpec extends Specification implements ControllerUnitTest<StockTransferController>, DataTest {

    Location depot
    Location ward
    Location elsewhere

    void setupSpec() {
        mockDomains(Location, LocationType, Order)
    }

    void setup() {
        LocationType type = new LocationType(name: 'Depot', locationTypeCode: LocationTypeCode.DEPOT).save(validate: false)
        depot = new Location(name: 'Main depot', locationType: type).save(validate: false)
        ward = new Location(name: 'Ward', locationType: type).save(validate: false)
        elsewhere = new Location(name: 'Elsewhere', locationType: type).save(validate: false)
        session.warehouse = depot
        controller.metaClass.warehouse = [message: { Map attrs -> attrs.code }]
        controller.stockTransferService = Mock(StockTransferService)
    }

    private Order order(Location origin, Location destination, OrderStatus status = OrderStatus.PENDING) {
        return new Order(name: 'Return', origin: origin, destination: destination, status: status).save(validate: false)
    }

    void 'index, create, list and the return workflows use the react page'() {
        when:
        controller.index()

        then:
        response.redirectedUrl == '/stockTransfer/list'

        when:
        response.reset()
        controller."$action"()

        then:
        view == '/common/react'

        where:
        action << ['create', 'createOutboundReturn', 'createInboundReturn', 'list']
    }

    void 'edit opens the return workflow for the direction of the current location'() {
        given:
        Order outbound = order(depot, ward)
        Order inbound = order(ward, depot)

        when:
        params.id = outbound.id
        controller.edit()

        then:
        response.redirectedUrl.startsWith('/stockTransfer/createOutboundReturn')

        when:
        response.reset()
        params.id = inbound.id
        controller.edit()

        then:
        response.redirectedUrl.startsWith('/stockTransfer/createInboundReturn')
    }

    void 'edit refuses returns of other locations and received returns'() {
        given:
        Order unrelated = order(ward, elsewhere)
        Order received = order(depot, ward, OrderStatus.RECEIVED)

        when:
        params.id = unrelated.id
        controller.edit()

        then:
        flash.error != null
        response.redirectedUrl == "/stockMovement/show/${unrelated.id}"

        when:
        response.reset()
        flash.clear()
        params.id = received.id
        controller.edit()

        then:
        flash.error != null
        response.redirectedUrl == "/stockMovement/show/${received.id}"
    }

    void 'show returns the order or redirects to list'() {
        given:
        Order existing = order(depot, ward)

        when:
        params.id = existing.id
        def model = controller.show()

        then:
        model.orderInstance == existing

        when:
        params.id = 'missing'
        controller.show()

        then:
        flash.message == 'default.not.found.message'
        response.redirectedUrl == '/stockTransfer/list'
    }

    void 'print returns the stock transfer'() {
        given:
        Order existing = order(depot, ward)
        params.id = existing.id

        expect:
        controller.print().stockTransfer == existing
    }

    void 'eraseStockTransfer deletes the transfer'() {
        given:
        params.id = 'o1'

        when:
        controller.eraseStockTransfer()

        then:
        1 * controller.stockTransferService.deleteStockTransfer('o1')
        response.redirectedUrl == '/stockTransfer/list'
    }

    @PendingFeature(reason = 'OBAF eraseStockTransfer redirects in the catch and again after it (CannotRedirectException)')
    void 'eraseStockTransfer reports a missing transfer'() {
        given:
        params.id = 'missing'

        when:
        controller.eraseStockTransfer()

        then:
        1 * controller.stockTransferService.deleteStockTransfer('missing') >> { throw new IllegalArgumentException('not found') }
        flash.message == 'default.not.found.message'
        response.redirectedUrl == '/stockTransfer/list'
    }

    void 'remove deletes the transfer and returns to the list for its direction'() {
        given:
        Order outbound = order(depot, ward)
        Order inbound = order(ward, depot)
        Order unrelated = order(ward, elsewhere)

        when:
        params.id = outbound.id
        controller.remove()

        then:
        1 * controller.stockTransferService.deleteStockTransfer(outbound.id)
        flash.message == 'Stock Movement has been deleted successfully'
        response.redirectedUrl.startsWith('/stockMovement/list')
        response.redirectedUrl.contains('direction=OUTBOUND')

        when:
        response.reset()
        params.remove('direction')
        params.id = inbound.id
        controller.remove()

        then:
        1 * controller.stockTransferService.deleteStockTransfer(inbound.id)
        response.redirectedUrl.contains('direction=INBOUND')

        when:
        response.reset()
        params.remove('direction')
        params.id = unrelated.id
        controller.remove()

        then:
        1 * controller.stockTransferService.deleteStockTransfer(unrelated.id)
        response.redirectedUrl.startsWith('/stockTransfer/list')
    }

    void 'remove reports a failed delete and an unknown order'() {
        given:
        Order outbound = order(depot, ward)

        when:
        params.id = outbound.id
        controller.remove()

        then:
        1 * controller.stockTransferService.deleteStockTransfer(outbound.id) >> { throw new IllegalStateException('shipped') }
        flash.message == 'The Stock Movement could not be deleted'
        response.redirectedUrl == "/stockMovement/show/${outbound.id}"

        when:
        response.reset()
        params.id = 'missing'
        controller.remove()

        then:
        thrown(IllegalArgumentException)
    }

    void 'rollback rolls back the return order and reports errors'() {
        given:
        params.id = 'o1'

        when:
        controller.rollback()

        then:
        1 * controller.stockTransferService.rollbackReturnOrder('o1', depot)
        flash.message == 'Successfully rolled back return order with ID o1'
        response.redirectedUrl == '/stockMovement/show/o1'

        when:
        response.reset()
        controller.rollback()

        then:
        1 * controller.stockTransferService.rollbackReturnOrder('o1', depot) >> { throw new IllegalArgumentException('Already received') }
        flash.message == 'Already received'
    }
}
