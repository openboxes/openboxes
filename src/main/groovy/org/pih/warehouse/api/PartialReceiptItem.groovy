package org.pih.warehouse.api

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
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

    Boolean isSplitItem = Boolean.FALSE
    Boolean shouldSave = Boolean.FALSE

    String lotNumber
    Date expirationDate
    Integer quantityShipped
    Product product

    String comment

    Integer getQuantityReceived() {
        if (isSplitItem) {
            return 0
        }
        def receiptItems = getReceiptItemsByStatus([ReceiptStatusCode.RECEIVED] as ReceiptStatusCode[])
        return receiptItems ? receiptItems?.sum { it?.quantityReceived ?: 0 } : 0
    }

    Integer getQuantityCanceled() {
        if (isSplitItem) {
            return 0
        }
        def receiptItems = getReceiptItemsByStatus([ReceiptStatusCode.RECEIVED] as ReceiptStatusCode[])
        return receiptItems ? receiptItems?.sum { it?.quantityCanceled ?: 0 } : 0
    }

    Integer getQuantityRemaining() {
        Integer quantityCanceled = quantityCanceled ?: 0
        Integer quantityReceiving = quantityReceiving ?: 0
        Integer quantityReceived = quantityReceived ?: 0
        Integer quantityRemaining = (quantityShipped ?: 0) - (quantityReceiving + quantityReceived + quantityCanceled)
        return !cancelRemaining ? quantityRemaining : 0
    }

    Set<ReceiptItem> getReceiptItemsByStatus(ReceiptStatusCode[] receiptStatusCodes) {
        def receiptItems = ReceiptItem.findAllByShipmentItem(shipmentItem)
        return receiptItems.findAll { ReceiptItem receiptItem ->
            shipmentItem?.product == receiptItem?.product && receiptItem?.receipt?.receiptStatusCode in receiptStatusCodes
        }
    }

    InventoryItem getInventoryItem() {
        receiptItem ? receiptItem.inventoryItem : shipmentItem?.inventoryItem
    }

    Product getProduct() {
        return product ?: inventoryItem?.product
    }

    Map toJson() {
        return [

                "receiptItemId"       : receiptItem?.id,
                "shipmentItemId"      : shipmentItem?.id,
                "container.id"        : shipmentItem?.container?.id,
                "container.name"      : shipmentItem?.container?.name,
                "parentContainer.id"  : shipmentItem?.container?.parentContainer?.id,
                "parentContainer.name": shipmentItem?.container?.parentContainer?.name,
                "product.id"          : inventoryItem?.product?.id,
                "product.productCode" : inventoryItem?.product?.productCode,
                "product.name"        : inventoryItem?.product?.name,
                "lotNumber"           : lotNumber,
                "expirationDate"      : expirationDate?.format("MM/dd/yyyy"),
                "binLocation.id"      : binLocation?.id,
                "binLocation.name"    : binLocation?.name,
                "recipient.id"        : recipient?.id,
                "recipient.name"      : recipient?.name,
                quantityShipped       : quantityShipped,
                quantityReceived      : quantityReceived,
                quantityCanceled      : quantityCanceled,
                quantityReceiving     : quantityReceiving,
                quantityRemaining     : quantityRemaining,
                cancelRemaining       : cancelRemaining,
                comment               : comment
        ]
    }

}
