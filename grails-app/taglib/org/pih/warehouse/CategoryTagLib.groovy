package org.pih.warehouse

class CategoryTagLib {
	
	static Integer counter = 0;
	
	
	def displayCategories = { attrs, body ->	 
		out << "<h1>Display Tree</h1>";	 	
		def categories = attrs['categories'];
		displayTree (categories, "<h2>", "</h2>");
	}
	
	
	def displayTree = { categories, beginTag, endTag -> 			 
		counter++;
		categories.each {  		
			log.info beginTag + it + endTag 
			log.info counter.toString();	 		
			log.info "display children: " + it.categories;
			displayTree it.categories, "<h3>", "</h3>";
		}
	}
	
	def selectCategoryWithChosen = { attrs -> 
		out << """
			<select multiple="true" data-placeholder="${attrs.noSelection.value}" name="${attrs.name}" style="${attrs.style}" value="${attrs.value}" class='${attrs.class}'>
		"""
		displayCategoryOptions(attrs['rootNode'], attrs.value, 0);
		out << "</select>";
	}
	
	def selectCategory = { attrs ->
		out << "<select multiple=\"true\" name='" + attrs.name + "' class='"+ attrs.cssClass +"'>";
		displayCategoryOptions(attrs['rootNode'], attrs.value, 0);
		out << "</select>";
	}
	
	
	
	def displayCategoryOptions = { node, value, depth ->
		if (node) {
			if (node.id) {
				println value?.id + " == " + node?.id
				def selected = (value == node)
				out << """<option value="${node?.id}" ${selected?"selected":""}>${includeIndent(depth) + node?.name}</option>"""
			}
			if (node.categories) {
				//out << includeIndent(depth)
				node.categories.each { 
					displayCategoryOptions(it, value, depth+1);
				}
			}
		}
	}
	
	def includeIndent = { howMany ->
		def indent = ""
		while ( howMany-- > 0 ) {
			indent += "-";
		}
		indent
	}
}
