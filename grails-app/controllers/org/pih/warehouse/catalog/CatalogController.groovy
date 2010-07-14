package org.pih.warehouse.catalog

import org.pih.warehouse.product.Product;

class CatalogController {

	def list = { 
		
		[products : Product.list(params)]
		
	}
	
}
