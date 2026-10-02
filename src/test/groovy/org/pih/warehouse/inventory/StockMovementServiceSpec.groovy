package org.pih.warehouse.inventory

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

class StockMovementServiceSpec extends Specification implements ServiceUnitTest<StockMovementService>, DataTest {

    void setupSpec() {
        mockDomains(Shipment, ShipmentItem, Receipt, ReceiptItem)
    }

    void 'getStockMovementReceiptItems should return all receipt items by default'() {
        given:
        ReceiptItem receivedReceiptItem = new ReceiptItem(sortOrder: 1, quantityReceived: 5)
        ReceiptItem nullQuantityReceiptItem = new ReceiptItem(sortOrder: 2)
        StockMovement stockMovement = buildStockMovement([buildReceipt([receivedReceiptItem, nullQuantityReceiptItem])])

        when:
        List<ReceiptItem> result = service.getStockMovementReceiptItems(stockMovement)

        then:
        assert result == [receivedReceiptItem, nullQuantityReceiptItem]
    }

    void 'getStockMovementReceiptItems should filter out the items without quantity received or canceled'() {
        given:
        ReceiptItem receivedReceiptItem = new ReceiptItem(sortOrder: 1, quantityReceived: 5)
        ReceiptItem canceledReceiptItem = new ReceiptItem(sortOrder: 2, quantityReceived: 0, quantityCanceled: 3)
        ReceiptItem zeroQuantityReceiptItem = new ReceiptItem(sortOrder: 3, quantityReceived: 0, quantityCanceled: 0)
        ReceiptItem nullQuantityReceiptItem = new ReceiptItem(sortOrder: 4)
        Receipt receipt = buildReceipt(
                [receivedReceiptItem, canceledReceiptItem, zeroQuantityReceiptItem, nullQuantityReceiptItem])
        StockMovement stockMovement = buildStockMovement([receipt])

        when:
        List<ReceiptItem> result = service.getStockMovementReceiptItems(stockMovement, true)

        then:
        assert result == [receivedReceiptItem, canceledReceiptItem]
    }

    void 'getStockMovementReceiptItems should sort by requisition item, shipment item, receipt item and product'() {
        given:
        ReceiptItem receiptItem1 = buildReceiptItem(0, 9, 9, "Z")
        ReceiptItem receiptItem2 = buildReceiptItem(1, 0, 9, "Z")
        ReceiptItem receiptItem3 = buildReceiptItem(1, 1, 0, "Z")
        ReceiptItem receiptItem4 = buildReceiptItem(1, 1, 1, "A")
        ReceiptItem receiptItem5 = buildReceiptItem(1, 1, 1, "B")
        StockMovement stockMovement = buildStockMovement([
                buildReceipt([receiptItem5, receiptItem4, receiptItem3, receiptItem2, receiptItem1]),
        ])

        when:
        List<ReceiptItem> result = service.getStockMovementReceiptItems(stockMovement)

        then:
        assert result == [receiptItem1, receiptItem2, receiptItem3, receiptItem4, receiptItem5]
    }

    private static StockMovement buildStockMovement(List<Receipt> receipts) {
        return new StockMovement(shipment: new Shipment(receipts: receipts as SortedSet))
    }

    private static Receipt buildReceipt(List<ReceiptItem> receiptItems) {
        Receipt receipt = new Receipt()
        receiptItems.each { ReceiptItem receiptItem -> receipt.addToReceiptItems(receiptItem) }
        return receipt
    }

    private static ReceiptItem buildReceiptItem(Integer orderIndex, Integer shipmentItemSortOrder, Integer sortOrder,
                                                String productName) {
        return new ReceiptItem(
                shipmentItem: new ShipmentItem(
                        sortOrder: shipmentItemSortOrder,
                        requisitionItem: new RequisitionItem(orderIndex: orderIndex),
                ),
                sortOrder: sortOrder,
                inventoryItem: new InventoryItem(product: new Product(name: productName)),
        )
    }
}
