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
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.util.DateUtil

class RequisitionService {

    boolean transactional = true

    def identifierService
    def productService
    def shipmentService
    def inventoryService


    def getRequisitionStatistics(Location destination, Location origin, User user) {
        return getRequisitionStatistics(destination, origin, user, null, null)
    }

    def getRequisitionStatistics(Location destination, Location origin, User user, Date date) {
        return getRequisitionStatistics(destination, origin, user, date, null)
    }

    def getRequisitionStatistics(Location destination, Location origin, User user, Date date, List<RequisitionStatus> excludedStatuses) {
        log.info "destination " + destination
        log.info "origin " + origin
        log.info "user " + user

        log.info "Date " + date
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
            }
            isNotNull("status")
            if (excludedStatuses) {
                not {
                    'in'("status", excludedStatuses)
                }
            }
            if (date) {
                gt("dateRequested", date)
            }
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
                    }
                    or {
                        if (destination) eq("destination", destination)
                        if (origin) eq("origin", origin)
                    }
                }
            }

            statistics["MINE"] = results
        }


        return statistics
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
     * Get a single published stock list for the given origin
     *
     * @param origin
     * @param destination
     * @return
     */
    List<Requisition> getRequisitionTemplates(Location origin) {
        return Requisition.createCriteria().list {
            eq("isTemplate", Boolean.TRUE)
            eq("isPublished", Boolean.TRUE)
            eq("origin", origin)
        }
    }

    /**
     * Get a single published stock list for the given origin and destination.
     *
     * @param origin
     * @param destination
     * @return
     */
    List<Requisition> getRequisitionTemplates(Location origin, Location destination) {
        return Requisition.createCriteria().list {
            eq("isTemplate", Boolean.TRUE)
            eq("isPublished", Boolean.TRUE)
            eq("origin", origin)
            eq("destination", destination)
        }
    }


    /**
     * Get all items for given requisitions
     * @param List <Location>  origins
     * @param List <Location>  destinations
     * @return
     */
    List<Requisition> getRequisitionTemplatesItems(List<Requisition> requisitions) {
        return RequisitionItem.createCriteria().list() {
            if (requisitions) {
                'in'("requisition", requisitions)
            }
            product {
                category {
                    order("name", "asc")
                }
                order("name", "asc")
            }
        }
    }

    /**
     * Get requisition template
     */
    def getAllRequisitionTemplates(Requisition requisition, Map params) {
        return getRequisitions(requisition, params)
    }

    /**
     * Get all requisitions for the given destination.
     * @param destination
     * @return
     */
    def getRequisitions(Location destination) {
        return getRequisitions(new Requisition(destination: destination), [:])
    }


    /**
     * Get all requisitions for the given destination and origin.
     * @param destination
     * @param origin
     * @return
     */
    def getRequisitions(Location destination, Location origin) {
        return getRequisitions(new Requisition(destination: destination, origin: origin), [:])
    }

    /**
     * Get all requisitions for the given query.
     * @param destination
     * @param query
     * @param params
     * @return
     */
    def getRequisitions(Requisition requisition, Map params) {
        return getRequisitions(requisition, params, [], [])
    }

    /**
     * Get all requisitions for the given destination and query.
     * @param destination
     * @param query
     * @param params
     * @return
     */
    def getRequisitions(Requisition requisition, Map params, List<Location> origins, List<Location> destinations) {
        println "Get requisitions: " + params

        def issuedDateRange = DateUtil.parseDateRange(params?.issuedDateRange, "d/MMM/yyyy", "-")
        def requestedDateRange = DateUtil.parseDateRange(params?.requestedDateRange, "d/MMM/yyyy", "-")

        def criteria = Requisition.createCriteria()
        def results = criteria.list(max: params?.max ?: 10, offset: params?.offset ?: 0) {
            and {
                if (issuedDateRange) {
                    between("dateIssued", issuedDateRange[0], issuedDateRange[1])
                }

                if (requestedDateRange) {
                    between("dateRequested", requestedDateRange[0], requestedDateRange[1])
                }

                // Base query needs to include the following
                if (!requisition.isTemplate) {
                    or {
                        eq("isTemplate", false)
                        isNull("isTemplate")
                    }
                } else {
                    eq("isTemplate", requisition.isTemplate)
                }
                if (requisition.isPublished) {
                    eq("isPublished", requisition.isPublished)
                }
                if (params?.commodityClassIsNull) {
                    isNull("commodityClass")
                }

                if (requisition.destination) {
                    eq("destination", requisition.destination)
                }
                if (requisition.status) {
                    eq("status", requisition.status)
                }
                if (params?.relatedToMe) {
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
                    order(params?.sort, params?.order ?: 'desc')
                } else {
                    order("dateRequested", "desc")
                }
                and {
                    if (origins) {
                        'in'("origin", origins)
                    }
                    if (destinations) {
                        'in'("destination", destinations)
                    }
                }
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

        def savedRequisition = requisition.save(flush: true)
        println "requisition = " + savedRequisition
        println "requisition.errors = " + requisition.errors
        if (savedRequisition) {
            return savedRequisition
        } else {
            return requisition
        }

    }

    /**
     * Save the requisition
     *
     * @param requisition
     * @return
     */
    Requisition saveTemplateRequisition(Requisition requisition) {

        if (requisition.hasErrors() || !requisition.save(flush: true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }

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
    def issueRequisition(Requisition requisition, User issuedBy, Person deliveredBy, String comments) {

        // Make sure a transaction has not already been created for this requisition
        def outboundTransaction = Transaction.findByRequisition(requisition)
        if (outboundTransaction) {
            outboundTransaction.errors.reject("Cannot create multiple outbound transaction for the same requisition")
            throw new ValidationException("Cannot complete inventory transfer", outboundTransaction.errors)
        }

        // If an outbound transaction was not found, we create a new one
        if (!outboundTransaction) {
            // Create a new transaction
            outboundTransaction = new Transaction()
            outboundTransaction.transactionNumber = inventoryService.generateTransactionNumber()
            outboundTransaction.transactionDate = new Date()
            outboundTransaction.requisition = requisition
            // requisition origin is where the requisition originated from (the destination of stock transfer)
            outboundTransaction.destination = requisition.destination
            // requisition inventory is the location where the requisition is placed
            outboundTransaction.inventory = requisition?.origin?.inventory
            outboundTransaction.comment = comments
            outboundTransaction.transactionType = TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID)
        }

        def picklist = Picklist.findByRequisition(requisition)
        if (picklist) {
            picklist.picklistItems.each { picklistItem ->
                def transactionEntry = new TransactionEntry()
                transactionEntry.binLocation = picklistItem.binLocation
                transactionEntry.inventoryItem = picklistItem.inventoryItem
                transactionEntry.quantity = picklistItem.quantity
                outboundTransaction.addToTransactionEntries(transactionEntry)
            }

            if (!inventoryService.saveLocalTransfer(outboundTransaction)) {
                throw new ValidationException("Unable to save local transfer", outboundTransaction.errors)
            } else {
                Date now = new Date()
                requisition.status = RequisitionStatus.ISSUED
                requisition.dateIssued = now
                requisition.issuedBy = issuedBy
                requisition.dateDelivered = now
                requisition.deliveredBy = deliveredBy
                requisition.save(flush: true)
            }

        } else {
            requisition.errors.reject("requisition.picklist.mustHavePicklist")
            throw new ValidationException("Could not find a picklist associated with this requisition", requisition.errors)
        }

        return outboundTransaction
    }

    void rollbackRequisition(Requisition requisition) {
        try {
            if (requisition.status == RequisitionStatus.ISSUED) {
                requisition.status = RequisitionStatus.CHECKING
                requisition.issuedBy = null
                requisition.dateIssued = null
                requisition.transactions.each { transaction ->
                    if (transaction) {
                        requisition.removeFromTransactions(transaction)
                        if (transaction.localTransfer) {
                            transaction.localTransfer.destinationTransaction = null
                            transaction.localTransfer.sourceTransaction = null
                            transaction?.localTransfer?.delete()
                        }
                        transaction.delete()
                    }
                }
                requisition.save()
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
            def requisitionItems = itemsData.collect { itemData ->
                println "itemData: " + itemData
                def requisitionItem = requisition.requisitionItems?.find { i -> itemData.id && i.id == itemData.id }
                if (requisitionItem) {
                    requisitionItem.properties = itemData
                } else {
                    requisitionItem = new RequisitionItem(itemData)
                    requisition.addToRequisitionItems(requisitionItem)
                }
                println "package: " + requisitionItem?.productPackage
                println "json: " + requisitionItem.toJson()

                requisitionItem
            }

            def itemsToDelete = requisition.requisitionItems.findAll { dbItem ->
                !requisitionItems.any { clientItem -> clientItem.id == dbItem.id }
            }
            itemsToDelete.each { requisition.removeFromRequisitionItems(it) }
            requisition.origin = userLocation
            requisition.save(flush: true)
            println "Requisition: " + requisition
            println "Errors: " + requisition.errors

            requisition.requisitionItems?.each { it.save(flush: true) }
        } catch (Exception e) {
            log.error("Error saving requisition: " + e.message, e)

        }
        return requisition
    }

    void deleteRequisition(Requisition requisition) {
        requisition?.requisitionItems?.toArray().each { RequisitionItem requisitionItem ->
            deleteRequisitionItem(requisitionItem)
        }

        if (requisition?.picklist) {
            requisition.picklist.delete()
        }
        requisition.delete()
    }

    void deleteRequisitionItem(RequisitionItem requisitionItem) {
        requisitionItem.undoChanges()
        requisitionItem.delete()
    }


    void clearRequisition(Requisition requisition) {
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
        cloneRequisition.isPublished = false //requisition.isPublished
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

    List<RequisitionItem> getCanceledRequisitionItems(Location location, Product product) {
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
        return requisitionItems
    }


    List<RequisitionItem> getIssuedRequisitionItems(Location location, Product product, Date startDate, Date endDate, List<ReasonCode> cancelReasonCodes) {
        log.info "Reason codes: " + cancelReasonCodes

        def requisitionItems = RequisitionItem.createCriteria().list() {
            requisition {
                and {
                    eq("origin", location)
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
            // FIXME Should uncomment this once the demand calculation is implemented in RequisitionItem
            isNull("parentRequisitionItem")
            eq("product", product)
            if (cancelReasonCodes) {
                or {
                    isNull("cancelReasonCode")
                    'in'("cancelReasonCode", cancelReasonCodes)
                }
            }
        }
        return requisitionItems
    }

    def getIssuedTransactionEntries(Location location, Product product, Date startDate, Date endDate) {
        def transactionEntries = TransactionEntry.createCriteria().list {
            transaction {
                transactionType {
                    eq("transactionCode", TransactionCode.DEBIT)
                }
                eq("inventory", location.inventory)
                if (startDate) {
                    ge("transactionDate", startDate)
                }
                if (endDate) {
                    le("transactionDate", endDate)
                }
            }
            inventoryItem {
                eq("product", product)

            }
        }
        log.info("transaction entries " + transactionEntries.size())
        return transactionEntries
    }


    List<RequisitionItem> getPendingRequisitionItems(Location origin, Product product) {
        def requisitionItems = RequisitionItem.createCriteria().list() {
            requisition {
                eq("isTemplate", false)
                eq("origin", origin)
                not {
                    'in'("status", [RequisitionStatus.ISSUED, RequisitionStatus.CANCELED])
                }
            }
            eq("product", product)
        }

        return requisitionItems
    }


    List<RequisitionItem> getPendingRequisitionItems(Location location) {
        def requisitionItems = RequisitionItem.createCriteria().list() {
            requisition {
                eq("origin", location)
                lt("status", RequisitionStatus.ISSUED)
                not {
                    eq("status", RequisitionStatus.CANCELED)
                }
            }
        }

        return requisitionItems

    }

    List<RequisitionItem> getCanceledRequisitionItems(Location location, List cancelReasonCodes, Date dateRequestedFrom, Date dateRequestedTo, max, offset) {
        println "Get canceled items " + cancelReasonCodes + " " + max + " " + offset
        def requisitionItems = RequisitionItem.createCriteria().list(max: max, offset: offset) {
            requisition {
                eq("origin", location)
                eq("status", RequisitionStatus.ISSUED)
                if (dateRequestedFrom && dateRequestedTo) {
                    between("dateRequested", dateRequestedFrom, dateRequestedTo)
                } else if (dateRequestedFrom) {
                    ge("dateRequested", dateRequestedFrom)
                } else if (dateRequestedTo) {
                    le("dateRequested", dateRequestedTo)
                }
                order("dateRequested", "desc")
            }
            if (cancelReasonCodes) {
                'in'("cancelReasonCode", cancelReasonCodes)
            }
        }
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
            } else if (requisitionItem.isApproved()) {
                println "generate picklist for approved items "
            } else {
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
            requisition.save()
        }

    }

    def normalizeRequisition(Requisition requisition) {

        println "Normalizing requisition " + requisition.requestNumber
        requisition?.requisitionItems?.each { requisitionItem ->
            if (requisitionItem.requisitionItems) {
                if (requisitionItem.requisitionItems.size() > 1) {
                    throw new Exception("Cannot have more than one change per requisition item")
                } else {
                    requisitionItem.requisitionItems.each { childItem ->
                        println "Requisition item of type " + childItem.requisitionItemType + " is being normalized."

                        if (childItem.requisitionItemType == RequisitionItemType.SUBSTITUTION) {
                            requisitionItem.substitutionItem = childItem
                        } else if (childItem.requisitionItemType == RequisitionItemType.PACKAGE_CHANGE) {
                            requisitionItem.modificationItem = childItem
                        } else if (childItem.requisitionItemType == RequisitionItemType.QUANTITY_CHANGE) {
                            requisitionItem.modificationItem = childItem
                        } else if (childItem.requisitionItemType == RequisitionItemType.ORIGINAL) {
                            throw new Exception("Original requisition item cannot be modified for requisition ${requisition.requestNumber}")
                        } else if (childItem.requisitionItemType == RequisitionItemType.ADDITION) {
                            throw new Exception("Addition operation not supported for requisition ${requisition.requestNumber}")
                        } else {
                            throw new Exception("Operation not supported for requisition ${requisition.requestNumber}")
                        }
                    }
                }
            }
        }
    }


}
