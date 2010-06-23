package org.pih.warehouse

class ShipmentService {

    boolean transactional = true

    List<Shipment> getAllShipments() {
    	return Shipment.list()
    }
    
    List<Shipment> getShipmentsWithAnyLocation(Location location) {
    	return Shipment.withCriteria { 
    		or {	
	    		eq("destination", location)
	    		eq("origin", location) 
    		}
    	}
    }    

    List<Shipment> getShipmentsWithDestination(Location location) {
    	return Shipment.withCriteria { eq("destination", location) }
    }

    List<Transaction> getShipmentsWithOrigin(Location location) {
		return Shipment.withCriteria { eq("origin", location) }
    }

    

}
