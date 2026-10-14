package org.pih.warehouse.regression.platform.inventory

import org.pih.warehouse.inventory.*
import grails.testing.gorm.DataTest
import spock.lang.Specification

import org.pih.warehouse.core.Location
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductCatalogItem
import org.pih.warehouse.product.ProductField
import org.pih.warehouse.product.ProductType
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode

/**
 * Small behavior methods of inventory, picking and product domains.
 */
class DomainPredicatesSpec extends Specification implements DataTest {

    void setupSpec() {
        mockDomains(Order, OrderItem, InventoryItem, Product, Location, Picklist, PicklistItem, ProductCatalog,
                ProductCatalogItem)
    }

    void 'outbound list items report issue, shipment and receipt progress'() {
        given:
        OutboundStockMovementListItem item = new OutboundStockMovementListItem(
                requisition: new Requisition(status: requisitionStatus), shipment: new Shipment(currentStatus: shipmentStatus))

        expect:
        [item.hasBeenIssued(), item.hasBeenShipped(), item.hasBeenPartiallyReceived(), item.hasBeenReceived(),
         item.pending] == expected

        where:
        requisitionStatus          | shipmentStatus                        || expected
        RequisitionStatus.ISSUED   | ShipmentStatusCode.SHIPPED            || [true, true, false, false, false]
        RequisitionStatus.ISSUED   | ShipmentStatusCode.PARTIALLY_RECEIVED || [true, false, true, false, false]
        RequisitionStatus.ISSUED   | ShipmentStatusCode.RECEIVED           || [true, false, false, true, false]
        RequisitionStatus.PICKING  | ShipmentStatusCode.PENDING            || [false, false, false, false, true]
    }

    void 'the line count of an outbound order movement counts its order items'() {
        given:
        Order order = new Order(orderNumber: 'RET-1').save(validate: false)
        3.times { new OrderItem(order: order).save(validate: false, flush: true) }

        expect:
        new OutboundStockMovementListItem(order: order).lineItemCount == 3
        new OutboundStockMovementListItem().lineItemCount == 0
    }

    void 'expiration status buckets the days left before expiry'() {
        expect:
        new InventoryItem(expirationDate: daysFromNow == null ? null : new Date() + daysFromNow).expirationStatus == expected

        where:
        daysFromNow || expected
        -1          || 'expired'
        15          || 'within30Days'
        45          || 'within60Days'
        75          || 'within90Days'
        120         || 'within180Days'
        300         || 'within365Days'
        500         || 'greaterThan365Days'
        null        || 'never'
    }

    void 'an inventory item without a lot is the default lot'() {
        expect:
        new InventoryItem().isDefault
        !new InventoryItem(lotNumber: 'L1').isDefault
        new InventoryItem(product: new Product(name: 'x')).associatedProducts == [null]
    }

    void 'product types show every field until some fields are chosen'() {
        given:
        ProductType open = new ProductType(name: 'Open')
        ProductType limited = new ProductType(name: 'Limited', displayedFields: [ProductField.NAME, ProductField.CATEGORY])

        expect:
        open.isFieldDisplayed(ProductField.GL_ACCOUNT)
        open.isAnyFieldDisplayed([ProductField.GL_ACCOUNT])
        limited.isFieldDisplayed(ProductField.NAME)
        !limited.isFieldDisplayed(ProductField.GL_ACCOUNT)
        limited.isAnyFieldDisplayed([ProductField.GL_ACCOUNT, ProductField.CATEGORY])
        !limited.isAnyFieldDisplayed([ProductField.GL_ACCOUNT])
    }

    void 'product types number their identifiers sequentially'() {
        expect:
        new ProductType(sequenceNumber: 0).nextSequenceNumber == 1
        new ProductType(sequenceNumber: null).nextSequenceNumber == 1
        new ProductType(sequenceNumber: 41).nextSequenceNumber == 42
        new ProductType(productIdentifierFormat: 'MED-0000').hasSequentialFormat()
        !new ProductType(productIdentifierFormat: 'LLNN').hasSequentialFormat()
    }

    void 'a catalog knows whether it contains a product'() {
        given:
        Product gloves = new Product(name: 'Gloves').save(validate: false)
        Product masks = new Product(name: 'Masks').save(validate: false)
        ProductCatalog catalog = new ProductCatalog(name: 'Essentials',
                productCatalogItems: [new ProductCatalogItem(product: gloves)] as Set)

        expect:
        catalog.contains(gloves)
        !catalog.contains(masks)
        catalog.toString() == 'Essentials'
    }

    void 'a picklist groups its pickable items by product and lot'() {
        given:
        Product gloves = new Product(name: 'Gloves').save(validate: false)
        Product masks = new Product(name: 'Masks').save(validate: false)
        InventoryItem lotA = new InventoryItem(product: gloves, lotNumber: 'A').save(validate: false)
        InventoryItem lotB = new InventoryItem(product: gloves, lotNumber: 'B').save(validate: false)
        InventoryItem recalled = new InventoryItem(product: masks, lotNumber: 'R', lotStatus: LotStatusCode.RECALLED)
                .save(validate: false, flush: true)
        PicklistItem pickA = new PicklistItem(inventoryItem: lotA, quantity: 2)
        PicklistItem pickB = new PicklistItem(inventoryItem: lotB, quantity: 3)
        PicklistItem emptyPick = new PicklistItem(inventoryItem: lotB, quantity: 0)
        PicklistItem recalledPick = new PicklistItem(inventoryItem: recalled, quantity: 1)
        Picklist picklist = new Picklist(name: 'P1', picklistItems: [pickA, pickB, emptyPick, recalledPick] as Set)

        expect:
        picklist.pickablePicklistItems as Set == [pickA, pickB, emptyPick] as Set
        picklist.pickablePicklistItemsByProductId.keySet() == [gloves.id] as Set
        picklist.getPicklistItems(gloves) as Set == [pickA, pickB] as Set
        picklist.getPicklistItemsByLot(gloves).collectEntries { k, v -> [k, v.size()] } == [A: 1, B: 1]
        picklist.toString() == "id: null, name:P1"
    }
}
