// Companion spec: extra features for upstream's ShipmentItemSpec (the upstream file stays authoritative).
/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.regression.platform.shipping

import spock.lang.PendingFeature

import org.pih.warehouse.shipping.*
import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ShipmentItem
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.PaymentTerm
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.LotStatusCode
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.receiving.ReceiptStatusCode
import org.pih.warehouse.requisition.RequisitionItem

@Unroll
class ShipmentItemMoreSpec extends Specification implements DomainUnitTest<ShipmentItem>, DataTest {

    void setupSpec() {
        mockDomains(Shipment, Container, Product, InventoryItem, Location, Receipt, ReceiptItem, Order, OrderItem,
                OrderType, PaymentTerm, InvoiceItem, RequisitionItem, PicklistItem, Person, UnitOfMeasure)
    }

    void 'received and canceled quantities should only count receipt items of received receipts for the same product'() {
        given:
        Product product = new Product(name: 'product')
        ShipmentItem shipmentItem = new ShipmentItem(product: product, quantity: 10)
        shipmentItem.receiptItems = [
                buildReceiptItem(product, ReceiptStatusCode.RECEIVED, 5, 1),
                buildReceiptItem(product, ReceiptStatusCode.PENDING, 3, 2),
                buildReceiptItem(new Product(name: 'other product'), ReceiptStatusCode.RECEIVED, 7, 4),
                buildReceiptItem(product, ReceiptStatusCode.RECEIVED, null, null),
        ] as Set

        expect:
        assert shipmentItem.quantityReceived == 5
        assert shipmentItem.quantityCanceled == 1
        assert shipmentItem.quantityReceived() == 5
        assert shipmentItem.quantityCanceled() == 1
        assert shipmentItem.quantityReceivedAndCanceled == 6
        assert shipmentItem.quantityRemaining == 4
        assert !shipmentItem.isFullyReceived()
    }

    void 'received and canceled quantities should be zero when nothing has been received'() {
        given:
        ShipmentItem shipmentItem = new ShipmentItem(product: new Product(name: 'product'), quantity: 10)

        expect:
        assert shipmentItem.quantityReceived == 0
        assert shipmentItem.quantityCanceled == 0
        assert shipmentItem.quantityRemaining == 10
        assert !shipmentItem.isFullyReceived()
    }

    void 'isFullyReceived should be #expected when #received are received and #canceled canceled out of 10'() {
        given:
        Product product = new Product(name: 'product')
        ShipmentItem shipmentItem = new ShipmentItem(product: product, quantity: 10)
        shipmentItem.receiptItems = [buildReceiptItem(product, ReceiptStatusCode.RECEIVED, received, canceled)] as Set

        expect:
        assert shipmentItem.isFullyReceived() == expected

        where:
        received | canceled || expected
        10       | 0        || true
        6        | 4        || true
        0        | 10       || true
        12       | 0        || true
        6        | 3        || false
        0        | 0        || false
    }

    void 'lot number and expiration date should come from the inventory item when there is one'() {
        given:
        Date lotExpiry = Date.from(Instant.parse('2030-01-31T00:00:00Z'))
        Date itemExpiry = Date.from(Instant.parse('2029-06-30T00:00:00Z'))
        InventoryItem inventoryItem = new InventoryItem(lotNumber: 'LOT-A', expirationDate: lotExpiry)

        when:
        ShipmentItem withInventoryItem = new ShipmentItem(
                inventoryItem: inventoryItem, lotNumber: 'LOT-B', expirationDate: itemExpiry)
        ShipmentItem withoutInventoryItem = new ShipmentItem(lotNumber: 'LOT-B', expirationDate: itemExpiry)

        then:
        assert withInventoryItem.lotNumber == 'LOT-A'
        assert withInventoryItem.expirationDate == lotExpiry
        assert withoutInventoryItem.lotNumber == 'LOT-B'
        assert withoutInventoryItem.expirationDate == itemExpiry
    }

