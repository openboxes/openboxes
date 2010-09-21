package org.pih.warehouse.core

import java.util.List;

class ListCommand {
	String category;
	List objectList;
	Integer sortOrder;	
	
	public int compareTo(def other) {
		return sortOrder <=> other?.sortOrder
		
		//return sortOrder <=> other?.sortOrder // <=> is the compareTo operator in groovy
	}
}