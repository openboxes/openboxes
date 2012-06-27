package org.pih.warehouse

import java.text.SimpleDateFormat;

class GlobalSearchTagLib {
		
	def globalSearch = { attrs, body ->
		
		attrs.id = (attrs.id) ? attrs.id : "globalSearch_" + (new Random()).nextInt()
		attrs.name = (attrs.name) ? attrs.name : attrs.id
		attrs.value = (attrs.value)?:"";
		attrs.width = (attrs.width) ?: 200;
		attrs.minLength = (attrs.minLength) ?: 1;
		attrs.jsonUrl = (attrs.jsonUrl) ?: "";
		attrs.cssClass= (attrs.cssClass) ?:""
		attrs.size = (attrs.size)?:"30"
		attrs.display = (attrs.display)?:"visible"
		
		
		out << g.render(template: '/taglib/globalSearch', model: [attrs:attrs]);
	}
	
}