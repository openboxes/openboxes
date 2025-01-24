package org.pih.warehouse.api

import grails.util.Holders
import org.apache.commons.lang.math.NumberUtils
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.ShipmentItem

class StockMovementItem {

    String id
    String productCode
    Product product
    InventoryItem inventoryItem
    Location binLocation
    Person recipient

    StockMovement stockMovement
    RequisitionItem requisitionItem
    ProductSupplier productSupplier

    BigDecimal quantityRequested
    BigDecimal quantityOnHand
    BigDecimal quantityAvailable
    BigDecimal quantityRevised
    BigDecimal quantityCanceled
    BigDecimal quantityPicked
    BigDecimal quantityShipped

    String unitOfMeasure

    // saved QOH in ward request
    Integer quantityCounted

    String shipmentItemId
    String orderItemId
    String orderNumber
    String orderId

    List<StockMovementItem> splitLineItems = []
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

    BigDecimal packSize = 1

    BigDecimal getQuantityRequired() {
        return quantityRevised ?: quantityRequested
    }

    BigDecimal getQuantityAllowed() {
        def stocklistItem = requisitionItem?.requisition?.requisitionTemplate?.requisitionItems?.find {
            it?.product?.productCode == requisitionItem?.product?.productCode
        }
        return stocklistItem?.quantity ?: null
    }

    BigDecimal getPacksRequested () {
        if (packSize == null || packSize == 0 || quantityRequested == null) {
            return null
        }
        return quantityRequested?.toBigDecimal() / packSize.toBigDecimal()
    }


    static constraints = {
        id(nullable: true)
        productCode(nullable: true)
        product(nullable: true)
        inventoryItem(nullable: true)
        binLocation(nullable: true)
        quantityRequested(nullable: false)
        quantityAllowed(nullable: true)
        quantityOnHand(nullable: true)
        quantityAvailable(nullable: true)
        quantityRevised(nullable: true)
        quantityCanceled(nullable: true)
        quantityPicked(nullable: true)
        quantityCounted(nullable: true)
        statusCode(nullable: true)
        reasonCode(nullable: true)
        comments(nullable: true)
        recipient(nullable: true)
        delete(nullable: true)
        cancel(nullable: true)
        revert(nullable: true)
        substitute(nullable: true)
        lotNumber(nullable: true)
        expirationDate(nullable: true)
        palletName(nullable: true)
        boxName(nullable: true)
        sortOrder(nullable: true)
        substitutionItems(nullable: true)
    }

    String toString() {
        return "${id}:${product}:${statusCode}:${quantityRequested}:${quantityRevised}:${reasonCode}:${!substitutionItems?.empty}"
    }

    Map toJson() {
        return [
                id                        : id,
                productCode               : productCode,
                product                   : product,
                lotNumber                 : lotNumber,
                expirationDate            : expirationDate?.format("MM/dd/yyyy"),
                palletName                : palletName,
                boxName                   : boxName,
                statusCode                : statusCode,
                quantityRequested         : quantityRequested,
                quantityAllowed           : quantityAllowed,
                quantityOnHand            : quantityOnHand,
                quantityCounted           : quantityCounted,
                quantityAvailable         : quantityAvailable,
                quantityCanceled          : quantityCanceled,
                quantityRevised           : quantityRevised,
                quantityPicked            : quantityPicked,
                unitOfMeasure             : unitOfMeasure,
                packsRequested            : packsRequested,
                quantityRequired          : quantityRequired,
                reasonCode                : reasonCode,
                comments                  : comments,
                recipient                 : recipient,
                substitutionItems         : substitutionItems,
                sortOrder                 : sortOrder,
                orderItemId               : orderItemId,
                productSupplier           : productSupplier ? [
                        id            : productSupplier.id,
                        name          : productSupplier.name,
                        code          : productSupplier.code,
                ] : null,
                orderNumber               : orderNumber,
                orderId                   : orderId,
                packSize                  : packSize,
                inventoryItem             : !inventoryItem ? null : [
                        id            : inventoryItem.id,
                        lotNumber     : inventoryItem.lotNumber,
                        expirationDate: inventoryItem.expirationDate?.format("MM/dd/yyyy"),
                        quantity      : inventoryItem.quantity
                ]
        ]
    }


