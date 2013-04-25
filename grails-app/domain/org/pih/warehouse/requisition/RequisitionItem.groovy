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

import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductGroup
import org.pih.warehouse.product.ProductPackage

class RequisitionItem implements Serializable {
	
	String id
	String description	
	
	// Requested item or product
    Product product
    Category category
	InventoryItem inventoryItem
    ProductGroup productGroup
	ProductPackage productPackage
    Integer quantity

    // Cancellation / change
	Integer quantityCanceled
	String cancelReasonCode
	String cancelComments
	
	// Miscellaneous information
	Float unitPrice	
	Person requestedBy	// the person who actually requested the item
	Boolean substitutable = false
    String recipient
    String comment
    Integer orderIndex

	// Parent requisition item
	RequisitionItem parentRequisitionItem
	
	
	
	// Audit fields
	Date dateCreated
	Date lastUpdated

	static transients = [ "type" ]
	
	static belongsTo = [ requisition: Requisition ]	
	static hasMany = [ requisitionItems: RequisitionItem, picklistItems: PicklistItem ]
	
	static mapping = {
		id generator: 'uuid'
        picklistItems cascade: "all-delete-orphan", sort: "id"
		requisitionItems cascade: "all-delete-orphan", sort: "id"
	}
		
    static constraints = {
    	description(nullable:true)
        category(nullable:true)
        product(nullable:false)
        productGroup(nullable:true)
        productPackage(nullable:true)
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
		parentRequisitionItem(nullable:true)
	}

    /**
     * Return the package quantity multiplied by the quantity requested.
     *
     * @return
     */
    def totalQuantity() {
        return (productPackage?.quantity?:1) * (quantity?:0)
    }

    def totalQuantityCanceled() {
        println "product pacakage: " + productPackage
        println "product pacakage: " + productPackage?.quantity
        println "quantity canceled: " + quantityCanceled
        println "total quantity canceled: " + (productPackage?.quantity?:1) * (quantityCanceled?:0)


        return (productPackage?.quantity?:1) * (quantityCanceled?:0)
    }

    def isCanceled() {
        return totalQuantityCanceled() == totalQuantity()
    }

    def isCompleted() {
        return calculateQuantityRemaining() <= 0
    }

    def calculateQuantityPicked() {
        def quantityPicked = PicklistItem.findAllByRequisitionItem(this).sum{ it.quantity }
		return quantityPicked?:0
    }

	def calculateQuantityRemaining() {
		return totalQuantity() - (calculateQuantityPicked() + (quantityCanceled?:0))
	}
	
    def calculateNumInventoryItem(Inventory inventory) {
        InventoryItem.findAllByProduct(product).size()
    }

    def retrievePicklistItems() {
        return PicklistItem.findAllByRequisitionItem(this)
    }

    def availableInventoryItems() {
        return InventoryItem.findAllByProduct(product)
    }

    def getNextRequisitionItem() {
        def currentIndex = requisition.requisitionItems.findIndexOf { it == this }
        def nextItem = requisition?.requisitionItems[currentIndex+1]?:requisition?.requisitionItems[0]
        return nextItem
    }

    def getPreviousRequisitionItem() {
        def lastIndex = requisition?.requisitionItems?.size()-1
        def currentIndex = requisition.requisitionItems.findIndexOf { it == this }
        def previousItem = requisition?.requisitionItems[currentIndex-1]?:requisition?.requisitionItems[lastIndex]
        return previousItem
    }

    Map toJson(){
      [
        id: id,
        version: version,
        productId: product?.id,
        productName: product?.productCode + " " + product?.name + ((productPackage) ? " ("+productPackage?.uom?.code + "/" + productPackage?.quantity + ")" : " (EA/1)"),
        productPackageId: productPackage?.id,
        productPackageName: productPackage?.uom?.code + "/" + productPackage?.quantity,
        productPackageQuantity: productPackage?.quantity?:1,
		unitOfMeasure: product?.unitOfMeasure?:"EA",
        quantity:quantity,
        totalQuantity:totalQuantity(),
        quantityCanceled:quantityCanceled,
        totalQuantityCanceled:totalQuantityCanceled(),
        comment: comment,
        recipient: recipient,
        substitutable: substitutable,
        orderIndex: orderIndex
      ]
    }


}
