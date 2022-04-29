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

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.RefreshProductAvailabilityEvent
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.requisition.RequisitionItem

class PicklistItem implements Serializable {

    def publishRefreshEvent = {
        publishEvent(new RefreshProductAvailabilityEvent(this))
    }

    def afterInsert = publishRefreshEvent

    def afterUpdate = publishRefreshEvent

    def afterDelete = publishRefreshEvent

    String id
    RequisitionItem requisitionItem
    OrderItem orderItem
    InventoryItem inventoryItem
    Location binLocation

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
        quantity(nullable: false)
        status(nullable: true)
        reasonCode(nullable: true)
        comment(nullable: true)
        sortOrder(nullable: true)
    }

    static transients = ['associatedLocation', 'associatedProducts', 'disableRefresh', 'pickable']

    String getAssociatedLocation() {
        return requisitionItem ? requisitionItem?.requisition?.origin?.id : orderItem?.order?.origin?.id
    }

    List getAssociatedProducts() {
        return [inventoryItem?.product?.id]
    }

    Boolean isPickable() {
        return (inventoryItem ? inventoryItem.pickable : true) && (binLocation ? binLocation.pickable : true)
    }

    Map toJson() {
        [
            id                  : id,
            version             : version,
            status              : status,
            requisitionItemId   : requisitionItem?.id,
            orderItemId         : orderItem?.id,
            binLocationId       : binLocation?.id,
            inventoryItemId     : inventoryItem?.id,
            quantity            : quantity,
            reasonCode          : reasonCode,
            comment             : comment,
            // Used in Bin Replenishment feature
            "binLocation.id"    : binLocation?.id,
            "binLocation.name"  : binLocation?.name,
            "zone.id"           : binLocation?.zone?.id,
            "zone.name"         : binLocation?.zone?.name,
            product             : inventoryItem?.product,
            inventoryItem       : inventoryItem,
            lotNumber           : inventoryItem?.lotNumber,
            expirationDate      : inventoryItem?.expirationDate?.format("MM/dd/yyyy")
        ]
    }
}
