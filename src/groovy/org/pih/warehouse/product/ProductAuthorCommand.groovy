package org.pih.warehouse.product;

import java.io.Serializable;

class ProductAuthorCommand implements Serializable {

	String name
	String accountId
	
	static constraints = {
		name(nullable:true)
		accountId(nullable:true)
	}
}
