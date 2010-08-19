package org.pih.warehouse.shipping

import java.util.Date;
import org.pih.warehouse.core.Organization;

class ShipperService {

	String name

	static constraints = {
		name(nullable:true)
	}
	
	String toString() { return "$name"; }
}
