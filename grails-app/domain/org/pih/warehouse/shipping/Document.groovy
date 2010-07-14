package org.pih.warehouse.shipping

/**
 * An attachment is a document or image that is attached to a given order or shipment.
 */
class Document {

	long size
	String filename
    byte [] contents
	DocumentType documentType;
    
    static belongsTo = [ shipment : Shipment ];
	
    static constraints = {
		//type(inList:["Invoice", "Packing List", "Shipping Manifest", "Other"])
    }
}
