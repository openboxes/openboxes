package org.pih.warehouse.receiving

/**
 * A simple, general purpose DTO representing a receipt and its items.
 */
class ReceiptDto implements Comparable<ReceiptDto> {

    String id
    ReceiptStatusCode receiptStatus
    String shipmentId
    Date dateDelivered
    String recipientId
    Date dateCreated
    Date lastUpdated
    List<ReceiptItemDto> receiptItems = []

    static ReceiptDto from(Receipt receipt) {
        return !receipt ? null : new ReceiptDto(
                id: receipt.id,
                receiptStatus: receipt.receiptStatusCode,
                shipmentId: receipt.shipmentId,
                dateDelivered: receipt.actualDeliveryDate,
                recipientId: receipt.recipientId,
                dateCreated: receipt.dateCreated,
                lastUpdated: receipt.lastUpdated,
                receiptItems: receipt.receiptItems.collect { ReceiptItemDto.from(it) },
        )
    }

    @Override
    int compareTo(ReceiptDto o) {
        // When possible, order receipts by when they occurred in reality, otherwise by when they were input.
        return dateDelivered <=> o.dateDelivered ?:
               dateCreated <=> o.dateCreated ?:
               lastUpdated <=> o.lastUpdated ?:
               id <=> o.id
    }
}