    void 'order-derived properties should describe the single order the item was shipped against'() {
        given:
        UnitOfMeasure box = new UnitOfMeasure(code: 'BX', name: 'Box')
        Order order = new Order(orderNumber: 'PO-1', name: 'Purchase 1', paymentTerm: new PaymentTerm(name: 'Net 30'),
                orderType: new OrderType(orderTypeCode: OrderTypeCode.PURCHASE_ORDER))
        order.id = 'order-1'
        OrderItem orderItem = new OrderItem(order: order, quantityPerUom: 10, quantityUom: box)
        orderItem.id = 'order-item-1'
        ShipmentItem shipmentItem = new ShipmentItem(orderItems: [orderItem] as Set)

        expect:
        assert shipmentItem.orderItem == orderItem
        assert shipmentItem.orderItemId == 'order-item-1'
        assert shipmentItem.orderId == 'order-1'
        assert shipmentItem.orderNumber == 'PO-1'
        assert shipmentItem.orderName == 'Purchase 1'
        assert shipmentItem.paymentTerm == 'Net 30'
        assert shipmentItem.purchaseOrders == [order]
        assert shipmentItem.quantityPerUom == 10
        assert shipmentItem.packSize == 10
        assert shipmentItem.unitOfMeasure == 'BX/10'
    }

    void 'order-derived properties should be collapsed when several order items share the same order'() {
        given:
        Order order = new Order(orderNumber: 'PO-1', name: 'Purchase 1',
                orderType: new OrderType(orderTypeCode: OrderTypeCode.PURCHASE_ORDER))
        order.id = 'order-1'
        ShipmentItem shipmentItem = new ShipmentItem(orderItems: [
                new OrderItem(order: order),
                new OrderItem(order: order),
        ] as Set)

        expect: 'the order is listed once and an order without payment terms gives no payment term'
        assert shipmentItem.orderId == 'order-1'
        assert shipmentItem.orderNumber == 'PO-1'
        assert shipmentItem.purchaseOrders == [order]
        assert shipmentItem.paymentTerm == null
    }

    void 'purchaseOrders should leave out orders that are not purchase orders'() {
        given:
        Order transferOrder = new Order(orderNumber: 'TO-1',
                orderType: new OrderType(orderTypeCode: OrderTypeCode.TRANSFER_ORDER))
        ShipmentItem shipmentItem = new ShipmentItem(orderItems: [new OrderItem(order: transferOrder)] as Set)

        expect:
        assert shipmentItem.purchaseOrders == []
    }

    @PendingFeature(reason = 'OBAF indexes a null orderItems collection: NPE')
    void 'getOrderItem should be null when the item has no order items at all'() {
        expect:
        assert new ShipmentItem().orderItem == null
    }

    void 'order-derived properties should fall back to defaults when the item was not shipped against an order'() {
        given:
        ShipmentItem shipmentItem = new ShipmentItem(orderItems: [] as Set)

        expect:
        assert shipmentItem.orderItem == null
        assert shipmentItem.orderItemId == null
        assert shipmentItem.orderId == ''
        assert shipmentItem.orderNumber == ''
        assert shipmentItem.orderName == ''
        assert shipmentItem.paymentTerm == ''
        assert shipmentItem.quantityPerUom == 1
        assert shipmentItem.quantityRemainingToShip == 0
        assert shipmentItem.packSize == null
        assert shipmentItem.unitOfMeasure == null
    }

    void 'quantityRemainingToShip should subtract what other shipments already carry for the order item'() {
        given:
        Shipment thisShipment = new Shipment(name: 'this')
        Shipment otherShipment = new Shipment(name: 'other')
        ShipmentItem shipmentItem = new ShipmentItem(shipment: thisShipment, quantity: 4)
        ShipmentItem otherShipmentItem = new ShipmentItem(shipment: otherShipment, quantity: 3)
        OrderItem orderItem = new OrderItem(quantity: 10, quantityPerUom: 1,
                shipmentItems: [shipmentItem, otherShipmentItem] as Set)
        shipmentItem.orderItems = [orderItem] as Set

        expect:
        assert shipmentItem.quantityRemainingToShip == 7
    }

    void 'totalQuantityShipped should add up the items of the shipment with the same product and lot'() {
        given:
        Product product = new Product(name: 'product')
        Shipment shipment = new Shipment(name: 'shipment')
        ShipmentItem shipmentItem = new ShipmentItem(shipment: shipment, product: product, lotNumber: 'LOT-1', quantity: 4)
        shipment.shipmentItems = [
                shipmentItem,
                new ShipmentItem(shipment: shipment, product: product, lotNumber: 'LOT-1', quantity: 6),
                new ShipmentItem(shipment: shipment, product: product, lotNumber: 'LOT-2', quantity: 5),
                new ShipmentItem(shipment: shipment, product: new Product(name: 'other'), lotNumber: 'LOT-1', quantity: 9),
        ] as Set

        expect:
        assert shipmentItem.totalQuantityShipped() == 10
    }

