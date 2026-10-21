package org.pih.warehouse.receiving

/**
 * The columns shared by the shipment item rows and the rolled up receipt lines below them on the Goods Receipt Note.
 */
abstract class GoodsReceiptNoteRowDto {

    String productCode
    String productName
    String unitOfMeasure
    String lotNumber
    Date expirationDate

    /**
     * Keyed by receipt id; every receipt of the document has an entry (0 when nothing was received on it).
     */
    Map<String, Integer> quantityReceivedByReceiptId = [:]

    Integer totalQuantityReceived
}
