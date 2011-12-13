package org.pih.warehouse.core

import java.util.Date;

class Address implements Serializable {
	
	String id
	String address
	String address2
	String city
	String stateOrProvince
	String postalCode
	String country

	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	
	static mapping = {
		id generator: 'uuid'
	}
	
	
	static constraints = {
		address(nullable: true, maxSize: 255)
		address2(nullable:true, maxSize: 255)
		city(maxSize: 255)
		stateOrProvince(maxSize: 255)
		postalCode(nullable:true, maxSize: 255)
		stateOrProvince(nullable:true, maxSize: 255)
	}
}
