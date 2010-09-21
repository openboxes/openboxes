package org.pih.warehouse.shipping;

import java.util.List;
import java.util.Map;
import org.pih.warehouse.core.EventType;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.ListCommand;
import org.pih.warehouse.inventory.Transaction;

class ShipmentService {
	
	def sessionFactory;
	boolean transactional = true
	
	List<Shipment> getAllShipments() {
		return Shipment.list()
	}
	
	
	List<Shipment> getRecentOutgoingShipments(Long locationId) { 		
		Location location = Location.get(locationId);
		return Shipment.findAllByOriginAndExpectedShippingDateBetween(location, new Date()-30, new Date()+30, 
			[max:5, offset:2, sort:"expectedShippingDate", order:"desc"]);
	}
	
	
	List<Shipment> getRecentIncomingShipments(Long locationId) { 		
		Location location = Location.get(locationId);
		return Shipment.findAllByDestinationAndExpectedShippingDateBetween(location, new Date()-30, new Date()+30, 
			[max:5, offset:2, sort:"expectedShippingDate", order:"desc"]);
	}
	
	
	Map<EventType, ListCommand> getShipmentsByStatus(List shipments) {
		def shipmentMap = new TreeMap<EventType, ListCommand>();
		shipments.each {
			def eventType = it.getMostRecentStatus();
			log.info("eventType: " + eventType.name)
			def shipmentList = shipmentMap[eventType];
			if (!shipmentList) {
				shipmentList = new ListCommand(category: eventType, objectList: new ArrayList());
			}
			shipmentList.objectList.add(it);
			shipmentMap.put(eventType, shipmentList)
		}
		log.info("shipmentMap: " + shipmentMap)
		
		return shipmentMap;
	}
	
	
	
	List<Shipment> getShipments() { 		
		/*
		return Shipment.withCriteria { 				
			eq("mostRecentEvent.eventType.id", EventType.findByName("Departed"))
		}*/

		/*		
		//def sessionFactory
		//sessionFactory = ctx.sessionFactory  // this only necessary if your are working with the Grails console/shell
		def session = sessionFactory.currentSession		
		def query = session.createSQLQuery(
			"""
			select s.* , count(*)
			from shipment s, shipment_event se, event e 
			where se.shipment_events_id = s.id 
			and se.event_id = e.id 
			group by s.id having count(*) > 1
			order by s.name
			"""
		);
		query.addEntity(org.pih.warehouse.shipping.Shipment.class); // this defines the result type of the query
		//query.setInteger("ids", 1);
		return query.list();	// return query.list()*.name;
		*/
		
		def criteria = Shipment.createCriteria()
		def results = criteria.list {
			//or {
			//	   for (e in branchList) {
			//		   eq("branch", b)
			//	   }
			//	}
		}
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
