package org.pih.warehouse.receiving

class GoodsReceiptNoteReceiptDto {

    String id
    String receiptNumber
    Date actualDeliveryDate

    static GoodsReceiptNoteReceiptDto from(Receipt receipt) {
        return new GoodsReceiptNoteReceiptDto(
                id: receipt.id,
                receiptNumber: receipt.receiptNumber,
                actualDeliveryDate: receipt.actualDeliveryDate,
        )
    }
}
