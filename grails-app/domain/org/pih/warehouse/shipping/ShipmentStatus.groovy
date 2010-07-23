package org.pih.warehouse.shipping

import org.pih.warehouse.core.Type;

/**
 * Represents the Status of a Shipment.
 */
class ShipmentStatus extends Type {
	
	// Indicates whether this status is an initial or completed state
	Boolean initial = false; 
	Boolean pending = false;
	Boolean complete = false;		
	
    static constraints = {
		initial(nullable:true)
		pending(nullable:true)
		complete(nullable:true)
	}
	
	String toString() { return "$name"; }
}
