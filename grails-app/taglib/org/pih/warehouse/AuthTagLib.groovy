package org.pih.warehouse

import org.pih.warehouse.inventory.Warehouse;

class AuthTagLib {
   	
	//Locale defaultLocale = new Locale(grailsApplication.config.locale.defaultLocale)
	
	def authorize = { attrs, body ->
		//attrs.locale = attrs.locale ?: session?.user?.locale ?: defaultLocale;
		//def defaultTagLib = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib')
		//out << defaultTagLib.message.call(attrs)
		def authorized = false;
		
		def warehouseInstance = Warehouse.get(session.warehouse.id)
		
		// Need to handle this case better
		if (!warehouseInstance)
			throw new Exception("Please choose a warehouse")

		// Check if the activity attribute has any activities supported by the given warehouse
		authorized = attrs?.activity?.any { warehouseInstance.supportsActivity(it) } 
				
		if (authorized) { 
			out << body{}
		}
		
	}
}
