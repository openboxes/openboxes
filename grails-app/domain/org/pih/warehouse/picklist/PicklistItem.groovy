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
import org.pih.warehouse.requisition.RequisitionItem

class PicklistItem implements Serializable {

    String id
    RequisitionItem requisitionItem
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

    static belongsTo = [picklist: Picklist]

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        inventoryItem(nullable: true)
        binLocation(nullable: true)
        requisitionItem(nullable: true)
        quantity(nullable: false)
        status(nullable: true)
        reasonCode(nullable: true)
        comment(nullable: true)
        sortOrder(nullable: true)
    }

    Map toJson() {
        [
                id               : id,
                version          : version,
                status           : status,
                requisitionItemId: requisitionItem?.id,
                binLocationId    : binLocation?.id,
                inventoryItemId  : inventoryItem?.id,
                quantity         : quantity,
                reasonCode       : reasonCode,
                comment          : comment
        ]
    }
}
