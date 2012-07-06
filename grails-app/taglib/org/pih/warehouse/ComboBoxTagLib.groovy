package org.pih.warehouse

import java.text.SimpleDateFormat;
import org.pih.warehouse.core.Person;

class ComboBoxTagLib {
		
	def productService
	
	def comboBox = { attrs, body ->
		out << render(template: "/taglib/comboBox", model: [attrs:attrs]);
	}
}