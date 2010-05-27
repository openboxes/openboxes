package org.pih.warehouse

class User {
    
    Integer id;

    // Details 
    String firstName;
    String lastName;
    String email;
    
    // User's current warehouse
    Warehouse warehouse;

    // Authorization
    String role;
    User manager;

    // Authentication
    String username;
    String password;

    String toString() { return "$firstName $lastName"; }


    static mapping = {
    	table "`user`"
    }

    static constraints = {
    	manager(nullable:true)
    	email(email:true)
		password(blank:false, password: true)
		role(inList:["Supervisor", "Manager", "Stocker", "Other"])
		warehouse(nullable:true)
    }
}
