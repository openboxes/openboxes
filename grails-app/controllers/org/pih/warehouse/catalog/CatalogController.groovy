package org.pih.warehouse.catalog

import org.pih.warehouse.Product;

class CatalogController {

	def list = { 
		
		[products : Product.list(params)]
		
	}
	
}
