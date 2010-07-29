package org.pih.warehouse.user

import org.pih.warehouse.inventory.Warehouse;


class Person {
    
	String name;
	String firstName;
	String lastName;
	
	String toString() { return "$firstName $lastName"; }

	static constraints = { 
		name(nullable:true)	
		firstName(nullable:true)	
		lastName(nullable:true)	
	}
	
	
}
