package org.pih.warehouse.core

import java.util.Date;

class Role {
	
	RoleType roleType;
	String description;

	static constraints = {
		roleType(nullable:false)
		description(nullable:true)
	}

	String toString() { return "${roleType.name}"; } 
	
}



