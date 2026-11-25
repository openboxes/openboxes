package org.pih.warehouse.receiving

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.spring.beans.factory.InstanceFactoryBean
import spock.lang.Specification

import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

class GoodsReceiptNoteServiceSpec extends Specification implements ServiceUnitTest<GoodsReceiptNoteService>, DataTest {

    Shipment shipment
    Product amlodipine
    Product atenolol50
    Product atenolol100

    void setupSpec() {
        mockDomains(Shipment, ShipmentItem, Receipt, ReceiptItem, Product, InventoryItem, Location)
    }

    void setup() {
        service.shipmentReceivingCalculator = new ShipmentReceivingCalculator()

        // Product display names are localized through a bean looked up statically, which the DataTest context lacks.
        LocalizationService localizationServiceStub = Stub(LocalizationService) {
            getCurrentLocale() >> Locale.ENGLISH
        }
        defineBeans {
            localizationService(InstanceFactoryBean, localizationServiceStub, LocalizationService)
        }

        shipment = new Shipment(shipmentNumber: "257TSG", description: "Drugs", origin: new Location(name: "Origin"),
                destination: new Location(name: "Destination"))
        shipment.id = "s1"
        shipment.events = new TreeSet()
        GroovySpy(Shipment, global: true)
        Shipment.get("s1") >> shipment
        shipment.receipts = new TreeSet()
        shipment.shipmentItems = [] as Set
        amlodipine = buildProduct("p1", "12660")
        atenolol50 = buildProduct("p2", "10993")
        atenolol100 = buildProduct("p3", "12334")
    }

    void 'should only take completed receipts into account'() {
        given:
        ShipmentItem shipmentItem = buildShipmentItem(1, amlodipine, buildLot("i1", amlodipine, "Lot1"), 300)
        Receipt received = buildReceipt("r1", ReceiptStatusCode.RECEIVED, new Date() - 2)
        Receipt pending = buildReceipt("r2", ReceiptStatusCode.PENDING, new Date())
        buildReceiptItem(received, shipmentItem, amlodipine, shipmentItem.inventoryItem, 100)
        buildReceiptItem(pending, shipmentItem, amlodipine, shipmentItem.inventoryItem, null)

        when:
        GoodsReceiptNoteDto document = service.buildGoodsReceiptNote("s1")

        then:
        assert document.receipts*.id == ["r1"]
        assert !document.receiptColumnsDisplayed
        assert document.lastReceiptDate == received.actualDeliveryDate
        assert document.items.size() == 1
        assert document.items[0].totalQuantityReceived == 100
        assert document.items[0].status == GoodsReceiptNoteItemStatus.REMAINING
        assert document.items[0].quantityRemaining == 200
    }

    void 'should list a shipment item that was not received with nothing received'() {
        given:
        buildShipmentItem(1, amlodipine, null, 50)

        when:
        GoodsReceiptNoteOriginalItemDto item = service.buildGoodsReceiptNote("s1").items[0]

        then:
        assert item.index == 1
        assert item.quantityShipped == 50
        assert item.totalQuantityReceived == 0
        assert item.splitItems.empty
        assert !item.lotCrossedOut
        assert item.status == GoodsReceiptNoteItemStatus.REMAINING
    }

    void 'should roll up lines of the shipped product and lot onto the shipment item row'() {
        given:
        ShipmentItem shipmentItem = buildShipmentItem(1, amlodipine, buildLot("i1", amlodipine, "Lot1"), 300)
        Receipt first = buildReceipt("r1", ReceiptStatusCode.RECEIVED, new Date() - 2)
        Receipt second = buildReceipt("r2", ReceiptStatusCode.RECEIVED, new Date() - 1)
        buildReceiptItem(first, shipmentItem, amlodipine, shipmentItem.inventoryItem, 100, [comment: "Damaged box"])
        buildReceiptItem(first, shipmentItem, amlodipine, shipmentItem.inventoryItem, 50, [isSplitItem: true])
        buildReceiptItem(second, shipmentItem, amlodipine, shipmentItem.inventoryItem, 150, [comment: "Damaged box"])

        when:
        GoodsReceiptNoteDto document = service.buildGoodsReceiptNote("s1")
        GoodsReceiptNoteOriginalItemDto item = document.items[0]

        then:
        assert document.receiptColumnsDisplayed
        assert item.splitItems.empty
        assert !item.lotCrossedOut
        assert item.quantityReceivedByReceiptId == [r1: 150, r2: 150]
        assert item.totalQuantityReceived == 300
        assert item.status == GoodsReceiptNoteItemStatus.COMPLETE
        assert item.quantityRemaining == 0
        assert item.comments == ["Damaged box"]
    }