    void 'totalQuantityReceived should add up the receipt items of the shipment with the same product and lot'() {
        given:
        Product product = new Product(name: 'product')
        Shipment shipment = new Shipment(name: 'shipment')
        ShipmentItem shipmentItem = new ShipmentItem(shipment: shipment, product: product, lotNumber: 'LOT-1', quantity: 4)
        Receipt firstReceipt = new Receipt(receiptItems: [
                new ReceiptItem(product: product, lotNumber: 'LOT-1', quantityReceived: 2),
                new ReceiptItem(product: product, lotNumber: 'LOT-2', quantityReceived: 8),
        ] as Set)
        firstReceipt.id = 'receipt-1'
        Receipt secondReceipt = new Receipt(receiptItems: [
                new ReceiptItem(product: product, lotNumber: 'LOT-1', quantityReceived: 1),
        ] as Set)
        secondReceipt.id = 'receipt-2'
        shipment.receipts = new TreeSet([firstReceipt, secondReceipt])

        expect:
        assert shipmentItem.totalQuantityReceived() == 3
    }

    void 'totalQuantityReceived should be zero for a shipment without receipts'() {
        given:
        Shipment shipment = new Shipment(name: 'shipment')
        ShipmentItem shipmentItem = new ShipmentItem(shipment: shipment, product: new Product(name: 'product'))

        expect:
        assert shipmentItem.totalQuantityReceived() == 0
    }

    void 'quantityPicked should only count picks of the same lot from the same bin when the item has a bin'() {
        given: 'lots with ids, since inventory items compare by id'
        InventoryItem lot = buildLot('lot-1', 'LOT-1')
        Location bin = new Location(name: 'bin-1')
        // OBAF: getQuantityPicked() sums PicklistItem.quantityPicked (not the allocated quantity), and with a bin it
        // reads shipment.origin to pick the bin, staging or outbound container match; a depot without internal
        // transactions keeps the plain bin match asserted here
        RequisitionItem requisitionItem = new RequisitionItem(picklistItems: [
                new PicklistItem(inventoryItem: lot, binLocation: bin, quantity: 3, quantityPicked: 3),
                new PicklistItem(inventoryItem: lot, binLocation: bin, quantity: 2, quantityPicked: 2),
                new PicklistItem(inventoryItem: lot, binLocation: new Location(name: 'bin-2'), quantity: 7, quantityPicked: 7),
                new PicklistItem(inventoryItem: buildLot('lot-2', 'LOT-2'), binLocation: bin, quantity: 11, quantityPicked: 11),
        ] as Set)
        Shipment shipment = new Shipment(name: 'shipment', origin: new Location(name: 'depot',
                supportedActivities: [ActivityCode.PICK_STOCK.id] as Set))

        when:
        ShipmentItem withBin = new ShipmentItem(shipment: shipment,
                requisitionItem: requisitionItem, inventoryItem: lot, binLocation: bin, quantity: 100)
        ShipmentItem withoutBin = new ShipmentItem(requisitionItem: requisitionItem, inventoryItem: lot, quantity: 100)

        then:
        assert withBin.quantityPicked == 5
        assert withoutBin.quantityPicked == 12
    }

    void 'quantityPicked should fall back to the shipped quantity when nothing was picked'() {
        given:
        ShipmentItem shipmentItem = new ShipmentItem(
                requisitionItem: new RequisitionItem(picklistItems: [] as Set),
                inventoryItem: new InventoryItem(lotNumber: 'LOT-1'),
                quantity: 8)

        expect:
        assert shipmentItem.quantityPicked == 8
    }

