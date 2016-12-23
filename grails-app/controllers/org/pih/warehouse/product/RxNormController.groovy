/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.product

import static groovyx.net.http.Method.GET
import org.pih.warehouse.core.ApiException
import groovyx.net.http.RESTClient


class RxNormController {

	def productService
	
	def index() {
		redirect(action: "list")
	}
	
	def lookupDisplayNames() {
		//[terms:productService.findRxNormDisplayNames()]
	}
	
	
	def lookupProducts(ProductSearchCommand search) {
		println "lookupProducts: " + params 
		if (search.searchTerms) { 
			try {
				search.results = productService.getNdcProduct(search.searchTerms)
				println search.results.ndcCode
				if (!search.results) { 
					println "getCode -> no results "				
					search.results = productService.findNdcProducts(search)
				}
			} catch (ApiException e) {
				flash.message = e.message
			}

		}
		[search:search]
	}
	
	def createProduct() {
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
	
	def saveProduct() {
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
	
	
	def getCode(ProductSearchCommand search) {
		println "getCode: " +  params
		search.results = productService.getNdcProduct(params.id)
		search.searchTerms = params.id
		render (view: "lookupProducts", model:[search:search])
	}
	
	def test2() {
		def client = new RESTClient("http://rxnav.nlm.nih.gov/REST/")
	//	def data = "";
		
		def rxnormApiKey = grailsApplication.config.rxnorm.api.key
		if (!rxnormApiKey) {
			throw new ApiException(message: "Your administrator must specify RxNorm API key (rxnorm.api.key) in configuration file (openboxes-config.properties).  For more information, please go to <a href='http://rxnav.nlm.nih.gov/' target='_blank'>rxnav.nlm.nih.gov</a>.")
		}

		client.request(GET) {
			uri.path = '/displaynames'
			uri.query = [ 'client_id': rxnormApiKey ]
					  
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
