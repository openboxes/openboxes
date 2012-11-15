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

import org.pih.warehouse.core.Person;


import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.picklist.PicklistItem;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.ProductGroup;


class RequisitionItem implements Serializable {
	
	String id
	String description	
	Category category
	Product product
	ProductGroup productGroup
	InventoryItem inventoryItem
	Integer quantity = 1
	Float unitPrice	
	Person requestedBy	// the person who actually requested the item
	Boolean substitutable = false
    String recipient
    String comment
    Integer orderIndex

    List picklistItems = []
	
	// Audit fields
	Date dateCreated
	Date lastUpdated

	static transients = [ "type", "status", "quantityPicked", "quantityRemaining" ]
	
	static belongsTo = [ requisition: Requisition ]

    static hasMany = [ picklistItems: PicklistItem ]

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
		unitPrice(nullable:true)
        substitutable(nullable:false)
        comment(nullable:true)
        recipient(nullable:true)
        orderIndex(nullable: true)
	}

	String getStatus() { 
		def quantityPicked = getQuantityPicked()
		if (quantityPicked >= quantity) return "Complete"
        if (quantityPicked > 0) return "PartiallyComplete"
        return "Incomplete"
	}

    Integer getQuantityPicked() {
        return picklistItems.sum(0) { it.quantity }
    }

    Integer getQuantityRemaining() {
        return (quantity ?: 0) - quantityPicked
    }

    List<InventoryItem> findExistingInventoryItems() {
        return []//InventoryItem.findAll { it.product == product }
    }

//
//	List getPicklistItems() {
//		return PicklistItem.findAllByRequestItem(this)
//	}
//
	String getType() { 
		return (product)?"Product":(productGroup)?"ProductGroup":(category)?"Category":""
	}
	
	String displayName() {
		if (product) {
			return product.name;
		}
		else if (productGroup) { 
			return productGroup.description
		}
		else if (category) {
			return category.name
		}
		else {
			return description;
		}
	}

	Integer quantityFulfilled() { 
		def fulfillmentItems = requisition?.fulfillment?.fulfillmentItems.findAll { it.requestItem == this }
		return (fulfillmentItems) ? fulfillmentItems.sum { it.quantity } : 0;
	}
	
	Integer quantityRemaining() { 
		return quantity - quantityFulfilled();
	}
	
	
	Boolean isComplete() { 
		return !isPending();
	}
	
	Boolean isPending() { 
		return quantityRemaining() > 0;
	}
	
	def totalPrice() {
		return ( quantity ? quantity : 0.0 ) * ( unitPrice ? unitPrice : 0.0 );
	}


    boolean checkIsEmpty() {
     (id == null || id == "") && quantity == 1 && product == null && substitutable == false && (comment == null || comment == "") && (recipient == null || recipient == "")
    }

    String toString(){
        "id:${id} product:${product} quantity:${quantity} substitutable:${substitutable} comment:${comment} recipient:${recipient}"
    }

    Map toJson(){
      [
        "id": id,
        "productId": product?.id,
        "productName": product?.name,
        "quantity":quantity,
        "comment": comment,
        "recipient": recipient
      ]
    }


}
