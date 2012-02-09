package org.pih.warehouse

class MessageTagLib {
   
	static namespace = "warehouse"
	
	Locale defaultLocale = new Locale(grailsApplication.config.locale.defaultLocale)
	
	def message = { attrs, body ->		
		if (session.useDebugLocale) { 
			out << attrs.code
		}
		else { 
			attrs.locale = attrs.locale ?: session?.user?.locale ?: defaultLocale;
			def defaultTagLib = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib')
			out << defaultTagLib.message.call(attrs)
		}		
	}
}
