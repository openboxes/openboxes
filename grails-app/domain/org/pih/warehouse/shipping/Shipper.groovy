package org.pih.warehouse.shipping

import java.util.Date;

class Shipper implements java.io.Serializable {
	
	String id
	String name
	String description	
	String trackingUrl
	String trackingFormat
	String parameterName
	Date dateCreated;
	Date lastUpdated;

	static hasMany = [ shipperServices : ShipperService ];
	static mapping = {
		id generator: 'uuid'
		shipperServices joinTable: [name:'shipper_service', column: 'shipper_service_id', key: 'shipper_id']
	}
	
    static constraints = {
		name(nullable:false, maxSize: 255)
		description(nullable:true, maxSize: 255)		
		trackingUrl(nullable:true, blank:true, maxSize: 255)
		trackingFormat(nullable:true, maxSize: 255)
		parameterName(nullable:true, blank:true, maxSize: 255)
		
		dateCreated(display:false)
		lastUpdated(display:false)
   	}
	
	String toString() { name }

}
