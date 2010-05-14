package org.pih.warehouse

/**
 * An attachment is a document or image that is attached to a given order or shipment.
 */
class Attachment {

	long size
	String type
	String filename
    byte [] contents

    static constraints = {
		type(inList:["Invoice", "Packing List", "Shipping Manifest", "Other"])
    }
}
