package org.pih.warehouse.api

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.receiving.ReceiptStatusCode
import org.pih.warehouse.shipping.ShipmentItem

class PartialReceiptItem {

    ReceiptItem receiptItem
    ShipmentItem shipmentItem
    Integer quantityReceiving

    Location binLocation
    Person recipient
    Boolean cancelRemaining = Boolean.FALSE


    Integer getQuantityShipped() {
        return shipmentItem?.quantity?:0
    }

    Integer getQuantityReceived() {
        def receiptItems = getReceiptItemsByStatus([ReceiptStatusCode.RECEIVED] as ReceiptStatusCode[])
        return receiptItems ? receiptItems?.sum { it?.quantityReceived?:0 } : 0
    }

    Integer getQuantityCanceled() {
        def receiptItems = getReceiptItemsByStatus([ReceiptStatusCode.RECEIVED] as ReceiptStatusCode[])
        return receiptItems ? receiptItems?.sum { it?.quantityCanceled?:0 } : 0
    }

    Integer getQuantityRemaining() {
        Integer quantityCanceled = quantityCanceled?:0
        Integer quantityReceiving = quantityReceiving?:0
        Integer quantityReceived = quantityReceived?:0
        Integer quantityRemaining = quantityShipped - (quantityReceiving + quantityReceived + quantityCanceled)
        return !cancelRemaining ? (quantityRemaining > 0) ? quantityRemaining : 0 : 0
    }

    Set<ReceiptItem> getReceiptItemsByStatus(ReceiptStatusCode[] receiptStatusCodes) {
        def receiptItems = ReceiptItem.findAllByShipmentItem(shipmentItem)
        return receiptItems.findAll { ReceiptItem receiptItem -> receiptItem?.receipt?.receiptStatusCode in receiptStatusCodes }
    }

    Map toJson() {
        return [

                "receiptItem.id": receiptItem?.id,
                "shipmentItem.id": shipmentItem?.id,
                "container.id": shipmentItem?.container?.id,
                "container.name": shipmentItem?.container?.name,
                "parentContainer.id": shipmentItem?.container?.parentContainer?.id,
                "parentContainer.name": shipmentItem?.container?.parentContainer?.name,
                "product.id": shipmentItem?.inventoryItem?.product?.id,
                "product.productCode": shipmentItem?.inventoryItem?.product?.productCode,
                "product.name": shipmentItem?.inventoryItem?.product?.name,
                "inventoryItem.id": shipmentItem?.inventoryItem?.id,
                "inventoryItem.lotNumber": shipmentItem?.inventoryItem?.lotNumber,
                "inventoryItem.expirationDate": shipmentItem?.inventoryItem?.expirationDate?.format("MM/dd/yyyy"),
                "binLocation.id": binLocation?.id,
                "binLocation.name": binLocation?.name,
                "recipient.id": recipient?.id,
                "recipient.name": recipient?.name,
                quantityShipped: quantityShipped,
                quantityReceived: quantityReceived,
                quantityReceiving: quantityReceiving,
                quantityRemaining: quantityRemaining,
                cancelRemaining: cancelRemaining
        ]
    }

}
