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

import grails.util.GrailsUtil;
import org.pih.warehouse.core.Address;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.fulfillment.Fulfillment;
import org.pih.warehouse.fulfillment.FulfillmentCommand;
import org.pih.warehouse.fulfillment.FulfillmentItem;
import org.pih.warehouse.fulfillment.FulfillmentStatus;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.ReceiptException;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentException;
import org.pih.warehouse.shipping.ShipmentItem;
import org.springframework.dao.DataIntegrityViolationException;

class FulfillRequestWorkflowController {

	def requestService;
	def shipmentService;
	def inventoryService;
	
	def index = { redirect(action:"fulfillRequest") }
	def fulfillRequestFlow = {		
		start {
			action {
				log.info("Starting fulfill request workflow " + params)
				
				// create a new shipment instance if we don't have one already
				def command = new FulfillmentCommand();
				if (params.id) {
					command = 
						fulfillmentService.getFulfillment(params.id, session.user.id)
				}
				else {
					flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
					redirect(controller: "request", action: "list")
				}
				
				flow.command = command;
				
				if (params.skipTo) {
					if (params.skipTo == 'previewRequest') return previewRequest()
					else if (params.skipTo == 'packRequestItems') return packRequestItems()
					else if (params.skipTo == 'pickRequestItems') return pickRequestItems()
					else if (params.skipTo == 'confirmRequestFulfillment') return confirmRequestFulfillment()
				}	
							
				return success()
			}
			on("success").to("previewRequest")
			on(Exception).to("handleError")
			
			on("previewRequest").to("previewRequest")
			on("pickRequestItems").to("pickRequestItems")
			on("packRequestItems").to("packRequestItems")
			on("confirmFulfillment").to("confirmFulfillment")
		}
		/********************* Step 0.  Preview Request ******************/
		previewRequest { 
			//on("back").to("previewRequest")
			on("next").to("pickRequestItems")
			on("cancel").to("finish")
			
			
			on("previewRequest").to("previewRequest")
			on("pickRequestItems").to("pickRequestItems")
			on("packRequestItems").to("packRequestItems")
			on("confirmFulfillment").to("confirmFulfillment")
		}
				
		/********************* Step 1.  Pack Request items ******************/
		
		pickRequestItems {			
			
			on("back").to("previewRequest")
			on("next").to("packRequestItems")
			on("showPickDialog").to("showPickDialog")
			on("closeDialog").to("closeDialog")
			on("changeProduct").to("changeProduct")
			on("fulfillItem").to("fulfillItem")
			on("saveAndContinue").to("saveAndContinue")
			on("saveAndClose").to("saveAndClose")
			on("cancel").to("finish")
			//on("error").to("processRequestItems")
			on(Exception).to("handleError")
			on("previewRequest").to("previewRequest")
			on("packRequestItems").to("packRequestItems")
			on("pickRequestItems").to("pickRequestItems")
			on("confirmFulfillment").to("confirmFulfillment")
		}
		
			
		showPickDialog { 
			action { 
				log.info "show fulfillment dialog " + params
				def requestItems = flow.command.request.requestItems as List;
				
				// Get the request item that we want to view in the dialog
				def requestItem = RequestItem.get(params?.requestItem?.id);
				/*
				if (params.direction) { 
					if (params.direction == 'next') { 
						requestItem = requestService.getNextRequestItem(requestItems, requestItem)
					}
					else if (params.direction == 'previous') { 
						requestItem = requestService.getPreviousRequestItem(requestItems, requestItem)
					}
				}
				*/

				if (!requestItem) { 
					throw new RuntimeException("Request item is invalid")
				}

				/*								
				// Get the next/previous request items 
				def nextRequestItem = requestService.getNextRequestItem(requestItems, requestItem);
				def previousRequestItem = requestService.getPreviousRequestItem(requestItems, requestItem)
				*/
				
				// Get inventory items
				def inventoryItems = [:]
				def warehouse = Location.get(session.warehouse.id);
				if (warehouse.inventory) {
					inventoryItems =
						inventoryService.getQuantityByInventoryAndProduct(warehouse.inventory, requestItem?.product);
				}
				else {
					throw new RuntimeException("Location does not have an associated inventory")
				}
				
				[	
					requestItem: requestItem, 
					product: requestItem.product,
					inventoryItems: inventoryItems, 
					//nextRequestItem: nextRequestItem, 
					//previousRequestItem: previousRequestItem, 
					showDialog: Boolean.TRUE]
			}
			on("success").to "pickRequestItems"
			on(Exception).to "handleError"
		}
		changeProduct {
			action { 
				log.info "change product: " + params
				def product = Product.get(params?.fulfillProduct.id) 
				log.info("changed product to " + product)
				
				def warehouse = Location.get(session.warehouse.id)
				def inventoryItems =
					inventoryService.getQuantityByInventoryAndProduct(warehouse.inventory, product);
				
				[product: product, inventoryItems: inventoryItems, showDialog: Boolean.TRUE]
			}
			on("success").to "pickRequestItems"
			on(Exception).to "handleError"
		}
		saveAndContinue {
			action { FulfillmentCommand command ->
				
				def fulfillment = flow.command.fulfillment;
				
				log.info "fulfill item and continue: " + params				
				def message = "${warehouse.message(code: 'request.fulfilledItem.message')}"
				command.fulfillmentItems.each { 
					log.info "fulfill item >>>>> requested item=" + it.requestItem + 
						" qty=" + it.quantity + " inventory item=" + it.inventoryItem 
					
					if (it.quantity > 0) { 
						log.info "add item to fulfillment"
						fulfillmentService.addToFulfillmentItems(fulfillment, it)
						def args = [it?.quantity, it?.inventoryItem?.lotNumber, it?.requestItem?.description]
						message += 
							"${warehouse.message(code: 'request.fulfillItem.message', args: args)}"
						
					}
				}
				
				flash.message = message
				[showDialog: Boolean.TRUE]
			}
			on("success").to "pickRequestItems"
			on(Exception).to "handleError"
		}
		saveAndClose { 
			action { 
				log.info "fulfill item and close: " + params
				flash.message = "${warehouse.message(code: 'request.fulfilledItem.message')}"
				
				[showDialog: Boolean.FALSE]
			}
			on("success").to "pickRequestItems"
			on(Exception).to "handleError"
		}
		closeDialog { 
			action { 
				log.info "close: " + params
				[showDialog: Boolean.FALSE]
			}
			on("success").to "pickRequestItems"
			on(Exception).to "handleError"
		}
		
		/********************* Step 2.  Pack Request items ******************/
		
		
		packRequestItems {
			on("back").to("pickRequestItems")			
			on("next").to("confirmFulfillment")
			
			on("showPackDialog").to("showPackDialog")
			on("saveAndContinuePackDialog").to("saveAndContinuePackDialog")
			on("saveAndClosePackDialog").to("saveAndClosePackDialog")
			on("closeDialog").to("closeDialog")
			on("error").to("packRequestItems")
			on(Exception).to("handleError")
			
			
			on("previewRequest").to("previewRequest")
			on("pickRequestItems").to("pickRequestItems")
			on("packRequestItems").to("packRequestItems")
			on("confirmFulfillment").to("confirmFulfillment")
			on("cancel").to("finish")
		}
		showPackDialog {
			action {
				def fulfillmentItem = FulfillmentItem.get(params?.fulfillmentItem?.id)
				def requestItem = RequestItem.get(params?.requestItem?.id)
				def warehouse = Location.get(session.warehouse.id)				
				def shipments = shipmentService.getOutgoingShipments(warehouse);
				log.info("shipments: " + shipments)

				
				[showPackDialog: Boolean.TRUE, fulfillmentItem: fulfillmentItem, requestItem: requestItem, shipments: shipments]
			}
			on("success").to "packRequestItems"
			on(Exception).to "handleError"
		}
		saveAndContinuePackDialog {
			action { 
				println "pack item and continue: " + params
				
				def shipment = Shipment.get(params?.shipment?.id)
				def fulfillmentItem = FulfillmentItem.get(params?.fulfillmentItem?.id);
				def requestItem = RequestItem.get(params?.requestItem?.id);
				
				println "shipment " + shipment
				println "fulfillmentItem " + fulfillmentItem
				println "requestItem " + requestItem
				
				if (fulfillmentItem && shipment && requestItem) { 
					def shipmentItem = new ShipmentItem();
					shipmentItem.expirationDate = fulfillmentItem?.inventoryItem?.expirationDate
					shipmentItem.lotNumber = fulfillmentItem?.inventoryItem?.lotNumber
					shipmentItem.product = fulfillmentItem?.inventoryItem?.product
					shipmentItem.quantity = fulfillmentItem.quantity
					shipmentItem.recipient = requestItem.requestedBy
					shipmentItem.shipment = shipment;
					// explicitly set to null so we add this to the unpacked items
					shipmentItem.container = null
					
					fulfillmentItem.addToShipmentItems(shipmentItem);
					fulfillmentItem.save(flush:true);
					
					shipment.addToShipmentItems(shipmentItem);
					shipment.save(flush:true);
					
					flash.message = "${warehouse.message(code: 'fulfillRequestWorkflow.saveAndContinuePack.message', args: [shipment?.name])}"
					
				}				
				else { 
					println "an error occurred"
					error();
				}
				
				[showPackDialog: Boolean.TRUE]
			}
			on("success").to "packRequestItems"
			on(Exception).to "handleError"
		}		
		saveAndClosePackDialog {
			action { FulfillmentCommand command ->
				log.info "fulfill item and close: " + params
				
				[showPackDialog: Boolean.FALSE]
			}
			on("success").to "packRequestItems"
			on(Exception).to "handleError"
		}
		
		/********************* Step 3.  Confirm Request Fulfillment ******************/
		
		confirmFulfillment  {
			
			on("finish") { 				
				def fulfillment = flow.command.fulfillment				
				fulfillment.status = FulfillmentStatus.FULFILLED;
				fulfillment.save();
				
				def requestInstance = flow.command.request;
				requestInstance.status = RequestStatus.FULFILLED;
				requestInstance.save();
				flash.message = "${warehouse.message(code: 'fulfillRequestWorkflow.confirmFulfillment.message')}"
				
			}.to("finish")
			on("cancel").to("finish")
			on("back").to("packRequestItems")
			on("error").to("confirmFulfillment")
			on(Exception).to("handleError")
			
			on("previewRequest").to("previewRequest")
			on("pickRequestItems").to("pickRequestItems")
			on("packRequestItems").to("packRequestItems")
			on("confirmFulfillment").to("confirmFulfillment")
		}		
		
		
		
		handleError() { 
			
			
			on("previewRequest").to("previewRequest")
			on("pickRequestItems").to("pickRequestItems")
			on("packRequestItems").to("packRequestItems")
			on("confirmFulfillment").to("confirmFulfillment")			
		}
		finish {
			
			if (flow?.command?.request?.id) { 
				redirect(controller:"request", action : "show", params : [ "id" : flow.command?.request.id ?: '' ])
			} 
			else { 
				redirect(controller: "request", action: "list")
			}
		}
	}

	

	
}