    static StockMovementItem createFromShipmentItem(ShipmentItem shipmentItem) {

        String palletName, boxName
        if (shipmentItem?.container?.parentContainer) {
            palletName = shipmentItem?.container?.parentContainer?.name
            boxName = shipmentItem?.container?.name
        } else if (shipmentItem.container) {
            palletName = shipmentItem?.container?.name
        }

        return new StockMovementItem(
                id: shipmentItem?.id,
                statusCode: null,
                productCode: shipmentItem?.product?.productCode,
                product: shipmentItem?.product,
                inventoryItem: shipmentItem?.inventoryItem,
                quantityRequested: shipmentItem?.quantity,
                recipient: shipmentItem.recipient,
                palletName: palletName,
                boxName: boxName,
                orderItemId: shipmentItem.orderItemId,
                productSupplier: shipmentItem.orderItem?.productSupplier,
                comments: null,
                lotNumber: shipmentItem?.inventoryItem?.lotNumber ?: "",
                expirationDate: shipmentItem?.inventoryItem?.expirationDate,
                sortOrder: shipmentItem?.sortOrder,
                orderNumber: shipmentItem?.orderNumber,
                orderId: shipmentItem?.orderId,
                quantityAvailable: shipmentItem.quantityRemainingToShip,
                unitOfMeasure: shipmentItem.unitOfMeasure,
                packSize: shipmentItem.quantityPerUom,
        )
    }


    static StockMovementItem createFromRequisitionItem(RequisitionItem requisitionItem) {

        List<StockMovementItem> substitutionItems = []

        if (requisitionItem.substitutionItems) {
            substitutionItems = requisitionItem?.substitutionItems ?
                    requisitionItem.substitutionItems.collect {
                        return StockMovementItem.createFromRequisitionItem(it)
                    } : []
        } else if (requisitionItem.substitutionItem) {
            substitutionItems.push(StockMovementItem.createFromRequisitionItem(requisitionItem.substitutionItem))
        }

        return new StockMovementItem(
                id: requisitionItem.id,
                statusCode: requisitionItem.status?.name(),
                productCode: requisitionItem?.product?.productCode,
                product: requisitionItem?.product,
                inventoryItem: requisitionItem?.inventoryItem,
                quantityRequested: requisitionItem.quantity,
                quantityCounted: requisitionItem.quantityCounted,
                quantityOnHand: null,
                quantityAvailable: null,
                quantityCanceled: requisitionItem?.quantityCanceled,
                quantityRevised: requisitionItem.calculateQuantityRevised(),
                quantityPicked: requisitionItem?.totalQuantityPicked(),
                substitutionItems: substitutionItems,
                reasonCode: requisitionItem.cancelReasonCode,
                comments: requisitionItem.comment,
                recipient: requisitionItem.recipient ?: requisitionItem?.parentRequisitionItem?.recipient,
                palletName: requisitionItem?.palletName ?: "",
                boxName: requisitionItem?.boxName ?: "",
                lotNumber: requisitionItem?.lotNumber ?: "",
                expirationDate: requisitionItem?.expirationDate,
                sortOrder: requisitionItem?.orderIndex,
                requisitionItem: requisitionItem
        )
    }


    static StockMovementItem createFromOrderItem(OrderItem orderItem) {
        return new StockMovementItem(
                id: orderItem?.id,
                statusCode: orderItem?.orderItemStatusCode,
                productCode: orderItem?.product?.productCode,
                product: orderItem?.product,
                inventoryItem: orderItem?.inventoryItem,
                quantityRequested: orderItem.quantityRemainingToShip * orderItem.quantityPerUom,
                recipient: orderItem.recipient,
                orderItemId: orderItem.id,
                productSupplier: orderItem.productSupplier,
                orderNumber: orderItem.order.orderNumber,
        )
    }


