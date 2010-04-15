package org.pih.warehouse

class User {
    
    Integer id;

    // Details 
    String firstName;
    String lastName;
    String email;

    // Authorization
    String role;

    // Authentication
    String username;
    String password;

    String toString() { return "$email"; }


    static constraints = {
	email(email:true)
	password(blank:false, password: true)
	role(inList:["Stock Manager", "Stock Person", "Other"]);
    }
}
