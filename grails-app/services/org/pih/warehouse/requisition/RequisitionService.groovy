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

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.plugins.csv.CSVWriter
import grails.validation.ValidationException
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.joda.time.LocalDate
import org.pih.warehouse.DateUtil
import javassist.NotFoundException
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product

import java.text.SimpleDateFormat

@Transactional
class RequisitionService {

    GrailsApplication grailsApplication
    AuthService authService
    RequisitionIdentifierService requisitionIdentifierService
    def inventoryService

    def getRequisitionStatistics(Location destination, Location origin, User user) {
        return getRequisitionStatistics(destination, origin, user, null, null)
    }

    def getRequisitionStatistics(Location destination, Location origin, User user, Date date) {
        return getRequisitionStatistics(destination, origin, user, date, null)
    }

    def getRequisitionStatistics(Location destination, Location origin, User user, Date date, List<RequisitionStatus> excludedStatuses) {
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
                if (!params.boolean("includeUnpublished")) {
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
                    def currentUser = authService.currentUser
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
                    if (params?.sort == "origin") {
                        origin {
                            order("name", params?.order ?: 'desc')
                        }
                    } else if (params?.sort == "destination") {
                        destination {
                            order("name", params?.order ?: 'desc')
                        }
                    } else if (params?.sort == "createdBy") {
                        createdBy {
                            order("firstName", params?.order ?: 'desc')
                            order("lastName", params?.order ?: 'desc')
                        }
                    } else if (params?.sort == "updatedBy") {
                        updatedBy {
                            order("firstName", params?.order ?: 'desc')
                            order("lastName", params?.order ?: 'desc')
                        }
                    } else if (params?.sort == "requestedBy") {
                        requestedBy {
                            order("firstName", params?.order ?: 'desc')
                            order("lastName", params?.order ?: 'desc')
                        }
                    } else {
                        order(params?.sort, params?.order ?: 'desc')
                    }
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
            requisition.requestNumber = requisitionIdentifierService.generate(requisition)
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
            outboundTransaction.transactionNumber = inventoryService.generateTransactionNumber(outboundTransaction)
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
                requisition.requestNumber = requisitionIdentifierService.generate(requisition)
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

    List<RequisitionItem> getPendingRequisitionItems(Product product) {
        return getPendingRequisitionItems(null, product)
    }

    List<RequisitionItem> getPendingRequisitionItems(Location origin, Product product) {
        def requisitionItems = RequisitionItem.createCriteria().list() {
            requisition {
                eq("isTemplate", false)
                if (origin) {
                    eq("origin", origin)
                }
                not {
                    'in'("status", [RequisitionStatus.ISSUED, RequisitionStatus.CANCELED, RequisitionStatus.REJECTED])
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

    def getRequisitionCountInCurrentFiscalYear(Location location) {
        def fiscalYearConfig = grailsApplication.config.openboxes.dashboard.yearTypes.fiscalYear

        if (!fiscalYearConfig) {
            throw new IllegalArgumentException("Missing fiscal year type definition in configuration")
        }

        def currentDate = new Date()
        def currentYear = currentDate.year + 1900
        def fiscalYearStart = fiscalYearConfig.start.split("/") // fiscalYearStart[0] = month, fiscalYearStart[1] = day

        if (fiscalYearStart.size() != 2) {
            throw new IllegalArgumentException("Wrong fiscal year type definition in configuration. Start date should have format 'MM/DD'")
        }

        String startMonth = fiscalYearStart[0]
        String startDay = fiscalYearStart[1]

        def startYear = currentYear - 1 // by default assume that we are in fiscal year that started in previous year
        def endYear = currentYear
        if (currentDate.month > startMonth.toInteger() || (currentDate.month == startMonth.toInteger() && currentDate.date >= startDay.toInteger())) {
            startYear = currentYear
            endYear = currentYear + 1
        }

        def startDate = new Date("${fiscalYearConfig.start}/${startYear}")
        def endDate = new Date("${fiscalYearConfig.end}/${endYear}")

        // Data fetch
        return Requisition.executeQuery("""
            SELECT 
                COUNT(r.id) 
            FROM Requisition r 
            WHERE r.origin = :location AND r.dateCreated >= :startDate AND r.dateCreated <= :endDate AND r.isTemplate = false 
            """, ['location': location, 'startDate': startDate, 'endDate': endDate])[0] ?: 0
    }

    Event createEvent(EventCode eventCode, Location eventLocation, Date eventDate, User currentUser) {
        EventType eventType = EventType.findByEventCode(eventCode)
        if (!eventType) {
            throw new NotFoundException("No event type with code ${eventCode} has been found")
        }
        return new Event(eventDate: eventDate, eventType: eventType, eventLocation: eventLocation, createdBy: currentUser)
    }

    Requisition transitionRequisitionStatus(Requisition requisition, RequisitionStatus requisitionStatus, EventCode eventCode, User currentUser, Comment comment = null) {
        requisition.status = requisitionStatus
        Event event = createEvent(eventCode, requisition.origin, new Date(), currentUser)
        requisition.addToEvents(event)

        if (comment) {
            event.comment = comment
            requisition.addToComments(comment)
        }
    }

    void triggerRequisitionStatusTransition(Requisition requisition, User currentUser, RequisitionStatus newStatus, Comment comment = null) {
        // OBPIH-5134 Request approval feature implements additional status transitions for a request
        switch(newStatus) {
            case RequisitionStatus.VERIFYING:
                transitionRequisitionStatus(requisition, RequisitionStatus.VERIFYING, EventCode.SUBMITTED, currentUser)
                requisition.approvalRequired = false
                break
            case RequisitionStatus.PENDING_APPROVAL:
                if (!requisition.origin.approvalRequired) {
                    throw new IllegalArgumentException("Fulfilling location must support Request Approval")
                }
                transitionRequisitionStatus(requisition, RequisitionStatus.PENDING_APPROVAL, EventCode.PENDING_APPROVAL, currentUser)
                requisition.approvalRequired = true
                break
            case RequisitionStatus.APPROVED:
                if (!requisition.origin.approvalRequired) {
                    throw new IllegalArgumentException("Fulfilling location must support Request Approval")
                }
                transitionRequisitionStatus(requisition, RequisitionStatus.APPROVED, EventCode.APPROVED, currentUser)
                requisition.dateApproved = new Date()
                requisition.approvedBy = currentUser
                break
            case RequisitionStatus.REJECTED:
                if (!requisition.origin.approvalRequired) {
                    throw new IllegalArgumentException("Fulfilling location must support Request Approval")
                }
                transitionRequisitionStatus(requisition, RequisitionStatus.REJECTED, EventCode.REJECTED, currentUser, comment)
                requisition.dateRejected = new Date()
                requisition.rejectedBy = currentUser
                break
            default:
                requisition.status = newStatus
        }
        requisition.save(flush: true)
    }

    void deleteEvent(Requisition requisition, Event event) {
        requisition.removeFromEvents(event)
        event.delete()
        requisition.save()
    }

    void rollbackLastEvent(Requisition requisition) {
        ApplicationTagLib g = grailsApplication.mainContext.getBean(ApplicationTagLib)
        Event event = requisition.mostRecentEvent
        if (!event) {
            String errorMessage = g.message(
                    code: "requisition.error.rollback.noRecentEvents",
                    default: "Cannot rollback requisition because there are no recent events"
            )
            throw new RuntimeException(errorMessage)
        }
        deleteEvent(requisition, event)
    }

    void deleteComment(Comment comment, Requisition requisition) {
        requisition.removeFromComments(comment)
        Event event = Event.findByComment(comment)
        if (event) {
            event.comment = null
        }
    }

    Comment saveComment(Comment comment) {
        return comment.save()
    }

    void addCommentToRequisition(Comment comment, Requisition requisition) {
        requisition.addToComments(comment)
        requisition.save()
    }

    RequisitionItem buildRequisitionItem(Map params) {
        String productCode = params.productCode
        Product product = Product.findByProductCode(productCode)
        if (!product) {
            throw new IllegalArgumentException("Product not found for ${productCode}")
        }

        def quantityRequested = params.quantity as Integer
        if (!(quantityRequested > 0)) {
            throw new IllegalArgumentException("Requested quantity should be greater than 0")
        }

        def deliveryDate = params.requestedDeliveryDate
        if (!isDateOneWeekFromNow(deliveryDate)) {
            throw new IllegalArgumentException("Delivery date must be after seven days from now")
        }

        def comments = params.destination
        def requestNumber = params.requestNumber
        def requisition = Requisition.findByRequestNumber(requestNumber)
        if (!requisition) {
            requisition = new Requisition(
                    name: "Outbound Order ${requestNumber}",
                    requestNumber: requestNumber,
                    status: RequisitionStatus.CREATED
            )

            Location origin = Location.findByLocationNumber(params.origin)
            if (!origin) {
                throw new IllegalArgumentException("Location not found for origin ${params.origin}")
            }
            requisition.origin = origin

            Location destination = Location.findByLocationNumber(params.destination)
            if (!destination) {
                throw new IllegalArgumentException("Location not found for destination ${params.destination}")
            }
            requisition.destination = destination
            requisition.requestedDeliveryDate = deliveryDate.toDate()
            requisition.requestedBy = authService.currentUser
            requisition.save(failOnError: true)
        }

        RequisitionItem requisitionItem = RequisitionItem.createCriteria().get {
            eq 'product' , product
            eq "requisition", requisition
        }
        if (!requisitionItem) {
            requisitionItem = new RequisitionItem()
        }

        requisitionItem.product = product
        requisitionItem.quantity = quantityRequested
        requisitionItem.comment = comments

        requisition.addToRequisitionItems(requisitionItem)

        return requisitionItem
    }

    boolean isDateOneWeekFromNow(def date) {
        LocalDate today = LocalDate.now()
        LocalDate oneWeekFromNow = new LocalDate(today.getYear(), today.getMonthOfYear(), today.getDayOfMonth()+7)
        if(date > oneWeekFromNow) {
            return true
        }
        return false
    }

    /**
     * Export the given requisitions to CSV.
     *
     * @param requisitions
     * @return
     */
    String exportRequisitions(requisitions) {
        def formatDate = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss")
        def sw = new StringWriter()

        def csvWriter = new CSVWriter(sw, {
            "Requisition Number" { it.requisitionNumber }
            "Status" { it.status }
            "Type" { it.type }
            "Class" { it.commodityClass }
            "Name" { it.name }
            "Origin" { it.origin }
            "Destination" { it.destination }

            "Requested by" { it?.requestedBy?.name ?: "" }
            "Date Requested" { it.dateRequested }

            "Verified" { it?.verifiedBy?.name ?: "" }
            "Date Verified" { it.dateVerified }

            "Picked" { it?.pickedBy?.name ?: "" }
            "Date Picked" { it.datePicked }

            "Checked" { it?.checkedBy?.name ?: "" }
            "Date Checked" { it.dateChecked }

            "Issued" { it?.issuedBy?.name ?: "" }
            "Date Issued" { it.dateIssued }

            "Created" { it?.createdBy?.name ?: "" }
            "Date Created" { it.dateCreated }

            "Updated" { it?.updatedBy?.name ?: "" }
            "Date Updated" { it.lastUpdated }
        })

        requisitions.each { requisition ->
            def row = [
                    requisitionNumber: requisition.requestNumber,
                    type             : requisition?.type,
                    commodityClass   : requisition?.commodityClass,
                    status           : requisition.status,
                    name             : requisition.name,
                    origin           : requisition.origin,
                    destination      : requisition.destination,

                    requestedBy      : requisition.requestedBy,
                    dateRequested    : requisition.dateRequested ? "${formatDate.format(requisition.dateRequested)}" : "",

                    reviewedBy       : requisition.reviewedBy,
                    dateReviewed     : requisition.dateReviewed ? "${formatDate.format(requisition.dateReviewed)}" : "",

                    verifiedBy       : requisition.verifiedBy,
                    dateVerified     : requisition.dateVerified ? "${formatDate.format(requisition.dateVerified)}" : "",

                    checkedBy        : requisition.checkedBy,
                    dateChecked      : requisition.dateChecked ? "${formatDate.format(requisition.dateChecked)}" : "",

                    deliveredBy      : requisition.deliveredBy,
                    dateDelivered    : requisition.dateDelivered ? "${formatDate.format(requisition.dateDelivered)}" : "",

                    pickedBy         : requisition?.picklist?.picker,
                    datePicked       : requisition?.picklist?.datePicked ? "${formatDate.format(requisition?.picklist?.datePicked)}" : "",

                    issuedBy         : requisition.issuedBy,
                    dateIssued       : requisition.dateIssued ? "${formatDate.format(requisition.dateIssued)}" : "",

                    receivedBy       : requisition.receivedBy,
                    dateReceived     : requisition.dateReceived ? "${formatDate.format(requisition.dateReceived)}" : "",

                    createdBy        : requisition.createdBy,
                    dateCreated      : requisition.dateCreated ? "${formatDate.format(requisition.dateCreated)}" : "",

                    updatedBy        : requisition.updatedBy,
                    lastUpdated      : requisition.lastUpdated ? "${formatDate.format(requisition.lastUpdated)}" : "",
            ]
            csvWriter << row
        }
        return CSVUtils.prependBomToCsvString(sw.toString())
    }

    String exportRequisitionItems(requisitions) {
        def formatDate = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss")
        def sw = new StringWriter()

        def csvWriter = new CSVWriter(sw, {
            "Requisition Number" { it.requisitionNumber }
            "Status" { it.status }
            "Type" { it.type }
            "Class" { it.commodityClass }
            "Name" { it.name }
            "Origin" { it.origin }
            "Destination" { it.destination }
            "Requested by" { it?.requestedBy?.name }
            "Date Requested" { it.dateRequested }
            "Product code" { it.productCode }
            "Product name" { it.productName }
            "Status" { it.itemStatus ?: "" }
            "Requested" { it.quantity ?: "" }
            "Approved" { it.quantityApproved ?: "" }
            "Picked" { it.quantityPicked ?: "" }
            "Canceled" { it.quantityCanceled ?: "" }
            "Reason Code" { it.reasonCode ?: "" }
            "Comments" { it.comments ?: "" }

        })

        requisitions.each { requisition ->
            requisition.requisitionItems.each { requisitionItem ->
                def row = [
                        requisitionNumber: requisition.requestNumber,
                        type             : requisition?.type,
                        commodityClass   : requisition?.commodityClass,
                        status           : requisition.status,
                        name             : requisition.name,
                        requestedBy      : requisition.requestedBy ?: "",
                        dateRequested    : requisition.dateRequested ? "${formatDate.format(requisition.dateRequested)}" : "",
                        origin           : requisition.origin,
                        destination      : requisition.destination,
                        productCode      : requisitionItem.product.productCode,
                        productName      : requisitionItem.product.name,
                        itemStatus       : requisitionItem.status,
                        quantity         : requisitionItem.quantity,
                        quantityCanceled : requisitionItem.quantityCanceled,
                        quantityApproved : requisitionItem.quantityApproved,
                        quantityPicked   : requisitionItem.calculateQuantityPicked(),
                        reasonCode       : requisitionItem.cancelReasonCode,
                        comments         : requisitionItem.cancelComments,
                ]
                csvWriter << row
            }
        }
        return CSVUtils.prependBomToCsvString(sw.toString())
    }


}
