package org.pih.warehouse.shipping

/**
 * Represents a particular Event of interest during the course of a Shipment
 * Examples might be:
 *  Shipment #1 departed from Boston to Miami on 1/1/2010: 
 *  	{eventDate: 1/1/2010, eventLocation: Boston, eventType: SHIPPED, targetLocation: Miami}
 *  Shipment #2 arrived at Customs from Miami on 5/5/2010: 
 *  	{eventDate: 5/5/2010, eventLocation: Customs, eventType: ARRIVED, targetLocation: null}
 */
class ShipmentEvent extends Event  {
	
	static belongsTo = { shipment:Shipment }
		

	
	
	
}
