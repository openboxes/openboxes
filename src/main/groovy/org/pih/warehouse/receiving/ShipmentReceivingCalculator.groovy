package org.pih.warehouse.receiving

import org.springframework.stereotype.Component

import org.pih.warehouse.api.receiving.v2.ReceiptV2Service
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

/**
 * The v2 receiving math: what a shipment item still has left to receive, and whether a shipment has anything left
 * at all. Both are read off the receipt items of a shipment item across every completed (RECEIVED) receipt of its
 * shipment, and both deliberately ignore the product a line was received against - see the individual methods for
 * why that differs from the legacy getters on {@link ShipmentItem} and {@link Shipment}.
 *
 * Read-only: nothing here writes, so it can be called at any point of a request.
 */
@Component
class ShipmentReceivingCalculator {

    /**
     * The quantity of a shipment item still left to receive: its quantity minus everything received or canceled
     * on its receipt items across completed receipts (the receipt being completed is already flagged as RECEIVED
     * when the cancels are computed, so its lines count too).
     *
     * Deliberately not {@link ShipmentItem#getQuantityRemaining}: the legacy getters behind it only count receipt
     * items whose product matches the shipment item's, while lines can be received against an edited product
     * (see {@link ReceiptV2Service#upsertReceiptItem}) and must still consume the shipment item's remainder -
     * exactly as {@link ShipmentItemReceivingSummaryDto} totals them for the receiving client.
     */
    Integer getShipmentItemQuantityRemaining(ShipmentItem shipmentItem) {
        int quantityReceivedAndCanceled = (shipmentItem.receiptItems ?: []).sum(0) { ReceiptItem receiptItem ->
            receiptItem.receipt?.receiptStatusCode == ReceiptStatusCode.RECEIVED
                    ? (receiptItem.quantityReceived ?: 0) + (receiptItem.quantityCanceled ?: 0)
                    : 0
        } as int
        return (shipmentItem.quantity ?: 0) - quantityReceivedAndCanceled
    }

    /**
     * Whether every line of the shipment is fully received: nothing left to receive (or cancel) on any of its
     * shipment items, per the same product-agnostic math as {@link #getShipmentItemQuantityRemaining}.
     *
     * Deliberately not {@link Shipment#isFullyReceived}: the legacy check ignores lines received against an edited
     * product, so after the v2 flow consumed a shipment item's full quantity that way it would still report the
     * shipment as receivable (and only ever partially received).
     */
    boolean isShipmentFullyReceived(Shipment shipment) {
        return shipment.shipmentItems?.every { ShipmentItem shipmentItem ->
            getShipmentItemQuantityRemaining(shipmentItem) <= 0
        }
    }
}
