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
			//on("error").to("error")
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
				
			}.to("results")
			on("search").to("search")
			on("back").to("start")
			on("cancel").to("finish")	
    	}
		error { 
			
		}
		results { 
			//on("error").to("error")
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
				if (search.searchTerms) { 
					productService.findGoogleProducts(search)
				}
				
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
				flow.product = product
				//[ product : product ]
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
			//on("error").to("error")
			on("next") { ProductDetailsCommand command -> 
				log.info("VERIFY: next")
				
				log.info("flow.product.category: " + flow.product.category)
				log.info("flow.product.description: " + flow.product.description)
				log.info("flow.product.title: " + flow.product.title)
				log.info("flow.product.gtin: " + flow.product.gtin)
				log.info("command.category: " + command.category)
				log.info("command.description: " + command.description)
				log.info("command.title: " + command.title)
				log.info("command.gtin: " + command.gtin)

				
				flow.product = command
				if (flow.product.hasErrors()) {
					flash.message = "Validation exception"
					return error();
				}
				
				//bindData(flow.product, command)
				//[product : product]
			}.to("create")
			on("search").to("search")
			on("results").to("results")
			on("back").to("results")
			on("cancel").to("finish")
		}
		create { 
			on("next"){ 

				Product productInstance = new Product()
				productInstance.category = flow.product.category
				productInstance.name = flow.product.title
				productInstance.description = flow.product.description
				productInstance.upc = flow.product.gtin
				
				if(!productInstance.hasErrors() && productInstance.save(flush: true)){	
					flash.message = "${message(code: 'success.adding.product')}"
					log.info("Saved product " + productInstance?.name + " with ID " + productInstance?.id )
					flow.productInstance = productInstance
					//return success()
				}
				else {
					log.info ("Validation errors " + productInstance.errors)
					flow.product.errors = productInstance.errors
					return error()
				}
				
			}.to("finish")
			on("search").to("search")
			on("results").to("results")
			on("back").to("verify")
			on("cancel").to("finish")

		}
		
		finish { 			
			// redirect to product edit page
			redirect(controller: "product", action: "edit", id: flow.productInstance.id)
			//redirect(controller: "createProduct", action: "index")
		}
    }
}



