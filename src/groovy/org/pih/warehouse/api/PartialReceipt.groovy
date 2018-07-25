package org.pih.warehouse.api

import org.pih.warehouse.core.Person
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.shipping.Shipment


class PartialReceipt {

    Shipment shipment
    Receipt receipt

    PartialReceiptStatus receiptStatus = PartialReceiptStatus.PENDING

    Date dateShipped
    Date dateDelivered

    Person recipient

    List<PartialReceiptContainer> partialReceiptContainers = []

    List<PartialReceiptItem> getPartialReceiptItems() {
        List<PartialReceiptItem> partialReceiptItems = []
        partialReceiptContainers.each {
            partialReceiptItems.addAll(it.partialReceiptItems)
        }
        return partialReceiptItems
    }

    Map toJson() {
        return [

                "receipt.id": receipt?.id,
                receiptStatus: receiptStatus?.name(),
                "shipment.id": shipment?.id,
                "shipment.name": shipment?.name,
                "shipment.shipmentNumber": shipment.shipmentNumber,
                shipmentStatus: shipment?.currentStatus?.name(),
                "origin.id": shipment?.origin?.id,
                "origin.name": shipment?.origin?.name,
                "destination.id": shipment?.destination?.id,
                "destination.name": shipment?.destination?.name,
                dateShipped: shipment.actualShippingDate,
                dateDelivered: dateDelivered,
                containers: partialReceiptContainers
        ]
    }

}
