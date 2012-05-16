package org.pih.warehouse.fulfillment

import org.pih.warehouse.core.Person;
import org.pih.warehouse.request.Request;
import org.pih.warehouse.request.RequestCommand;
import org.pih.warehouse.request.RequestItemCommand;

class FulfillmentService {

    boolean transactional = true

	
	/**
	 * Adds a fulfillment item to a fulfillment object and saves the parent.
	 * @param fulfillment
	 * @param fulfillmentItem
	 */
	void addToFulfillmentItems(Fulfillment fulfillment, FulfillmentItem fulfillmentItem) { 
		
		log.info("Request: " + fulfillment.request);
		
		fulfillment.addToFulfillmentItems(fulfillmentItem);
		fulfillment.save();
	}
	
	
	/**
	 * Returns fulfillment command object with a
	 * @param id
	 * @param fulfilledById
	 * @return
	 */
	FulfillmentCommand getFulfillment(String id, String fulfilledById) {
		def command = new FulfillmentCommand();

		// Make sure that the request we are trying to fulfill actually exists		
		def requestInstance = Request.get(id)
		if (!requestInstance)
			throw new Exception("Unable to proceed with fulfillment without a valid request ");
					
		// Populate the command object with the 
		command.request = requestInstance
		
		log.info("Request: " + requestInstance)
		log.info("Fulfillment: " + requestInstance.fulfillment);

		def fulfillment 
		// Use existing fulfillment object
		if (requestInstance?.fulfillment) { 
			fulfillment = requestInstance?.fulfillment
		}
		// Create a new fulfillment object
		else { 
			fulfillment = new Fulfillment();
			fulfillment.status = FulfillmentStatus.NOT_FULFILLED;
			fulfillment.dateFulfilled = new Date();
			fulfillment.fulfilledBy = Person.get(fulfilledById);
			fulfillment.request = requestInstance;
			
			// Need to set the other side of the relationship as well
			requestInstance.fulfillment = fulfillment;
		}		
		command.fulfillment = fulfillment;
				
		/*
		requestInstance?.requestItems?.each {
			def requestItemCommand = new RequestItemCommand();
			requestItemCommand.primary = true;
			requestItemCommand.requestItem = it
			requestItemCommand.type = it.type
			requestItemCommand.description = it.description
			requestItemCommand.productReceived = it.product
			requestItemCommand.quantityRequested = it.quantity;
			//requestItemCommand.quantityReceived = it.quantity
			requestCommand?.requestItems << requestItemCommand
		}
		*/
		return command;
	}
}
