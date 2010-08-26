package org.pih.warehouse.core

import java.net.URI;
import java.util.Date;
import org.pih.warehouse.core.DocumentType;

/**
 * A document is a file (e.g. document, image) that can be associated with an 
 * entity in the system.  Currently, users can only upload and link documents to 
 * shipments.  
 */
class Document {

	String name			// Document name (optional)
	String filename			// Document filename
	String extension 		// The extension of the file
	String contentType		// The content type of the file
	byte [] fileContents		// The contents of the file (if stored in database)
	Date dateCreated;		// The date the document was created
	Date lastUpdated;		// The date the document was last updated

	URI fileUri			// Universal Resource Identifier
	String documentNumber;		// Document reference number
	DocumentType documentType;	// Type of document
	
	// Documents should exist on their own in case we want to tie them to other objects. 
	// Shipment (and other entities) should create a join table for documents.	
	//static belongsTo = [ shipment : Shipment ];
	
	static transients = ["size"]

	static constraints = {
		name(nullable:true)		
		filename(nullable:true)
		fileContents(nullable:true)
		extension(nullable:true)
		contentType(nullable:true)
		fileUri(nullable:true)
		fileContents(nullable:true, maxSize:10485760) // 10 MBs
		documentNumber(nullable:true)
		documentType(nullable:true)		
	}

	String toString() { return "$name"; }
	String getSize() { return fileContents?.length; } 
	
}
