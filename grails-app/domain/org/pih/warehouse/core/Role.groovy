package org.pih.warehouse.core

import java.util.Date;

class Role implements Serializable {
	
	String id
	RoleType roleType;
	String description;

	static constraints = {
		roleType(nullable:false)
		description(nullable:true, 	maxSize:255)
	}

	static mapping = { 
		id generator: 'uuid'
	}
	
	
	String toString() { return "${roleType.name}"; } 
	
}



