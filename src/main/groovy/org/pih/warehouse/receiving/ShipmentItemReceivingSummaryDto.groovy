package org.pih.warehouse.receiving

import com.fasterxml.jackson.annotation.JsonProperty

import org.pih.warehouse.shipping.ShipmentItemDto

/**
 * Pulls together all the receipt items (including pending ones) associated with a specific shipment item
 * for the purpose of determining the shipment item's current state of receiving.
 */
class ShipmentItemReceivingSummaryDto {

    ShipmentItemDto shipmentItem

    /**
     * The items of receipts that have already been submitted.
     *
     * Note that this collects all receipt items across all previous receipts into a single, flat list.
     * We do this because the receiving summary doesn't need to distinguish between receipts, it only
     * cares about the rollup of data that sum across *all* receipt items.
     *
     * If you need the receipt items in the context of their receipt, consider using {@link ReceiptDto} instead.
     */
    List<ReceiptItemDto> previousReceiptItems = []

    /**
     * The items of receipts that are currently pending and not yet submitted.
     */
    List<ReceiptItemDto> currentReceiptItems = []

    /**
     * @return The total quantity received for the shipment item across all receipt, including not submitted receipts.
     */
    @JsonProperty("totalQuantityReceived")
    int getTotalQuantityReceived() {
        int previousQuantity = previousReceiptItems.sum(0) { ReceiptItemDto item -> item.quantityReceived ?: 0 } as int
        int currentQuantity = currentReceiptItems.sum(0) { ReceiptItemDto item -> item.quantityReceived ?: 0 } as int
        return previousQuantity + currentQuantity
    }

    /**
     * @return The total quantity canceled for the shipment item across all receipt, including not submitted receipts.
     */
    @JsonProperty("totalQuantityCanceled")
    int getTotalQuantityCanceled() {
        int previousQuantity = previousReceiptItems.sum(0) { ReceiptItemDto item -> item.quantityCanceled ?: 0 } as int
        int currentQuantity = currentReceiptItems.sum(0) { ReceiptItemDto item -> item.quantityCanceled ?: 0 } as int
        return previousQuantity + currentQuantity
    }

    /**
     * @return True if the quantity shipped for the item is/will be completely received.
     */
    @JsonProperty("isFullyReceived")
    boolean isFullyReceived() {
        return totalQuantityReceived + totalQuantityCanceled >= shipmentItem.quantity
    }
}
