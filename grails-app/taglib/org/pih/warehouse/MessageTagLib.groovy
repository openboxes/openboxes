package org.pih.warehouse

class MessageTagLib {
   
	static namespace = "warehouse"
	
	def message = { attr, body ->
		// note that use the locale associated with the user here, if available, otherwise we use the default locale
		out << g.message(code:attr.code, default: attr.default, args: attr.args, encodeAs: attr.encodeAs, error: attr.error, message: attr.message, 
			locale: session?.user?.locale ?: new Locale(grailsApplication.config.warehouse.defaultLocale) )
	}
}
