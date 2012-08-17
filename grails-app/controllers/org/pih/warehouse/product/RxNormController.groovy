package org.pih.warehouse.product;

import groovyx.net.http.RESTClient
import static groovyx.net.http.Method.GET
import grails.converters.XML

class RxNormController {

	def productService
	
	def index = { 
		redirect(action: "list")
	}
	
	def lookupDisplayNames = { 
		//[terms:productService.findRxNormDisplayNames()]
	}
	
	
	def lookupProducts = { ProductSearchCommand search ->
		println "lookupProducts: " + params 
		if (search.searchTerms) { 
			search.results = productService.getNdcProduct(search.searchTerms)
			println search.results.ndcCode
			if (!search.results) { 
				println "getCode -> no results "
				search.results = productService.findNdcProducts(search)
			}
		}
		[search:search]
	}
	
	def createProduct = { 
		def results = productService.getNdcProduct(params.id)
		if (!results) { 
			def search = new ProductSearchCommand();
			flash.message = "No results"
			render (view: "lookupProducts", model:[search:search])
		}
		else { 
			def product = results[0]			
			render (view: "createProduct", model:[product:product])
		}		
	}
	
	def saveProduct = { 
		println "save product: " + params
		
		def product = new Product(params)
		if (!product.hasErrors() && product.save(flush:true)) { 
			println "Saved product"
			flash.message = "Successfully created new product for '${product.name}'"
		}
		else { 
			println "Errors " + product.errors
			flash.message = "There was an error creating a new product for '${product.name}'"			
		}
		
		redirect(action: "lookupProducts")
	}
	
	
	def getCode = { ProductSearchCommand search ->		
		println "getCode: " +  params
		search.results = productService.getNdcProduct(params.id)
		search.searchTerms = params.id
		render (view: "lookupProducts", model:[search:search])
	}
	
	def test2 = { 
		def client = new RESTClient("http://rxnav.nlm.nih.gov/REST/")
		def data = "";
		client.request(GET) {
			uri.path = '/displaynames'
			uri.query = [ 'client_id': 'bff71b0439e75797f6af27b220eefe7b9b0b989d' ]
					  
			println "Response: " + response.status
			
			response.success = { resp, json ->
				println 'request success '
			}
			/*
			response.failure = { resp ->
				println 'request failed '
				println resp
			}*/
		}
		
	}

	
}
