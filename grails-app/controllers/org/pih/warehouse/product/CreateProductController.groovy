package org.pih.warehouse.product

import java.util.Set;

import org.hibernate.exception.ConstraintViolationException;
import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.MailService;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.InventoryService;
import org.pih.warehouse.inventory.TransactionException;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.report.ReportService;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.validation.Errors;

import sun.util.logging.resources.logging;

class CreateProductController {
	
	MailService mailService
	ProductService productService
	InventoryService inventoryService
	
	def index =  { 
		redirect(action: "create")
	}
	
	
    def createFlow = {    	
    	start {
    		action {
    			//Product flow.product = new Product()
				//[product:flow.product]
    		}
    		on("success").to("search")
			on(Exception).to("error")
    	}
    		
    	search {
			on("search") { ProductSearchCommand search ->
				log.info("search: " + params)
				if (search.hasErrors()) { 
					flash.message = "Validation exception"
					flow.search = search
					return error();
				}				
				
				productService.findGoogleProducts(search)
				
				//def searchTerms = search?.searchTerms?.split(" ").toList()
				//flow.localResults = productService.findProducts(searchTerms)
				//flow.googleResults = productService.findGoogleProducts(search?.searchTerms)
							
				[ search : search ]
				
			}.to("search")
			on("search").to("search")
			on("back").to("start")
			on("cancel").to("finish")	
    	}
		results { 
			on("search") { ProductSearchCommand search ->
				log.info("results: " + params)				
				if (search.hasErrors()) { 
					flow.search = search
					return error();
				}
				
				//if (search.searchTerms) { 
					//def searchTerms = search?.searchTerms?.split(" ").toList()
					//flow.localResults = productService.findProducts(searchTerms)
					//flow.googleResults = productService.findGoogleProducts(search?.searchTerms)
				//}				
				//if (search.searchTerms) { 
					productService.findGoogleProducts(search)
				//}
				[search:search]
				
			}.to("results")
			
			on("select") {
				log.info("select: " + params)
				def product = Product.get(params.id)
				if (!product) { 
					
				
					def googleResultsMap = [:]
					flow.search.results.each { googleProduct ->
						googleResultsMap[googleProduct.googleId] = googleProduct
					}
	
					if (googleResultsMap) { 
						product = googleResultsMap[params.id]
					}
					
				}
				
				[ product : product ]
			}.to("verify")
			on("verify").to("verify")
			on("search").to("search")
			on("results").to("results")			
			on("back").to("search")
			on("next").to("verify")
			on("previousResults") { ProductSearchCommand search ->
			
				if (search.searchTerms) {
					//def searchTerms = search?.searchTerms?.split(" ")?.toList()
					search?.startIndex -= 25
					//flow.googleResults = productService.findGoogleProducts(search?.searchTerms, search?.startIndex, true)
					productService.findGoogleProducts(search)
				}
				[search : search]
			}.to("results")
			on("nextResults") { ProductSearchCommand search ->
				if (search.searchTerms) { 
					//def searchTerms = search?.searchTerms?.split(" ")?.toList()
					search?.startIndex += 25
					//flow.googleResults = productService.findGoogleProducts(search?.searchTerms, search?.startIndex, true)
					productService.findGoogleProducts(search)
				}
				[ search : search ]
			}.to("results")

			on("cancel").to("finish")
		}
		verify { 
			on("next") { ProductDetailsCommand product -> 
				
				if (product.hasErrors()) {
					flash.message = "Validation exception"
					flow.product = product
					return error();
				}
				
				//bindData(flow.product, command)
				[product : product]
			}.to("create")
			on("search").to("search")
			on("results").to("results")
			on("back").to("results")
			on("cancel").to("finish")
		}
		create { 
			on("next"){ ProductDetailsCommand product ->
				
				if (product.hasErrors()) {
					flash.message = "Validation exception"
					flow.product = product
					return error();
				}
				
				//bindData(flow.product, command)
				[product : product]
				
			}.to("complete")
			on("search").to("search")
			on("results").to("results")
			on("back").to("verify")
			on("cancel").to("finish")

		}
		complete { 
			on("next") { 
				//if (!flow.product.save()) { 
				//	flash.message = "Couldn't save product"
				//	return error()
				//}				
			}.to("finish")
			
			on("search").to("search")
			on("results").to("results")
			on("back").to("details")
			on("cancel").to("finish")
			on(Exception).to("error")
		
		}
		finish { 			
			
			//flow.product = null
			//flow.search = null
			//flow.googleResults = null
			//flow.localResults = null
			
			redirect(controller: "createProduct", action: "index")
		}
    }
}