    static StockMovementItem createFromTokens(String[] tokens, Boolean validateLotAndExpiry) {
        String requisitionItemId = tokens[0] ?: null
        String productCode = tokens[1] ?: null
        String productName = tokens[2] ?: null
        String palletName = tokens[3] ?: null
        String boxName = tokens[4] ?: null
        String lotNumber = tokens[5] ?: null
        Date expirationDate = tokens[6] ? Constants.EXPIRATION_DATE_FORMATTER.parse(tokens[6]) : null
        Integer quantityRequested = tokens[7] ? tokens[7].toInteger() : null
        String recipientId = tokens[8]

        if (!productCode && !quantityRequested) {
            throw new IllegalArgumentException("Product code and quantity requested are required")
        }

        if (lotNumber?.contains("E") && NumberUtils.isNumber(lotNumber)) {
            throw new IllegalArgumentException("Lot numbers must not be specified in scientific notation. " +
                    "Please reformat field with Lot Number: \"${lotNumber}\" to a number format")
        }

        Date date = Constants.EXPIRATION_DATE_FORMATTER.parse(
                Holders.grailsApplication.config.openboxes.expirationDate.minValue
        )
        if (expirationDate && date > expirationDate) {
            throw new IllegalArgumentException("Expiration date for item ${productCode} is not valid. Please enter a date after ${date.getYear()+1900}.")
        }

        Person recipient = recipientId ? Person.get(recipientId) : null
        if (!recipient && recipientId) {
            String[] names = recipientId.split(" ")
            if (names.length != 2) {
                throw new IllegalArgumentException("Please enter recipient's first and last name only")
            }

            String firstName = names[0], lastName = names[1]
            recipient = Person.findByFirstNameAndLastName(firstName, lastName)
            if (!recipient) {
                throw new IllegalArgumentException("Unable to locate person with first name ${firstName} and last name ${lastName}")
            }
        }

        Product product = productCode ? Product.findByProductCode(productCode) : null
        if (!product) {
            throw new IllegalArgumentException("Product '${productCode} ${productName}' could not be found")
        }

        if (validateLotAndExpiry) {
            if (product.lotAndExpiryControl && (!expirationDate || !lotNumber)) {
                throw new IllegalArgumentException("Both lot number and expiry date are required for the '${productCode} ${productName}' product.")
            }
        }

        StockMovementItem stockMovementItem = new StockMovementItem()
        stockMovementItem.id = requisitionItemId

        if (quantityRequested == 0) {
            stockMovementItem.delete = true
        }

        stockMovementItem.product = product
        stockMovementItem.quantityRequested = quantityRequested
        stockMovementItem.palletName = palletName
        stockMovementItem.boxName = boxName
        stockMovementItem.lotNumber = lotNumber
        stockMovementItem.expirationDate = expirationDate
        stockMovementItem.recipient = recipient
        return stockMovementItem
    }
}

class AvailableItem {

    InventoryItem inventoryItem
    Location binLocation
    BigDecimal quantityAvailable
    BigDecimal quantityOnHand

    List<String> pickedRequisitionNumbers

    static constraints = {
        inventoryItem(nullable: true)
        binLocation(nullable: true)
        quantityAvailable(nullable: true)
    }

    String getBinLocationName() {
        binLocation?.name?:Constants.DEFAULT_BIN_LOCATION_NAME
    }

    AvailableItemStatus getStatus() {
        if (recalled) {
            return AvailableItemStatus.RECALLED
        }
        if (onHold) {
            return AvailableItemStatus.HOLD
        }
        if (available && quantityAvailable < quantityOnHand) {
            return AvailableItemStatus.PICKED
        }
        if (available) {
            return AvailableItemStatus.AVAILABLE
        }
        if (quantityOnHand <= 0) {
            return AvailableItemStatus.NOT_AVAILABLE
        }

        return AvailableItemStatus.PICKED
    }

    Boolean isPickable() {
        return (inventoryItem ? inventoryItem.pickable : true) && (binLocation ? binLocation.pickable : true)
    }

    Boolean isOnHold() {
        return binLocation?.onHold
    }

    Boolean isRecalled() {
        return inventoryItem?.recalled
    }

    Boolean getIsDefaultInventoryItem() {
        return inventoryItem?.isDefault
    }

