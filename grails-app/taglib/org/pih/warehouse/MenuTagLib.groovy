package org.pih.warehouse

class MenuTagLib {
   
	static result = "";
	
	 def displayMenu = { attrs ->	 
	 	out << "<ul>";
	 	displayMenuHtml(attrs['rootNode'], 1);
        out << "</ul>";
	 }
	 	 
	 def displayMenuHtml = { node, depth ->
	     if (node) {
	    	 if (node.id) { 	    		 
	    		 out << includeIndent(depth) + "<li><a href=\"browse?browseBy=category&categoryId=${node?.id}\">" + node.name + "</a></li>";   
	    	 }
		     if (node.categories) {
		         out << includeIndent(depth) + "<ul>"
		         node.categories.each { displayMenuHtml(it, depth+1); } 
		         out << includeIndent(depth) + "</ul>"            
		     }
	     }
	 }	 
	 
	 /**
	  * Used to make the HTML a little easier to read.
	  */
	 def includeIndent = { howMany ->
     	def indent = ""
 		while ( howMany-- > 0 ) {
 			indent+="\t";        
 		}
     	indent
	 } 	 
	 
}
