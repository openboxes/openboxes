package org.pih.warehouse

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Role;
import org.pih.warehouse.core.User;

class AuthTagLib {
   	
	//Locale defaultLocale = new Locale(grailsApplication.config.locale.defaultLocale)
	def isUserInRole = { attrs, body ->	
		def user = User.get(session?.user?.id)		
		def isUserInRole = getIsUserInAnyRoles(user, attrs.roles)				  
		if (isUserInRole) { 
			out << body()
		}		
	}
	
	
	def isUserNotInRole = { attrs, body ->
		def user = User.get(session?.user?.id)
		def isUserInRole = getIsUserInAnyRoles(user, attrs.roles)				
		if (!isUserInRole) {
			out << body()
		}
	}
	
	
	Boolean getIsUserInAnyRoles(User user, Collection roles) { 
		Boolean isUserInRole;
		if (!user || !user?.roles) {
			isUserInRole = false;
		}
		else {
			isUserInRole = user?.roles.any { roles.contains(it.roleType) }
		}
		return isUserInRole;
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
