package org.pih.warehouse.request

import grails.util.GrailsUtil;
import org.pih.warehouse.core.Address;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.inventory.Warehouse;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.ReceiptException;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentException;
import org.springframework.dao.DataIntegrityViolationException;

class FulfillRequestWorkflowController {

	def requestService;
	def inventoryService;
	
	def index = { redirect(action:"fulfillRequest") }
	def fulfillRequestFlow = {		
		start {
			action {
				log.info("Starting fulfill request workflow " + params)
				
				// create a new shipment instance if we don't have one already
				def requestCommand = new RequestCommand();
				if (params.id) {
					requestCommand = requestService.getRequest(params.id as int, session.user.id as int)
				}
				else {
					flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
					redirect(controller: "request", action: "list")
				}
				
				flow.requestCommand = requestCommand;
				flow.requestInstance = requestCommand?.request;
				flow.requestItems = requestCommand?.requestItems
				flow.requestItemList = requestCommand?.request?.requestItems as List
				
				if (params.skipTo) {
					if (params.skipTo == 'packRequestItems') return packRequestItems()
					else if (params.skipTo == 'pickRequestItems') return pickRequestItems()
					else if (params.skipTo == 'confirmRequestFulfillment') return confirmRequestFulfillment()
				}	
							
				return success()
			}
			on("success").to("pickRequestItems")
			on("pickRequestItems").to("pickRequestItems")
			on("packRequestItems").to("packRequestItems")
			on("confirmFulfillment").to("confirmFulfillment")
		}
		pickRequestItems {			
			
			on("back").to("pickRequestItems")
			on("next").to("packRequestItems")
			on("showDialog").to("showDialog")
			on("closeDialog").to("closeDialog")
			on("changeProduct").to("changeProduct")
			on("fulfillItem").to("fulfillItem")
			on("saveAndContinue").to("saveAndContinue")
			on("saveAndClose").to("saveAndClose")
			on("cancel").to("finish")
			//on("error").to("processRequestItems")
			on("packRequestItems").to("packRequestItems")
			on("pickRequestItems").to("pickRequestItems")
			on("confirmFulfillment").to("confirmFulfillment")
		}
		
			
		showDialog { 
			action { 
				log.info "show fulfillment dialog " + params
				//log.info "command " + command?.requestItem?.id + " " + command?.productReceived
				//flow.requestCommand.fulfillItems.add(command)
				def inventoryItems = [:]
				def requestItem = RequestItem.get(params?.requestItem?.id);
				if (params.direction) { 
					if (params.direction == 'next') { 
						requestItem = requestService.getNextRequestItem(flow.requestItemList, requestItem)
					}
					else if (params.direction == 'previous') { 
						requestItem = requestService.getPreviousRequestItem(flow.requestItemList, requestItem)
					}
				}
				def nextRequestItem = requestService.getNextRequestItem(flow.requestItemList, requestItem);
				def previousRequestItem = requestService.getPreviousRequestItem(flow.requestItemList, requestItem)

				def warehouse = Warehouse.get(session.warehouse.id);
				if (warehouse.inventory) {
					inventoryItems =
						inventoryService.getQuantityByInventoryAndProduct(warehouse.inventory, requestItem.product);
				}
				else {
					throw new RuntimeException("Warehouse does not have an associated inventory")
				}
				
				[	
					product: requestItem.product,
					requestItem: requestItem, 
					inventoryItems: inventoryItems, 
					nextRequestItem:nextRequestItem, 
					previousRequestItem: previousRequestItem, 
					showDialog: Boolean.TRUE]
			}
			on("success").to "pickRequestItems"
			on(Exception).to "handleError"
		}
		changeProduct {
			action { RequestItemCommand command ->
				log.info "change product: " + params
				def product = Product.get(params?.product?.id) 
				def warehouse = Warehouse.get(session.warehouse.id)
				def inventoryItems =
					inventoryService.getQuantityByInventoryAndProduct(warehouse.inventory, product);
				[product: product, inventoryItems: inventoryItems, showDialog: Boolean.TRUE]
			}
			on("success").to "pickRequestItems"
			on(Exception).to "handleError"
		}
		saveAndContinue {
			action { RequestItemListCommand command ->
				flash.message = ${warehouse.message(code: 'request.fulfilledItem.message')}
				log.info "fulfill item and continue: " + params
				log.info "fulfill items " + command.requestItems
				[showDialog: Boolean.TRUE]
			}
			on("success").to "pickRequestItems"
			on(Exception).to "handleError"
		}
		saveAndClose { 
			action { 
				log.info "fulfill item and close: " + params
				flash.message = ${warehouse.message(code: 'request.fulfilledItem.message')}
				
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
		
		
		packRequestItems {
			on("back").to("pickRequestItems")			
			on("next") { RequestCommand cmd ->
				flow.requestCommand = cmd
				if (flow.requestCommand.hasErrors()) {
					return error() 	
				}
				log.info("setting request command for process request items " + flow.requestItems)
				cmd.requestItems = flow.requestItems
				[requestCommand : cmd]
							
			}.to("confirmFulfillment")
			on("cancel").to("finish")
			on("error").to("packRequestItems")
			on("pickRequestItems").to("pickRequestItems")
			on("packRequestItems").to("packRequestItems")
			on("confirmFulfillment").to("confirmFulfillment")
		}
		confirmRequestFulfillment  {
			on("submit") { 
				def requestCommand = flow.requestCommand;
				requestCommand.requestItems = flow.requestItems;
				requestCommand.currentUser = User.get(session.user.id)
				requestCommand.currentLocation = Location.get(session.warehouse.id)
				try {
					requestService.saveRequestShipment(requestCommand)
				}
				catch (ShipmentException se) {
					flow.shipment = se?.shipment;
					flow.receipt = se?.shipment?.receipt;
					return error();
				}
				catch (ReceiptException re) {
					flow.receipt = re.receipt;
					return error();
				}
				catch (RequestException oe) {
					flow.request = oe.request;
					return error();
				}
				// RuntimeExceptions should propagate to the UI
				//catch (RuntimeException e) {
				//	flow.requestCommand = requestCommand
				//	return error();
				//}
				log.info(">>>>>>>>>>>>> Success!!!")
				success()
				
			}.to("finish")
			on("cancel").to("finish")
			on("back").to("packRequestItems")
			on("error").to("confirmFulfillment")
			//on(Exception).to("handleError")
			//on("success").to("finish")
			on("pickRequestItems").to("pickRequestItems")
			on("packRequestItems").to("packRequestItems")
			on("confirmFulfillment").to("confirmFulfillment")
		}		
		handleError() { 
			on("pickRequestItems").to("pickRequestItems")
			on("packRequestItems").to("packRequestItems")
			on("confirmFulfillment").to("confirmFulfillment")
			
		}
		finish {
			redirect(controller:"request", action : "show", params : [ "id" : flow.request.id ?: '' ])
		}
	}

	

	
}
