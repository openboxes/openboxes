package org.pih.warehouse.shipping

import java.io.Serializable;
import java.util.Date;

class ShipmentWorkflow implements Serializable {

	String id
	String name					// user-defined name of the workflow
	ShipmentType shipmentType  	// the shipment type this workflow is associated with
	String excludedFields   	// comma-delimited (with no spaces) list of Shipment fields to exclude in this workflow
	String documentTemplate		// the template to use when auto-generating documents for this workflow
	
	// Audit fields
	Date dateCreated
	Date lastUpdated
	
	// one-to-many associations
	List referenceNumberTypes
	List containerTypes
	
	// Core association mappings
	static hasMany = [ referenceNumberTypes : ReferenceNumberType,
	                   containerTypes : ContainerType ]
	         
	static mapping = {
		id generator: 'uuid'
	}
	          
    static constraints = {
		name(nullable:false, blank: false, maxSize: 255)
		shipmentType(nullable:false, unique:true)  // for now, we are only allowing one workflow per shipment type, though we may want to change this
		excludedFields(nullable:true, maxSize: 255)
		documentTemplate(nullable:true, maxSize: 255)
		dateCreated(blank:true)
		lastUpdated(blank:true)
		
		// a shipment workflow can't have two identical reference number types
		referenceNumberTypes ( validator: { referenceNumberTypes ->
        	referenceNumberTypes?.unique( { a, b -> a.id <=> b.id } )?.size() == referenceNumberTypes?.size()       
		} )
		
		// a shipment workflow can't have two identical container types
		containerTypes ( validator: { containerTypes ->
        	containerTypes?.unique( { a, b -> a.id <=> b.id } )?.size() == containerTypes?.size()       
		} )
		
    }
	
	String toString() { name }
	
	Boolean isExcluded(String field) {
		// ?i: -> sets case insensitive
		// (^|,) -> matches start-of-line or comma
		// (,|$) -> matches comma or end-of-line	
		return excludedFields =~ (/(?i:(^|,)$field(,|$))/)
	}
}
