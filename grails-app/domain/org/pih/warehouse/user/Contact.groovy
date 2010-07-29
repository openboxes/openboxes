package org.pih.warehouse.user


class Contact extends Person {
    
    String email;
	String phoneNo;

    String toString() { return "Name: $firstName $lastName, Email: $email, Phone: $phoneNo"; }

    static constraints = {
    	email(nullable:true, email:true)
		phoneNo(nullable:true)
    }
}