    Boolean getIsDefaultLocation() {
        return binLocation == null
    }

    // TODO Need to test this thoroughly to make sure it works as expected
    Boolean getIsPhysicalLocation() {
        return !isVirtualLocation && !isOnHold()
    }

    /**
     * Virtual locations are the default or receiving locations.
     *
     * @return
     */
    Boolean getIsVirtualLocation() {
        return isDefaultLocation || isReceivingLocation
    }

    // TODO This may not actually be the correct logic, but it's all we have for now. The
    //  side effect of this is that it may return true in cases that we don't want it to.
    Boolean getIsReceivingLocation() {
        return binLocation?.supports(ActivityCode.RECEIVE_STOCK)
    }

    Boolean isAvailable() {
        return quantityAvailable > 0
    }

    Boolean isQuantityPickable(Integer quantity) {
        return available && pickable && quantity <= quantityAvailable
    }

    Map toJson() {
        return [
                "inventoryItem.id"      : inventoryItem?.id,
                "product.name"          : inventoryItem?.product?.name,
                "product"               : inventoryItem?.product,
                "productCode"           : inventoryItem?.product?.productCode,
                lotNumber               : inventoryItem?.lotNumber,
                expirationDate          : inventoryItem?.expirationDate?.format("MM/dd/yyyy"),
                binLocation             : binLocation,
                zone                    : binLocation?.zone,
                quantityAvailable       : quantityAvailable > 0 ? quantityAvailable : 0,
                quantityOnHand          : quantityOnHand,
                status                  : status?.name(),
                pickedRequisitionNumbers: pickedRequisitionNumbers ? pickedRequisitionNumbers?.join(",") : "",
                // deprecated
                "binLocation.id"        : binLocation?.id,
                "binLocation.name"      : binLocation?.name,
        ]
    }

}

enum AvailableItemStatus {
    AVAILABLE, PICKED, RECALLED, HOLD, NOT_AVAILABLE

    static list() {
        [AVAILABLE, PICKED, RECALLED, HOLD, NOT_AVAILABLE]
    }

    static listUnavailable() {
        [RECALLED, HOLD, NOT_AVAILABLE]
    }
}

class SuggestedItem extends AvailableItem {

    BigDecimal quantityRequested
    BigDecimal quantityPicked

    static constraints = {
        quantityRequested(nullable: true)
        quantityPicked(nullable: true)
    }

    Map toJson() {
        Map json = super.toJson()
        json << [quantityRequested: quantityRequested, quantityPicked: quantityPicked]
        return json
    }
}

class SubstitutionItem {

    String productId
    String productCode
    String productName
    Integer quantitySelected
    Integer sortOrder

    Product product

    List availableItems

    Date getMinExpirationDate() {
        return availableItems ?
                availableItems.findAll { it.inventoryItem.expirationDate != null }.collect {
                    it.inventoryItem?.expirationDate
                }.min() :
                null
    }

    Integer getQuantityAvailable() {
        availableItems ? availableItems.sum { it.quantityAvailable < 0 ? 0 : it.quantityAvailable } : 0
    }

    Integer getQuantityOnHand() {
        availableItems ? availableItems.sum { it.quantityOnHand } : 0
    }

    Map toJson() {
        return [
                productId        : productId,
                productCode      : productCode,
                productName      : productName,
                product          : product,
                minExpirationDate: minExpirationDate?.format("MM/dd/yyyy"),
                quantityAvailable: quantityAvailable,
                quantityOnHand   : quantityOnHand,
                quantitySelected : quantitySelected,
                quantityRequested: quantitySelected,
                availableItems   : availableItems
        ]
    }
}

enum SubstitutionStatusCode {

    EARLIER(0),
    YES(1),
    NO(2)

    int sortOrder

    SubstitutionStatusCode(int sortOrder) { [this.sortOrder = sortOrder] }

    static int compare(SubstitutionStatusCode a, SubstitutionStatusCode b) {
        return a.sortOrder <=> b.sortOrder
    }

