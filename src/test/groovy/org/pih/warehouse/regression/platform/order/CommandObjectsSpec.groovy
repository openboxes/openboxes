package org.pih.warehouse.regression.platform.order

import org.pih.warehouse.order.*
import grails.testing.gorm.DataTest
import spock.lang.Specification

import java.time.LocalDate

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.fulfillment.FulfillmentCommand
import org.pih.warehouse.inventory.InventoryCommand
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.TransferStockCommand
import org.pih.warehouse.order.cart.Cart
import org.pih.warehouse.order.cart.CartItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.report.InventoryReportCommand
import org.pih.warehouse.report.InventoryReportEntryCommand
import org.pih.warehouse.report.MultiLocationInventoryReportCommand
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentType

class CommandObjectsSpec extends Specification implements DataTest {

    void setupSpec() {
        mockDomains(Location, Person, Product, Category, ShipmentType, Order, OrderItem, Shipment, ShipmentItem,
                InventoryItem)
    }

    private static OrderCommand shipped(Date shippedOn, Date deliveredOn) {
        return new OrderCommand(shipmentType: new ShipmentType(name: 'Air'), recipient: new Person(firstName: 'A'),
                shippedOn: shippedOn, deliveredOn: deliveredOn, order: new Order(), shipment: new Shipment())
    }

    void 'an order shipment must be shipped and delivered in the past, delivery not before shipping'() {
        given:
        Date today = new Date()

        expect:
        shipped(today - 3, today - 1).validate()

        when:
        OrderCommand future = shipped(today + 2, today + 3)

        then:
        !future.validate()
        future.errors.getFieldError('shippedOn').code == 'invalid.mustOccurOnOrBeforeToday'
        future.errors.getFieldError('deliveredOn').code == 'invalid.mustOccurOnOrBeforeToday'

        when:
        OrderCommand reversed = shipped(today - 1, today - 5)

        then:
        !reversed.validate()
        reversed.errors.getFieldError('shippedOn').code == 'invalid.mustOccurOnOrBeforeDeliveredOn'
        reversed.errors.getFieldError('deliveredOn').code == 'invalid.mustOccurOnOrAfterShippedOn'

        when:
        OrderCommand missing = new OrderCommand()

        then:
        !missing.validate()
        missing.errors.getFieldError('shipmentType').code == 'nullable'
        missing.errors.getFieldError('recipient').code == 'nullable'
    }

    void 'the inventory report groups the products of its entries by category'() {
        given:
        Category drugs = new Category(name: 'Drugs')
        Category supplies = new Category(name: 'Supplies')
        Product aspirin = new Product(name: 'Aspirin', category: drugs)
        Product ibuprofen = new Product(name: 'Ibuprofen', category: drugs)
        Product gauze = new Product(name: 'Gauze', category: supplies)
        InventoryReportEntryCommand aspirinEntry = new InventoryReportEntryCommand(product: aspirin)
        Location depot = new Location(name: 'Depot')
        InventoryReportCommand report = new InventoryReportCommand(location: depot, category: drugs,
                rootCategory: new Category(name: 'Root'), status: 'ALL',
                startDate: LocalDate.of(2026, 1, 1), entries: [(aspirin): aspirinEntry,
                                                                (ibuprofen): new InventoryReportEntryCommand(),
                                                                (gauze): new InventoryReportEntryCommand()])

        expect:
        report.validate()
        report.getProductEntry(aspirin).is(aspirinEntry)
        report.products == [aspirin, ibuprofen, gauze] as Set
        report.getProducts(drugs) as Set == [aspirin, ibuprofen] as Set
        report.productsByCategory.keySet() == [drugs, supplies] as Set
        report.toString() == "Location: ${depot}"

        when:
        InventoryReportCommand empty = new InventoryReportCommand()

        then:
        !empty.validate()
        empty.errors.getFieldError('location').code == 'nullable'
        empty.errors.getFieldError('category').code == 'nullable'
    }

