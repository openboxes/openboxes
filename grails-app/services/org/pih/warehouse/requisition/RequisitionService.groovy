/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.requisition

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.LocationType;
import org.pih.warehouse.core.Person;


import org.pih.warehouse.product.Product;


class RequisitionService {

	boolean transactional = true
	
	def shipmentService;

	/**
	 * 
	 * @param location
	 * @return
	 */
	List<Requisition> getIncomingRequests(Location location) {
		return Requisition.findAllByDestination(location).findAll { it.isPending() }
	}

	
	/**
	 * 
	 * @param location
	 * @return
	 */
	List<Requisition> getOutgoingRequests(Location location) {
		//return Request.findAllByOriginAndStatus(location, RequisitionStatus.REQUESTED)
		return Requisition.findAllByOrigin(location).findAll { it.isPending() }
	}
	
	/**
	 * 
	 * @return
	 */
	List<Location> getSuppliers() { 
		def suppliers = []
		LocationType supplierType = LocationType.findById(Constants.SUPPLIER_LOCATION_TYPE_ID);
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
	RequisitionItem getNextRequestItem(List requestItems, RequisitionItem requestItem) {
		return getRequestItemAt(requestItems, requestItem, +1);
	}
	
	/**
	 * 
	 * @param requestItems
	 * @param requestItem
	 * @return
	 */
	RequisitionItem getPreviousRequestItem(List requestItems, RequisitionItem requestItem) {
		return getRequestItemAt(requestItems, requestItem, -1);
	}
	
	/**
	 * 
	 * @param requestItems
	 * @param requestItem
	 * @param direction
	 * @return
	 */
	RequisitionItem getRequestItemAt(List requestItems, RequisitionItem requestItem, int direction) {
		return requestItems[requestItems.indexOf(requestItem)+direction];	
	}

	
	/**
	 * 
	 * @param id
	 * @param recipientId
	 * @return
	 */
	RequisitionCommand getRequest(Integer id, Integer recipientId) {
		def requestCommand = new RequisitionCommand();
		
		def requestInstance = Requisition.get(id)
		if (!requestInstance)
			throw new Exception("Unable to locate request with ID " + id)
			
		if (recipientId) {
			requestCommand.recipient = Person.get(recipientId)
		}
		
		requestCommand.origin = Location.get(requestInstance?.origin?.id)
		requestCommand.destination = Location.get(requestInstance?.destination?.id)
		requestCommand.requestedBy = Person.get(requestInstance?.requestedBy?.id)
		requestCommand.dateRequested = requestInstance?.dateRequested
		requestCommand.requisition = requestInstance;
		requestInstance?.requestItems?.each {
			def requestItemCommand = new RequisitionItemCommand();
			requestItemCommand.primary = true;
			requestItemCommand.requisitionItem = it
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
	RequisitionCommand saveRequestShipment(RequisitionCommand requestCommand) {
//		def shipmentInstance = new Shipment()
//		def shipments = requestCommand?.request?.shipments();
//		def numberOfShipments = (shipments) ? shipments?.size() + 1 : 1;
//
//		shipmentInstance.name = requestCommand?.request?.description + " - " + "Shipment #"  + numberOfShipments
//		shipmentInstance.shipmentType = requestCommand?.shipmentType;
//		shipmentInstance.origin = requestCommand?.request?.origin;
//		shipmentInstance.destination = requestCommand?.request?.destination;
//		shipmentInstance.expectedDeliveryDate = requestCommand?.deliveredOn;
//		shipmentInstance.expectedShippingDate = requestCommand?.shippedOn;
//
//		requestCommand?.shipment = shipmentInstance
//		requestCommand?.requestItems.each { requestItemCommand ->
//
//			// Ignores any null request items and makes sure that the request item has a product and quantity
//			if (requestItemCommand && requestItemCommand.productReceived && requestItemCommand?.quantityReceived) {
//				def shipmentItem = new ShipmentItem();
//				shipmentItem.lotNumber = requestItemCommand.lotNumber
//				shipmentItem.expirationDate = requestItemCommand.expirationDate
//				shipmentItem.product = requestItemCommand.productReceived;
//				shipmentItem.quantity = requestItemCommand.quantityReceived;
//				shipmentItem.recipient = requestCommand?.recipient;
//				shipmentInstance.addToShipmentItems(shipmentItem)
//
//				def requestShipment = new RequestShipment(shipmentItem:shipmentItem, requestItem:requestItemCommand?.requestItem)
//				shipmentItem.addToRequestShipments(requestShipment)
//				requestItemCommand?.requestItem.addToRequestShipments(requestShipment)
//			}
//		}
//
//		// Validate the shipment and save it if there are no errors
//		if (shipmentInstance.validate() && !shipmentInstance.hasErrors()) {
//			shipmentService.saveShipment(shipmentInstance);
//		}
//		else {
//			log.info("Errors with shipment " + shipmentInstance?.errors)
//			throw new ShipmentException(message: "Validation errors on shipment ", shipment: shipmentInstance)
//		}
//
//
//
//		// Send shipment, receive shipment, and add
//		if (shipmentInstance) {
//			// Send shipment
//			log.info "Sending shipment " + shipmentInstance?.name
//			shipmentService.sendShipment(shipmentInstance, "", requestCommand?.currentUser, requestCommand?.currentLocation, requestCommand?.shippedOn, [] as Set);
//
//			// Receive shipment
//			log.info "Receiving shipment " + shipmentInstance?.name
//			Receipt receiptInstance = shipmentService.createReceipt(shipmentInstance, requestCommand?.deliveredOn)
//
//			// FIXME
//			// receiptInstance.validate() && !receiptInstance.hasErrors()
//			if (!receiptInstance.hasErrors() && receiptInstance.save()) {
//				shipmentService.receiveShipment(shipmentInstance, "", requestCommand?.currentUser, requestCommand?.currentLocation);
//			}
//			else {
//				throw new ShipmentException(message: "Unable to save receipt ", shipment: shipmentInstance)
//			}
//
//			// Once the request has been completely received, we set the status to RECEIVED
//			if (requestCommand?.request && requestCommand?.request?.isComplete()) {
//				requestCommand?.request.status = RequisitionStatus.RECEIVED;
//				saveRequest(requestCommand?.request);
//			}
//		}
//		return requestCommand;
        return null
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	Requisition saveRequest(Requisition requestwar) {
		if (!request.hasErrors() && request.save()) {
			return request;
		}
		else {
			throw new RequisitionException(message: "Unable to save request due to errors", request: request)
		}
	}
	
	/**
	 *
	 * @param location
	 * @return
	 */
	List<Requisition> getPendingRequests(Location location) {
		def requests = Requisition.withCriteria {
			or { 
				eq("origin", location) 
				eq("destination", location) 
			}
		}			
		return requests.findAll { it.isPending() }
	}
	
	
	/**
	 * 
	 * @param location
	 * @param product
	 * @return
	 */
	List<RequisitionItem> getPendingRequestItemsWithProduct(Location location, Product product) {
		def requestItems = []
//		def requests = getPendingRequests(location);
//
//		requests.each {
//			def requestItemList = it.requestItems.findAll { it.product == product }
//			requestItemList.each { requestItems << it; }
//		}
	
		return requestItems;
	}

	/**
	 *
	 * @param location
	 * @return
	 */
	Map getIncomingQuantityByProduct(Location location) {
		return getQuantityByProduct(getIncomingRequests(location))
	}

	/**
	 *
	 * @param location
	 * @return
	 */
	Map getOutgoingQuantityByProduct(Location location) {
		return getQuantityByProduct(getOutgoingRequests(location))
	}
	
	/**
	*
	* @param shipments
	* @return
	*/
	Map getQuantityByProduct(def requests) {
		def quantityMap = [:]
		requests.each { request ->
			request.requestItems.each { requestItem ->
				def product = requestItem.product
				if (product) {
					def quantity = quantityMap[product];
					if (!quantity) quantity = 0;
					quantity += requestItem.quantity;
					quantityMap[product] = quantity
				}
			}
		}
		return quantityMap;
	}
	
		
}
