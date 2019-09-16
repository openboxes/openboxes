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


    PartialReceiptContainer findPartialReceiptContainer(String containerId) {
        PartialReceiptContainer partialReceiptContainer =
                partialReceiptContainers.find { it?.container?.id == containerId }
        if (!partialReceiptContainer) {
            partialReceiptContainer = findDefaultPartialReceiptContainer()
        }
        return partialReceiptContainer
    }

    PartialReceiptContainer findDefaultPartialReceiptContainer() {
        return partialReceiptContainers.find { it.isDefault() }
    }

    List<PartialReceiptItem> getPartialReceiptItems() {
        List<PartialReceiptItem> partialReceiptItems = []
        partialReceiptContainers.each {
            partialReceiptItems.addAll(it.partialReceiptItems)
        }
        return partialReceiptItems
    }

    Map toJson() {
        return [

                "receiptId"              : receipt?.id,
                receiptStatus            : receiptStatus?.name(),
                "shipmentId"             : shipment?.id,
                "shipment.name"          : shipment?.name,
                "shipment.shipmentNumber": shipment.shipmentNumber,
                shipmentStatus           : shipment?.currentStatus?.name(),
                "origin.id"              : shipment?.origin?.id,
                "origin.name"            : shipment?.origin?.name,
                "destination.id"         : shipment?.destination?.id,
                "destination.name"       : shipment?.destination?.name,
                dateShipped              : shipment.actualShippingDate?.format("MM/dd/yyyy HH:mm XXX"),
                dateDelivered            : dateDelivered?.format("MM/dd/yyyy HH:mm XXX"),
                containers               : partialReceiptContainers,
                requisition              : shipment?.requisition?.id
        ]
    }

}
