package org.pih.warehouse.shipping

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.MessageService
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.data.PersonService
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Product

import javax.xml.bind.ValidationException
import java.text.SimpleDateFormat

@Unroll
class CombinedShipmentServiceSpec extends Specification implements ServiceUnitTest<CombinedShipmentService>, DataTest {

    static final String CSV_HEADER = "Order number,Order Item id,Product code,Product name,Lot number,Expiry," +
            "Quantity to ship,UOM,Pack level 1,Pack level 2,Recipient,Budget code"

    InventoryService inventoryServiceMock

    Location supplier

    Location facility

    Shipment shipment

    Order order

    Product product

    OrderItem orderItem

    void setupSpec() {
        mockDomains(Shipment, ShipmentItem, ShipmentType, Order, OrderItem, Product, Location, UnitOfMeasure,
                InventoryItem, Person)
    }

    void setup() {
        config.openboxes.expirationDate.minValue = "01/01/2000"

        MessageService messageServiceMock = Mock(MessageService)
        messageServiceMock.getMessage(_ as String, _ as Object[], _ as String, _ as Locale) >> { args -> args[2] }
        service.messageService = messageServiceMock

        LocalizationService localizationServiceMock = Mock(LocalizationService)
        localizationServiceMock.getCurrentLocale() >> Locale.ENGLISH
        service.localizationService = localizationServiceMock

        inventoryServiceMock = Mock(InventoryService)
        service.inventoryService = inventoryServiceMock

        service.personService = Mock(PersonService)

        supplier = new Location(name: 'Supplier').save(validate: false)
        facility = new Location(name: 'Facility').save(validate: false)
        shipment = new Shipment(
                name: 'Shipment',
                origin: supplier,
                destination: facility,
                expectedShippingDate: new Date(),
                shipmentType: new ShipmentType(name: 'Default').save(validate: false),
        ).save(validate: false)
        order = createOrder('PO-1', supplier)
        product = createProduct('P1')
        orderItem = createOrderItem(order, product)
    }

    private Order createOrder(String orderNumber, Location origin) {
        return new Order(orderNumber: orderNumber, origin: origin, destination: facility).save(validate: false)
    }

    private static Product createProduct(String productCode, boolean lotAndExpiryControl = false) {
        return new Product(productCode: productCode, name: "Product ${productCode}", lotAndExpiryControl: lotAndExpiryControl)
                .save(validate: false)
    }

    private static OrderItem createOrderItem(Order order, Product product, UnitOfMeasure quantityUom = null, BigDecimal quantityPerUom = 1) {
        return new OrderItem(order: order, product: product, quantity: 10, quantityUom: quantityUom, quantityPerUom: quantityPerUom)
                .save(validate: false)
    }

    private Map<String, Object> buildLine(Map<String, Object> overrides = [:]) {
        return [
                orderNumber: order.orderNumber,
                id: '',
                productCode: product.productCode,
                productName: product.name,
                lotNumber: '',
                expiry: '',
                quantityToShip: '5',
                unitOfMeasure: '',
                palletName: '',
                boxName: '',
                recipient: '',
                budgetCode: '',
        ] + overrides
    }

    private static String formatDate(Date date) {
        return new SimpleDateFormat("MM/dd/yyyy").format(date)
    }

    private static ImportDataCommand buildImportCommand(String content, String contentType = "text/csv") {
        return new ImportDataCommand(importFile: new MockMultipartFile("importFile", "import.csv", contentType, content.bytes))
    }

    void 'parseOrderItemsFromTemplateImport should map csv columns to line fields skipping the header row'() {
        given:
        String csv = CSV_HEADER + "\nPO-1,item-1,SKU-1,Product 1,LOT-1,12/31/2099,5,EA/1,Pallet 1,Box 1,John Doe,BUDGET-1"

        when:
        List<Map<String, Object>> lines = service.parseOrderItemsFromTemplateImport(csv)

        then:
        assert lines == [[
                orderNumber: 'PO-1',
                id: 'item-1',
                productCode: 'SKU-1',
                productName: 'Product 1',
                lotNumber: 'LOT-1',
                expiry: '12/31/2099',
                quantityToShip: '5',
                unitOfMeasure: 'EA/1',
                palletName: 'Pallet 1',
                boxName: 'Box 1',
                recipient: 'John Doe',
                budgetCode: 'BUDGET-1',
        ]]
    }

    void 'validateItemsFromTemplateImport should pass when line matches an order item of the shipment route'() {
        given:
        Map<String, Object> line = buildLine()

        expect:
        assert service.validateItemsFromTemplateImport(shipment, [line])
        assert line.errors == []
    }

