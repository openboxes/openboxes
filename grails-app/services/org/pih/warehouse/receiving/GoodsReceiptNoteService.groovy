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

    GoodsReceiptNoteDto buildGoodsReceiptNote(Shipment shipment) {
        List<Receipt> receipts = (shipment.receipts ?: []).findAll { Receipt receipt ->
            receipt.receiptStatusCode == ReceiptStatusCode.RECEIVED
        } as List<Receipt>

        return new GoodsReceiptNoteDto(
                shipmentNumber: shipment.shipmentNumber,
                description: shipment.description,
                statusCode: shipment.status?.code,
                originName: shipment.origin?.name,
                destinationName: shipment.destination?.name,
                dateShipped: shipment.actualShippingDate,
                lastReceiptDate: receipts*.actualDeliveryDate.max(),
                receipts: receipts.collect { Receipt receipt -> GoodsReceiptNoteReceiptDto.from(receipt) },
                items: shipment.sortShipmentItemsBySortOrder().indexed(1).collect { Integer index, ShipmentItem shipmentItem ->
                    buildOriginalItem(shipmentItem, index, receipts)
                },
        )
    }

    private static GoodsReceiptNoteOriginalItemDto buildOriginalItem(ShipmentItem shipmentItem, Integer index, List<Receipt> receipts) {
        List<ReceiptItem> receiptItems = findReceivedReceiptItems(shipmentItem, receipts)
        List<List<ReceiptItem>> receivedGroups = groupByProductAndLot(receiptItems)

        boolean productCrossedOut = receivedGroups.any { List<ReceiptItem> group ->
            group.first().product != shipmentItem.product
        }
        boolean lotCrossedOut = productCrossedOut || receivedGroups.any { List<ReceiptItem> group ->
            getLotNumber(group.first()) != getLotNumber(shipmentItem)
        }

        int totalQuantityReceived = sumQuantityReceived(receiptItems)
        int quantityCanceled = sumQuantityCanceled(receiptItems)
        int quantityRemaining = (shipmentItem.quantity ?: 0) - totalQuantityReceived - quantityCanceled

        return new GoodsReceiptNoteOriginalItemDto(
                index: index,
                productCode: shipmentItem.product?.productCode,
                productName: shipmentItem.product?.displayNameOrDefaultName,
                unitOfMeasure: shipmentItem.product?.unitOfMeasure,
                lotNumber: getLotNumber(shipmentItem),
                expirationDate: shipmentItem.inventoryItem?.expirationDate ?: shipmentItem.expirationDate,
                quantityShipped: shipmentItem.quantity,
                quantityReceivedByReceiptId: sumQuantityReceivedByReceiptId(receiptItems, receipts),
                totalQuantityReceived: totalQuantityReceived,
                quantityCanceled: quantityCanceled,
                quantityRemaining: quantityRemaining,
                status: GoodsReceiptNoteItemStatus.of(quantityRemaining, quantityCanceled),
                comments: receiptItems*.comment.findAll().unique(),
                productCrossedOut: productCrossedOut,
                lotCrossedOut: lotCrossedOut,
                splitItems: lotCrossedOut
                        ? receivedGroups.collect { List<ReceiptItem> group -> buildSplitItem(group, receipts, productCrossedOut) }
                        : [],
        )
    }

    private static GoodsReceiptNoteSplitItemDto buildSplitItem(List<ReceiptItem> group, List<Receipt> receipts, boolean productDisplayed) {
        ReceiptItem receiptItem = group.first()
        Product product = receiptItem.product
        return new GoodsReceiptNoteSplitItemDto(
                productCode: product?.productCode,
                productName: product?.displayNameOrDefaultName,
                unitOfMeasure: product?.unitOfMeasure,
                productDisplayed: productDisplayed,
                lotNumber: getLotNumber(receiptItem),
                expirationDate: receiptItem.inventoryItem?.expirationDate ?: receiptItem.expirationDate,
                quantityReceivedByReceiptId: sumQuantityReceivedByReceiptId(group, receipts),
                totalQuantityReceived: sumQuantityReceived(group),
        )
    }

    /**
     * The receipt items of completed receipts, in receipt order with the original line of each receipt first, so that
     * the rolled up lines and comments come out in the order they were received.
     */
    private static List<ReceiptItem> findReceivedReceiptItems(ShipmentItem shipmentItem, List<Receipt> receipts) {
        List<String> receiptIds = receipts*.id
        return (shipmentItem.receiptItems ?: [])
                .findAll { ReceiptItem receiptItem -> receiptItem.receipt?.id in receiptIds }
                .sort { ReceiptItem a, ReceiptItem b ->
                    receiptIds.indexOf(a.receipt.id) <=> receiptIds.indexOf(b.receipt.id) ?:
                            b.isOriginalLine() <=> a.isOriginalLine() ?:
                                    (a.sortOrder ?: 0) <=> (b.sortOrder ?: 0)
                }
    }

    /**
     * Rolls the receipt items up by product and lot, leaving out the combinations where nothing was received.
     */
    private static List<List<ReceiptItem>> groupByProductAndLot(List<ReceiptItem> receiptItems) {
        return receiptItems
                .groupBy { ReceiptItem receiptItem -> [receiptItem.product?.id, receiptItem.inventoryItem?.id] }
                .values()
                .findAll { List<ReceiptItem> group -> sumQuantityReceived(group) > 0 } as List<List<ReceiptItem>>
    }

    private static String getLotNumber(ShipmentItem shipmentItem) {
        return shipmentItem.inventoryItem?.lotNumber ?: shipmentItem.lotNumber ?: null
    }

    private static String getLotNumber(ReceiptItem receiptItem) {
        return receiptItem.inventoryItem?.lotNumber ?: receiptItem.lotNumber ?: null
    }

    private static int sumQuantityReceived(List<ReceiptItem> receiptItems) {
        return receiptItems.sum(0) { ReceiptItem receiptItem -> receiptItem.quantityReceived ?: 0 } as int
    }

    private static int sumQuantityCanceled(List<ReceiptItem> receiptItems) {
        return receiptItems.sum(0) { ReceiptItem receiptItem -> receiptItem.quantityCanceled ?: 0 } as int
    }

    private static Map<String, Integer> sumQuantityReceivedByReceiptId(List<ReceiptItem> receiptItems, List<Receipt> receipts) {
        Map<String, List<ReceiptItem>> receiptItemsByReceiptId = receiptItems.groupBy { ReceiptItem receiptItem -> receiptItem.receipt.id }
        return receipts.collectEntries { Receipt receipt ->
            [(receipt.id): sumQuantityReceived(receiptItemsByReceiptId[receipt.id] ?: [])]
        }
    }
}
