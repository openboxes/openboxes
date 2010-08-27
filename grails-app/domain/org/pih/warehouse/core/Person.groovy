package org.pih.warehouse.core

import java.util.Date;

import org.pih.warehouse.inventory.Warehouse;


class Person implements Comparable {
    
	String firstName;
	String lastName;
	String email;
	String phoneNumber;
	Date dateCreated;
	Date lastUpdated;
	
	static mapping = { 
		tablePerHierarchy false
		table 'person' 
	}


	String toString() { return "Name: $firstName $lastName, Email: $email, Phone: $phoneNumber"; }

	static constraints = { 
		firstName(nullable:true)	
		lastName(nullable:true)	
		email(nullable:true, email:true)
		phoneNumber(nullable:true)
	}
	
	int compareTo(obj) { obj.id <=> id }
	
	
}