    void 'validateItemsFromTemplateImport should fail with "#expectedError" when #description'() {
        given:
        Map<String, Object> line = buildLine(overrides)

        expect:
        assert !service.validateItemsFromTemplateImport(shipment, [line])
        assert line.errors.any { it.toString().contains(expectedError) }

        where:
        description                              | overrides                            || expectedError
        "order number is missing"                | [orderNumber: '']                    || "Order number is required"
        "order does not exist"                   | [orderNumber: 'UNKNOWN-PO']          || "There is no order with number: UNKNOWN-PO"
        "product code is missing"                | [productCode: '']                    || "Product code is required"
        "product does not exist"                 | [productCode: 'UNKNOWN-SKU']         || "Product does not exit"
        "expiry date cannot be parsed"           | [expiry: 'not-a-date']               || "Unable to parse expiry date: not-a-date"
        "expiry date is before the minimum date" | [expiry: '01/01/1999']               || "Expiry date is invalid"
        "expiry date is in the past"             | [expiry: formatDate(new Date() - 1)] || "cannot be in the past"
        "quantity to ship is missing"            | [quantityToShip: '']                 || "Quantity To Ship is empty"
        "quantity to ship is not a number"       | [quantityToShip: 'abc']              || "can't be parsed properly"
        "quantity exceeds quantity to ship"      | [quantityToShip: '11']               || "is greater than qty available to ship(10)"
        "unit of measure does not exist"         | [unitOfMeasure: 'UNKNOWN/1']         || "Could not find provided Unit of Measure: UNKNOWN/1"
    }

    void 'validateItemsFromTemplateImport should fail when order route differs from the shipment route'() {
        given:
        Location otherSupplier = new Location(name: 'Other supplier').save(validate: false)
        Order otherOrder = createOrder('PO-2', otherSupplier)
        createOrderItem(otherOrder, product)
        Map<String, Object> line = buildLine(orderNumber: otherOrder.orderNumber)

        expect:
        assert !service.validateItemsFromTemplateImport(shipment, [line])
        assert line.errors.contains("Order must be from the same origin and destination as shipment")
    }

    void 'validateItemsFromTemplateImport should fail when product requires lot and expiry but they are missing'() {
        given:
        Product lotControlledProduct = createProduct('P2', true)
        createOrderItem(order, lotControlledProduct)
        Map<String, Object> line = buildLine(productCode: lotControlledProduct.productCode, productName: lotControlledProduct.name)

        expect:
        assert !service.validateItemsFromTemplateImport(shipment, [line])
        assert line.errors*.toString().contains("Both lot number and expiry date are required for the 'P2 Product P2' product.")
    }

    void 'validateItemsFromTemplateImport should fail when product code appears more than once without order item id'() {
        given:
        List<Map<String, Object>> lines = [buildLine(quantityToShip: '1'), buildLine(quantityToShip: '1')]

        expect:
        assert !service.validateItemsFromTemplateImport(shipment, lines)
        assert lines.every { line ->
            line.errors*.toString().contains("Product code P1 appears more than once on PO. Must specify order item id")
        }
    }

    void 'validateItemsFromTemplateImport should pass when product code appears more than once with order item ids'() {
        given:
        List<Map<String, Object>> lines = [buildLine(id: orderItem.id, quantityToShip: '4'), buildLine(id: orderItem.id, quantityToShip: '6')]

        expect:
        assert service.validateItemsFromTemplateImport(shipment, lines)
        assert lines*.errors == [[], []]
    }

    void 'validateItemsFromTemplateImport should fail when total quantity of lines for the same order item exceeds quantity to ship'() {
        given:
        List<Map<String, Object>> lines = [buildLine(id: orderItem.id, quantityToShip: '6'), buildLine(id: orderItem.id, quantityToShip: '6')]

        expect:
        assert !service.validateItemsFromTemplateImport(shipment, lines)
        assert lines.every { line -> line.errors.any { it.toString().contains("is greater than qty available to ship(10)") } }
    }

    void 'validateItemsFromTemplateImport should fail when product does not match the product of the order item with given id'() {
        given:
        createProduct('P2')
        Map<String, Object> line = buildLine(id: orderItem.id, productCode: 'P2')

        expect:
        assert !service.validateItemsFromTemplateImport(shipment, [line])
        assert line.errors*.toString().contains("Product code P2 does not match product on the order item with given id ${orderItem.id}".toString())
    }

    void 'validateItemsFromTemplateImport should return #expectedValid when unit of measure is #unitOfMeasure and order item uom is #orderItemUomCode/#orderItemQuantityPerUom'() {
        given:
        new UnitOfMeasure(code: 'BX', name: 'Box').save(validate: false)
        new UnitOfMeasure(code: 'EA', name: 'Each').save(validate: false)
        Product productWithUom = createProduct('P2')
        OrderItem orderItemWithUom = createOrderItem(order, productWithUom, UnitOfMeasure.findByCode(orderItemUomCode), orderItemQuantityPerUom)
        Map<String, Object> line = buildLine(id: orderItemWithUom.id, productCode: 'P2', quantityToShip: '1', unitOfMeasure: unitOfMeasure)

        expect:
        assert service.validateItemsFromTemplateImport(shipment, [line]) == expectedValid
        assert line.errors.any { it.toString().contains("does not match UOM on PO") } == !expectedValid

        where:
        orderItemUomCode | orderItemQuantityPerUom | unitOfMeasure || expectedValid
        'BX'             | 10                      | 'BX/10'       || true
        'BX'             | 10                      | 'BX/5'        || false
        'BX'             | 10                      | 'EA/10'       || false
        'EA'             | 1                       | 'EA/1'        || true
    }

