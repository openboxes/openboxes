package org.pih.warehouse

import org.pih.warehouse.product.Category;

class AnotherMenuTagLib {
	
	static result = "";
	
	
	def menu = { attrs ->	 		
		Category rootCategory = new Category(name: "[root]");
		rootCategory.categories = Category.createCriteria().list {  isNull("parentCategory") }
		
		out << "<ul class='parentCategory'>";
		menuHtml(rootCategory, 1);
		out << "</ul>";
	}
	
	def menuHtml = { node, depth ->
		if (node) {
			if (node.id) { 	    		 
				
				def breadcrumbs = "";
				def parentList = node.parents
				parentList.each { 
					breadcrumbs += it
					if (it != parentList.last()) { 
						breadcrumbs += "&nbsp;&rsaquo;&nbsp;"
					}
				}
				
				out << includeIndent(depth) + 
						"<li><a href=\"#\" id=\"" + node.id + "\" name=\"" + breadcrumbs + "\" class=\"selectableCategory\">" + node.name + "</a></li>";
			}
			if (node.categories) {
				out << includeIndent(depth) + "<li><ul class='childCategory'>"
				node.categories.each { 
					menuHtml(it, depth+1);
				} 
				out << includeIndent(depth) + "</ul></li>"
			}
		}
	}	 
	
	
	
	def includeIndent = { howMany ->
		def indent = ""
		while ( howMany-- > 0 ) {
			indent+="\t";
		}
		indent
	}
}
