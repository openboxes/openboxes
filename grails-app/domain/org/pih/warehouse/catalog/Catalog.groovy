package org.pih.warehouse.catalog

import org.pih.warehouse.core.Location;

class Catalog {

	Location store	// Should be a warehouse
	static hasMany = [ items : CatalogItem ]
	
}
