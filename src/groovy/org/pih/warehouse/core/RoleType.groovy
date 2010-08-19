package org.pih.warehouse.core;

public enum RoleType {

	ROLE_USER('User'),
	ROLE_MANAGER('Manager'),
	ROLE_ADMIN('Admin')
 
	String name

	RoleType(String name) {
		this.name = name
	}

	static list() {
		[ROLE_USER, ROLE_MANAGER, ROLE_ADMIN]
	}
}
