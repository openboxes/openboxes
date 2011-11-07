package org.pih.warehouse.inventory

import org.codehaus.groovy.grails.validation.Validateable;
import org.pih.warehouse.product.Product;

@Validateable
class TransactionEntryCommand {
	
	boolean deleted
	
	InventoryItem inventoryItem
	String comment
	String lotNumber
	Product product
	Date expirationDate
	Integer quantity
	
	static constraints = {

	}
}
