package org.pih.warehouse.receiving

/**
 * The row of a single shipment item on the Goods Receipt Note, carrying the shipped product and lot, together with the
 * receipt lines rolled up by product and lot below it.
 */
class GoodsReceiptNoteOriginalItemDto extends GoodsReceiptNoteRowDto {

    Integer index
    Integer quantityShipped
    Integer quantityCanceled

    /**
     * Shipped minus received and canceled, so negative when more was received than shipped.
     */
    Integer quantityRemaining

    GoodsReceiptNoteItemStatus status

    List<String> comments = []

    /**
     * Something was received against a different product than the shipped one.
     */
    boolean productCrossedOut

    /**
     * Something was received against a different lot (or product) than the shipped one.
     */
    boolean lotCrossedOut

    /**
     * Empty when everything was received against the shipped product and lot, since the item row already shows it.
     */
    List<GoodsReceiptNoteSplitItemDto> splitItems = []
}
