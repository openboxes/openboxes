package org.pih.warehouse.core

import java.util.Date;

class Role implements Serializable {
	
	RoleType roleType;
	String description;

	static constraints = {
		roleType(nullable:false)
		description(nullable:true, 	maxSize:255)
	}

	String toString() { return "${roleType.name}"; } 
	
}



