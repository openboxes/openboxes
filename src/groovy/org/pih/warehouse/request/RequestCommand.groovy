package org.pih.warehouse.request

import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;
import org.pih.warehouse.shipping.ShipmentType;

class RequestCommand implements Serializable {

	Request request
	Person recipient
	Date shippedOn
	Date deliveredOn
	ShipmentType shipmentType
	Shipment shipment
	
	User currentUser;
	Location currentLocation
	
	Location origin
	Location destination
	Date dateRequested;
	Person requestedBy
	
	
	// Not the actual request items, but rather all the line items on the receive order page.  
	// This means that we might have more than one ReqeuestItemCommand per ReqeuestItem.
	def requestItems = 
		LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(RequestItemCommand.class));
	
	def fulfillItems = 
		LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(RequestItemCommand.class));
	
	static constraints = {
		shipmentType(nullable:false)
		recipient(nullable:false)
		// Should ship on or before the day it's delivered
		shippedOn(nullable:false, 
			validator: { value, obj -> 		
				//println "value: " + value		
				//println "obj.deliveredOn: " + obj.deliveredOn
				//println "new Date(): " + new Date()
				if (!(value <= new Date())) { 
					//println "value <= new Date(): " + (value <= new Date())
					return ["invalid.mustOccurOnOrBeforeToday", value, new Date()]
				}
				// subtract a day from the shippedOn date in case the dates are the same
				if (!obj.deliveredOn.after(value-1)) { 
					//println "obj.deliveredOn.after(value-1): " + obj.deliveredOn.after(value-1)
					return ["invalid.mustOccurOnOrBeforeDeliveredOn", value, obj.deliveredOn]
				}
			}
		)
		deliveredOn(nullable:false, 
			validator: { value, obj ->
				//println "value: " + value
				//println "new Date(): " + new Date()
				if (!(value <= new Date()) ) { 
					//println "value <= new Date(): " + (value <= new Date())					
					return ["invalid.mustOccurOnOrBeforeToday", value, new Date()]
				}
				if (!(value).after(obj.shippedOn-1)) { 
					return ["invalid.mustOccurOnOrAfterShippedOn", value, obj.shippedOn]
				}
			}
		)
		currentUser(nullable:true)
		currentLocation(nullable:true)
		origin(nullable:true)
		destination(nullable:true)
		dateReqeuested(nullable:true)
		requestedBy(nullable:true)
	}
	
}