    void 'unavailableQuantityPicked should count picks from a hold bin when the item has a bin'() {
        given:
        InventoryItem lot = buildLot('lot-1', 'LOT-1')
        Location holdBin = new Location(name: 'hold', supportedActivities: [ActivityCode.HOLD_STOCK.id] as Set)
        RequisitionItem requisitionItem = new RequisitionItem(picklistItems: [
                new PicklistItem(inventoryItem: lot, binLocation: holdBin, quantity: 3),
                new PicklistItem(inventoryItem: buildLot('lot-2', 'LOT-2'), binLocation: holdBin, quantity: 5),
        ] as Set)
        ShipmentItem shipmentItem = new ShipmentItem(
                requisitionItem: requisitionItem, inventoryItem: lot, binLocation: holdBin, quantity: 10)

        expect:
        assert shipmentItem.unavailableQuantityPicked == 3
    }

    void 'unavailableQuantityPicked should count picks of a recalled lot from the same bin'() {
        given:
        InventoryItem recalledLot = new InventoryItem(lotNumber: 'LOT-1', lotStatus: LotStatusCode.RECALLED)
        Location bin = new Location(name: 'bin-1', supportedActivities: [ActivityCode.PICK_STOCK.id] as Set)
        RequisitionItem requisitionItem = new RequisitionItem(picklistItems: [
                new PicklistItem(inventoryItem: recalledLot, binLocation: bin, quantity: 2),
                new PicklistItem(inventoryItem: recalledLot, binLocation: new Location(name: 'bin-2'), quantity: 6),
        ] as Set)
        ShipmentItem shipmentItem = new ShipmentItem(
                requisitionItem: requisitionItem, inventoryItem: recalledLot, binLocation: bin, quantity: 10)

        expect:
        assert shipmentItem.unavailableQuantityPicked == 2
    }

    void 'unavailableQuantityPicked without a bin should be #expected for a lot with status #lotStatus'() {
        given:
        InventoryItem lot = new InventoryItem(lotNumber: 'LOT-1', lotStatus: lotStatus)
        RequisitionItem requisitionItem = new RequisitionItem(picklistItems: [
                new PicklistItem(inventoryItem: lot, binLocation: new Location(name: 'bin-1'), quantity: 2),
                new PicklistItem(inventoryItem: lot, binLocation: new Location(name: 'bin-2'), quantity: 6),
        ] as Set)
        ShipmentItem shipmentItem = new ShipmentItem(requisitionItem: requisitionItem, inventoryItem: lot, quantity: 10)

        expect:
        assert shipmentItem.unavailableQuantityPicked == expected

        where:
        lotStatus                || expected
        LotStatusCode.RECALLED   || 8
        null                     || 0
    }

    void 'quantityPickedFromOrders should add up the matching picks across all order items'() {
        given:
        InventoryItem lot = new InventoryItem(lotNumber: 'LOT-1')
        Location bin = new Location(name: 'bin-1')
        // OBAF: getQuantityPickedFromOrders() sums PicklistItem.quantityPicked instead of the allocated quantity
        OrderItem firstOrderItem = new OrderItem(picklistItems: [
                new PicklistItem(inventoryItem: lot, binLocation: bin, quantity: 3, quantityPicked: 3),
                new PicklistItem(inventoryItem: lot, binLocation: new Location(name: 'bin-2'), quantity: 7, quantityPicked: 7),
        ] as Set)
        OrderItem secondOrderItem = new OrderItem(picklistItems: [
                new PicklistItem(inventoryItem: lot, binLocation: bin, quantity: 4, quantityPicked: 4),
        ] as Set)

        when:
        ShipmentItem withBin = new ShipmentItem(orderItems: [firstOrderItem, secondOrderItem] as Set,
                inventoryItem: lot, binLocation: bin, quantity: 100)
        ShipmentItem withoutBin = new ShipmentItem(orderItems: [firstOrderItem, secondOrderItem] as Set,
                inventoryItem: lot, quantity: 100)
        ShipmentItem nothingPicked = new ShipmentItem(orderItems: [new OrderItem(picklistItems: [] as Set)] as Set,
                inventoryItem: lot, quantity: 100)

        then:
        assert withBin.quantityPickedFromOrders == 7
        assert withoutBin.quantityPickedFromOrders == 14
        assert nothingPicked.quantityPickedFromOrders == 100
    }

