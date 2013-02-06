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

import grails.validation.ValidationException;

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.LocationType;
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.Inventory;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.TransactionEntry;
import org.pih.warehouse.inventory.TransactionType;
import org.pih.warehouse.requisition.Requisition;
import org.pih.warehouse.picklist.Picklist;
import org.pih.warehouse.product.Product

class RequisitionService {

	boolean transactional = true

	def shipmentService;
	def inventoryService;

	def completeInventoryTransfer(Requisition requisition, String comments) { 
		
		// Make sure a transaction has not already been created for this requisition
		def outboundTransaction = Transaction.findByRequisition(requisition)
		if (outboundTransaction) { 
			outboundTransaction.errors.reject("Cannot create multiple outbound transaction for the same requisition")
			throw new ValidationException("Cannot complete inventory transfer", outboundTransaction.errors)
		}
		
		// If an outbound transaction was not found, we create a new one
		if (!outboundTransaction) {
			// Create a new transaction
			outboundTransaction = new Transaction();
			outboundTransaction.transactionNumber = inventoryService.generateTransactionNumber()
			outboundTransaction.transactionDate = new Date();
			outboundTransaction.requisition = requisition
			outboundTransaction.destination = requisition.origin
			outboundTransaction.inventory = requisition?.destination.inventory
			outboundTransaction.comment = comments
			outboundTransaction.transactionType = TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID)
		}
		
		// where the requisition came from is where the stock will be sent
		//if (!requisition.origin) { 
		//	outboundTransaction.errors.reject("Must have to location")
		//	throw new ValidationException("Cannot complete inventory transfer", outboundTransaction.errors)
		//}
		// where the requisition was processed is where the stock will be sent from
		//if (!requisition?.destination) { 
		//	outboundTransaction.errors.reject("Must have from location")
		//	throw new ValidationException("Cannot complete inventory transfer", outboundTransaction.errors)
		//}
		
		
		def picklist = Picklist.findByRequisition(requisition)
		if (picklist) {			
			picklist.picklistItems.each { picklistItem ->				
				def transactionEntry = new TransactionEntry();
				transactionEntry.inventoryItem = picklistItem.inventoryItem;
				transactionEntry.quantity = picklistItem.quantity;
				outboundTransaction.addToTransactionEntries(transactionEntry)				
			}
			// Not sure if this needs to be done here
			//outboundTransaction.save(flush:true)
			
			if (!inventoryService.saveLocalTransfer(outboundTransaction)) {
				throw new ValidationException("Unable to save local transfer", outboundTransaction.errors)
			}
			else {
				requisition.status = RequisitionStatus.ISSUED
				requisition.save(flush:true) 
			}
	
		}
		else { 
			requisition.errors.reject("requisition.picklist.mustHavePicklist")
			throw new ValidationException("Could not find a picklist associated with this requisition", requisition.errors)
		}
		
		return outboundTransaction
		
	}	

	Requisition saveRequisition(Map data, Location userLocation) {

		def itemsData = data.requisitionItems ?: []
		data.remove("requisitionItems")

		def requisition = Requisition.get(data.id?.toString()) ?: new Requisition(status: RequisitionStatus.CREATED)
		requisition.properties = data

		def requisitionItems = itemsData.collect{  itemData ->
			def requisitionItem = requisition.requisitionItems?.find{i -> itemData.id  && i.id == itemData.id }
			if(requisitionItem) {
				requisitionItem.properties = itemData
			}
			else{
				requisitionItem = new RequisitionItem(itemData)
				requisition.addToRequisitionItems(requisitionItem)
			}
			requisitionItem
		}

		def itemsToDelete = requisition.requisitionItems.findAll { dbItem ->
			!requisitionItems.any{ clientItem-> clientItem.id == dbItem.id}
		}
		itemsToDelete.each{requisition.removeFromRequisitionItems(it)}
		requisition.destination = userLocation
		requisition.save(flush:true)
		requisition.requisitionItems?.each{it.save(flush:true)}
		requisition
	}

	void deleteRequisition(Requisition requisition) {
		requisition.delete(flush: true)
	}

	void cancelRequisition(Requisition requisition) {
		requisition.status = RequisitionStatus.CANCELED
		requisition.save(flush: true)
	}

	void uncancelRequisition(Requisition requisition) {
		requisition.status = RequisitionStatus.OPEN
		requisition.save(flush: true)
	}

	
	
	
}
