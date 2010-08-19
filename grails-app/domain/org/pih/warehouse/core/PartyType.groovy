package org.pih.warehouse.core

class PartyType {

	String name
	String description 

	static constraints = { 
		name(nullable:false);
		description(nullable:false);
	}

}