    void 'should list each received lot below a shipment item shipped without a lot'() {
        given:
        ShipmentItem shipmentItem = buildShipmentItem(1, amlodipine, buildLot("i0", amlodipine, null), 200)
        Receipt receipt = buildReceipt("r1", ReceiptStatusCode.RECEIVED, new Date() - 1)
        buildReceiptItem(receipt, shipmentItem, amlodipine, shipmentItem.inventoryItem, 0)
        buildReceiptItem(receipt, shipmentItem, amlodipine, buildLot("i1", amlodipine, "Lot1"), 100, [isSplitItem: true])
        buildReceiptItem(receipt, shipmentItem, amlodipine, buildLot("i2", amlodipine, "Lot2"), 150, [isSplitItem: true])

        when:
        GoodsReceiptNoteOriginalItemDto item = service.buildGoodsReceiptNote("s1").items[0]

        then: 'the empty original line is left out'
        assert item.lotCrossedOut
        assert !item.productCrossedOut
        assert item.splitItems*.lotNumber == ["Lot1", "Lot2"]
        assert item.splitItems*.totalQuantityReceived == [100, 150]
        assert item.splitItems.every { !it.productDisplayed }
        assert item.totalQuantityReceived == 250
        assert item.status == GoodsReceiptNoteItemStatus.OVER
        assert item.quantityRemaining == -50
    }

    void 'should cross out the product and display it on every line when a different product was received'() {
        given:
        ShipmentItem shipmentItem = buildShipmentItem(1, atenolol50, buildLot("i0", atenolol50, null), 300)
        Receipt receipt = buildReceipt("r1", ReceiptStatusCode.RECEIVED, new Date() - 1)
        buildReceiptItem(receipt, shipmentItem, atenolol50, shipmentItem.inventoryItem, 100, [quantityCanceled: 50])
        buildReceiptItem(receipt, shipmentItem, atenolol100, buildLot("i3", atenolol100, "32435"), 150, [isSplitItem: true])

        when:
        GoodsReceiptNoteOriginalItemDto item = service.buildGoodsReceiptNote("s1").items[0]

        then:
        assert item.productCrossedOut
        assert item.lotCrossedOut
        assert item.splitItems*.productCode == ["10993", "12334"]
        assert item.splitItems.every { it.productDisplayed }
        assert item.totalQuantityReceived == 250
        assert item.status == GoodsReceiptNoteItemStatus.CANCELED
        assert item.quantityCanceled == 50
    }

    void 'should fail when the shipment does not exist'() {
        when:
        service.buildGoodsReceiptNote("missing")

        then:
        IllegalStateException exception = thrown()
        assert exception.message == "Unable to locate shipment missing"
    }

    void 'should keep shipment order and number every shipment item'() {
        given:
        buildShipmentItem(2, atenolol50, null, 10)
        buildShipmentItem(1, amlodipine, null, 20)

        when:
        List<GoodsReceiptNoteOriginalItemDto> items = service.buildGoodsReceiptNote("s1").items

        then:
        assert items*.index == [1, 2]
        assert items*.productCode == ["12660", "10993"]
    }

    private static Product buildProduct(String id, String productCode) {
        Product product = new Product(productCode: productCode, name: productCode)
        product.id = id
        return product
    }

    private static InventoryItem buildLot(String id, Product product, String lotNumber) {
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: lotNumber)
        inventoryItem.id = id
        return inventoryItem
    }

    private ShipmentItem buildShipmentItem(Integer sortOrder, Product product, InventoryItem inventoryItem, Integer quantity) {
        ShipmentItem shipmentItem = new ShipmentItem(shipment: shipment, product: product, inventoryItem: inventoryItem,
                quantity: quantity, sortOrder: sortOrder)
        shipmentItem.receiptItems = [] as Set
        shipment.shipmentItems.add(shipmentItem)
        return shipmentItem
    }

    private Receipt buildReceipt(String id, ReceiptStatusCode statusCode, Date dateCreated) {
        Receipt receipt = new Receipt(shipment: shipment, receiptNumber: id, receiptStatusCode: statusCode,
                actualDeliveryDate: dateCreated)
        receipt.id = id
        receipt.dateCreated = dateCreated
        receipt.receiptItems = [] as Set
        shipment.receipts.add(receipt)
        return receipt
    }

    private static ReceiptItem buildReceiptItem(Receipt receipt, ShipmentItem shipmentItem, Product product,
                                                InventoryItem inventoryItem, Integer quantityReceived, Map args = [:]) {
        ReceiptItem receiptItem = new ReceiptItem(receipt: receipt, shipmentItem: shipmentItem, product: product,
                inventoryItem: inventoryItem, quantityReceived: quantityReceived,
                quantityCanceled: args.quantityCanceled, comment: args.comment,
                isSplitItem: args.isSplitItem ?: false, sortOrder: receipt.receiptItems.size())
        receipt.receiptItems.add(receiptItem)
        shipmentItem.receiptItems.add(receiptItem)
        return receiptItem
    }
}
