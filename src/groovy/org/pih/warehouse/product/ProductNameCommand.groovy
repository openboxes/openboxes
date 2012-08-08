package org.pih.warehouse.product;

import java.io.Serializable;

class ProductNameCommand implements Serializable {
	String name
	String description
	
	static constraints = {
		name(blank:false, nullable:false)
		description(nullable:true)
	}
}

