package org.pih.warehouse

import org.pih.warehouse.product.Category;

class CategorySelectTagLib {
	
	def productService
	
	
	
	def categorySelect = { attrs ->		
		def selectedCategory = Category.get(attrs.value as int)
		println selectedCategory
		def rootCategory = productService.getRootCategory();
		out << "<select class='" + attrs.cssClass + "' id='" + attrs.id + "' name='" + attrs.name + "'>";
		out << render(template:"../category/selectOptions", model:[category:rootCategory, selected:selectedCategory, level: 0])		
		out << "</select>"
	}
	
}
