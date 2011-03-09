package org.pih.warehouse.product

class ProductCommand {
	String id
	String ean
	String name
	String description
	String productType
	
	static constraints = {
	   ean(nullable: true, blank: false)
	   name(nullable: true, blank: false)
	   description(nullable:true, blank:false)
	   productType(nullable:true, blank:false)
	}
 }