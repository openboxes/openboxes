package org.pih.warehouse.api

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionItem
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

    List<StockMovementItem> substitutionItems = []

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

    String lotNumber
    Date expirationDate
    String palletName
    String boxName

    Integer sortOrder = 0

    BigDecimal getQuantityRequired() {
        return quantityRevised?:quantityRequested
    }


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
        lotNumber(nullable:true)
        expirationDate(nullable:true)
        palletName(nullable:true)
        boxName(nullable:true)
        sortOrder(nullable:true)
    }

    String toString() {
        return "${id}:${productCode}:${statusCode}:${quantityRequested}:${quantityRevised}:${reasonCode}:${!substitutionItems?.empty}"
    }

    Map toJson() {

        return [
                id: id,
                productCode: productCode,
                product: product,
                lotNumber: lotNumber,
                expirationDate: expirationDate,
                palletName: palletName,
                boxName: boxName,
                statusCode: statusCode,
                quantityRequested: quantityRequested,
                quantityAllowed: quantityAllowed,
                quantityAvailable: quantityAvailable,
                quantityCanceled: quantityCanceled,
                quantityRevised: quantityRevised,
                quantityPicked: quantityPicked,
                quantityRequired: quantityRequired,
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

        List<StockMovementItem> substitutionItems = requisitionItem?.substitutionItems ?
                requisitionItem.substitutionItems.collect {
            return StockMovementItem.createFromRequisitionItem(it)
        } : []

        return new StockMovementItem(id: requisitionItem.id,
                statusCode: requisitionItem.status?.name(),
                productCode: requisitionItem?.product?.productCode,
                product: requisitionItem?.product,
                inventoryItem: requisitionItem?.inventoryItem,
                quantityRequested: requisitionItem.quantity,
                quantityAllowed: null,
                quantityAvailable: null,
                quantityCanceled: requisitionItem?.quantityCanceled,
                quantityRevised: requisitionItem?.modificationItem?.quantity,
                quantityPicked: requisitionItem?.totalQuantityPicked(),
                substitutionItems: substitutionItems,
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

class SubstitutionItem {

    String productId
    String productCode
    String productName
    //Date minExpirationDate
    //Integer quantityAvailable
    Integer quantitySelected

    List availableItems

    Date getMinExpirationDate() {
        return availableItems ?
                availableItems.findAll { it.inventoryItem.expirationDate != null }.collect {
                    it.inventoryItem?.expirationDate }.min() :
                null
    }

    Integer getQuantityAvailable() {
        availableItems ? availableItems.sum { it.quantityAvailable } : 0
    }

    Map toJson() {
        return [
                productId       : productId,
                productCode      : productCode,
                productName      : productName,
                minExpirationDate: minExpirationDate?.format("MM/dd/yyyy"),
                quantityAvailable: quantityAvailable,
                quantitySelected : quantitySelected
        ]
    }


    static SubstitutionItem createFromRequisitionItem(RequisitionItem requisitionItem) {
        SubstitutionItem substitutionItem = new SubstitutionItem()
        substitutionItem.productId = requisitionItem?.product?.id
        substitutionItem.productName = requisitionItem?.product?.name
        substitutionItem.productCode = requisitionItem?.product?.productCode
        substitutionItem.quantitySelected = requisitionItem?.quantity
        return substitutionItem
    }

}

enum SubstitutionStatusCode {

    EARLIER(0),
    YES(1),
    NO(2)

    int sortOrder

    SubstitutionStatusCode(int sortOrder) { [ this.sortOrder = sortOrder ] }

    static int compare(SubstitutionStatusCode a, SubstitutionStatusCode b) {
        return a.sortOrder <=> b.sortOrder
    }

    static list() {
        [ EARLIER, YES, NO ]
    }

    String getName() { return name() }

    String toString() { return name() }


}

class EditPageItem {

    String productId
    String productCode
    String productName

    RequisitionItem requisitionItem

    Integer quantityRequested
    //Integer quantityAvailable
    Integer quantityConsumed

    List<AvailableItem> availableItems
    List<SubstitutionItem> availableSubstitutions

    Integer getQuantityAvailable() {
        availableItems ? availableItems.sum { it.quantityAvailable } : 0
    }

    Integer getQuantityRevised() {
        requisitionItem?.modificationItem ? requisitionItem?.modificationItem?.quantity : null
    }

    Date getMinExpirationDate() {
        return availableItems ?
                availableItems.findAll { it.inventoryItem.expirationDate != null }.collect {
                    it.inventoryItem?.expirationDate }.min() :
                null
    }

    Date getMinExpirationDateForSubstitutionItems() {
        return availableSubstitutions ? availableSubstitutions?.collect { it.minExpirationDate }?.min() : null
    }

    List<SubstitutionItem> getSubstitutionItems() {
        return requisitionItem?.substitutionItems ?
                requisitionItem?.substitutionItems?.collect { RequisitionItem requsitionItem ->
                    SubstitutionItem.createFromRequisitionItem(requsitionItem) } :
                null
    }

    Boolean hasEarlierExpirationDate() {
        Date productExpirationDate = getMinExpirationDate()
        Date substitutionExpirationDate = getMinExpirationDateForSubstitutionItems()
        return productExpirationDate && substitutionExpirationDate ?
                productExpirationDate.after(substitutionExpirationDate) :
                false
    }

    String getSubstitutionStatusCode() {
        if (hasEarlierExpirationDate()) {
            return SubstitutionStatusCode.EARLIER
        }
        else {
            return (!availableSubstitutions?.empty) ? SubstitutionStatusCode.YES : SubstitutionStatusCode.NO
        }
    }

    Map toJson() {
        return [
                requisitionItemId: requisitionItem.id,
                statusCode       : requisitionItem.status.name(),
                reasonCode       : requisitionItem?.cancelReasonCode,
                comments         : requisitionItem?.cancelComments,
                productId        : productId,
                productCode      : productCode,
                productName      : productName,
                minExpirationDate: minExpirationDate?.format("MM/dd/yyyy"),
                quantityRequested: quantityRequested,
                quantityRevised  : quantityRevised,
                quantityConsumed : quantityConsumed,
                quantityAvailable: quantityAvailable,
                substitutionStatus    : substitutionStatusCode,
                availableSubstitutions: availableSubstitutions,
                substitutionItems     : substitutionItems
        ]
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
                reasonCode          : requisitionItem?.cancelReasonCode,
                comments            : requisitionItem?.cancelComments,
                quantityRequested   : requisitionItem.quantity,
                quantityRequired    : quantityRequired,
                quantityPicked      : quantityPicked,
                quantityAvailable   : quantityAvailable,
                quantityRemaining   : quantityRemaining,
                availableItems      : availableItems,
                suggestedItems      : suggestedItems,
                picklistItems       : picklistItems,
        ]
    }


    Integer getQuantityRemaining() {
        Integer quantityRemaining = quantityRequired - quantityPicked
        return quantityRemaining > 0 ? quantityRemaining : 0
    }

    // FIXME Don't love this logic in multiple places (see StockMovementItem). Refactor method to use
    // StockMovementItem instead of RequisitionItem
    Integer getQuantityRequired() {
        return requisitionItem?.modificationItem?.quantity?:requisitionItem?.quantity?:0
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

        if (quantityRequired == quantityPicked && quantityRemaining == 0) {
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