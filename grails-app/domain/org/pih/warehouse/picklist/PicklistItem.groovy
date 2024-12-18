/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.picklist

import grails.util.Holders
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.RefreshProductAvailabilityEvent
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.requisition.RequisitionItem

class PicklistItem implements Serializable {

    def publishRefreshEvent() {
        Holders.grailsApplication.mainContext.publishEvent(new RefreshProductAvailabilityEvent(this))
    }

    def afterInsert() {
        publishRefreshEvent()
    }

    def afterUpdate() {
        publishRefreshEvent()
    }

    def afterDelete() {
        publishRefreshEvent()
    }

    String id
    RequisitionItem requisitionItem
    OrderItem orderItem
    InventoryItem inventoryItem
    Location binLocation

    Integer quantityPicked
    Person pickedBy
    Date datePicked

    Integer quantity

    String status
    String reasonCode
    String comment

    // Audit fields
    Date dateCreated
    Date lastUpdated

    Integer sortOrder = 0

    Boolean disableRefresh = Boolean.FALSE

    static belongsTo = [picklist: Picklist]

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        inventoryItem(nullable: true)
        binLocation(nullable: true)
        requisitionItem(nullable: true)
        orderItem(nullable: true)
        quantityPicked(nullable: true)
        pickedBy(nullable: true)
        datePicked(nullable: true)
        quantity(nullable: false)
        status(nullable: true)
        reasonCode(nullable: true)
        comment(nullable: true)
        sortOrder(nullable: true)
    }

    static transients = ['associatedLocation', 'associatedProducts', 'disableRefresh', 'pickable']

    String getAssociatedLocation() {
        return binLocation?.parentLocation?.id ?:
                requisitionItem?.requisition?.origin?.id ?:
                        orderItem?.order?.origin?.id
    }

    List getAssociatedProducts() {
        return [inventoryItem?.product?.id]
    }

    Boolean isPickable() {
        return (inventoryItem ? inventoryItem.pickable : true) && (binLocation ? binLocation.pickable : true)
    }

    Boolean isShortage() {
        return (reasonCode != null)
    }

    Integer getQuantityRemaining() {
        if (shortage) {
            return 0
        }
        else {
            Integer quantityRemaining = (quantity?:0) - (quantityPicked?:0)
            return (quantityRemaining > 0) ? quantityRemaining : 0
        }
    }

    Integer getQuantityCanceled() {
        if (shortage) {
            Integer quantityRemaining = (quantity?:0) - (quantityPicked?:0)
            return (quantityRemaining > 0) ? quantityRemaining : 0
        }
        else {
            return 0
        }
    }

    Integer getIndex() {
        def index = picklist?.picklistItems?.findIndexOf { PicklistItem picklistItem -> picklistItem?.id == id }
        return index >= 0 ? index + 1 : 1
    }

    Integer getTotalCount() {
        return picklist?.picklistItems?.size() ?: 0
    }

    String getIndexString() {
        return "${index} / ${totalCount}"
    }

    String getReasonCodeMessage() {
        def g = Holders.grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')
        return reasonCode ? (g.message(code: 'enum.ReasonCode.' + reasonCode)?:reasonCode) : null
    }

    String getStatusMessage() {
        return shortage ? "SHORTED" : quantityRemaining == 0 ? "COMPLETED" : quantityPicked == 0 ? "READY" : quantityRemaining > 0 ? "IN PROGRESS" : ""
    }


    ProductPackage getPickUnitOfMeasure() {
        return requisitionItem?.product?.getProductPackage(quantity)
    }

    // FIXME Consider moving this mapping to configuration so we don't have to hard-code the UOM code here
    static PickTypeClassification getPickTypeClassification(String uomCode) {
        switch(uomCode) {
            case Constants.UOM_CODE_PALLET:
                return PickTypeClassification.FULL_PALLET
            case Constants.UOM_CODE_CASE:
                return PickTypeClassification.CASE_PICK
            case Constants.UOM_CODE_EACH:
            default:
                return PickTypeClassification.PIECE_PICK
        }
    }

    Map toJson() {
        [
            id                  : id,
            version             : version,
            status              : status,
            statusMessage       : statusMessage,
            requisitionItemId   : requisitionItem?.id,
            orderItemId         : orderItem?.id,
            binLocationId       : binLocation?.id,
            inventoryItemId     : inventoryItem?.id,
            quantity            : quantity,
            quantityPicked      : quantityPicked ?: 0,
            pickedBy            : pickedBy?.id,
            datePicked          : datePicked,
            reasonCode          : reasonCode,
            reasonCodeMessage   : reasonCodeMessage,
            comment             : comment,

            // Used in Bin Replenishment feature
            binLocation                 : binLocation?.toJson(LocationTypeCode.INTERNAL),
            zone                        : binLocation?.zone?.toJson(LocationTypeCode.ZONE),
            product                     : inventoryItem?.product,
            inventoryItem               : inventoryItem,
            lotNumber                   : inventoryItem?.lotNumber,
            expirationDate              : inventoryItem?.expirationDate?.format("MM/dd/yyyy"),

            // Used in React Native app
            "picklist.id"               : picklist?.id,
            "requisitionItem.id"        : requisitionItem?.id,
            "product.id"                : inventoryItem?.product?.id,
            "product.name"              : inventoryItem?.product?.name,
            "productCode"               : inventoryItem?.product?.productCode,
            "inventoryItem.id"          : inventoryItem?.id,
            "binLocation.id"            : binLocation?.id,
            "binLocation.name"          : binLocation?.name,
            "binLocation.locationNumber": binLocation?.locationNumber,
            "binLocation.locationType"  : binLocation?.locationType?.name,
            "binLocation.zoneId"        : binLocation?.zone?.id,
            "binLocation.zoneName"      : binLocation?.zone?.name,
            quantityRequested           : requisitionItem?.quantity ?: 0,
            quantityRemaining           : quantityRemaining ?: 0,
            quantityToPick              : quantity ?: 0,
            quantityCanceled            : quantityCanceled ?: 0,
            unitOfMeasure               : requisitionItem?.product?.unitOfMeasure ?: "EA",
            pickUnitOfMeasure           : pickUnitOfMeasure,
            pickTypeClassification      : PicklistItem.getPickTypeClassification(pickUnitOfMeasure?.uom?.code)?.name(),
            shortage                    : shortage,
            picker                      : pickedBy,
            index                       : index,
            totalCount                  : totalCount,
            indexString                 : indexString,
            pickerName                  : pickedBy?.name,
        ]
    }
}

public enum PickTypeClassification {
    FULL_PALLET(0),
    CASE_PICK(1),
    PIECE_PICK(2)

    int sortOrder

    PickTypeClassification(int sortOrder) { [this.sortOrder = sortOrder] }

    static int compare(PickTypeClassification a, PickTypeClassification b) {
        return a.sortOrder <=> b.sortOrder
    }

    static list() {
        [FULL_PALLET, CASE_PICK, PIECE_PICK]
    }

    String getName() { return name() }

    String toString() { return name() }
}
