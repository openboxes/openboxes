package org.pih.warehouse.order

import org.pih.warehouse.core.Address;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.Shipment;
import org.springframework.dao.DataIntegrityViolationException;

class ReceiveOrderWorkflowController {

	def orderService;
	
	def index = { redirect(action:"receiveOrder") }
	def receiveOrderFlow = {		
		start {
			action {
				log.info("Starting order workflow " + params)
				
				// create a new shipment instance if we don't have one already
				def orderCommand = new OrderCommand();
				if (params.id) {
					orderCommand = orderService.getOrder(params.id as int, session.user.id as int)
				}
				else {
					flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'order.label', default: 'Order'), params.id])}"
					redirect(controller: "order", action: "list")
				}
				
				flow.orderCommand = orderCommand;
				flow.order = orderCommand.order;
				flow.orderItems = orderCommand.orderItems 

				if (params.skipTo) {
					if (params.skipTo == 'enterShipmentDetails') return enterShipmentDetails()
					else if (params.skipTo == 'processOrderItems') return processOrderItems()
					else if (params.skipTo == 'confirmOrderReceipt') return confirmOrderReceipt()
				}				
				return success()
			}
			on("success").to("enterShipmentDetails")
			on("enterShipmentDetails").to("enterShipmentDetails")
			on("processOrderItems").to("processOrderItems")
			on("confirmOrderReceipt").to("confirmOrderReceipt")
		}
		enterShipmentDetails {
			on("next") { OrderCommand cmd ->
				flow.orderCommand = cmd
				if (flow.orderCommand.hasErrors() || !flow.orderCommand.validate()) {
					return error() 	
				}
				log.info("setting order command for process order items " + flow.orderItems)
				cmd.orderItems = flow.orderItems
				[orderCommand : cmd]
							
			}.to("processOrderItems")
			on("cancel").to("finish")
			on("error").to("enterShipmentDetails")
			on("enterShipmentDetails").to("enterShipmentDetails")
			on("processOrderItems").to("processOrderItems")
			on("confirmOrderReceipt").to("confirmOrderReceipt")
		}
		processOrderItems {
			on("next") { OrderItemListCommand cmd ->
				flow.orderItems = cmd.orderItems				
				//!flow.orderCommand.validate() ? error() : success()
				
			}.to("confirmOrderReceipt")
			
			on("back") { OrderItemListCommand cmd ->
				flow.orderItems = cmd.orderItems	
				
				
			}.to("enterShipmentDetails")
			on("cancel").to("finish")
			on("error").to("processOrderItems")
			on("enterShipmentDetails").to("enterShipmentDetails")
			on("processOrderItems").to("processOrderItems")
			on("confirmOrderReceipt").to("confirmOrderReceipt")
		}
		confirmOrderReceipt  {
			on("finish") { 
				def orderCommand = flow.orderCommand;
				orderCommand.orderItems = flow.orderItems;
				
				orderService.saveOrderShipment(orderCommand)
				
				// If the shipment was saved, let's redirect back to the order received page
				if (orderCommand?.shipment?.hasErrors() || !orderCommand?.shipment?.id) {
					error();
				}
				
				
			}.to("finish")
			
			on("cancel").to("finish")
			on("back").to("processOrderItems")
			on("error") { log.info "error during confirm order receipt" }.to("confirmOrderReceipt")
			//on(Exception).to("confirmOrderReceipt")
			//on("success").to("finish")
			on("enterShipmentDetails").to("enterShipmentDetails")
			on("processOrderItems").to("processOrderItems")
			on("confirmOrderReceipt").to("confirmOrderReceipt")
		}		
		finish {
			redirect(controller:"order", action : "show", params : [ "id" : flow.order.id ?: '' ])
		}
		handleError()
	}

	

	
}
