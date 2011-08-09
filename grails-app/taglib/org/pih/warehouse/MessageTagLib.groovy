package org.pih.warehouse

class MessageTagLib {
   
	static namespace = "warehouse"
	
	def message = { attr, body ->
		// note that we use the locale associated with the user here
		out << g.message(code:attr.code, default: attr.default, args: attr.args, encodeAs: attr.encodeAs, error: attr.error, message: attr.message, locale: session?.user?.locale )
	}
}