    static list() {
        [EARLIER, YES, NO]
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
    Integer totalMonthlyQuantity
    Integer sortOrder

    List<AvailableItem> availableItems
    List<SubstitutionItem> availableSubstitutions
    List<SubstitutionItem> substitutionItems

    Integer getQuantityOnHand() {
        availableItems ? availableItems.sum { it.quantityOnHand } : null
    }

    Integer getQuantityAvailable() {
        availableItems ? availableItems.sum { it.quantityAvailable } : null
    }

    Integer getQuantityAfterCancellation() {
        return requisitionItem.quantityCanceled ? (requisitionItem.quantity - requisitionItem.quantityCanceled) : null
    }

    Integer getQuantityRevised() {
        requisitionItem?.modificationItem ?
                requisitionItem?.modificationItem?.quantity : requisitionItem.quantityCanceled ? quantityAfterCancellation : null
    }

    Date getMinExpirationDate() {
        return availableItems ?
                availableItems.findAll { it.inventoryItem.expirationDate != null }.collect {
                    it.inventoryItem?.expirationDate
                }.min() :
                null
    }

    Date getMinExpirationDateForSubstitutionItems() {
        return availableSubstitutions ? availableSubstitutions?.collect {
            it.minExpirationDate
        }?.min() : null
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
        } else {
            return (!availableSubstitutions?.empty && availableSubstitutions.sum {
                it.quantityAvailable
            } > 0) ? SubstitutionStatusCode.YES : SubstitutionStatusCode.NO
        }
    }

    Map toJson() {
        return [
            requisitionItemId           : requisitionItem.id,
            statusCode                  : requisitionItem.status.name(),
            reasonCode                  : requisitionItem?.cancelReasonCode,
            comments                    : requisitionItem?.comment,
            productId                   : productId,
            productCode                 : productCode,
            productName                 : productName,
            product                     : requisitionItem?.product,
            minExpirationDate           : minExpirationDate?.format("MM/dd/yyyy"),
            quantityRequested           : quantityRequested,
            quantityRevised             : quantityRevised,
            quantityAvailable           : quantityAvailable,
            quantityOnHand              : quantityOnHand,
            totalMonthlyQuantity        : totalMonthlyQuantity,
            substitutionStatus          : substitutionStatusCode,
            availableSubstitutions      : availableSubstitutions,
            substitutionItems           : substitutionItems,
            sortOrder                   : sortOrder
        ]
    }
}

class PickPageItem {

    RequisitionItem requisitionItem
    InventoryItem inventoryItem
    Location binLocation
    Integer sortOrder

    Set<PicklistItem> picklistItems = []
    List<AvailableItem> availableItems = []
    Set<SuggestedItem> suggestedItems = []


    Map toJson() {
        return [
                pickStatusCode      : statusCode,
                requestStatusCode   : requisitionItem?.status?.name(),
                "requisitionItem.id": requisitionItem?.id,
                "product.name"      : requisitionItem?.product?.name,
                productCode         : requisitionItem?.product?.productCode,
                productId           : requisitionItem?.product?.id,
                product             : requisitionItem?.product,
                reasonCode          : requisitionItem?.cancelReasonCode,
                autoAllocated       : requisitionItem?.autoAllocated,
                comments            : requisitionItem?.cancelComments,
                quantityRequested   : requisitionItem.quantity,
                quantityRequired    : quantityRequired,
                quantityPicked      : quantityPicked,
                quantityAvailable   : quantityAvailable,
                quantityRemaining   : quantityRemaining,
                hasAdjustedInventory: hasAdjustedInventory,
                hasChangedPick      : hasChangedPick,
                availableItems      : availableItems,
                suggestedItems      : suggestedItems,
                picklistItems       : picklistItems,
                recipient           : requisitionItem?.recipient,
                sortOrder           : sortOrder,
        ]
    }

    Boolean getHasChangedPick() {
        return Boolean.FALSE
    }

    Boolean getHasAdjustedInventory() {
        return Boolean.FALSE
    }


    Integer getQuantityRemaining() {
        Integer quantityRemaining = quantityRequired - quantityPicked
        return quantityRemaining > 0 ? quantityRemaining : 0
    }

    Integer getQuantityRequired() {
        return requisitionItem?.calculateQuantityRequired() ?: 0
    }

