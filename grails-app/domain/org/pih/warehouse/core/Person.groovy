package org.pih.warehouse.core

import java.util.Date;

import org.pih.warehouse.core.Location;


class Person implements Comparable, Serializable {
    
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

	static transients = ["name"]
	
	static constraints = { 
		name(display:false)
		firstName(blank:false, maxSize: 255)	
		lastName(blank:false, maxSize: 255)	
		email(blank:false, email:true, unique:true, maxSize: 255)
		phoneNumber(nullable:true, maxSize: 255)
		dateCreated(display:false)
		lastUpdated(display:false)
		
	}
	
	int compareTo(obj) { obj.id <=> id }
	
	String toString() { return "$firstName $lastName"; }
	String getName() { return "$firstName $lastName"; }
	
}
