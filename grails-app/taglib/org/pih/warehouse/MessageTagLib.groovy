package org.pih.warehouse

class MessageTagLib {
   
	static namespace = "warehouse"
	
	Locale defaultLocale = new Locale(grailsApplication.config.warehouse.defaultLocale)
	
	def message = { attr, body ->
		Locale l = session?.user?.locale ?: defaultLocale; 
		String translation = g.message(code:attr.code, args: attr.args, encodeAs: attr.encodeAs, error: attr.error, message: attr.message, locale: l)
		out << (translation == attr.code ? (attr.default ?: attr.code) : translation);
	}
}
