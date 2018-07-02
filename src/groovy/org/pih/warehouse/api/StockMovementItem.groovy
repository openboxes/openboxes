package org.pih.warehouse.api

import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemType
import org.pih.warehouse.shipping.ShipmentItem

class StockMovementItem {

    String id
    String productCode
    Product product
    InventoryItem inventoryItem

    BigDecimal quantityRequested
    BigDecimal quantityAllowed
    BigDecimal quantityAvailable
    BigDecimal quantityRevised
    BigDecimal quantityCanceled
    Person recipient

    // Actions
    Boolean cancel = Boolean.FALSE
    Boolean delete = Boolean.FALSE
    Boolean revert = Boolean.FALSE
    Boolean substitute = Boolean.FALSE

    Product newProduct
    BigDecimal newQuantity

    String statusCode

    String reasonCode
    String comments

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
        quantityRevised(nullable:true)
        quantityCanceled(nullable:true)
        statusCode(nullable:true)
        reasonCode(nullable:true)
        comments(nullable:true)
        recipient(nullable:true)
        delete(nullable:true)
        cancel(nullable:true)
        revert(nullable:true)
        substitute(nullable:true)
        palletName(nullable:true)
        boxName(nullable:true)
        sortOrder(nullable:true)
    }

    Map toJson() {

        // Gather all substitutions
        RequisitionItem requisitionItem = RequisitionItem.load(id)
        def substitutions = requisitionItem.requisitionItems.findAll {
            it.requisitionItemType = RequisitionItemType.SUBSTITUTION
        }
        substitutions = substitutions.collect { substitutionItem ->
            StockMovementItem.createFromRequisitionItem(substitutionItem)
        }
        return [
                id: id,
                productCode: productCode,
                product: product,
                palletName: palletName,
                boxName: boxName,
                statusCode: statusCode,
                quantityRequested: quantityRequested,
                quantityAllowed: quantityAllowed,
                quantityAvailable: quantityAvailable,
                quantityCanceled: quantityCanceled,
                quantityRevised: quantityRevised,
                substitutions: substitutions,
                reasonCode: reasonCode,
                comments: comments,
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
                statusCode: null,
                quantityRequested: shipmentItem?.quantity,
                quantityAllowed: null,
                quantityAvailable: null,
                quantityCanceled: null,
                quantityRevised: null,
                reasonCode: null,
                comments: null,
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
                statusCode: requisitionItem.status?.name(),
                quantityRequested: requisitionItem.quantity,
                quantityAllowed: null,
                quantityAvailable: null,
                quantityCanceled: requisitionItem?.quantityCanceled,
                quantityRevised: requisitionItem?.modificationItem?.quantity,
                reasonCode: requisitionItem.cancelReasonCode,
                comments: requisitionItem.cancelComments,
                recipient: requisitionItem.recipient,
                palletName:null,
                boxName:null,
                sortOrder: requisitionItem.orderIndex

        )
    }

}