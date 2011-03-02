package org.pih.warehouse.shipping

import java.util.Date;

class Shipper implements java.io.Serializable {
	
	String name
	String description	
	String trackingUrl
	String trackingFormat
	String parameterName
	Date dateCreated;
	Date lastUpdated;

	static hasMany = [ shipperServices : ShipperService ];
	static mapping = {
		shipperServices joinTable: [name:'shipper_service', column: 'shipper_service_id', key: 'shipper_id']
	}

    static constraints = {
		name(nullable:false)
		description(nullable:true)		
		trackingUrl(nullable:true, blank:true)
		trackingFormat(nullable:true)
		parameterName(nullable:true, blank:true)
		
		dateCreated(display:false)
		lastUpdated(display:false)
   	}
	
	String toString() { name }

}
