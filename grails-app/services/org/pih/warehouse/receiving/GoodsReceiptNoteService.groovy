package org.pih.warehouse.receiving

import grails.gorm.transactions.Transactional

import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

/**
 * Builds the data of the Goods Receipt Note so that it renders the same for receipts of the old and the new (v2)
 * receiving workflow. The rows are derived from the shipment items rather than from the receipt items.
 */
@Transactional(readOnly = true)
class GoodsReceiptNoteService {

    ShipmentReceivingCalculator shipmentReceivingCalculator

    GoodsReceiptNoteDto buildGoodsReceiptNote(String shipmentId) {
        Shipment shipment = Shipment.get(shipmentId)
        if (!shipment) {
            throw new IllegalStateException("Unable to locate shipment ${shipmentId}")
        }

        List<Receipt> receivedReceipts = (shipment.receipts ?: []).findAll { Receipt receipt ->
            receipt.receiptStatusCode == ReceiptStatusCode.RECEIVED
        } as List<Receipt>

        return new GoodsReceiptNoteDto(
                shipmentNumber: shipment.shipmentNumber,
                description: shipment.description,
                statusCode: shipment.status?.code,
                originName: shipment.origin?.name,
                destinationName: shipment.destination?.name,
                dateShipped: shipment.actualShippingDate,
                lastReceiptDate: receivedReceipts*.actualDeliveryDate.max(),
                receipts: receivedReceipts.collect { Receipt receipt -> GoodsReceiptNoteReceiptDto.from(receipt) },
                // Starts at 1 since the index is printed as the line number ("#" column) of the note.
                items: shipment.sortShipmentItemsBySortOrder().indexed(1).collect { Integer index, ShipmentItem shipmentItem ->
                    buildOriginalItem(shipmentItem, index, receivedReceipts)
                },
        )
    }

    private GoodsReceiptNoteOriginalItemDto buildOriginalItem(ShipmentItem shipmentItem, Integer index, List<Receipt> receivedReceipts) {
        List<ReceiptItem> receivedReceiptItems = findReceivedReceiptItems(shipmentItem)
        List<List<ReceiptItem>> productLotGroups = groupByProductAndLot(receivedReceiptItems)

        boolean productCrossedOut = productLotGroups.any { List<ReceiptItem> productLotGroup ->
            productLotGroup.first().product != shipmentItem.product
        }
        boolean lotCrossedOut = productCrossedOut || productLotGroups.any { List<ReceiptItem> productLotGroup ->
            getLotNumber(productLotGroup.first()) != getLotNumber(shipmentItem)
        }

        int totalQuantityReceived = sumQuantityReceived(receivedReceiptItems)
        int quantityCanceled = sumQuantityCanceled(receivedReceiptItems)
        int quantityRemaining = shipmentReceivingCalculator.getShipmentItemQuantityRemaining(shipmentItem)

        return new GoodsReceiptNoteOriginalItemDto(
                index: index,
                productCode: shipmentItem.product?.productCode,
                productName: shipmentItem.product?.displayNameOrDefaultName,
                unitOfMeasure: shipmentItem.product?.unitOfMeasure,
                lotNumber: getLotNumber(shipmentItem),
                expirationDate: shipmentItem.inventoryItem?.expirationDate ?: shipmentItem.expirationDate,
                quantityShipped: shipmentItem.quantity,
                quantityReceivedByReceiptId: sumQuantityReceivedByReceiptId(receivedReceiptItems, receivedReceipts),
                totalQuantityReceived: totalQuantityReceived,
                quantityCanceled: quantityCanceled,
                quantityRemaining: quantityRemaining,
                status: resolveStatus(quantityRemaining, quantityCanceled),
                comments: receivedReceiptItems*.comment.findAll().unique(),
                productCrossedOut: productCrossedOut,
                lotCrossedOut: lotCrossedOut,
                splitItems: lotCrossedOut
                        ? productLotGroups.collect { List<ReceiptItem> productLotGroup -> buildSplitItem(productLotGroup, receivedReceipts, productCrossedOut) }
                        : [],
        )
    }

