package org.pih.warehouse.receiving

/**
 * The receipt items of a shipment item received against one product and lot, summed across receipts.
 *
 * Not a single split receipt item ({@link ReceiptItem#isSplitItem}): the sum can include original lines too, e.g. an
 * original line whose lot was edited while receiving.
 */
class GoodsReceiptNoteSplitItemDto extends GoodsReceiptNoteRowDto {

    /**
     * Whether to print the product code, name and UOM on the line. Only needed when the product of the shipment item
     * row is crossed out, otherwise it is the shipped product and repeating it would only add noise.
     */
    boolean productDisplayed
}
