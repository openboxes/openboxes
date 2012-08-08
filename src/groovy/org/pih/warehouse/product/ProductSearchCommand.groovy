package org.pih.warehouse.product;

import java.io.Serializable;
import java.util.Map;

class ProductSearchCommand implements Serializable {
	String searchTerms
	Integer startIndex = 1
	Boolean spellingEnabled = true
	Integer totalResults = 0
	Integer itemsPerPage = 0
	
	List results = new ArrayList();
	Map links = new HashMap();
	
	static constraints = {
		searchTerms(nullable:true)
		startIndex(nullable:true)
		spellingEnabled(nullable:true)
		totalResults(nullable:true)
		itemsPerPage(nullable:true)	
	}
}
