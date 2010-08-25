package org.pih.warehouse.product;

class DurableProduct extends Product {

	String make 
	String model
	
	static mapping = {
		table "durable_product"
	}
	
	static constraints = {	
		make(nullable:true)
		model(nullable:true)
	}
}
