package org.pih.warehouse

import java.text.SimpleDateFormat;
import org.pih.warehouse.core.Person;

class ChooseCategoryTagLib {

	def productService
	
	def chooseCategory = { attrs, body ->
		
		attrs.rootNode = productService.getRootCategory()
		
		out << render(template: "/taglib/chooseCategory", model: [attrs:attrs]);
	}
}