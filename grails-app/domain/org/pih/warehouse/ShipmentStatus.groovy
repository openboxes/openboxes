package org.pih.warehouse

/**
 * Represents the Status of a Shipment.
 */
class ShipmentStatus extends Type {
	
	boolean finalStatus		// Indicates whether this status indicates a completed shipment

    static constraints = {
	
	}
	
	String toString() { return "$name"; }
}
