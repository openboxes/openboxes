package org.pih.warehouse.product;

class DurableProduct extends Product {

	String make 
	String model
	String serialNumber
	
	static constraints = {	
		make(nullable:true)
		model(nullable:true)
		serialNumber(nullable:true)
	}
}
