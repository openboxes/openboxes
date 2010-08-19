package org.pih.warehouse.donation

import java.util.Date;
import org.pih.warehouse.core.Organization;

class Donor extends Organization {
	
	
	static mapping = {
		tablePerHierarchy false
		table 'donor'
	}
	
	static constraints = {
		
	}

}
