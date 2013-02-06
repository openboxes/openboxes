/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.requisition

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.Inventory;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.picklist.PicklistItem;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.ProductGroup;


class RequisitionItem implements Serializable {
	
	String id
	String description	
	InventoryItem inventoryItem
    Product product
    Category category
    ProductGroup productGroup
	Integer quantity 
	Integer quantityCanceled
	String cancelReasonCode
	String cancelComments
	Float unitPrice	
	Person requestedBy	// the person who actually requested the item
	Boolean substitutable = false
    String recipient
    String comment
    Integer orderIndex

	
	// Audit fields
	Date dateCreated
	Date lastUpdated

	static transients = [ "type" ]
	
	static belongsTo = [ requisition: Requisition ]

	static mapping = {
		id generator: 'uuid'
        picklistItems cascade: "all-delete-orphan", sort: "id"
	}
		
    static constraints = {
    	description(nullable:true)
        category(nullable:true)
        product(nullable:false)
        productGroup(nullable:true)
        inventoryItem(nullable:true)
        requestedBy(nullable:true)
        quantity(nullable:false, min:1)
		quantityCanceled(nullable:true)
		cancelReasonCode(nullable:true)
		cancelComments(nullable:true)
        unitPrice(nullable:true)
        substitutable(nullable:false)
        comment(nullable:true)
        recipient(nullable:true)
        orderIndex(nullable: true)
	}

    def calculateQuantityPicked() {
        def quantityPicked = PicklistItem.findAllByRequisitionItem(this).sum{it.quantity}		
		return quantityPicked?:0
    }

	def calculateQuantityRemaining() {
		return quantity - (calculateQuantityPicked() + (quantityCanceled?:0))
	}
	
    def calculateNumInventoryItem(Inventory inventory) {
        InventoryItem.findAllByProduct(product).size()
    }

    def retrievePicklistItems() {
        return PicklistItem.findAllByRequisitionItem(this)
    }

    Map toJson(){
      [
        id: id,
        version: version,
        productId: product?.id,
        productName: product?.name,
		unitOfMeasure: product?.unitOfMeasure?:"EA",
        quantity:quantity,
        comment: comment,
        recipient: recipient,
        substitutable: substitutable,
        orderIndex: orderIndex
      ]
    }


}
