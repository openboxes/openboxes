package org.pih.warehouse.product;

import java.io.Serializable;

class ProductImageCommand implements Serializable {

	String link
	String status
	
	static constraints = {
		name(nullable:true)
		accountId(nullable:true)
	}
}
