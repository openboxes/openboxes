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
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.LocalTransfer
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product

class RequisitionService {

	boolean transactional = true

	def identifierService
	def productService
	def shipmentService;
	def inventoryService;

    /*
    def getRequisitionStats(requisition, params) {
        def criteria = Requisition.createCriteria()
        def results = criteria.list(max:params?.max?:10,offset:params?.offset?:0) {
    }
    */

    def getRequisitionStatistics(destination, origin) {
        return getRequisitionStatistics(destination, origin, null)
    }

    def getRequisitionStatistics(destination, origin, user) {
        def statistics = [:]

        def criteria = Requisition.createCriteria()
        def results = criteria.list {
            projections {
                groupProperty('status')
                rowCount()
            }
            and {
                or {
                    eq("isTemplate", false)
                    isNull("isTemplate")
                }

                or {

                    if (destination) eq("destination", destination)
                    if (origin) eq("origin", origin)
                }
                //if (user) {
                //    or {
                //        eq("createdBy", user)
                //        eq("updatedBy", user)
                //        eq("requestedBy", user)
                //    }
                //}
            }
            isNotNull("status")
        }



        results.each {
            statistics[it[0]] = it[1]
        }
        statistics["ALL"] = results.collect { it[1] }.sum()

        if (user) {
            def criteria2 = Requisition.createCriteria()
            results = criteria2.get {
                projections {
                    countDistinct("id")
                }
                and {
                    or {
                        eq("isTemplate", false)
                        isNull("isTemplate")
                    }
                    or {
                        eq("createdBy", user)
                        eq("updatedBy", user)
                        eq("requestedBy", user)
                    }
                    or {

                        if (destination) eq("destination", destination)
                        if (origin) eq("origin", origin)
                    }
                }
            }

            statistics["MINE"] = results
        }


        return statistics;
    }

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

    /**
     * Get requisition template
     */
    def getAllRequisitionTemplates(Requisition requisition, Map params) {
        return getRequisitions(requisition, params)
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
        println "Get requisitions: " + params

        //def getRequisitions(Location destination, Location origin, User createdBy, RequisitionType requisitionType, RequisitionStatus status, CommodityClass commodityClass, String query, Map params) {
        //return Requisition.findAllByDestination(session.warehouse)

        def isRelatedToMe = Boolean.parseBoolean(params.isRelatedToMe)
        def commodityClassIsNull = Boolean.parseBoolean(params.commodityClassIsNull)

        def criteria = Requisition.createCriteria()
        def results = criteria.list(max:params?.max?:10,offset:params?.offset?:0) {
            and {
                // Base query needs to include the following
                if (!requisition.isTemplate) {
                    or {
                        eq("isTemplate", false)
                        isNull("isTemplate")
                    }
                }
                else {
                    eq("isTemplate", requisition.isTemplate)
                }
                if (requisition.isPublished) {
                    eq("isPublished", requisition.isPublished)
                }
                if (params.commodityClassIsNull) {
                    isNull("commodityClass")
                }

                if (requisition.destination) {
                    eq("destination", requisition.destination)
                }
                if (requisition.status) {
                    eq("status", requisition.status)
                }
                if (params.relatedToMe) {
                    def currentUser = AuthService.getCurrentUser().get()
                    or {
                        eq("createdBy.id", currentUser.id)
                        eq("updatedBy.id", currentUser.id)
                        eq("requestedBy.id", currentUser.id)
                    }
                }
                if (requisition.requestedBy) {
                    eq("requestedBy.id", requisition.requestedBy.id)
                }
                if (requisition.createdBy) {
                    eq("createdBy.id", requisition.createdBy.id)
                }
                if (requisition.updatedBy) {
                    eq("updatedBy.id", requisition.updatedBy.id)
                }
                if (requisition.commodityClass) {
                    eq("commodityClass", requisition.commodityClass)
                }
                //if (commodityClassIsNull) {
                //    isNull("commodityClass")
                //}
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
                else {
                    order("dateRequested", "desc")
                }
                //maxResults(10)
                //eq("isPublished", false)
            }
        }

        return results

    }

