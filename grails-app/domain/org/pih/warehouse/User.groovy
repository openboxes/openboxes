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

    // Authentication
    String username;
    String password;

    String toString() { return "$firstName $lastName"; }


    static mapping = {
    	table "`user`"
    }

    static constraints = {
		email(email:true)
		password(blank:false, password: true)
		role(inList:["Stock Manager", "Stock Person", "Other"])
		warehouse(nullable:true)
    }
}
