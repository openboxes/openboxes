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

import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.requisition.RequisitionItem

class PicklistItem implements Serializable {
	
	String id	
	RequisitionItem requisitionItem
	InventoryItem inventoryItem
	Integer quantity
	
	String status
	String reasonCode
	String comment
	
	// Audit fields
	Date dateCreated
	Date lastUpdated

	
	static belongsTo = [ picklist : Picklist ]

	static mapping = {
		id generator: 'uuid'
	}
		
    static constraints = {
		id(bindable:true)
		inventoryItem(nullable:true)
        requisitionItem(nullable:true)
		quantity(nullable:false)
		status(nullable:true)
		reasonCode(nullable:true)
		comment(nullable:true)
		
	}

    Map toJson(){
        [
            id: id,
            requisitionItemId: requisitionItem?.id,
            inventoryItemId: inventoryItem?.id,
            version: version,
            quantity:quantity
        ]
    }
		
}
