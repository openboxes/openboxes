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
			log.debug beginTag + it + endTag 
			log.debug counter.toString();	 		
			log.debug "display children: " + it.categories;
			displayTree it.categories, "<h3>", "</h3>";
		}
	}
	
	def selectCategory = { attrs ->
		out << "<select multiple=\"true\" size=\"5\" name='" + attrs.name + "'>";
		displayCategorySelect(attrs['rootNode'], 0);
		out << "</select>";
	}
	
	def displayCategorySelect = { node, depth ->
		if (node) {
			if (node.id) {
				out << "<option value=\"" + node?.id + "\">" + includeIndent(depth) + node?.name + "</option>";
			}
			if (node.categories) {

				//out << includeIndent(depth)
				node.categories.each { 
					displayCategorySelect(it, depth+1);
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
