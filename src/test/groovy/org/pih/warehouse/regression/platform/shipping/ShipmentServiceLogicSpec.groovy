package org.pih.warehouse.regression.platform.shipping

import spock.lang.PendingFeature

import org.pih.warehouse.shipping.*
import org.hibernate.ObjectNotFoundException
import grails.validation.ValidationException
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import spock.lang.Specification

import org.pih.warehouse.api.StockTransfer
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.ListCommand
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem

/**
 * The shipment service logic that works on objects in memory: packing list rows and cells, shipment names,
 * quantities per product and lot, receipts and the shipment items made from order items.
 */
class ShipmentServiceLogicSpec extends Specification implements ServiceUnitTest<ShipmentService>, DataTest {

    void setupSpec() {
        mockDomains(Shipment, ShipmentItem, Container, Location, InventoryItem, Product, Order, OrderItem, OrderType,
                PicklistItem, Person, Receipt, ReceiptItem, Event, EventType)
    }

    private static Product product(String id, String name) {
        Product product = new Product(name: name, productCode: id.toUpperCase())
        product.id = id
        return product
    }

    private static InventoryItem item(String id, Product product, String lotNumber) {
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: lotNumber)
        inventoryItem.id = id
        return inventoryItem
    }

    void 'findDuplicatePackingListItems finds rows with the same pallet, box, product, lot, expiry and recipient'() {
        given:
        Map row = [palletName: 'P', boxName: 'B', productCode: 'AS1', lotNumber: 'L1', expirationDate: null, recipient: null,
                   quantity: 1]

        expect:
        service.findDuplicatePackingListItems([row, row + [quantity: 5], row + [boxName: 'C']])*.productCode == ['AS1']
        service.findDuplicatePackingListItems([row, row + [lotNumber: 'L2']]).isEmpty()
    }

    void 'generateName joins origin, destination, tracking number and description without spaces'() {
        given:
        config.merge([openboxes: [generateName: [separator: ' / ']]])
        String separator = grails.util.Holders.config.openboxes.generateName.separator.replace(' ', '')
        StockTransfer stockTransfer = new StockTransfer(origin: new Location(name: 'Main depot', locationNumber: 'MD'),
                destination: new Location(name: 'Ward three'), trackingNumber: 'TR 9', description: 'urgent return')

        expect:
        separator
        service.generateName(stockTransfer) == ['MD', 'Wardthree', 'TR9', 'urgentreturn'].join(separator)
        service.generateName(new StockTransfer(origin: new Location(name: 'A'), destination: new Location(name: 'B'))) ==
                ['A', 'B'].join(separator)
    }

    void 'getQuantityByProduct and getQuantityByInventoryItem add up the items of the shipments'() {
        given:
        Product aspirin = product('p1', 'Aspirin')
        Product gauze = product('p2', 'Gauze')
        InventoryItem lot = item('i1', aspirin, 'L1')
        Shipment first = new Shipment()
        first.addToShipmentItems(new ShipmentItem(product: aspirin, lotNumber: 'L1', quantity: 3))
        first.addToShipmentItems(new ShipmentItem(product: gauze, lotNumber: null, quantity: 4))
        Shipment second = new Shipment()
        second.addToShipmentItems(new ShipmentItem(product: aspirin, lotNumber: 'L1', quantity: 5))
        second.addToShipmentItems(new ShipmentItem(product: null, quantity: 9))
        service.inventoryService = Mock(InventoryService) {
            _ * findInventoryItemByProductAndLotNumber(aspirin, 'L1') >> lot
        }

        expect:
        service.getQuantityByProduct([first, second], [aspirin]) == [(aspirin): 8]
        service.getQuantityByProduct([first, second], [aspirin, gauze]) == [(aspirin): 8, (gauze): 4]
        service.getQuantityByInventoryItem([first, second]) == [(lot): 8]
    }

    void 'getShipmentsByStatus groups the shipments by the status of their events'() {
        given:
        Shipment pending = new Shipment(name: 'pending')
        Shipment shipped = new Shipment(name: 'shipped')
        shipped.addToEvents(new Event(eventDate: new Date() - 1, eventType: new EventType(eventCode: EventCode.SHIPPED)))

        when:
        Map<ShipmentStatusCode, ListCommand> byStatus = service.getShipmentsByStatus([pending, shipped])

        then:
        byStatus.keySet().containsAll(ShipmentStatusCode.list())
        byStatus[ShipmentStatusCode.PENDING].objectList*.name == ['pending']
        byStatus[ShipmentStatusCode.SHIPPED].objectList*.name == ['shipped']
        byStatus[ShipmentStatusCode.RECEIVED] == []
    }

    void 'copyShipmentItem copies what makes up an item'() {
        given:
        Product aspirin = product('p1', 'Aspirin')
        ShipmentItem original = new ShipmentItem(product: aspirin, inventoryItem: item('i1', aspirin, 'L1'), lotNumber: 'L1',
                expirationDate: new Date(), quantity: 6, recipient: new Person(firstName: 'Ann'),
                container: new Container(name: 'Box'), shipment: new Shipment(name: 'S'), donor: null)

        when:
        ShipmentItem copy = service.copyShipmentItem(original)

        then:
        !copy.is(original)
        [copy.product, copy.inventoryItem, copy.lotNumber, copy.expirationDate, copy.quantity, copy.recipient, copy.container,
         copy.shipment] == [original.product, original.inventoryItem, original.lotNumber, original.expirationDate, 6,
                            original.recipient, original.container, original.shipment]
    }

    void 'the packing list cell readers trim text, round numbers and refuse text as a date'() {
        given:
        HSSFRow row = new HSSFWorkbook().createSheet().createRow(0)
        row.createCell(0).setCellValue('  Pallet 1 ')
        row.createCell(1).setCellValue(2.5d)
        row.createCell(2).setCellValue(12d)
        Date date = new Date()
        row.createCell(3).setCellValue(date)
        row.createCell(4).setCellValue('not a date')

        expect:
        service.getStringCellValue(row.getCell(0)) == 'Pallet 1'
        service.getNumericCellValue(row.getCell(1)) == 3d
        service.getStringCellValue(row.getCell(2)) == '12'
        Math.abs(service.getDateCellValue(row.getCell(3)).time - date.time) < 1000
        service.getStringCellValue(null) == null
        service.getNumericCellValue(null) == 0d
        service.getDateCellValue(null) == null

        when:
        service.getDateCellValue(row.getCell(4))

        then:
        thrown(IllegalStateException)
    }

    void 'getNumericCellValue refuses a text cell'() {
        given:
        Cell cell = new HSSFWorkbook().createSheet().createRow(0).createCell(0)
        cell.setCellValue('many')

        when:
        service.getNumericCellValue(cell)

        then:
        thrown(IllegalStateException)
    }

    void 'createReceipt receives every shipped item in full'() {
        given:
        Product aspirin = product('p1', 'Aspirin')
        InventoryItem lot = item('i1', aspirin, 'L1')
        Shipment shipment = new Shipment(name: 'S', expectedDeliveryDate: new Date() + 2, recipient: new Person(firstName: 'Ann'))
        shipment.addToShipmentItems(new ShipmentItem(product: aspirin, inventoryItem: lot, lotNumber: 'L1', quantity: 7))
        Date delivered = new Date() - 1

        when:
        Receipt receipt = service.createReceipt(shipment, delivered)

        then:
        shipment.receipts as List == [receipt]
        receipt.actualDeliveryDate == delivered
        receipt.expectedDeliveryDate == shipment.expectedDeliveryDate
        receipt.recipient == shipment.recipient
        receipt.receiptItems.collect { [it.quantityShipped, it.quantityReceived, it.lotNumber, it.inventoryItem] } ==
                [[7, 7, 'L1', lot]]
        shipment.shipmentItems.first().receiptItems.size() == 1
    }

    void 'createShipmentItems makes one item per order item for an inbound order'() {
        given:
        Location depot = new Location(name: 'Depot').save(validate: false)
        AuthService authService = new AuthService()
        authService.currentLocation = depot
        service.authService = authService
        Product aspirin = product('p1', 'Aspirin')
        InventoryItem lot = item('i1', aspirin, 'L1')
        Order order = new Order(origin: new Location(name: 'Supplier'), destination: depot,
                orderType: new OrderType(code: 'PURCHASE_ORDER'))
        OrderItem orderItem = new OrderItem(order: order, product: aspirin, inventoryItem: lot, quantity: 4,
                recipient: new Person(firstName: 'Ann'))

        when:
        List<ShipmentItem> items = service.createShipmentItems(orderItem)

        then:
        items.collect { [it.product, it.inventoryItem, it.lotNumber, it.quantity, it.recipient, it.sortOrder] } ==
                [[aspirin, lot, 'L1', 4, orderItem.recipient, 0]]
        items[0].orderItems as List == [orderItem]

        cleanup:
        authService.currentLocation = null
    }

    void 'createShipmentItems makes one item per picked lot for an outbound return'() {
        given:
        Location depot = new Location(name: 'Depot').save(validate: false)
        AuthService authService = new AuthService()
        authService.currentLocation = depot
        service.authService = authService
        Product aspirin = product('p1', 'Aspirin')
        InventoryItem lotA = item('i1', aspirin, 'LA')
        InventoryItem lotB = item('i2', aspirin, 'LB')
        Order order = new Order(origin: depot, destination: new Location(name: 'Supplier'),
                orderType: new OrderType(code: Constants.RETURN_ORDER))
        OrderItem orderItem = new OrderItem(order: order, product: aspirin, quantity: 5)
        Location bin = new Location(name: 'Bin')
        orderItem.addToPicklistItems(new PicklistItem(inventoryItem: lotA, binLocation: bin, quantity: 3))
        orderItem.addToPicklistItems(new PicklistItem(inventoryItem: lotB, quantity: 0))

        when:
        List<ShipmentItem> items = service.createShipmentItems(orderItem)

        then:
        items.collect { [it.inventoryItem, it.binLocation, it.quantity, it.lotNumber] } == [[lotA, bin, 3, 'LA']]

        when: 'the shipment items exist already, their quantities follow the picks'
        orderItem.addToShipmentItems(items[0])
        items[0].quantity = 99
        def again = service.createShipmentItems(orderItem)

        then:
        again*.quantity == [3]

        cleanup:
        authService.currentLocation = null
    }

    void 'getShipmentStatusCodes lists the localized status codes that are not excluded'() {
        given:
        service.messageLocalizer = Stub(MessageLocalizer) {
            localizeEnumValue(_) >> { Enum value -> "label ${value.name()}".toString() }
        }

        when:
        List<Map<String, String>> codes = service.getShipmentStatusCodes(['RECEIVED', 'CREATED'])

        then:
        codes*.id == ShipmentStatusCode.list()*.name().findAll { !(it in ['RECEIVED', 'CREATED']) }
        codes.every { it.label == "label ${it.id}" && it.value == it.id && it.variant }
    }

    @PendingFeature(reason = 'OBAF reads stockTransfer.dateOrdered, which StockTransfer does not have')
    void 'generateName includes the creation date of the stock transfer'() {
        given:
        config.merge([openboxes: [generateName: [separator: '.']]])
        Date created = new Date(126, 0, 15)
        StockTransfer stockTransfer = new StockTransfer(origin: new Location(name: 'A'), destination: new Location(name: 'B'),
                dateCreated: created)

        expect:
        service.generateName(stockTransfer) == "A.B.${created.format('ddMMMyyyy')}"
    }

    @PendingFeature(reason = 'OBAF looks up the shipment item before the shipment check: NPE')
    void 'addToShipment refuses a row with a quantity but no shipment'() {
        given:
        ItemListCommand command = new ItemListCommand()
        command.items << new ItemCommand(quantity: 2, inventoryItem: new InventoryItem(lotNumber: 'L1'))

        when:
        service.addToShipment(command)

        then:
        ValidationException exception = thrown()
        exception.message.startsWith('Shipment is required')
        command.errors.globalErrors*.code == ['shipmentItem.shipment.required']
    }

    @PendingFeature(reason = 'OBAF calls a nonexistent ObjectNotFoundException(String) constructor')
    void 'addShipmentComment reports an unknown shipment as not found'() {
        when:
        service.addShipmentComment('no-such-shipment', 'hello', null, null)

        then:
        ObjectNotFoundException exception = thrown()
        exception.identifier == 'no-such-shipment'
        exception.entityName == Shipment.class.toString()
    }
}
