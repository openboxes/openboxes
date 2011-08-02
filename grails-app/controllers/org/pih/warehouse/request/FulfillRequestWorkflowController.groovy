package org.pih.warehouse.request

import grails.util.GrailsUtil;
import org.pih.warehouse.core.Address;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.ReceiptException;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentException;
import org.springframework.dao.DataIntegrityViolationException;

class FulfillRequestWorkflowController {

	def requestService;
	
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
				flow.request = requestCommand.request;
				flow.requestItems = requestCommand.requestItems 

				if (params.skipTo) {
					if (params.skipTo == 'enterShipmentDetails') return enterShipmentDetails()
					else if (params.skipTo == 'processRequestItems') return processRequestItems()
					else if (params.skipTo == 'confirmRequestReceipt') return confirmRequestReceipt()
				}				
				return success()
			}
			on("success").to("enterShipmentDetails")
			on("enterShipmentDetails").to("enterShipmentDetails")
			on("processRequestItems").to("processRequestItems")
			on("confirmRequestReceipt").to("confirmRequestReceipt")
		}
		enterShipmentDetails {
			on("next") { RequestCommand cmd ->
				flow.requestCommand = cmd
				if (flow.requestCommand.hasErrors()) {
					return error() 	
				}
				log.info("setting request command for process request items " + flow.requestItems)
				cmd.requestItems = flow.requestItems
				[requestCommand : cmd]
							
			}.to("processRequestItems")
			on("cancel").to("finish")
			on("error").to("enterShipmentDetails")
			on("enterShipmentDetails").to("enterShipmentDetails")
			on("processRequestItems").to("processRequestItems")
			on("confirmRequestReceipt").to("confirmRequestReceipt")
		}
		processRequestItems {			
			on("next") { RequestItemListCommand command ->
				flow.requestListCommand = command
				flow.requestItems = command.requestItems				
				if (command.hasErrors()) {					
					return error()
				}
			}.to("confirmRequestReceipt")
			
			on("back") { RequestItemListCommand command ->
				flow.requestListCommand = command
				flow.requestItems = command.requestItems				
				if (command.hasErrors()) {					
					return error()
				}
			}.to("enterShipmentDetails")
			
			on("cancel").to("finish")
			//on("error").to("processRequestItems")
			on("enterShipmentDetails").to("enterShipmentDetails")
			on("processRequestItems").to("processRequestItems")
			on("confirmRequestReceipt").to("confirmRequestReceipt")
		}
		confirmRequestReceipt  {
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
			on("back").to("processRequestItems")
			on("error").to("confirmRequestReceipt")
			//on(Exception).to("handleError")
			//on("success").to("finish")
			on("enterShipmentDetails").to("enterShipmentDetails")
			on("processRequestItems").to("processRequestItems")
			on("confirmRequestReceipt").to("confirmRequestReceipt")
		}		
		handleError() { 
			on("enterShipmentDetails").to("enterShipmentDetails")
			on("processRequestItems").to("processRequestItems")
			on("confirmRequestReceipt").to("confirmRequestReceipt")
			
		}
		finish {
			redirect(controller:"request", action : "show", params : [ "id" : flow.request.id ?: '' ])
		}
	}

	

	
}
