package org.pih.warehouse.shipping

import java.util.Date;

/**
 * An attachment is a document or image that is attached to a given order or shipment.
 */
class Document {

	long size
	String filename
	String extension 
	byte [] fileContents
	String documentNumber;
	DocumentType documentType;
	
	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	    
	static belongsTo = [ shipment : Shipment ];
	
	static constraints = {
		extension(nullable:true)
		fileContents(nullable:true, maxSize:10485760) // 10 MBs
		documentNumber(nullable:true)
		documentType(nullable:true)		
	}
}
