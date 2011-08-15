package org.pih.warehouse.fulfillment

import java.util.Date;

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.fulfillment.FulfillmentStatus;
import org.pih.warehouse.request.Request;

class Fulfillment implements Serializable {

	// Attributes
	FulfillmentStatus status;
	//Request request;				// request [to be] fulfilled	
	Person fulfilledBy				// person whom fulfilled request
	Date dateFulfilled				// the date that the request was fulfilled
	
	// Audit fields
	Date dateCreated
	Date lastUpdated

	// Bi-directional Associations	
	static belongsTo = [ request : Request ]
	
	// One-to-many associations 
	static hasMany = [ fulfillmentItems : FulfillmentItem ]
	
	static mapping = {
		fulfillmentItems cascade: "all-delete-orphan", sort: "id"
	}
	
	// Constraints
    static constraints = {
		status(nullable:true)
		request(nullable:false)
		fulfilledBy(nullable:true)
		dateFulfilled(nullable:true)
    }
}