    void 'the multi location report knows which button was pressed'() {
        expect:
        new MultiLocationInventoryReportCommand(actionButton: 'Run').isActionRun
        !new MultiLocationInventoryReportCommand(actionButton: 'Run').isActionDownload
        new MultiLocationInventoryReportCommand(actionButton: 'download').isActionDownload
        !new MultiLocationInventoryReportCommand().isActionRun
        new MultiLocationInventoryReportCommand(actionButton: 'run').validate()
        !new MultiLocationInventoryReportCommand(actionButton: 'print').validate()
        new MultiLocationInventoryReportCommand().includeSubcategories
    }

    void 'shipping an order groups the ship items by order item and describes them'() {
        given:
        Product product = new Product(name: 'Gloves')
        Order order = new Order(orderNumber: 'PO-1')
        OrderItem first = new OrderItem(order: order, product: product).save(validate: false)
        OrderItem second = new OrderItem(order: order, product: product).save(validate: false)
        Shipment shipment = new Shipment(name: 'S-1')
        ShipmentItem shipmentItem = new ShipmentItem(shipment: shipment)
        ShipOrderItemCommand a = new ShipOrderItemCommand(orderItem: first, shipmentItem: shipmentItem, quantityToShip: 3)
        ShipOrderItemCommand b = new ShipOrderItemCommand(orderItem: second, quantityToShip: 1)
        ShipOrderItemCommand c = new ShipOrderItemCommand(orderItem: first, quantityToShip: 2)
        ShipOrderCommand command = new ShipOrderCommand(order: order, shipment: shipment, shipOrderItems: [a, b, c])

        expect:
        command.getShipOrderItemsByOrderItem(first) == [a, c]
        command.toJson() == [order: order, shipment: shipment, shipOrderItems: [a, b, c]]
        command.toString() == command.toJson().toMapString()
        a.toJson() == [order: order, orderItem: first, shipmentItem: shipmentItem, product: product, shipment: shipment,
                       quantityToShip: 3]
        a.toString() == a.toJson().toMapString()
        new ShipOrderItemCommand().toJson().values().every { it == null }
    }

    void 'a cart adds and removes items'() {
        given:
        Cart cart = new Cart()
        CartItem item = new CartItem(lotNumber: 'L1')

        expect:
        cart.isEmpty()

        when:
        cart.addItem(item)

        then:
        !cart.isEmpty()
        cart.items == [item]

        when:
        cart.removeItem(item)

        then:
        cart.isEmpty()
    }

    void 'stock transfers need both ends of the transfer'() {
        given:
        Location depot = new Location(name: 'Depot')
        Location bin = new Location(name: 'Bin')
        TransferStockCommand complete = new TransferStockCommand(quantity: 5, inventoryItem: new InventoryItem(),
                location: depot, binLocation: bin, transferOut: true, otherLocation: depot, otherBinLocation: bin)
        TransferStockCommand incomplete = new TransferStockCommand(quantity: 5)

        expect:
        complete.validate()
        !incomplete.validate()
        incomplete.errors.getFieldError('otherLocation').code == 'nullable'
        incomplete.errors.getFieldError('transferOut').code == 'nullable'
        !incomplete.disableRefresh
    }

    void 'an inventory search has sensible defaults and validates without filters'() {
        given:
        InventoryCommand command = new InventoryCommand(tags: [], catalogs: [], productTypes: [])

        expect:
        command.validate()
        command.offset == 0
        !command.showHiddenProducts
        command.searchResults == []
    }

    void 'a fulfillment without items has nothing fulfilled'() {
        given:
        FulfillmentCommand command = new FulfillmentCommand()
        RequisitionItem requestItem = new RequisitionItem()

        expect:
        command.fulfillmentItemsMap() == null
        command.fulfillmentItems(requestItem) == []
        command.quantityFulfilledMap() == [:]
        command.quantityFulfilledByRequestItem(requestItem) == 0
        command.fulfillmentItems.get(0) != null
    }
}
