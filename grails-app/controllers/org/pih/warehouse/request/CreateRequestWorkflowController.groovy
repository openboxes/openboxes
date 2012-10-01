/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.request

import org.pih.warehouse.core.Address;
import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.picklist.Picklist;
import org.pih.warehouse.picklist.PicklistItem;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.ProductGroup;
import org.springframework.dao.DataIntegrityViolationException;

class CreateRequestWorkflowController {

	def requestService;
	def inventoryService
	
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
					requestInstance.createdBy = Person.get(session.user.id)
					requestInstance.status = RequestStatus.NEW;
					requestInstance.dateRequested = new Date();
					def warehouse = Location.get(session.warehouse.id)
					//requestInstance.destination = warehouse;
					//requestInstance.description = "Request - " + Constants.DEFAULT_DATE_FORMATTER.format(requestInstance.dateRequested);
					
					flow.requestInstance = requestInstance;
				}
				
				
				if (params.skipTo) {
					if (params.skipTo == 'details') return success()
					else if (params.skipTo == 'items') return addRequestItems()
					//else if (params.skipTo == 'confirm') return confirmRequest()
					
				}
				
				return success()
			}
			on("success").to("enterRequestDetails")
			on("addRequestItems").to("addRequestItems")
			//on("confirmRequest").to("confirmRequest")			
		}
		
		enterRequestDetails {
			on("next") {
				log.info params
				
				flow.requestInstance.properties = params
				try {
					
					//flow.requestInstance.name = 
					//println "${warehouse.message(code:'request.name.label')} - " + flow?.requestInstance?.destination + " - " + flow?.requestInstance?.dateRequested 
					def dateRequested = "${g.formatDate(date: flow?.requestInstance?.dateRequested, format: 'dd MMM yyyy')}";
					if (!flow?.requestInstance?.name) {
						flow?.requestInstance?.name = "${warehouse.message(code: 'request.name.label', args:[flow?.requestInstance?.destination, dateRequested])}"
					}
					if (!requestService.saveRequest(flow.requestInstance)) {
						println requestInstance.errors
						return error()
					}
				} catch (Exception e) {
					log.error(e)
					return error()
				}
				
			}.to("addRequestItems")
			on("cancel").to("cancel")
			on("finish").to("finish")
			
			
			//on("enterRequestDetails").to("enterRequestDetails")
			//on("addRequestItems").to("addRequestItems")
			//on("mapRequestItems").to("mapRequestItems")
			//on("pickRequestItems").to("pickRequestItems")
			//on("showPicklist").to("showPicklist")
			//on("confirmRequest").to("confirmRequest")
		}

		
		addRequestItems {
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
			}.to("addRequestItems")
			
			on("editItem") { 
				def requestItem = RequestItem.get(params.id)
				if (requestItem) { 
					flow.requestItem = requestItem;
				}
			}.to("addRequestItems")
			
			
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
				
				if (!requestItem.quantity) { requestItem.quantity = 1; }				
				if (!requestItem.requestedBy) { requestItem.requestedBy = Person.get(session.user.id) }
				
				if (params.item.id) { 
					println params
					def values = params.item.id.split(":")
					def type = values[0]
					def id = values[1]
					if (type=="Product") { 
						def product = Product.get(id)
						if (product) {
							requestItem.product = product
							requestItem.description = product.name
							//requestItem.category = product.category
						}
					} 
					else if (type=="Category") { 
						def category = Category.get(id)
						if (category) {
							requestItem.category = category
							requestItem.description = category.name
						}
					}
					else if (type=="ProductGroup") { 
						def productGroup = ProductGroup.get(id)
						if (productGroup) { 
							requestItem.productGroup = productGroup
							requestItem.description = productGroup.description
							//requestItem.category = productGroup.category
						}
						// not supported yet
					} 
				}		
				else if (params.item.name) { 
					requestItem.description = params.item.name					
				}		
				else { 
					// FIXME Prevents an item from being added but does not provide a user-friendly error message 
					flash.message = "Error"
					return error();
				}
				

				if (!requestItem.validate() || requestItem.hasErrors()) { 
					flash.message = "Validation error"
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
				
			}.to("addRequestItems")
			
			on("next") {
				log.info "picklist " + params
				def location = Location.get(session.warehouse.id)
				
				flow.quantityOnHandMap = inventoryService.getQuantityByProductMap(location.inventory);
				//flow.quantityOutgoingMap = inventoryService.getOutgoingQuantityByProduct(location);
				//flow.quantityIncomingMap = inventoryService.getIncomingQuantityByProduct(location);
				
				flow.requestInstance.properties = params
				
				
				
			}.to("mapRequestItems")

			on("cancel").to("cancel")
			on("finish").to("enterRequestDetails")
			on("error").to("addRequestItems")
		}
		
		
		mapRequestItems {
			on("back") {
				
			}.to("addRequestItems")
			
			on("next") {
				log.info "pickRequestItems " + params
				def requestInstance = new Request(params);
				
				flow.requestInstance.properties = params
				
				flow.requestInstance.requestItems.each { requestItem ->
					println "requestItem " + requestItem.displayName() + " " + requestItem.product
				}
				//requestInstance.properties = params
				//flow.requestInstance.properties = params

			}.to("pickRequestItems")

		}
		pickRequestItems {
			on("mapRequestItem") {
				
			}.to("mapRequestItems")

			on("back") {
				
			}.to("addRequestItems")
			
			
			on("deletePicklistItem") {
				log.info "Delete pick list item " + params
				
				PicklistItem picklistItem = PicklistItem.get(params.picklistItem.id)
				def picklist = picklistItem.picklist;				
				picklist.removeFromPicklistItems(picklistItem)
				picklistItem.delete()
				picklist.save(flush:true)
				
				[picklistInstance:picklist]
			}.to("pickRequestItems")
			
			on("pickRequestItem") {
				log.info "Pick request item " + params
				
				def picklistInstance = Picklist.findByRequest(flow.requestInstance) 
				if (!picklistInstance) { 
					picklistInstance = new Picklist();
					picklistInstance.request = flow.requestInstance
					picklistInstance.picker = Person.get(session.user.id)
					picklistInstance.datePicked = new Date();
				}
				PicklistItem picklistItem = new PicklistItem(params)
				println picklistItem.requestItem.id + " " + picklistItem.inventoryItem + " " + picklistItem.quantity + " " 				
				

				def location = Location.get(session.warehouse.id)
				def quantityOnHand = inventoryService.getQuantity(location.inventory, picklistItem.inventoryItem)				
				if (picklistItem.quantity > quantityOnHand) { 
					picklistInstance.errors.reject("picklistItem.quantity.invalid","Quantity to pick must be less than quantity on hand.");
				}
				else { 
					picklistInstance.addToPicklistItems(picklistItem)
					picklistInstance.save(flush:true)
				}
				[picklistInstance:picklistInstance]
				
			}.to("pickRequestItems")
			
			
			on("next") {
				log.info "confirm " + params
				//flow.requestInstance.properties = params
				def picklist = Picklist.findByRequest(flow.requestInstance)
				[picklist : picklist]

			}.to("showPicklist")
			

		}
		
		showPicklist { 
			on("back").to("pickRequestItems")
			on("showPicklist").to("showPicklist")
			on("next") { 
				log.info "confirm " + params
				def picklist = Picklist.findByRequest(flow.requestInstance)
				[picklist : picklist]

			}.to("confirmRequest")
		}
		confirmRequest { 
			on("back").to("showPicklist")
			
			
			on("save") {	
				println "\nSAVE picklist " + params
				//def picklist = Picklist.get(params.id)				
				
				def picklist = new Picklist();
				picklist.properties = params
				
				picklist.picklistItems.each { 
					println it.inventoryItem.id  +  " " + it.quantity + " " + it.comment
					
				}
				picklist.save();
				
				[picklist:picklist]
								
			}.to("confirmRequest")
			
			
			on("finish") {
				log.info "confirm request " + params
				//flow.requestInstance.properties = params
				flow.requestInstance.status = RequestStatus.PICKED
				flow.requestInstance.save();
				
			}.to("finish")
		}
		
		finish {
			redirect(controller:"request", action : "show", params : [ "id" : flow.requestInstance.id ?: '' ])			
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
