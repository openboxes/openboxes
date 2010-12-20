package org.pih.warehouse.inventory

import grails.converters.JSON;

import org.pih.warehouse.core.DialogForm;
import org.pih.warehouse.core.Address;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Event;
import org.pih.warehouse.core.EventType;
import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.Container;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;
import org.pih.warehouse.shipping.ShipmentType;


class EnterInventoryWorkflowController {
	
	def mailService;
	
	def index = {
		redirect(action:'inventory')
	}
	
	
	
	def inventoryFlow = {
		start {
			action {
				log.info("inventory flow started!!!")
				/*
				if (!flow.shipmentInstance) { 
					if (params.id) { 
						flow.shipmentInstance = Shipment.get(params.id);
					} 
					else { 
						flow.shipmentInstance = new Shipment();
					}
				}
				*/
				return success();
			}
			on("success").to "enterTransactionDetails"
			on("error").to "enterTransactionDetails"
			on(Exception).to "handleError"
		}
		enterTransactionDetails {
			on("cancel").to "cancel"
			on("submit") {
					
				
				
				
			}.to "enterTransactionEntryDetails"
			on("finish").to "finish"
			on("enterTransactionDetails").to "enterTransactionDetails"
			on("enterTransactionEntryDetails").to "enterTransactionEntryDetails"			
			on("reviewTransaction").to "reviewTransaction"
			on("submitTransaction").to "submitTransaction"
			
			on("done").to "redirectToShowDetails"
			
			on("return").to "start"			
			on(Exception).to "handleError"
		}
		enterTransactionEntryDetails {
			on("cancel").to "cancel"
			on("back").to "enterShipmentDetails"
			on("submit") { 
				
				
			}.to "enterContainerDetails"
			on("enterShipmentDetails").to "enterShipmentDetails"
			on("enterTravelerDetails").to "enterTravelerDetails"
			on("enterContainerDetails").to "enterContainerDetails"
			on("reviewShipment").to "reviewShipment"
			on("sendShipment").to "sendShipment"
			on("done").to "redirectToShowDetails"
		}
		reviewTransaction  {
			
			on("cancel").to "cancel"
			on("back").to "enterContainerDetails"
			on("reviewLetter").to "reviewLetter"
			on("clear").to "clear"
			on("submit") {
				
			

			}.to "submitTransaction"
		
			on("enterTransactionDetails").to "enterTransactionDetails"
			on("enterTravelerDetails").to "enterTravelerDetails"
			on("enterContainerDetails").to "enterContainerDetails"
			on("reviewShipment").to "reviewShipment"
			on("sendShipment").to "sendShipment"
			on("done").to "redirectToShowDetails"
		}
		
		submitTransaction  {
			on("cancel").to "cancel"
			on("back").to "reviewShipment"
			on("finish").to "finish"	
			on("enterShipmentDetails").to "enterShipmentDetails"
			on("enterTravelerDetails").to "enterTravelerDetails"
			on("enterContainerDetails").to "enterContainerDetails"
			on("reviewShipment").to "reviewShipment"
			on("sendShipment").to "sendShipment"
			on("done").to "redirectToShowDetails"

			
			
		}
		finish { 
			action {
				
			}
			on("error").to "reviewShipment"
			on(Exception).to "reviewShipment"
			on("success").to "complete"
		}
		complete { 
			// renders complete.gsp 		
		}
		handleError()
		cancel {
			redirect(controller:"dashboard", action:"index")
		}
		clear {
			action {
				//flow.clear();
			}
			on("success").to "enterShipmentDetails"
			on(Exception).to "handleError"
		}
		redirectToShowDetails { 	
			//redirect(url: '/shipment/showDetails/${flowScope?.shipmentIntance?.id}')
			redirect(controller:"shipment", action:"showDetails", id: flow?.shipmentInstance?.id)			
		}
	}
}

