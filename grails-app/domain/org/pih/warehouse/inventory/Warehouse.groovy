package org.pih.warehouse.inventory;

import org.pih.warehouse.core.Location;
import org.pih.warehouse.user.User;

class Warehouse extends Location {

    // Core elements
    //Integer id
    
    User manager

    // Core associations
    Inventory inventory
    List<Transaction> transactions   // might be better at inventory level

    
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