    private GoodsReceiptNoteSplitItemDto buildSplitItem(List<ReceiptItem> productLotGroup, List<Receipt> receivedReceipts, boolean productDisplayed) {
        ReceiptItem firstReceiptItem = productLotGroup.first()
        Product product = firstReceiptItem.product
        return new GoodsReceiptNoteSplitItemDto(
                productCode: product?.productCode,
                productName: product?.displayNameOrDefaultName,
                unitOfMeasure: product?.unitOfMeasure,
                productDisplayed: productDisplayed,
                lotNumber: getLotNumber(firstReceiptItem),
                expirationDate: firstReceiptItem.inventoryItem?.expirationDate ?: firstReceiptItem.expirationDate,
                quantityReceivedByReceiptId: sumQuantityReceivedByReceiptId(productLotGroup, receivedReceipts),
                totalQuantityReceived: sumQuantityReceived(productLotGroup),
        )
    }

    /**
     * The receipt items of completed receipts, in receipt order (the same order as the receipt columns, see
     * {@link Receipt#compareTo}) with the original line of each receipt first, so that the rolled up lines and comments
     * come out in the order they were received.
     */
    private List<ReceiptItem> findReceivedReceiptItems(ShipmentItem shipmentItem) {
        return (shipmentItem.receiptItems ?: [])
                .findAll { ReceiptItem receiptItem -> receiptItem.receipt?.receiptStatusCode == ReceiptStatusCode.RECEIVED }
                .sort { ReceiptItem a, ReceiptItem b ->
                    a.receipt <=> b.receipt ?:
                            b.isOriginalLine() <=> a.isOriginalLine() ?:
                                    (a.sortOrder ?: 0) <=> (b.sortOrder ?: 0)
                }
    }

    /**
     * Rolls the receipt items up by product and lot, leaving out the combinations where nothing was received.
     * All receipt items of a group share the product and inventory item, so any of them (we take the first) can be
     * used to read those for the whole group.
     */
    private List<List<ReceiptItem>> groupByProductAndLot(List<ReceiptItem> receiptItems) {
        return receiptItems
                .groupBy { ReceiptItem receiptItem -> [receiptItem.product?.id, receiptItem.inventoryItem?.id] }
                .values()
                .findAll { List<ReceiptItem> productLotGroup -> sumQuantityReceived(productLotGroup) > 0 } as List<List<ReceiptItem>>
    }

    private String getLotNumber(ShipmentItem shipmentItem) {
        return shipmentItem.inventoryItem?.lotNumber ?: shipmentItem.lotNumber ?: null
    }

    private String getLotNumber(ReceiptItem receiptItem) {
        return receiptItem.inventoryItem?.lotNumber ?: receiptItem.lotNumber ?: null
    }

    private int sumQuantityReceived(List<ReceiptItem> receiptItems) {
        return receiptItems.sum(0) { ReceiptItem receiptItem -> receiptItem.quantityReceived ?: 0 } as int
    }

    private int sumQuantityCanceled(List<ReceiptItem> receiptItems) {
        return receiptItems.sum(0) { ReceiptItem receiptItem -> receiptItem.quantityCanceled ?: 0 } as int
    }

    private Map<String, Integer> sumQuantityReceivedByReceiptId(List<ReceiptItem> receiptItems, List<Receipt> receivedReceipts) {
        Map<String, List<ReceiptItem>> receiptItemsByReceiptId = receiptItems.groupBy { ReceiptItem receiptItem -> receiptItem.receipt.id }
        return receivedReceipts.collectEntries { Receipt receipt ->
            [(receipt.id): sumQuantityReceived(receiptItemsByReceiptId[receipt.id] ?: [])]
        }
    }

    /**
     * The same precedence as the receiving workflow: a canceled remainder wins, then complete, then over/remaining.
     */
    private GoodsReceiptNoteItemStatus resolveStatus(int quantityRemaining, int quantityCanceled) {
        if (quantityCanceled > 0) {
            return GoodsReceiptNoteItemStatus.CANCELED
        }
        if (quantityRemaining == 0) {
            return GoodsReceiptNoteItemStatus.COMPLETE
        }
        return quantityRemaining < 0 ? GoodsReceiptNoteItemStatus.OVER : GoodsReceiptNoteItemStatus.REMAINING
    }
}
