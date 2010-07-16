package org.pih.warehouse.user

import java.util.Date;

import org.pih.warehouse.inventory.Warehouse;

class User extends Contact {

	// Authentication
	String username;
	String password;

    // User's last warehouse
	Date lastLoginDate;
    Warehouse warehouse;
	
    // Authorization
    String role;
    User manager;


    String toString() { return "Username: $username"; }

	/*
    static mapping = {
    	table "`user`"
    }*/

    static constraints = {
    	manager(nullable:true)
    	email(email:true)		
		password(blank:false, password: true)
		role(inList:["Supervisor", "Manager", "Stocker", "Other"])
		lastLoginDate(nullable:true)
		warehouse(nullable:true)
    }

	
	
}
