package org.pih.warehouse.api.receiving.v2

import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.shipping.Shipment

class ReceiptDto {

    String id
    String receiptStatus
    ReceiptShipmentDto shipment
    ReceiptLocationDto origin
    ReceiptLocationDto destination
    Date dateShipped
    Date dateDelivered
    ReceiptRequisitionDto requisition
    ReceiptOrderDto order
    String description
    ReceiptRecipientDto recipient
    // TODO: To be replaced by List<ReceiptItemDto> after the read endpoint is done
    List receiptItems = []

    static ReceiptDto toDto(Receipt receipt) {
        if (!receipt) {
            return null
        }
        Shipment shipment = receipt.shipment
        return new ReceiptDto(
                id: receipt.id,
                receiptStatus: receipt.receiptStatusCode?.name(),
                shipment: ReceiptShipmentDto.toDto(shipment),
                origin: ReceiptLocationDto.toDto(shipment?.origin),
                destination: ReceiptLocationDto.toDto(shipment?.destination),
                dateShipped: shipment?.actualShippingDate,
                dateDelivered: receipt.actualDeliveryDate,
                requisition: ReceiptRequisitionDto.toDto(shipment?.requisition),
                order: ReceiptOrderDto.toDto(shipment?.purchaseOrder),
                description: shipment?.description,
                recipient: ReceiptRecipientDto.toDto(receipt.recipient),
                receiptItems: receipt.receiptItems
        )
    }
}
