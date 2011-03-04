package org.pih.warehouse.inventory;

import java.util.Date;

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.User;


/**
 * 
 *
 */
class Warehouse extends Location {

    User manager								// the person in charge of the warehouse
    Inventory inventory							// each warehouse has a single inventory
	Boolean local = Boolean.TRUE				// indicates whether this warehouse is being managed on the locally deployed system
	Boolean active = Boolean.TRUE				// indicates whether this warehouse is currently active
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	
    // Association mapping
    static hasMany = [ employees: User ];

    // Constraints
    static constraints = {
	    manager(nullable:true)
	    inventory(nullable:true)
		active(nullable:false)
    }
	
}



