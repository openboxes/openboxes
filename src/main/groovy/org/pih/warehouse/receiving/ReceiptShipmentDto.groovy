package org.pih.warehouse.receiving

import org.pih.warehouse.shipping.Shipment

class ReceiptShipmentDto {
    String id
    String name
    String shipmentNumber
    String shipmentStatus
    Boolean isFromPurchaseOrder

    static ReceiptShipmentDto toDto(Shipment shipment) {
        if (!shipment) {
            return null
        }
        return new ReceiptShipmentDto(
                id: shipment.id,
                name: shipment.name,
                shipmentNumber: shipment.shipmentNumber,
                shipmentStatus: shipment.status?.code?.name(),
                isFromPurchaseOrder: shipment.isFromPurchaseOrder,
        )
    }
}
