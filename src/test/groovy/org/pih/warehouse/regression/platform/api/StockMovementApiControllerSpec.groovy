package org.pih.warehouse.regression.platform.api

import spock.lang.PendingFeature

import org.pih.warehouse.api.*
import grails.testing.web.controllers.ControllerUnitTest
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.OutboundStockMovement
import org.pih.warehouse.inventory.OutboundStockMovementService
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.order.Order
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.stockTransfer.StockTransferService

/**
 * Stock movement API paths the integration tests cannot reasonably reach: deleting return orders and invoiced
 * shipments, failures inside the services, and a missing template file.
 */
class StockMovementApiControllerSpec extends Specification implements ControllerUnitTest<StockMovementApiController> {

    OutboundStockMovementService outboundStockMovementService = Stub()
    StockMovementService stockMovementService = Mock()
    StockTransferService stockTransferService = Mock()

    void setup() {
        controller.outboundStockMovementService = outboundStockMovementService
        controller.stockMovementService = stockMovementService
        controller.stockTransferService = stockTransferService
        Location.metaClass.static.get = { Serializable id -> null }
        params.id = 'sm-1'
    }

    void cleanup() {
        GroovySystem.metaClassRegistry.removeMetaClass(Location)
    }

    private Map json() {
        return new JsonSlurper().parseText(response.text) as Map
    }

    /** An outbound stock movement that may be deleted from here, with an order or a shipment if given. */
    private OutboundStockMovement stockMovement(Map state) {
        return Stub(OutboundStockMovement) {
            getOrder() >> state.order
            getShipment() >> state.shipment
            isDeleteOrRollbackAuthorized(_) >> true
            isPending() >> true
        }
    }

    void 'a return order is deleted through the stock transfer service'() {
        given:
        OutboundStockMovement returnOrder = stockMovement(order: new Order())
        outboundStockMovementService.getStockMovement('sm-1') >> returnOrder

        when:
        controller.delete()

        then:
        1 * stockTransferService.deleteStockTransfer(returnOrder)
        response.status == 204
    }

    @PendingFeature(reason = 'the refused delete answers {errorMessage} without the API error shape (no errorCode)')
    void 'a return order that cannot be deleted answers 400'() {
        given:
        outboundStockMovementService.getStockMovement('sm-1') >> stockMovement(order: new Order())
        stockTransferService.deleteStockTransfer(_) >> { throw new IllegalStateException('in use') }

        when:
        controller.delete()

        then:
        response.status == 400
        json() == [errorCode: 400, errorMessage: 'The Stock Movement could not be deleted']
    }

    @PendingFeature(reason = 'deleting an invoiced stock movement answers 400 instead of 409 in the error shape')
    void 'a stock movement whose shipment has invoiced items cannot be deleted (409)'() {
        given:
        outboundStockMovementService.getStockMovement('sm-1') >> stockMovement(shipment: Stub(Shipment) { hasInvoicedItem() >> true })

        when:
        controller.delete()

        then:
        0 * stockMovementService.deleteStockMovement(_)
        response.status == 409
        json().errorCode == 409
    }

    @PendingFeature(reason = 'the failed delete answers {errorMessage} without the API error shape (no errorCode)')
    void 'a failure while deleting answers 400 without the internal reason'() {
        given:
        outboundStockMovementService.getStockMovement('sm-1') >> stockMovement([:])
        stockMovementService.deleteStockMovement(_) >> { throw new RuntimeException('constraint fk_123') }

        when:
        controller.delete()

        then:
        response.status == 400
        json() == [errorCode: 400, errorMessage: 'The Stock Movement could not be deleted']
    }

    @PendingFeature(reason = 'rollbackApproval renders the error and then render(status: 200): response already committed')
    void 'an unexpected failure while rolling back an approval answers 500, once'() {
        given:
        stockMovementService.rollbackApproval('sm-1') >> { throw new IllegalStateException('database down') }

        when:
        controller.rollbackApproval()

        then:
        response.status == 500
        json().errorCode == 500
        json().errorMessage.contains('database down')
    }

    @PendingFeature(reason = 'rollbackApproval answers 500 then 200 (double render) instead of one 403')
    void 'rolling back an approval without the permission answers 403'() {
        given:
        stockMovementService.rollbackApproval('sm-1') >> { throw new IllegalAccessException('insufficient permissions') }

        when:
        controller.rollbackApproval()

        then:
        response.status == 403
        json().errorCode == 403
    }

    @PendingFeature(reason = 'a missing packing list template answers a bare 404 with an empty body')
    void 'a missing packing list template answers 404 in the error shape'() {
        given:
        controller.documentService = Stub(DocumentService) {
            findFile(_) >> { throw new FileNotFoundException('templates/completedPackingList.xls') }
        }

        when:
        controller.downloadPackingListTemplate()

        then:
        response.status == 404
        json() == [errorCode: 404, errorMessage: 'Packing list template not found']
    }

}
