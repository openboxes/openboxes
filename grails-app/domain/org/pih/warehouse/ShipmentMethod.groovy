package org.pih.warehouse

class ShipmentMethod {
	
	String name
	String methodName
	String trackingUrl
	String trackingUrlParameterName

    static constraints = {
		name(nullable:false)
		methodName(nullable:false)
		trackingUrl(nullable:true, blank:true)
		trackingUrlParameterName(nullable:true, blank:true)
    }
	
	String toString() { return "$methodName"; }
}