    Integer getQuantityRequested() {
        requisitionItem?.quantity ?: 0
    }

    Integer getQuantityPicked() {
        return picklistItems ? picklistItems?.sum { it.quantity } : 0
    }

    // TODO Document the reason why the default value is null instead of 0? See OBPIH-912.
    Integer getQuantityAvailable() {
        return availableItems ? availableItems?.sum { it.quantityAvailable } : null
    }

    /**
     * @deprecated Not the correct way to find available items.
     * @param binLocationName
     * @param lotNumber
     * @return
     */
    // TODO Remove this method if we decide that it's no longer required. There's logic here
    //  that I don't fully understand so I want to make sure the alternate solution takes what I
    //  assume is the "default" bin and lot number logic into account.
    //  i.e. (!item.binLocation and !item.inventoryItem?.lotNumber)
//    AvailableItem getAvailableItem(String binLocationName, String lotNumber) {
//        return availableItems?.find { item ->
//            Boolean binLocationMatches = binLocationName ? item.binLocation?.name == binLocationName : !item.binLocation
//            Boolean lotMatches = lotNumber ? item.inventoryItem?.lotNumber == lotNumber : !item.inventoryItem?.lotNumber
//            binLocationMatches && lotMatches
//        }
//    }

    /**
     * Get all available items for the given inventory item.
     * @param inventoryItem
     * @return
     */
    List<AvailableItem> getAvailableItems(InventoryItem inventoryItem) {
        return availableItems.findAll { availableItem ->
            availableItem.inventoryItem == inventoryItem
        }
    }

    List<AvailableItem> getAvailableItemsInDefaultLocation(InventoryItem inventoryItem) {
        return availableItems.findAll { availableItem ->
            availableItem.inventoryItem == inventoryItem && availableItem.binLocation == null
        }
    }

    /**
     * Get all available items for the given inventory item and internal location.
     * @param inventoryItem
     * @param internalLocation
     * @return
     */
    AvailableItem getAvailableItem(InventoryItem inventoryItem, Location internalLocation) {
        // Return only exact matches on inventory item and internal location
        return availableItems?.find { availableItem ->
            availableItem.inventoryItem == inventoryItem &&
                    availableItem.binLocation == internalLocation
        }
    }

    String getStatusCode() {

        if (quantityRequired == quantityPicked && quantityRemaining == 0) {
            return "PICKED"
        } else if (quantityPicked > 0 && quantityRemaining > 0) {
            return "PARTIALLY_PICKED"
        } else {
            return "NOT_PICKED"
        }
    }

    static PickPageItem createFromRequisitionItem(RequisitionItem requisitionItem) {
        return new PickPageItem(requisitionItem: requisitionItem)
    }

}

class PackPageItem {
    ShipmentItem shipmentItem
    String palletName
    String boxName
    Integer sortOrder

    String shipmentItemId
    Person recipient
    Integer quantityShipped
    List<PackPageItem> splitLineItems

    Map toJson() {
        return [
                shipmentItemId : shipmentItem?.id,
                "product.id"   : shipmentItem?.product?.id,
                productName    : shipmentItem?.product?.name,
                productCode    : shipmentItem?.product?.productCode,
                product        : shipmentItem?.product,
                lotNumber      : shipmentItem?.lotNumber,
                expirationDate : shipmentItem?.expirationDate?.format("MM/dd/yyyy"),
                binLocation    : shipmentItem?.binLocation,
                uom            : shipmentItem?.product?.unitOfMeasure,
                quantityShipped: shipmentItem?.quantity,
                recipient      : shipmentItem?.recipient,
                palletName     : palletName,
                boxName        : boxName,
                sortOrder      : sortOrder,
                recalled       : shipmentItem?.hasRecalledLot,
                // deprecated
                binLocationName: shipmentItem?.binLocation?.name,
        ]
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        PackPageItem that = (PackPageItem) o

        if (shipmentItem?.id != that.shipmentItem?.id) return false

        return true
    }

    int hashCode() {
        return (shipmentItem?.id != null ? shipmentItem?.id?.hashCode() : 0)
    }
}
