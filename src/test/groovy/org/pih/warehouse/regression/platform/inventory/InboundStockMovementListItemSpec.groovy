package org.pih.warehouse.regression.platform.inventory

import spock.lang.Ignore
import org.pih.warehouse.inventory.*
import grails.testing.gorm.DataTest
import grails.testing.web.taglib.TagLibUnitTest
import org.grails.plugins.web.taglib.ApplicationTagLib
import spock.lang.Specification

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode

class InboundStockMovementListItemSpec extends Specification implements TagLibUnitTest<ApplicationTagLib>, DataTest {

    void setupSpec() {
        mockDomains(Order, OrderItem, OrderType, Requisition, RequisitionItem, Shipment, Location, LocationType)
        mockTagLib(ApplicationTagLib)
    }

    void 'the status comes from the requisition, then the shipment, then the order'() {
        expect:
        new InboundStockMovementListItem(requisition: new Requisition(status: RequisitionStatus.VERIFYING),
                shipment: new Shipment()).status == 'VERIFYING'
        new InboundStockMovementListItem(shipment: new Shipment()).status == 'PENDING'
        new InboundStockMovementListItem(order: new Order(status: OrderStatus.PLACED)).status == 'PLACED'
        new InboundStockMovementListItem().status == null
    }

    void 'shipment progress flags follow the current shipment status'() {
        when:
        InboundStockMovementListItem item = new InboundStockMovementListItem(shipment: new Shipment(currentStatus: status))

        then:
        [item.pending, item.received, item.partiallyReceived] == expected

        where:
        status                                || expected
        ShipmentStatusCode.PENDING            || [true, false, false]
        ShipmentStatusCode.RECEIVED           || [false, true, false]
        ShipmentStatusCode.PARTIALLY_RECEIVED || [false, false, true]
        ShipmentStatusCode.SHIPPED            || [false, false, false]
    }

    void 'items from a return order are flagged as returns'() {
        given:
        OrderType returnType = new OrderType(code: Constants.RETURN_ORDER)

        expect:
        new InboundStockMovementListItem(order: new Order(orderType: returnType)).fromReturnOrder
        !new InboundStockMovementListItem(order: new Order(orderType: new OrderType(code: 'PURCHASE_ORDER'))).fromReturnOrder
        !new InboundStockMovementListItem().fromReturnOrder
    }

    void 'the line item count comes from the shipment or the order'() {
        given:
        Order order = new Order(orderNumber: 'PO-1').save(validate: false)
        2.times { new OrderItem(order: order).save(validate: false, flush: true) }

        expect:
        new InboundStockMovementListItem(shipment: new Shipment(shipmentItemCount: 4)).lineItemCount == 4
        new InboundStockMovementListItem(shipment: new Shipment()).lineItemCount == 0
        new InboundStockMovementListItem(order: order).lineItemCount == 2
        new InboundStockMovementListItem().lineItemCount == 0
    }

}
