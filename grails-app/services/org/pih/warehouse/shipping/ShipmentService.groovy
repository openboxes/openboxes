package org.pih.warehouse.shipping;

import org.pih.warehouse.core.Location;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.inventory.Transaction;

class ShipmentService {
	
	boolean transactional = true
	
	List<Shipment> getAllShipments() {
		return Shipment.list()
	}
	
	List<Shipment> getShipmentsByLocation(Location location) {
		return Shipment.withCriteria { 
			or {	
				eq("destination", location)
				eq("origin", location)
			}
		}
	}    

	List<Shipment> getShipmentsByName(String name) {
		return Shipment.withCriteria { 
			ilike("name", "%" +name + "%")
		}
	}
	
	List<Shipment> getShipmentsByNameAndDestination(String name, Location location) {
		return Shipment.withCriteria {
			and { 
				ilike("name", "%" +name + "%")
				eq("destination", location)
			}
		}
	}

	List<Shipment> getShipmentsByNameAndOrigin(String name, Location location) {
		return Shipment.withCriteria {
			and {
				ilike("name", "%" +name + "%")
				eq("origin", location)
			}
		}
	}


	List<Shipment> getShipmentsByDestination(Location location) {
		return Shipment.withCriteria { 
			eq("destination", location) 
		}
	}
	
	List<Transaction> getShipmentsByOrigin(Location location) {
		return Shipment.withCriteria { 
			eq("origin", location);
		}
	}
}
