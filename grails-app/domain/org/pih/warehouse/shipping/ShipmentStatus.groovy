package org.pih.warehouse.shipping

import org.pih.warehouse.core.Type;

/**
 * Represents the Status of a Shipment.
 */
class ShipmentStatus extends Type {
	
	// Indicates whether this status is an initial or completed state
	Boolean initialStatus = false; 
	Boolean finalStatus = false;		
	
    static constraints = {
		initialStatus(nullable:true)
		finalStatus(nullable:true)
	}
	
	String toString() { return "$name"; }
}
