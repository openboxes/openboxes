package org.pih.warehouse.product

import java.util.Date;

class Tag {

    String tag

	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	
    static constraints = {
        tag(blank:false, unique:true)
    }
    
}