    void 'comments should list the non-empty comments of the receipt items'() {
        given:
        ShipmentItem shipmentItem = new ShipmentItem(receiptItems: [
                new ReceiptItem(comment: 'damaged'),
                new ReceiptItem(comment: ''),
                new ReceiptItem(),
        ] as Set)

        expect:
        assert shipmentItem.comments as List == ['damaged']
        assert new ShipmentItem().comments as List == []
    }

    void 'hasRecalledLot should be #expected for a lot with status #lotStatus'() {
        given:
        ShipmentItem shipmentItem = new ShipmentItem(inventoryItem: new InventoryItem(lotStatus: lotStatus))

        expect:
        assert shipmentItem.hasRecalledLot == expected

        where:
        lotStatus                || expected
        LotStatusCode.RECALLED   || true
        null                     || false
    }

    void 'hasRecalledLot should be false without an inventory item'() {
        expect:
        assert !new ShipmentItem().hasRecalledLot
    }

    void 'invoicing quantities should convert between the order unit of measure and the standard unit'() {
        given: 'a line of 50 each shipped against an order item packed by 10'
        OrderItem orderItem = new OrderItem(quantityPerUom: 10)
        ShipmentItem shipmentItem = new ShipmentItem(quantity: 50, orderItems: [orderItem] as Set)
        shipmentItem.invoiceItems = [
                new InvoiceItem(quantity: 2),
                new InvoiceItem(quantity: 1, inverse: true),
                new InvoiceItem(quantity: null),
        ] as Set

        expect: 'inverse invoice items do not count as invoiced'
        assert shipmentItem.quantityInvoiced == 2
        assert shipmentItem.quantityInvoicedInStandardUom == 20
        assert shipmentItem.quantityToInvoiceInStandardUom == 30
        assert shipmentItem.quantityToInvoice == 3
        assert shipmentItem.isInvoiceable()
    }

    void 'a fully invoiced line should not be invoiceable'() {
        given:
        ShipmentItem shipmentItem = new ShipmentItem(quantity: 50, orderItems: [new OrderItem(quantityPerUom: 10)] as Set)
        shipmentItem.invoiceItems = [new InvoiceItem(quantity: 5)] as Set

        expect:
        assert shipmentItem.quantityToInvoiceInStandardUom == 0
        assert shipmentItem.quantityToInvoice == 0
        assert !shipmentItem.isInvoiceable()
    }

    void 'a line without invoice items should be invoiceable for its whole quantity'() {
        given:
        ShipmentItem shipmentItem = new ShipmentItem(quantity: 50, orderItems: [] as Set)

        expect:
        assert shipmentItem.quantityInvoiced == 0
        assert shipmentItem.quantityToInvoice == 50
        assert shipmentItem.isInvoiceable()
    }

    void 'cloneShipmentItem should copy the line details but not its identity or associations to orders'() {
        given:
        Product product = new Product(name: 'product')
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: 'LOT-1')
        Location bin = new Location(name: 'bin-1')
        Person recipient = new Person(firstName: 'Ann', lastName: 'Lee')
        Container container = new Container(name: 'box')
        ShipmentItem shipmentItem = new ShipmentItem(product: product, inventoryItem: inventoryItem, binLocation: bin,
                quantity: 7, recipient: recipient, container: container, shipment: new Shipment(name: 'shipment'),
                orderItems: [new OrderItem()] as Set)
        shipmentItem.id = 'shipment-item-1'

        when:
        ShipmentItem clone = shipmentItem.cloneShipmentItem()

        then:
        assert !clone.is(shipmentItem)
        assert clone.id == null
        assert clone.shipment == null
        assert !clone.orderItems
        assert clone.product == product
        assert clone.inventoryItem == inventoryItem
        assert clone.lotNumber == 'LOT-1'
        assert clone.binLocation == bin
        assert clone.quantity == 7
        assert clone.recipient == recipient
        assert clone.container == container
    }

    private static InventoryItem buildLot(String id, String lotNumber) {
        InventoryItem inventoryItem = new InventoryItem(lotNumber: lotNumber)
        inventoryItem.id = id
        return inventoryItem
    }

    private static ReceiptItem buildReceiptItem(
            Product product, ReceiptStatusCode status, Integer quantityReceived, Integer quantityCanceled) {
        return new ReceiptItem(product: product, quantityReceived: quantityReceived, quantityCanceled: quantityCanceled,
                receipt: new Receipt(receiptStatusCode: status))
    }
}
