package org.pih.warehouse.shipping

import java.util.Date;

/**
 * An attachment is a document or image that is attached to a given order or shipment.
 */
class Document {

	long size
	String filename
    byte [] contents
	DocumentType documentType;

	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	    
    static belongsTo = [ shipment : Shipment ];
	
    static constraints = {
		//type(inList:["Invoice", "Packing List", "Shipping Manifest", "Other"])
    }
}
