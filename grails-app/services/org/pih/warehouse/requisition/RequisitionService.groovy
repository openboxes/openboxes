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

import grails.validation.ValidationException
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.picklist.Picklist

class RequisitionService {

	boolean transactional = true

	def identifierService
	def productService
	def shipmentService;
	def inventoryService;

    /**
     * Get recent requisitions
     */
    def getRequisitions() {
        return Requisition.findAllByIsTemplate(false)
    }

    /**
     * Get requisition template
     */
    def getRequisitionTemplates() {
        return Requisition.findAllByIsTemplateAndIsPublished(true, true)
    }

    def getAllRequisitions(Location destination) {
        return getRequisitions(new Requisition(destination:destination), [max: -1, offset: 0])
    }

    /**
     * Get all requisitions for the given destination.
     * @param destination
     * @return
     */
    def getRequisitions(Location destination) {
        return getRequisitions(new Requisition(destination:destination), [:])
    }


    /**
     * Get all requisitions for the given destination and origin.
     * @param destination
     * @param origin
     * @return
     */
    def getRequisitions(Location destination, Location origin) {
        return getRequisitions(new Requisition(destination:destination, origin: origin), [:])
        //return getRequisitions(destination, origin, null, null, null, null, null, null)
    }


    /**
     * Get all requisitions for the given destination and query.
     * @param destination
     * @param query
     * @param params
     * @return
     */
    def getRequisitions(Requisition requisition, Map params) {
    //def getRequisitions(Location destination, Location origin, User createdBy, RequisitionType requisitionType, RequisitionStatus status, CommodityClass commodityClass, String query, Map params) {
        //return Requisition.findAllByDestination(session.warehouse)

        def criteria = Requisition.createCriteria()

        def results = criteria.list(max:params?.max?:10,offset:params?.offset?:0) {
            and {
                // Base query needs to include the following
                or {
                    eq("isTemplate", false)
                    isNull("isTemplate")
                }
                if (requisition.destination) {
                    eq("destination", requisition.destination)
                }
                if (requisition.status) {
                    eq("status", requisition.status)
                }
                if (requisition.requestedBy) {
                    eq("createdBy.id", requisition.requestedBy.id)
                }
                if (requisition.createdBy) {
                    eq("createdBy.id", requisition.createdBy.id)
                }
                if (requisition.updatedBy) {
                    eq("createdBy.id", requisition.updatedBy.id)
                }
                if (requisition.commodityClass) {
                    eq("commodityClass", requisition.commodityClass)
                }
                if (requisition.type) {
                    eq("type", requisition.type)
                }
                if (requisition.origin) {
                    eq("origin", requisition.origin)
                }
                if (params?.q) {
                    or {
                        ilike("name", "%" + params?.q + "%")
                        ilike("requestNumber", "%" + params?.q + "%")
                    }
                }
                if (params?.sort) {
                    order(params?.sort, params?.order?:'desc')
                }
                //maxResults(10)
                //eq("isPublished", false)
            }
        }

        return results

    }


    /**
     * Save the requisition
     *
     * @param requisition
     * @return
     */
    def saveRequisition(Requisition requisition) {
        if (!requisition.requestNumber) {
            requisition.requestNumber = identifierService.generateRequisitionIdentifier()
        }
        //requisition.name = generateRequisitionName(requisition)
        requisition.save(flush: true)
        return requisition
    }

    /**
     * Issues a requisition, which should create a new transfer in/out transaction for all requisition items
     * in the requisition.
     *
     * @param requisition
     * @param comments
     * @return
     */
	def issueRequisition(Requisition requisition, String comments) {
		
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
			// requisition origin is where the requisition originated from (the destination of stock transfer)
			outboundTransaction.destination = requisition.origin
			// requisition inventory is the location where the requisition is placed
			outboundTransaction.inventory = requisition?.destination?.inventory
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

    void rollbackRequisition(Requisition requisition) {

        try {
            requisition.status = RequisitionStatus.PENDING
            requisition.transactions.each {
                it.delete();
            }

        } catch (Exception e) {
            throw new RuntimeException(e)
        }
    }


	Requisition saveRequisition(Map data, Location userLocation) {

		def itemsData = data.requisitionItems ?: []
		data.remove("requisitionItems")

		def requisition = Requisition.get(data.id?.toString()) ?: new Requisition(status: RequisitionStatus.CREATED)

        try {
            requisition.properties = data
            if (!requisition.requestNumber) {
                requisition.requestNumber = identifierService.generateRequisitionIdentifier()
            }
            def requisitionItems = itemsData.collect{  itemData ->
                println "itemData: " + itemData
                def requisitionItem = requisition.requisitionItems?.find{i -> itemData.id  && i.id == itemData.id }
                if(requisitionItem) {
                    requisitionItem.properties = itemData
                }
                else{
                    requisitionItem = new RequisitionItem(itemData)
                    requisition.addToRequisitionItems(requisitionItem)
                }
                println "package: " + requisitionItem?.productPackage
                println "json: " + requisitionItem.toJson()

                requisitionItem
            }

            def itemsToDelete = requisition.requisitionItems.findAll { dbItem ->
                !requisitionItems.any{ clientItem-> clientItem.id == dbItem.id}
            }
            itemsToDelete.each{requisition.removeFromRequisitionItems(it)}
            requisition.destination = userLocation
            requisition.save(flush:true)
            println "Requisition: " + requisition
            println "Errors: " + requisition.errors

            requisition.requisitionItems?.each{it.save(flush:true)}
        } catch (Exception e) {
            log.error("Error saving requisition: " + e.message, e);

        }
		return requisition
	}

	void deleteRequisition(Requisition requisition) {
		requisition.delete(flush: true)
	}

	void cancelRequisition(Requisition requisition) {
		requisition.status = RequisitionStatus.CANCELED
		requisition.save(flush: true)
	}

	void undoCancelRequisition(Requisition requisition) {
		requisition.status = RequisitionStatus.PENDING
		requisition.save(flush: true)
	}

    /*
    def changeQuantity(Integer newQuantity, String reasonCode, String comments) {
        // And then create a new requisition item for the remaining quantity (if not 0)
        if (newQuantity) {
            // Cancel the original quantity
            cancelReasonCode = reasonCode
            quantityCanceled = quantity
            cancelComments = comments

            // And then create a new requisition item to represent the new quantity
            def newRequisitionItem = new RequisitionItem()
            //newRequisitionItem.requisition = requisition
            newRequisitionItem.product = product
            newRequisitionItem.product = productPackage
            newRequisitionItem.parentRequisitionItem = this
            newRequisitionItem.quantity = newQuantity
            if (newQuantity == quantity) {
                throw new ValidationException("Quantity was not changed")
                //newRequisitionItem.errors.reject("quantity was not changed")
            }
            else if (newQuantity == 0) {
                throw new ValidationException("Are you sure you want to cancel?")
                //newRequisitionItem.errors.reject("quantity was 0")
            }
            else {
                addToRequisitionItems(newRequisitionItem)
            }

            //requisition.addToRequisitionItems(newRequisitionItem)
            //requisitionItem.addToRequisitionItems(newRequisitionItem)
            //requisitionItem.save(flush: true)
        }
    }
    */



}
