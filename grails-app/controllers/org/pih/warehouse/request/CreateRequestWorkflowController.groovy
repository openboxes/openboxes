package org.pih.warehouse.request

import org.pih.warehouse.core.Address;
import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.springframework.dao.DataIntegrityViolationException;

class CreateRequestWorkflowController {

	def requestService;
	
	def index = { redirect(action:"createRequest") }
	def createRequestFlow = {		
		
		start {
			action {
				log.info("Starting create request workflow " + params)
				// create a new request instance if we don't have one already
				if (params.id) {
					flow.requestInstance = Request.get(params.id)
				}
				else {
					def requestInstance = new Request();
					requestInstance.requestedBy = Person.get(session.user.id)
					requestInstance.status = RequestStatus.NOT_REQUESTED;
					requestInstance.dateRequested = new Date();
					def warehouse = Location.get(session.warehouse.id)
					requestInstance.destination = warehouse;
					//requestInstance.description = "Request - " + Constants.DEFAULT_DATE_FORMATTER.format(requestInstance.dateRequested);
					
					flow.requestInstance = requestInstance;
				}
				
				
				if (params.skipTo) {
					if (params.skipTo == 'details') return success()
					else if (params.skipTo == 'items') return showRequestItems()
					//else if (params.skipTo == 'confirm') return confirmRequest()
					
				}
				
				return success()
			}
			on("success").to("enterRequestDetails")
			on("showRequestItems").to("showRequestItems")
			//on("confirmRequest").to("confirmRequest")			
		}
		
		enterRequestDetails {
			on("next") {
				log.info params
				
				flow.requestInstance.properties = params
				try {
					if (!requestService.saveRequest(flow.requestInstance)) {
						return error()
					}
				} catch (Exception e) {
					return error()
				}
				
			}.to("showRequestItems")
			on("cancel").to("cancel")
			on("finish").to("finish")
		}

		
		showRequestItems {
			on("back") { 
				log.info "saving items " + params
				flow.requestInstance.properties = params
				if (!requestService.saveRequest(flow.requestInstance)) {
					return error()
				}

			}.to("enterRequestDetails")
			
			on("deleteItem") { 
				log.info "deleting an item " + params
				def requestItem = RequestItem.get(params.id)
				if (requestItem) { 
					flow.requestInstance.removeFromRequestItems(requestItem);
					requestItem.delete();
				}
			}.to("showRequestItems")
			
			on("editItem") { 
				def requestItem = RequestItem.get(params.id)
				if (requestItem) { 
					flow.requestItem = requestItem;
				}
			}.to("showRequestItems")
			
			on("addItem") {
				log.info "adding an item " + params
				if(!flow.requestInstance.requestItems) flow.requestInstance.requestItems = [] as HashSet
				
				def requestItem = RequestItem.get(params?.requestItem?.id)
				if (requestItem) { 
					requestItem.properties = params
				} 
				else { 
					requestItem = new RequestItem(params);
				}				
				
				requestItem.requestedBy = Person.get(session.user.id)
				
				if (params?.product?.id && params?.category?.id) { 
					log.info("error with product and category")
					requestItem.errors.rejectValue("product.id", "Please choose a product OR a category OR enter a description")
					flow.requestItem = requestItem
					return error()
				}				
				else if (params?.product?.id) { 
					def product = Product.get(params?.product?.id)
					if (product) { 
						requestItem.description = product.name
						requestItem.category = product.category
					}
				}
				else if (params?.category?.id) { 
					def category = Category.get(params?.category?.id) 
					if (category) {
						requestItem.description = category.name
						requestItem.category = category
					}
				}
				else if (params?.description) { 
					requestItem.description = params.description
				}
				else { 
					// FIXME Prevents an item from being add but does not provide a user-friendly error message 
					return error();
				}
				
				if (!requestItem.validate() || requestItem.hasErrors()) { 
					flow.requestItem = requestItem
					return error();
				}
				
				
				flow.requestInstance.addToRequestItems(requestItem);
				log.info ("Request item " + requestItem.description + " " + requestItem.product + " ")
				if (!requestService.saveRequest(flow.requestInstance)) {
					log.info("error")
					return error()
				}
				
				// Need to clear request item because we use this for editing items
				flow.requestItem = null
				
			}.to("showRequestItems")
			
			
			
			on("next") {
				log.info "confirm request " + params
				flow.requestInstance.properties = params
				
				log.info("request " + flow.requestInstance)
			

					
			}.to("finish")

			on("cancel").to("cancel")
			on("finish").to("enterRequestDetails")
			on("error").to("showRequestItems")
		}
		
		
		
		finish {
			
			action {
				log.info("Finishing workflow, save request object " + flow.requestInstance)
				def request = flow.requestInstance;

				try {
					
					if (!requestService.saveRequest(flow.requestInstance)) {
						return error()
					}
					else { 
						return success()
					} 
					
				} catch (DataIntegrityViolationException e) {
					log.info ("data integrity exception")
					return error();
				}
			}
			on("success").to("showRequest")
		}
		cancel { 
			redirect(controller:"request", action: "list")
		}
		showRequest { 
			redirect(controller:"request", action : "show", params : [ "id" : flow.requestInstance.id ?: '' ])
		}
		
		handleError()
	}
}
