package org.pih.warehouse

import org.pih.warehouse.core.ActivityCode;
import org.pih.warehouse.core.Location;

class SelectTagLib {
	
	def locationService
	
	def selectTransactionDestination = { attrs,body ->		
		def currentLocation = Location.get(session.warehouse.id)
		attrs.from = locationService.getTransactionDestinations(currentLocation).sort { it?.name?.toLowerCase() };
		attrs.optionKey = 'id'		
		//attrs.optionValue = 'name'
		attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]"}
		out << g.select(attrs)
	}

	def selectTransactionSource = { attrs,body ->
		def currentLocation = Location.get(session.warehouse.id)
		attrs.from = locationService.getTransactionSources(currentLocation).sort { it?.name?.toLowerCase() };
		attrs.optionKey = 'id'
		//attrs.optionValue = 'name'
		attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]"}
		out << g.select(attrs)
	}

	def selectOrderSupplier = { attrs,body ->
		def currentLocation = Location.get(session.warehouse.id)
		attrs.from = locationService.getOrderSuppliers(currentLocation).sort { it?.name?.toLowerCase() };
		attrs.optionKey = 'id'
		//attrs.optionValue = 'name'
		attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]"}
		out << g.select(attrs)
	}

	def selectRequestSupplier = { attrs,body ->
		def currentLocation = Location.get(session.warehouse.id)
		attrs.from = locationService.getRequestSuppliers(currentLocation).sort { it?.name?.toLowerCase() };
		attrs.optionKey = 'id'
		//attrs.optionValue = 'name'
		attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]"}
		out << g.select(attrs)
	}

	def selectCustomer = { attrs,body ->
		def currentLocation = Location.get(session.warehouse.id)
		attrs.from = locationService.getCustomers(currentLocation).sort { it?.name?.toLowerCase() };
		attrs.optionKey = 'id'
		//attrs.optionValue = 'name'
		attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]"}
		out << g.select(attrs)
	}

	def selectShipmentOrigin = { attrs,body ->
		def currentLocation = Location.get(session.warehouse.id)
		attrs.from = locationService.getShipmentOrigins().sort { it?.name?.toLowerCase() };
		attrs.optionKey = 'id'
		//attrs.optionValue = 'name'
		attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]"}
		out << g.select(attrs)
	}

	def selectShipmentDestination = { attrs,body ->
		def currentLocation = Location.get(session.warehouse.id)
		attrs.from = locationService.getShipmentDestinations().sort { it?.name?.toLowerCase() } ;
		attrs.optionKey = 'id'
		//attrs.optionValue = 'name'
		attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]"}
		out << g.select(attrs)
	}


		
}