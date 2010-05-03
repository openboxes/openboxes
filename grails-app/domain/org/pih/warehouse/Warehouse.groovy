package org.pih.warehouse

class Warehouse {

    // Core elements
    Integer id
    String name
    String city
    String country
    User manager

    // Core associations
    Inventory inventory
    List<Transaction> transactions   // might be better at inventory level

    
    // Association mapping
    static hasMany = [transactions:Transaction];
    static mappedBy = [transactions:"localWarehouse"]

    // Other elements to be supported soon
    //Organization organization
    //Location location
    //Country country
    //City city
    //State stateOrProvince
    // Address address

    String toString() { return "$name"; }


    // Constraints
    static constraints = {
	manager(nullable:true)
	inventory(nullable:true)
	transactions(nullable:true)
    }
}
