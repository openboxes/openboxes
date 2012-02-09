package org.pih.warehouse

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Role;
import org.pih.warehouse.core.User;

class AuthTagLib {
   	
	//Locale defaultLocale = new Locale(grailsApplication.config.locale.defaultLocale)
	def isInRole = { attrs, body ->
		
		def isInRole = false;
		def roles = attrs.roles
		def userInstance = User.get(session?.user?.id)
		
		if (!userInstance || !userInstance?.roles) { 
			isInRole = false;
		}
		else { 
			// FIXME We need to check to see if the currently logged in user has any of the given roles
			log.debug "attrs: " + attrs?.roles 
			isInRole = userInstance?.roles.any { attrs.roles.contains(it.roleType) }
		}
				  
		if (isInRole) { 
			out << body{}
		}
		
	}
	def authorize = { attrs, body ->
		//attrs.locale = attrs.locale ?: session?.user?.locale ?: defaultLocale;
		//def defaultTagLib = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib')
		//out << defaultTagLib.message.call(attrs)
		def authorized = false;
		
		def warehouseInstance = Location.get(session.warehouse.id)
		
		// Need to handle this case better
		if (!warehouseInstance)
			throw new Exception("Please choose a warehouse")

		// Check if the activity attribute has any activities supported by the given warehouse
		authorized = attrs?.activity?.any { warehouseInstance.supports(it) } 
				
		if (authorized) { 
			out << body{}
		}
		
	}
}
