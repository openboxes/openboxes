package org.pih.warehouse.api

import grails.converters.JSON;
import org.pih.warehouse.product.Product;

class ApiController {


	
	def products = { 
		def products = new ArrayList();
		
		products = Product.getAll();
		def jsonProducts = [products:products]		
		
		
		render jsonProducts as JSON 		
	}
}
