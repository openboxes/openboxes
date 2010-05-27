package org.pih.warehouse

/**
 * A location can be a customer, warehouse, or supplier.  
 */
class Location {

    String name
    
    //Address address
    String city
    String state 
    String postalCode
    String country
    
    // Other elements to be supported soon
    //Organization organization
    //Country country
    //City city
    //State stateOrProvince
    //Address address
    
    //employees : Employee
    
    

    static constraints = {
    	state(nullable:true)
    	postalCode(nullable:true)
    }
}
