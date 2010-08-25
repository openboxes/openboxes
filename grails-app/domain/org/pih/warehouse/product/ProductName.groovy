package org.pih.warehouse.product;

import java.util.Date;

/**
 *  
 */
class ProductName {

    String name							// The name of generic products needs to be i18n'd'
	ProductNameType productNameType  	// Whether the name is a Brand, Generic, or Branded Generic
	
	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	
	static belongsTo = [ product : Product ]
	
    static constraints = {
		name(nullable:false)
		productNameType(nullable:false)
    }
}
