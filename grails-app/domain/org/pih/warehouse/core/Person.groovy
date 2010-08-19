package org.pih.warehouse.core

import java.util.Date;

import org.pih.warehouse.inventory.Warehouse;


class Person extends Party {
    
	String name;
	String firstName;
	String lastName;
	String email;
	String phoneNumber;

	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	
	
	static mapping = { 
		tablePerHierarchy false
		table 'person' 
	}


	String toString() { return "Name: $firstName $lastName, Email: $email, Phone: $phoneNo"; }

	static constraints = { 
		name(nullable:true)	
		firstName(nullable:true)	
		lastName(nullable:true)	
		email(nullable:true, email:true)
		phoneNumber(nullable:true)
	}
	
	
}
