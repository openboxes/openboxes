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
	 
}
