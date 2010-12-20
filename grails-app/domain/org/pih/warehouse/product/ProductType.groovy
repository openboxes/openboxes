package org.pih.warehouse.product;

import java.util.Date;

class ProductType {

    String name
	String code
	String description
	ProductClass productClass	
	Integer sortOrder = 0;
	Date dateCreated;
	Date lastUpdated;
	
	static hasMany = [ categories : Category, attributes : Attribute ]
	
	static constraints = {
		name(nullable:false)
		code(nullable:true)
		description(nullable:true)
		sortOrder(nullable:true)
		productClass(nullable:true)
		dateCreated(display:false)
		lastUpdated(display:false)
	}

	static mapping = {
		sort "sortOrder"
	}

}
