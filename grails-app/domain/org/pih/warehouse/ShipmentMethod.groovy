package org.pih.warehouse

class ShipmentMethod {
	
	String name
	String methodName
	String trackingUrl
	String trackingFormat
	String parameterName

    static constraints = {
		name(nullable:false)
		methodName(nullable:false)
		trackingUrl(nullable:true, blank:true)
		trackingFormat(nullable:true)
		parameterName(nullable:true, blank:true)
    }
	
	String toString() { return "$methodName"; }
}
