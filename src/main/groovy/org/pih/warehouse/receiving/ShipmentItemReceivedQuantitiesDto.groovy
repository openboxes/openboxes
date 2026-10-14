package org.pih.warehouse.receiving

/**
 * The received/canceled totals of a single shipment item, summed under the semantics of the workflow each receipt
 * was created with (see ReceiptV2Service.getReceivedQuantitiesByShipmentItemId and {@link ReceiptV2Marker}).
 *
 * Meant for legacy views (e.g. the stock movement packing list) in place of
 * {@link org.pih.warehouse.shipping.ShipmentItem#getQuantityReceived}/{@link
 * org.pih.warehouse.shipping.ShipmentItem#getQuantityCanceled}, which filter every line by product and so undercount
 * v2 receipts whose lines were received against an edited product.
 */
class ShipmentItemReceivedQuantitiesDto {

    Integer quantityReceived

    Integer quantityCanceled

    boolean fullyReceived
}
