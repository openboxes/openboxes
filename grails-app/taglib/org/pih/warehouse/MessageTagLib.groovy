package org.pih.warehouse

class MessageTagLib {
   
	static namespace = "warehouse"
	
	def message = { attr, body ->
		out << "${g.message(code:attr.code, default: attr.default, args: attr.args, encodeAs: attr.encodeAs)}"
	}
}
