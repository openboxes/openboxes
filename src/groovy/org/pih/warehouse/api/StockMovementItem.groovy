package org.pih.warehouse.api

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemType
import org.pih.warehouse.shipping.ShipmentItem

class StockMovementItem {

    String id
    String productCode
    Product product
    InventoryItem inventoryItem
    Location binLocation
    Person recipient

    BigDecimal quantityRequested
    BigDecimal quantityAllowed
    BigDecimal quantityAvailable
    BigDecimal quantityRevised
    BigDecimal quantityCanceled
    BigDecimal quantityPicked

    List substitutionItems = []

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
        binLocation(nullable:true)
        quantityRequested(nullable:false)
        quantityAllowed(nullable:true)
        quantityAvailable(nullable:true)
        quantityRevised(nullable:true)
        quantityCanceled(nullable:true)
        quantityPicked(nullable:true)
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
                quantityPicked: quantityPicked,
                reasonCode: reasonCode,
                comments: comments,
                recipient: recipient,
                substitutionItems: substitutionItems,
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
                quantityPicked: requisitionItem?.totalQuantityPicked(),
                reasonCode: requisitionItem.cancelReasonCode,
                comments: requisitionItem.cancelComments,
                recipient: requisitionItem.recipient,
                palletName:null,
                boxName:null,
                sortOrder: requisitionItem.orderIndex

        )
    }

}

class AvailableItem {

    InventoryItem inventoryItem
    Location binLocation
    BigDecimal quantityAvailable

    static constraints = {
        inventoryItem(nullable:true)
        binLocation(nullable:true)
        quantityAvailable(nullable:true)
    }

    Map toJson() {
        return [
                "inventoryItem.id": inventoryItem?.id,
                "product.name"    : inventoryItem?.product?.name,
                "productCode"     : inventoryItem?.product?.productCode,
                lotNumber         : inventoryItem?.lotNumber,
                expirationDate    : inventoryItem?.expirationDate,
                "binLocation.id"  : binLocation?.id,
                "binLocation.name": binLocation?.name,
                quantityAvailable : quantityAvailable,
        ]
    }

}


class SuggestedItem extends AvailableItem {

    BigDecimal quantityRequested
    BigDecimal quantityPicked

    static constraints = {
        quantityRequested(nullable:true)
        quantityPicked(nullable:true)
    }

    Map toJson() {
        Map json = super.toJson()
        json << [quantityRequested: quantityRequested, quantityPicked:quantityPicked]
        return json
    }
}


class PickPageItem {

    RequisitionItem requisitionItem
    InventoryItem inventoryItem
    Location binLocation

    Set<PicklistItem> picklistItems = []
    Set<AvailableItem> availableItems = []
    Set<SuggestedItem> suggestedItems = []


    Map toJson() {
        return [
                pickStatusCode      : statusCode,
                requestStatusCode   : requisitionItem?.status?.name(),
                "requisitionItem.id": requisitionItem?.id,
                "product.name"      : requisitionItem?.product?.name,
                productCode         : requisitionItem?.product?.productCode,
                quantityRequested   : requisitionItem.quantity,
                quantityPicked      : quantityPicked,
                quantityAvailable   : quantityAvailable,
                quantityRemaining   : requisitionItem.totalQuantityRemaining(),
                picklistItems       : picklistItems,
                availableItems      : availableItems,
                suggestedItems      : suggestedItems,

        ]
    }


    Integer getQuantityRemaining() {
        Integer quantityRemaining = quantityRequested - quantityPicked
        return quantityRemaining > 0 ? quantityRemaining : 0
    }

    Integer getQuantityRequested() {
        requisitionItem?.quantity?:0
    }

    Integer getQuantityPicked() {
        return picklistItems ? picklistItems?.sum { it.quantity } : 0
    }

    Integer getQuantityAvailable() {
        return availableItems ? availableItems?.sum { it.quantityAvailable } : 0
    }


    String getStatusCode() {
        if (quantityRequested == quantityPicked && quantityRemaining == 0) {
            return "PICKED"
        }
        else if (quantityPicked > 0 && quantityRemaining > 0) {
            return "PARTIALLY_PICKED"
        }
        else {
            return "NOT_PICKED"
        }
    }

    static PickPageItem createFromRequisitionItem(RequisitionItem requisitionItem) {
        return new PickPageItem(requisitionItem: requisitionItem)
    }

}