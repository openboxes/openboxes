package org.pih.warehouse.inventory;

import java.util.Date;

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.User;

class Warehouse extends Location {

    User manager						// the person in charge of the warehouse
    Inventory inventory					// each warehouse has a single inventory
    List<Transaction> transactions   	// might be better at inventory level

    // Association mapping
    static hasMany = [transactions:Transaction, users:User];
    static mappedBy = [transactions:"thisWarehouse"]

	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	
    String toString() { return "$name"; }

    // Constraints
    static constraints = {
	    manager(nullable:true)
	    inventory(nullable:true)
	    transactions(nullable:true)
    }
}
