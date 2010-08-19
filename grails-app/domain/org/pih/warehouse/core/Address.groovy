package org.pih.warehouse.core

import java.util.Date;

class Address {
	
	String address
	String address2
	String city
	String stateOrProvince
	String postalCode
	String country

	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	
	static constraints = {
		postalCode(nullable:true)
		stateOrProvince(nullable:true)
	}
}
