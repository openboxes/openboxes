package org.pih.warehouse.api

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.shipping.ShipmentItem

class PartialReceiptItem {

    ReceiptItem receiptItem
    ShipmentItem shipmentItem
    Integer quantityReceiving

    Location binLocation
    Person recipient
    Boolean cancelRemaining = Boolean.FALSE

    String lotNumber
    Date expirationDate
    Integer quantityShipped
    Product product

    Integer getQuantityReceived() {
        def receiptItems = ReceiptItem.findAllByShipmentItem(shipmentItem)
        return receiptItems ? receiptItems?.sum { it?.quantityReceived?:0 } : 0
    }

    Integer getQuantityCanceled() {
        def receiptItems = ReceiptItem.findAllByShipmentItem(shipmentItem)
        return receiptItems ? receiptItems?.sum { it?.quantityCanceled?:0 } : 0
    }


    Integer getQuantityRemaining() {
        Integer quantityCanceled = quantityCanceled?:0
        Integer quantityReceiving = quantityReceiving?:0
        Integer quantityReceived = quantityReceived?:0
        Integer quantityRemaining = quantityShipped - (quantityReceiving + quantityReceived + quantityCanceled)
        return !cancelRemaining ? (quantityRemaining > 0) ? quantityRemaining : 0 : 0
    }

    InventoryItem getInventoryItem() {
        receiptItem ? receiptItem.inventoryItem : shipmentItem?.inventoryItem
    }

    Product getProduct() {
        return product ?: inventoryItem?.product
    }

    Map toJson() {
        return [
                "shipmentItemId": shipmentItem?.id,
                "receiptItemId": receiptItem?.id,
                "container.id": shipmentItem?.container?.id,
                "container.name": shipmentItem?.container?.name,
                "parentContainer.id": shipmentItem?.container?.parentContainer?.id,
                "parentContainer.name": shipmentItem?.container?.parentContainer?.name,
                "product.id": inventoryItem?.product?.id,
                "product.productCode": inventoryItem?.product?.productCode,
                "product.name": inventoryItem?.product?.name,
                "lotNumber": lotNumber,
                "expirationDate": expirationDate?.format("MM/dd/yyyy"),
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
