package org.pih.warehouse

class Warehouse extends Location {

    // Core elements
    //Integer id
    
    User manager

    // Core associations
    Inventory inventory
    //List<Transaction> transactions   // might be better at inventory level

    
    // Association mapping
    static hasMany = [transactions:Transaction];
    static mappedBy = [transactions:"localWarehouse"]



    String toString() { return "$name"; }


    // Constraints
    static constraints = {
	    manager(nullable:true)
	    inventory(nullable:true)
	    transactions(nullable:true)
    }
}
