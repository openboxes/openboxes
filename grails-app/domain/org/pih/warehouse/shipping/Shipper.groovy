package org.pih.warehouse.shipping

import java.util.Date;
import org.pih.warehouse.core.Organization;

class Shipper extends Organization {
	
	String trackingUrl
	String trackingFormat
	String parameterName

	static mapping = {
		tablePerHierarchy false
		table 'shipper'
	}
	
	static hasMany = [ shipperServices: ShipperService ]

    	static constraints = {
		trackingUrl(nullable:true, blank:true)
		trackingFormat(nullable:true)
		parameterName(nullable:true, blank:true)
   	}

}