    void 'addItemsToShipment should add shipment items linked to order items with quantity in standard uom'() {
        given:
        OrderItem orderItemWithUom = createOrderItem(order, createProduct('P2'), null, 10)
        Date expiry = new SimpleDateFormat("MM/dd/yyyy").parse("12/31/2099")
        InventoryItem inventoryItem = new InventoryItem(product: orderItemWithUom.product, lotNumber: 'LOT-1', expirationDate: expiry)
                .save(validate: false)
        Map<String, Object> line = buildLine(id: orderItemWithUom.id, productCode: 'P2', quantityToShip: '2', lotNumber: 'LOT-1', expiry: '12/31/2099')

        when:
        service.addItemsToShipment(shipment, [line])

        then:
        1 * inventoryServiceMock.findOrCreateInventoryItem(orderItemWithUom.product, 'LOT-1', expiry) >> inventoryItem

        and:
        assert shipment.shipmentItems.size() == 1
        ShipmentItem shipmentItem = shipment.shipmentItems.first()
        assert shipmentItem.product == orderItemWithUom.product
        assert shipmentItem.quantity == 20
        assert shipmentItem.lotNumber == 'LOT-1'
        assert shipmentItem.expirationDate == expiry
        assert shipmentItem.inventoryItem == inventoryItem
        assert orderItemWithUom.shipmentItems*.id == [shipmentItem.id]
    }

    void 'addItemsToShipment should skip lines with zero quantity to ship'() {
        when:
        service.addItemsToShipment(shipment, [buildLine(quantityToShip: '0')])

        then:
        0 * inventoryServiceMock._
        assert !shipment.shipmentItems
        assert !orderItem.shipmentItems
    }

    void 'importTemplate should fail with "#expectedMessage" when file is #description'() {
        when:
        service.importTemplate(buildImportCommand(content, contentType), shipment.id)

        then:
        IllegalArgumentException e = thrown()
        assert e.message == expectedMessage

        where:
        description | content    | contentType  || expectedMessage
        "empty"     | ""         | "text/csv"   || "File cannot be empty"
        "not a csv" | CSV_HEADER | "text/plain" || "File must be in CSV format"
    }

    void 'importTemplate should fail listing row errors when lines are invalid'() {
        given:
        ImportDataCommand command = buildImportCommand(CSV_HEADER + "\nUNKNOWN-PO,,P1,,,,1,,,,,")

        when:
        service.importTemplate(command, shipment.id)

        then:
        ValidationException e = thrown()
        assert e.message == "Failed to import template due to validation errors:<br>Row 1: There is no order with number: UNKNOWN-PO; " +
                "Order item does not exit"
        0 * inventoryServiceMock._
        assert !shipment.shipmentItems
    }

    void 'importTemplate should add items to shipment when lines are valid'() {
        given:
        ImportDataCommand command = buildImportCommand(CSV_HEADER + "\nPO-1,,P1,,,,4,,,,,")

        when:
        service.importTemplate(command, shipment.id)

        then:
        0 * inventoryServiceMock._

        and:
        assert shipment.shipmentItems.size() == 1
        ShipmentItem shipmentItem = shipment.shipmentItems.first()
        assert shipmentItem.product == product
        assert shipmentItem.quantity == 4
        assert !shipmentItem.lotNumber
        assert !shipmentItem.inventoryItem
        assert orderItem.shipmentItems*.id == [shipmentItem.id]
    }

    void 'saveItemsToShipment should add shipment items for given order items with quantity in standard uom'() {
        given:
        OrderItem orderItemWithUom = createOrderItem(order, createProduct('P2'), null, 10)

        when:
        service.saveItemsToShipment(shipment, [
                [orderItemId: orderItem.id, quantityToShip: 3, sortOrder: 1],
                [orderItemId: orderItemWithUom.id, quantityToShip: 2, sortOrder: 0],
        ])

        then:
        assert shipment.shipmentItems.size() == 2
        ShipmentItem itemForOrderItem = orderItem.shipmentItems.first()
        ShipmentItem itemForOrderItemWithUom = orderItemWithUom.shipmentItems.first()
        assert itemForOrderItem.shipment == shipment
        assert itemForOrderItem.product == product
        assert itemForOrderItem.quantity == 3
        assert itemForOrderItemWithUom.quantity == 20
        assert itemForOrderItemWithUom.sortOrder < itemForOrderItem.sortOrder
    }

    void 'saveItemsToShipment should not add anything when items to add are #itemsToAdd'() {
        when:
        service.saveItemsToShipment(shipment, itemsToAdd)

        then:
        assert !shipment.shipmentItems

        where:
        itemsToAdd << [null, []]
    }
}