    def getRequisitionsByDestination(destination) {
        //return Requisition.findAllByDestination(destination)

        def criteria = Requisition.createCriteria()
        def results = criteria.list() {
            eq("destination", destination)
        }
        return results
    }


    def countRequisitions(destination) {
        def criteria = Requisition.createCriteria()
        def results = criteria.list {

            createAlias('status','statusAlias')

            projections {
                groupProperty('statusAlias')
                rowCount()
            }
            eq("destination", destination)
        }

        println results

        // HQL
        //results = Work.executeQuery('select w.artist.style, count(w) from Work as w group by w.artist.style')
        //println results
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
        def savedRequisition = requisition.save(flush: true)
        println "requisition = " + savedRequisition
        println "requisition.errors = " + requisition.errors
        if (savedRequisition) {
            return savedRequisition
        }
        else {
            return requisition
        }

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
            if (requisition.status == RequisitionStatus.ISSUED) {
                requisition.status = RequisitionStatus.CHECKING
                requisition.transactions*.id.each { transactionId ->
                    def transaction = Transaction.load(transactionId)
                    if (transaction) {
                        requisition.removeFromTransactions(transaction)
                        if (transaction.localTransfer) {
                            transaction.localTransfer.destinationTransaction = null
                            transaction.localTransfer.sourceTransaction = null
                            transaction?.localTransfer?.delete()
                        }
                        transaction.delete();
                    }
                }
                requisition.save()
            }
            // FIXME We actually need status history so we can rollback to the correct status here
            else if (requisition.status == RequisitionStatus.CANCELED) {
                requisition.status = RequisitionStatus.PENDING
            }
            else if (requisition.status == RequisitionStatus.PICKED) {
                requisition.status = RequisitionStatus.PICKING
            }
            else if (requisition.status == RequisitionStatus.PICKING) {
                requisition.status = RequisitionStatus.VERIFYING
            }
            else if (requisition.status == RequisitionStatus.VERIFYING) {
                requisition.status = RequisitionStatus.EDITING
            }
            else if (requisition.status == RequisitionStatus.EDITING) {
                requisition.status = RequisitionStatus.CREATED
            }
            requisition.save(flush:true)

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

    void clearRequisition(Requisition requisition) {
        //def ids = requisition.requisitionItems.collect { it }
        //ids.each { id ->
        //    def requisitionItem = RequisitionItem.get(id);
        //    requisition.removeFromRequisitionItems()
        //}
        requisition.requisitionItems*.delete()
        requisition.requisitionItems.clear()
        requisition.save(flush: true)
    }

    Requisition cloneRequisition(Requisition requisition) {
        def cloneRequisition = new Requisition()
        cloneRequisition.name = "Copy of " + requisition.name
        cloneRequisition.description = requisition.description
        cloneRequisition.commodityClass = requisition.commodityClass
        cloneRequisition.type = requisition.type
        cloneRequisition.status = requisition.status
        cloneRequisition.dateRequested = requisition.dateRequested
        cloneRequisition.origin = requisition.origin
        cloneRequisition.destination = requisition.destination
        cloneRequisition.requestedBy = requisition.requestedBy
        cloneRequisition.requestedDeliveryDate = requisition.requestedDeliveryDate
        cloneRequisition.isPublished = false; //requisition.isPublished
        cloneRequisition.datePublished = null //requisition.datePublished
        cloneRequisition.isTemplate = requisition.isTemplate

        requisition.requisitionItems.each { requisitionItem ->
            def cloneRequisitionItem = new RequisitionItem()
            cloneRequisitionItem.description = requisitionItem.description
            cloneRequisitionItem.product = requisitionItem.product
            cloneRequisitionItem.productPackage = requisitionItem.productPackage
            cloneRequisitionItem.quantity = requisitionItem.quantity
            cloneRequisitionItem.orderIndex = requisitionItem.orderIndex
            cloneRequisition.addToRequisitionItems(cloneRequisitionItem)
        }
        cloneRequisition.save(flush: true)

        return cloneRequisition


    }


    void cancelRequisition(Requisition requisition) {
		requisition.status = RequisitionStatus.CANCELED
		requisition.save(flush: true)
	}

	void undoCancelRequisition(Requisition requisition) {
		requisition.status = RequisitionStatus.PENDING
		requisition.save(flush: true)
	}


    public List<Requisition> getIssuedRequisitionsBetweenDates(List<Location> fromLocations, List<Location> toLocations, Date fromDate, Date toDate) {
        def requisitions = Transaction.createCriteria().list() {
            eq("status", RequisitionStatus.ISSUED)
            if (toLocations) {
                'in'("destination", toLocations)
            }
            if (fromLocations) {
                'in'("origin", fromLocations)
            }
            between('dateRequested', fromDate, toDate)
        }
        return requisitions
    }

    public List<Requisition> getPendingRequisitionsBetweenDates(List<Location> fromLocations, List<Location> toLocations, Date fromDate, Date toDate) {
        def requisitions = Requisition.createCriteria().list() {
            lt("status", RequisitionStatus.ISSUED)
            if (toLocations) {
                'in'("destination", toLocations)
            }
            if (fromLocations) {
                'in'("origin", fromLocations)
            }
            if (fromDate && toDate) {
                between('dateRequested', fromDate, toDate)
            }
            else if (fromDate) {
                ge("dateRequested", fromDate)
            }
            else if (toDate) {
                le("dateRequested", toDate)
            }
        }
        return requisitions
    }

    public List<RequisitionItem> getCanceledRequisitionItems(Location location, Product product) {
        def requisitionItems = RequisitionItem.createCriteria().list() {
            requisition {
                or {
                    eq("destination", location)
                    eq("origin", location)
                }
                and {
                    eq("isTemplate", false)
                    eq("status", RequisitionStatus.CANCELED)
                }
            }
            eq("product", product)
        }
        //println requisitionItems
        return requisitionItems
    }


    public List<RequisitionItem> getIssuedRequisitionItems(Location location, Product product, Date startDate, Date endDate, List<ReasonCode> cancelReasonCodes) {
        println "Reason codes: " + cancelReasonCodes

        def requisitionItems = RequisitionItem.createCriteria().list() {
            requisition {
                and {
                    eq("destination", location)
                    eq("isTemplate", false)
                    eq("status", RequisitionStatus.ISSUED)
                    if (startDate) {
                        ge("dateRequested", startDate)
                    }
                    if (endDate) {
                        le("dateRequested", endDate)
                    }
                }
            }
            eq("product", product)
            if (cancelReasonCodes) {
                or {
                    isNull("cancelReasonCode")
                    'in'("cancelReasonCode", cancelReasonCodes)
                }
            }
        }
        //println requisitionItems
        return requisitionItems
    }


    public List<RequisitionItem> getPendingRequisitionItems(Location location, Product product) {
        def requisitionItems = RequisitionItem.createCriteria().list() {
            requisition {
                or {
                    eq("destination", location)
                    eq("origin", location)
                }
                and {
                    eq("isTemplate", false)
                    'in'("status", [RequisitionStatus.CHECKING, RequisitionStatus.EDITING, RequisitionStatus.CONFIRMING, RequisitionStatus.PICKED, RequisitionStatus.PICKING])
                }
            }
            eq("product", product)
        }
        //println requisitionItems
        return requisitionItems
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

    public List<RequisitionItem> getPendingRequisitionItems(Location location) {
        def requisitionItems = RequisitionItem.createCriteria().list() {
            requisition {
                eq("destination", location)
                lt("status", RequisitionStatus.ISSUED)
                not {
                    eq("status", RequisitionStatus.CANCELED)
                }
            }
        }
        //println requisitionItems

        return requisitionItems

    }

    public List<RequisitionItem> getCanceledRequisitionItems(Location location, List cancelReasonCodes, Date dateRequestedFrom, Date dateRequestedTo, max, offset) {
        println "Get canceled items " + cancelReasonCodes + " " + max + " " + offset
        def requisitionItems = RequisitionItem.createCriteria().list(max: max, offset: offset) {
            requisition {
                eq("destination", location)
                eq("status", RequisitionStatus.ISSUED)
                //not {
                //    eq("status", RequisitionStatus.CANCELED)
                //
                //}
                if (dateRequestedFrom && dateRequestedTo) {
                    between("dateRequested", dateRequestedFrom, dateRequestedTo)
                }
                else if (dateRequestedFrom) {
                    ge("dateRequested", dateRequestedFrom)
                }
                else if (dateRequestedTo) {
                    le("dateRequested", dateRequestedTo)
                }
                order("dateRequested", "desc")
            }
            /*
            or {
                gt("quantityCanceled", 0)
                isNotNull("cancelReasonCode")
                isNotNull("cancelComments")
            }*/
            if (cancelReasonCodes) {
                'in'("cancelReasonCode", cancelReasonCodes)
            }

            //eq("cancelReasonCode", "STOCKOUT")
            //eq("status", RequisitionItemStatus.CANCELED)
            //firstResult(offset)
            //maxResults(max)
        }
        println requisitionItems

        return requisitionItems

    }

    public List<RequisitionItem> getRequisitionItemsByReasonCode(Location location,  max, offset) {
        println "get canceled items "
        def requisitionItems = RequisitionItem.createCriteria().list(max: max, offset: offset) {
            requisition {
                eq("destination", location)
                eq("status", RequisitionStatus.ISSUED)
                //not {
                //    eq("status", RequisitionStatus.CANCELED)
                //
                //}
            }
            eq("cancelReasonCode", "STOCKOUT")
        }
        println requisitionItems

        return requisitionItems

    }



    void generatePicklist(Requisition requisition) {
        requisition.requisitionItems.each { requisitionItem ->
            // If the requisition item has been changed, we'll use the modified item
            if (requisitionItem.isChanged()) {
                println "generate picklist for changed items "
                generatePicklistItem(requisitionItem.modificationItem)
            }
            // if the requisition item has been canceled, we'll ignore it
            else if (requisitionItem.isCanceled()) {
                println "ignore picklist for canceled items "
                // ignore
            }
            // if the requisition item has been substituted we'll use the substituted item
            else if (requisitionItem.isSubstituted()) {
                println "generate picklist for substituted items "
                generatePicklistItem(requisitionItem.substitutionItem)
            }
            else if (requisitionItem.isApproved()) {
                println "generate picklist for approved items "
            }
            else {
                throw new UnsupportedOperationException("Unknown ")
            }
        }
    }

    void generatePicklistItem(RequisitionItem requisitionItem) {
        println "generate picklist for requisition item " + requisitionItem



    }


    void clearPicklist(Requisition requisition) {
        if (requisition.picklist) {
            def picklistItems = requisition.picklist.picklistItems.collect { it.id }

            picklistItems.each {
                def picklistItem = PicklistItem.get(it)
                requisition.picklist.removeFromPicklistItems(picklistItem)
                picklistItem.delete()
            }
            requisition.save();
        }

    }

    def normalizeRequisition(Requisition requisition) {

        println "Normalizing requisition " + requisition.requestNumber
        requisition?.requisitionItems?.each { requisitionItem ->
            if (requisitionItem.requisitionItems) {
                if (requisitionItem.requisitionItems.size() > 1) {
                    throw new Exception("Cannot have more than one change per requisition item")
                }
                else {
                    requisitionItem.requisitionItems.each { childItem ->
                        println "Requisition item of type " + childItem.requisitionItemType + " is being normalized."

                        if (childItem.requisitionItemType == RequisitionItemType.SUBSTITUTION) {
                            requisitionItem.substitutionItem = childItem
                        }
                        else if (childItem.requisitionItemType == RequisitionItemType.PACKAGE_CHANGE) {
                            requisitionItem.modificationItem = childItem
                        }
                        else if (childItem.requisitionItemType == RequisitionItemType.QUANTITY_CHANGE) {
                            requisitionItem.modificationItem = childItem
                        }
                        else if (childItem.requisitionItemType == RequisitionItemType.ORIGINAL) {
                            throw new Exception("Original requisition item cannot be modified for requisition ${requisition.requestNumber}")
                        }
                        else if (childItem.requisitionItemType == RequisitionItemType.ADDITION) {
                            throw new Exception("Addition operation not supported for requisition ${requisition.requestNumber}")
                        }
                        else {
                            throw new Exception("Operation not supported for requisition ${requisition.requestNumber}")
                        }
                    }
                }
            }
        }
    }


}
