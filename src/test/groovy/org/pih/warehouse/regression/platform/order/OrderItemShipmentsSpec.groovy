package org.pih.warehouse.regression.platform.order

import org.pih.warehouse.order.*
import grails.testing.gorm.DataTest
import spock.lang.Specification

import org.pih.warehouse.core.Person
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode

class OrderItemShipmentsSpec extends Specification implements DataTest {

    Order order
    OrderItem orderItem
    OrderItem otherItem
    Shipment pending
    Shipment shipped
    ShipmentItem pendingItem
    ShipmentItem shippedItem

    void setupSpec() {
        mockDomains(Order, OrderItem, OrderItemSummary, PicklistItem, Shipment, ShipmentItem, Person, Product,
                UnitOfMeasure)
    }

    void setup() {
        order = new Order(orderNumber: 'PO-1').save(validate: false)
        orderItem = new OrderItem(order: order, quantity: 10).save(validate: false)
        otherItem = new OrderItem(order: order, quantity: 5).save(validate: false)
        pending = new Shipment(name: 'pending', currentStatus: ShipmentStatusCode.PENDING).save(validate: false)
        shipped = new Shipment(name: 'shipped', currentStatus: ShipmentStatusCode.SHIPPED).save(validate: false)
        pendingItem = new ShipmentItem(shipment: pending, quantity: 4, orderItems: [orderItem] as Set).save(validate: false)
        ShipmentItem otherPendingItem = new ShipmentItem(shipment: pending, quantity: 5, orderItems: [otherItem] as Set)
                .save(validate: false)
        shippedItem = new ShipmentItem(shipment: shipped, quantity: 6, orderItems: [orderItem] as Set).save(validate: false)
        pending.shipmentItems = [pendingItem, otherPendingItem] as Set
        shipped.shipmentItems = [shippedItem] as Set
        orderItem.shipmentItems = [pendingItem, shippedItem] as Set
        otherItem.shipmentItems = [otherPendingItem] as Set
        order.orderItems = [orderItem, otherItem] as Set
        order.save(validate: false, flush: true)
        // the shipment status is normally derived from its events
        pending.currentStatus = ShipmentStatusCode.PENDING
        shipped.currentStatus = ShipmentStatusCode.SHIPPED
    }

    void 'an order item knows its shipments and which of its shipment items are still pending'() {
        expect:
        orderItem.shipmentItems() as Set == [pendingItem, shippedItem] as Set
        orderItem.listShipments() as Set == [pending, shipped] as Set
        orderItem.pendingShipmentItems as List == [pendingItem]
        orderItem.shippedShipmentItems as List == [shippedItem]
        orderItem.hasShipmentAssociated()
        orderItem.hasShippedItems()
        order.pendingShipments == [pending]
        order.getShipmentsByStatus(ShipmentStatusCode.SHIPPED) == [shipped]
    }

    void 'the recipient is copied to the pending shipment items only'() {
        given:
        Person recipient = new Person(firstName: 'Ann', lastName: 'Lee').save(validate: false)
        orderItem.recipient = recipient

        when:
        orderItem.refreshPendingShipmentItemRecipients()

        then:
        pendingItem.recipient == recipient
        shippedItem.recipient == null
    }

    void 'picked quantities come from the picklist items of the order item'() {
        given:
        new PicklistItem(orderItem: orderItem, quantity: 3).save(validate: false)
        new PicklistItem(orderItem: orderItem, quantity: 2).save(validate: false)
        new PicklistItem(orderItem: otherItem, quantity: 9).save(validate: false, flush: true)

        expect:
        orderItem.totalQuantityPicked() == 5
        orderItem.retrievePicklistItems()*.quantity.sort() == [2, 3]
    }

    void 'item and order derived statuses come from the order item summaries'() {
        given:
        OrderItemSummary summary = new OrderItemSummary(order: order, derivedStatus: 'PARTIALLY_SHIPPED')
        summary.id = orderItem.id
        summary.save(validate: false, flush: true)

        expect:
        orderItem.orderItemStatus == 'PARTIALLY_SHIPPED'
        otherItem.orderItemStatus == null
        order.orderItemsDerivedStatus == [(orderItem.id): 'PARTIALLY_SHIPPED']
    }

    void 'invoiceable items, adjustments and shipment items are filtered by their own rules'() {
        given:
        OrderItem invoiceable = Stub(OrderItem) { isInvoiceable() >> true }
        OrderItem notInvoiceable = Stub(OrderItem) { isInvoiceable() >> false }
        OrderAdjustment adjustment = Stub(OrderAdjustment) { isInvoiceable() >> true }
        ShipmentItem shipmentItem = Stub(ShipmentItem) { isInvoiceable() >> true }
        Order stubbedOrder = new Order(orderItems: [invoiceable, notInvoiceable] as Set,
                orderAdjustments: [adjustment] as Set)
        OrderItem withShipments = new OrderItem(shipmentItems: [shipmentItem, Stub(ShipmentItem)] as Set)

        expect:
        stubbedOrder.invoiceableOrderItems == [invoiceable] as Set
        stubbedOrder.invoiceableAdjustments == [adjustment] as Set
        withShipments.invoiceableShipmentItems == [shipmentItem] as Set
    }

    void 'the unit of measure shows the package code and size, and the json lists the basics'() {
        given:
        Product product = new Product(name: 'Gloves')
        OrderItem boxed = new OrderItem(quantityUom: new UnitOfMeasure(code: 'BX'), quantityPerUom: 12.0,
                product: product, quantity: 2)

        expect:
        boxed.unitOfMeasure == 'BX/12'
        boxed.orderItemType == 'Product'
        boxed.toJson().subMap(['product', 'quantity']) == [product: product, quantity: 2]
    }

    void 'a fixed exchange rate on the order wins over the conversion table'() {
        expect:
        new Order(exchangeRate: 1.25).lookupCurrentExchangeRate() == 1.25
    }
}
