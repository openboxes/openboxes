package org.pih.warehouse.shipping

import org.pih.warehouse.core.Location

class ShipmentStatus implements Comparable {

	Date date
	Location location
	ShipmentStatusCode code
	
	String getName() { return code.getName() }
	
	String toString() { return getName() }
	
	int compareTo(obj) { 
		def diff = ShipmentStatusCode.compare(this.code, obj.code)
		if (diff==0) {
			diff = this.date <=> obj.date
		}
		
		return diff
	}
}
