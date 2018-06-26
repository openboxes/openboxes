package org.pih.warehouse.api

import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.ShipmentItem

class StockMovementItem {

    String id
    String productCode
    Product product
    InventoryItem inventoryItem
    BigDecimal quantityRequested
    BigDecimal quantityAllowed
    BigDecimal quantityAvailable
    Person recipient
    Boolean deleted = Boolean.FALSE


    String palletName
    String boxName

    Integer sortOrder = 0


    static constraints = {
        id(nullable:true)
        productCode(nullable:true)
        product(nullable:true)
        inventoryItem(nullable:true)
        quantityRequested(nullable:false)
        quantityAllowed(nullable:true)
        quantityAvailable(nullable:true)
        recipient(nullable:true)
        deleted(nullable:true)
        palletName(nullable:true)
        boxName(nullable:true)
        sortOrder(nullable:true)
    }

    Map toJson() {
        return [
                id: id,
                productCode: productCode,
                product: product,
                palletName: palletName,
                boxName: boxName,
                quantityRequested: quantityRequested,
                quantityAllowed: quantityAllowed,
                quantityAvailable: quantityAvailable,
                recipient: recipient,
                sortOrder: sortOrder
        ]
    }

    static StockMovementItem createFromShipmentItem(ShipmentItem shipmentItem) {

        String palletName, boxName
        if(shipmentItem?.container?.parentContainer) {
            palletName = shipmentItem?.container?.parentContainer?.name
            boxName = shipmentItem?.container?.name
        } else if (shipmentItem.container) {
            palletName = shipmentItem?.container?.name
        }

        return new StockMovementItem(id: shipmentItem?.id,
                productCode: shipmentItem?.product?.productCode,
                product: shipmentItem?.inventoryItem?.product,
                inventoryItem: shipmentItem?.inventoryItem,
                quantityRequested: shipmentItem?.quantity,
                quantityAllowed: null,
                quantityAvailable: null,
                palletName:palletName,
                boxName:boxName,
                recipient: shipmentItem.recipient,
                sortOrder: null

        )
    }

    static StockMovementItem createFromRequisitionItem(RequisitionItem requisitionItem) {
        return new StockMovementItem(id: requisitionItem.id,
                productCode: requisitionItem?.product?.productCode,
                product: requisitionItem?.product,
                inventoryItem: requisitionItem?.inventoryItem,
                quantityRequested: requisitionItem.quantity,
                quantityAllowed: null,
                quantityAvailable: null,
                palletName:null,
                boxName:null,
                recipient: requisitionItem.recipient,
                sortOrder: requisitionItem.orderIndex

        )
    }

}