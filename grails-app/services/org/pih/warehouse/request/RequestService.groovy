package org.pih.warehouse.request

import java.util.Date;
import java.util.Set;

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.LocationType;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.receiving.Receipt;
import org.pih.warehouse.receiving.ReceiptItem;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentException;
import org.pih.warehouse.shipping.ShipmentItem;

class RequestService {

	boolean transactional = true
	
	def shipmentService;

	/**
	 * 
	 * @param location
	 * @return
	 */
	List<Request> getIncomingRequests(Location location) { 
		return Request.findAllByDestination(location)
	}

	
	/**
	 * 
	 * @param location
	 * @return
	 */
	List<Request> getOutgoingRequests(Location location) { 
		//return Request.findAllByOriginAndStatus(location, RequestStatus.REQUESTED)
		return Request.findAllByOrigin(location)	
	}
	
	/**
	 * 
	 * @return
	 */
	List<Location> getSuppliers() { 
		def suppliers = []
		LocationType supplierType = LocationType.findByName("Supplier");
		if (supplierType) { 
			suppliers = Location.findAllByLocationType(supplierType);
		}
		return suppliers;
		
	}
	
	
	/**
	 * 
	 * @param requestItems
	 * @param requestItem
	 * @return
	 */
	RequestItem getNextRequestItem(List requestItems, RequestItem requestItem) { 
		return getRequestItemAt(requestItems, requestItem, +1);
	}
	
	/**
	 * 
	 * @param requestItems
	 * @param requestItem
	 * @return
	 */
	RequestItem getPreviousRequestItem(List requestItems, RequestItem requestItem) {
		return getRequestItemAt(requestItems, requestItem, -1);
	}
	
	/**
	 * 
	 * @param requestItems
	 * @param requestItem
	 * @param direction
	 * @return
	 */
	RequestItem getRequestItemAt(List requestItems, RequestItem requestItem, int direction) { 
		return requestItems[requestItems.indexOf(requestItem)+direction];	
	}

	
	/**
	 * 
	 * @param id
	 * @param recipientId
	 * @return
	 */
	RequestCommand getRequest(Integer id, Integer recipientId) { 
		def requestCommand = new RequestCommand();
		
		def requestInstance = Request.get(id)
		if (!requestInstance)
			throw new Exception("Unable to locate request with ID " + id)
			
		if (recipientId) {
			requestCommand.recipient = Person.get(recipientId)
		}
		
		requestCommand.origin = Location.get(requestInstance?.origin?.id)
		requestCommand.destination = Location.get(requestInstance?.destination?.id)
		requestCommand.requestedBy = Person.get(requestInstance?.requestedBy?.id)
		requestCommand.dateRequested = requestInstance?.dateRequested
		requestCommand.request = requestInstance;
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
		requestCommand.fulfillItems = new ArrayList();
		
		return requestCommand;
	}
	
	/**
	 * 
	 * @param requestCommand
	 * @return
	 */
	RequestCommand saveRequestShipment(RequestCommand requestCommand) { 
		def shipmentInstance = new Shipment()
		def shipments = requestCommand?.request?.shipments();
		def numberOfShipments = (shipments) ? shipments?.size() + 1 : 1;
		
		shipmentInstance.name = requestCommand?.request?.description + " - " + "Shipment #"  + numberOfShipments 
		shipmentInstance.shipmentType = requestCommand?.shipmentType;
		shipmentInstance.origin = requestCommand?.request?.origin;
		shipmentInstance.destination = requestCommand?.request?.destination;		
		shipmentInstance.expectedDeliveryDate = requestCommand?.deliveredOn;
		shipmentInstance.expectedShippingDate = requestCommand?.shippedOn;
		
		requestCommand?.shipment = shipmentInstance
		requestCommand?.requestItems.each { requestItemCommand ->
			
			// Ignores any null request items and makes sure that the request item has a product and quantity
			if (requestItemCommand && requestItemCommand.productReceived && requestItemCommand?.quantityReceived) {
				def shipmentItem = new ShipmentItem();
				shipmentItem.lotNumber = requestItemCommand.lotNumber
				shipmentItem.expirationDate = requestItemCommand.expirationDate
				shipmentItem.product = requestItemCommand.productReceived;
				shipmentItem.quantity = requestItemCommand.quantityReceived;
				shipmentItem.recipient = requestCommand?.recipient;
				shipmentInstance.addToShipmentItems(shipmentItem)
				
				def requestShipment = new RequestShipment(shipmentItem:shipmentItem, requestItem:requestItemCommand?.requestItem)
				shipmentItem.addToRequestShipments(requestShipment)
				requestItemCommand?.requestItem.addToRequestShipments(requestShipment)
			}
		}
		
		// Validate the shipment and save it if there are no errors
		if (shipmentInstance.validate() && !shipmentInstance.hasErrors()) { 
			shipmentService.saveShipment(shipmentInstance);
		}
		else { 
			log.info("Errors with shipment " + shipmentInstance?.errors)
			throw new ShipmentException(message: "Validation errors on shipment ", shipment: shipmentInstance)
		}
		
		
		
		// Send shipment, receive shipment, and add 
		if (shipmentInstance) { 
			// Send shipment 
			log.info "Sending shipment " + shipmentInstance?.name
			shipmentService.sendShipment(shipmentInstance, "", requestCommand?.currentUser, requestCommand?.currentLocation, requestCommand?.shippedOn, [] as Set);
						
			// Receive shipment
			log.info "Receiving shipment " + shipmentInstance?.name
			Receipt receiptInstance = shipmentService.createReceipt(shipmentInstance, requestCommand?.deliveredOn)
			
			// FIXME 
			// receiptInstance.validate() && !receiptInstance.hasErrors()
			if (!receiptInstance.hasErrors() && receiptInstance.save()) { 
				shipmentService.receiveShipment(shipmentInstance, "", requestCommand?.currentUser, requestCommand?.currentLocation);
			}
			else { 
				throw new ShipmentException(message: "Unable to save receipt ", shipment: shipmentInstance)
			}
			
			// Once the request has been completely received, we set the status to RECEIVED
			if (requestCommand?.request && requestCommand?.request?.isComplete()) { 
				requestCommand?.request.status = RequestStatus.RECEIVED;
				saveRequest(requestCommand?.request);
			}
		}
		return requestCommand;
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	Request saveRequest(Request request) { 		
		if (!request.hasErrors() && request.save()) {
			return request;
		}
		else {
			throw new RequestException(message: "Unable to save request due to errors", request: request)
		}
	}
	
}
