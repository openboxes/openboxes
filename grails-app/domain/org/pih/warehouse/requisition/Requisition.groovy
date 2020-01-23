/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.requisition

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.fulfillment.Fulfillment
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment

class Requisition implements Comparable<Requisition>, Serializable {

    def beforeInsert = {
        def currentUser = AuthService.currentUser.get()
        if (currentUser) {
            createdBy = currentUser
            updatedBy = currentUser
        }
    }
    def beforeUpdate = {
        def currentUser = AuthService.currentUser.get()
        if (currentUser) {
            updatedBy = currentUser
        }
    }

    String id
    String name
    String description         // a user-defined, searchable name for the order
    String requestNumber     // an auto-generated reference number

    // Dates
    Date dateRequested = new Date()
    Date dateReviewed
    Date dateVerified
    Date dateChecked
    Date dateDelivered
    Date dateIssued
    Date dateReceived

    Date requestedDeliveryDate = new Date()

    // Frequency - for stock requisitions we should know how often (monthly, weekly, daily)

    // Requisition type, status, and commodity class
    RequisitionType type
    RequisitionStatus status
    CommodityClass commodityClass
    Requisition requisitionTemplate
    RequisitionItemSortByCode sortByCode

    // where stock is originating from
    Location origin

    // where stock is being issued to
    Location destination

    // Person who submitted the initial requisition paper form
    Person requestedBy

    // Person who reviewed the requisition
    Person reviewedBy

    // Pharmacist who verified the requisition before it was issued
    Person verifiedBy

    // Person who reviewed the requisition
    Person checkedBy

    // Pharmacist or nurse who signed for the issued stock
    Person deliveredBy

    // Pharmacist or nurse who signed for the issued stock
    Person issuedBy

    // Pharmacist or nurse who signed for the issued stock
    Person receivedBy

    // Intended recipient
    Person recipient

    // Intended recipient program
    String recipientProgram

    // Stock requisitions will need to be handled through a template version of a requisition
    Boolean isTemplate = false
    Boolean isPublished = false
    Date datePublished

    // Not used yet
    Date dateValidFrom
    Date dateValidTo

    Fulfillment fulfillment

    // Audit fields
    Date dateCreated
    Date lastUpdated
    User createdBy
    User updatedBy
    String monthRequested

    Integer replenishmentPeriod = 0

    // Removed comments, documents, events for the time being.
    static transients = ["sortedStocklistItems", "requisitionItemsByDateCreated", "requisitionItemsByOrderIndex", "requisitionItemsByCategory", "shipment", "totalCost"]
    static hasOne = [picklist: Picklist]
    static hasMany = [requisitionItems: RequisitionItem, transactions: Transaction, shipments: Shipment]
    static mapping = {
        id generator: 'uuid'
        requisitionItems cascade: "all-delete-orphan", sort: "orderIndex", order: 'asc', batchSize: 100

        monthRequested formula: "date_format(date_requested, '%M %Y')"
    }

    static constraints = {
        status(nullable: true)
        type(nullable: true)
        name(nullable: false, blank: false)
        description(nullable: true)
        requestNumber(nullable: true, maxSize: 255)
        origin(nullable: false)
        destination(nullable: false)
        fulfillment(nullable: true)
        recipient(nullable: true)
        requestedBy(nullable: false)
        reviewedBy(nullable: true)
        verifiedBy(nullable: true)
        checkedBy(nullable: true)
        issuedBy(nullable: true)
        deliveredBy(nullable: true)
        receivedBy(nullable: true)
        picklist(nullable: true)
        dateRequested(nullable: false)
        requestedDeliveryDate(nullable: false)

        // FIXME Even though Grails complains that "derived properties may not be constrained", when you remove the constraint there are validation errors on Requisition
        // OB-3180 Derived properties may not be constrained. Property [monthRequested] of domain class org.pih.warehouse.requisition.Requisition will not be checked during validation.
        monthRequested(nullable: true)
        dateCreated(nullable: true)
        dateChecked(nullable: true)
        dateReviewed(nullable: true)
        dateVerified(nullable: true)
        dateDelivered(nullable: true)
        dateReceived(nullable: true)
        dateIssued(nullable: true)
        lastUpdated(nullable: true)
        dateValidFrom(nullable: true)
        dateValidTo(nullable: true)
        createdBy(nullable: true)
        updatedBy(nullable: true)
        recipientProgram(nullable: true)
        commodityClass(nullable: true)
        isTemplate(nullable: true)
        isPublished(nullable: true)
        datePublished(nullable: true)
        requisitionTemplate(nullable: true)
        replenishmentPeriod(nullable: true)
        sortByCode(nullable: true)
    }

    def getRequisitionItemCount() {
        return getOriginalRequisitionItems()?.size()
    }


    def calculatePercentageCompleted() {
        def numerator = getCompleteRequisitionItems()?.size() ?: 0
        def denominator = getInitialRequisitionItems()?.size() ?: 1
        if (denominator) {
            return (numerator / denominator) * 100
        } else {
            return 0
        }
    }

    /**
     * @return all requisition items that have been completed (canceled or fulfilled)
     */
    def getCompleteRequisitionItems() {
        return initialRequisitionItems?.findAll { it.isCompleted() }
    }

    /**
     * @return all requisition items that have not been completed
     */
    def getIncompleteRequisitionItems() {
        return initialRequisitionItems?.findAll { !it.isCompleted() }
    }

    /**
     * @return all requisition items that were apart of the original requisition
     */
    def getInitialRequisitionItems() {
        return requisitionItems?.findAll { !it.parentRequisitionItem }
    }

    def getOriginalRequisitionItems() {
        return requisitionItems?.findAll { it.requisitionItemType == RequisitionItemType.ORIGINAL }
    }

    /**
     * @return all requisition items that have been added as substitutions or supplements
     */
    def getAdditionalRequisitionItems() {
        return requisitionItems?.findAll { it.parentRequisitionItem }
    }

    Boolean isWardRequisition() {
        return (type in [RequisitionType.NON_STOCK, RequisitionType.STOCK, RequisitionType.ADHOC])
    }

    Boolean isOpen() {
        return (status in [RequisitionStatus.CREATED, RequisitionStatus.EDITING])
    }

    Boolean isPending() {
        return (status in [RequisitionStatus.CREATED, RequisitionStatus.EDITING, RequisitionStatus.VERIFYING, RequisitionStatus.PICKING, RequisitionStatus.PENDING])
    }

    Boolean isRequested() {
        return (status in [RequisitionStatus.VERIFYING, RequisitionStatus.PICKING, RequisitionStatus.PENDING, RequisitionStatus.ISSUED, RequisitionStatus.RECEIVED])
    }


    /**
     * Sort by sort order, name
     *
     * Sort requisitions by receiving location (alphabetical), requisition type, commodity class (consumables or medications), then date requested, then date created,
     */
    int compareTo(Requisition requisition) {
        return origin <=> requisition.origin ?:
                destination <=> requisition.destination ?:
                        type <=> requisition.type ?:
                                commodityClass <=> requisition.commodityClass ?:
                                        requisition.dateRequested <=> dateRequested ?:
                                                requisition.dateCreated <=> dateCreated
    }

    String toString() {
        return id
    }

    Requisition newInstance() {
        def requisition = new Requisition()
        requisition.origin = origin
        requisition.destination = destination
        requisition.type = type
        requisition.commodityClass = commodityClass
        requisition.requisitionItems = []
        requisitionItems.each {
            def requisitionItem = new RequisitionItem()
            requisitionItem.product = it.product
            requisitionItem.productPackage = it.productPackage
            requisitionItem.quantity = it.quantity
            requisitionItem.orderIndex = it.orderIndex
            requisition.addToRequisitionItems(requisitionItem)
        }

        return requisition
    }

    Boolean isRelatedToMe(Integer userId) {
        return (createdBy?.id == userId || updatedBy?.id == userId)
    }

    /**
     * Returns stocklist items in sorted order. Should only be used in stocklist-related operations.
     * @return
     */
    def getSortedStocklistItems() {

        if (!isTemplate) {
            throw new IllegalStateException("Must only be used with a stocklist")
        }

        return requisitionItems.sort { a, b ->
            a.product?.category?.name <=> b.product?.category?.name ?:
                    a.product?.name <=> b.product?.name ?:
                            a.orderIndex <=> b.orderIndex
        }
    }

    def getRequisitionItemsByDateCreated() {
        return requisitionItems.sort { a, b ->
            a.dateCreated <=> b.dateCreated
        }
    }

    def getRequisitionItemsByOrderIndex() {
        return requisitionItems.sort { a, b ->
            a.orderIndex <=> b.orderIndex
        }
    }

    def getRequisitionItemsByCategory() {
        return requisitionItems.sort { a, b ->
            a.product?.category?.name <=> b.product?.category?.name ?:
                    a.product?.name <=> b.product?.name ?:
                            a.orderIndex <=> b.orderIndex
        }
    }

    /**
     * Return the shipment associated with the requisition.
     *
     * @throws IllegalStateException if there are multiple shipments associated with a requisition (might be supported some day)
     *
     * @return
     */
    Shipment getShipment() {
        Shipment shipment

        if (shipments) {
            if (shipments.size() > 1) {
                throw new IllegalStateException("There are too many shipments associated with requisition ${requestNumber}")
            }
            shipment = shipments.iterator().next()
        }
        return shipment
    }

    /**
     * Return total value of the issued shipment
     *
     * @return
     */
    BigDecimal getTotalCost() {
        def itemsWithPrice = requisitionItems?.findAll { it.product.pricePerUnit }
        return itemsWithPrice.collect { it?.quantity * it?.product?.pricePerUnit }.sum() ?: 0
    }

    BigDecimal getQuantityByProduct(Product product) {
        return requisitionItems?.findAll { it.product == product }?.collect {
            it.quantity
        }?.sum() ?: 0
    }


    Map toJson() {
        [
                id                   : id,
                name                 : name,
                version              : version,
                requestedById        : requestedBy?.id,
                requestedByName      : requestedBy?.name,
                description          : description,
                dateRequested        : dateRequested.format("MM/dd/yyyy"),
                requestedDeliveryDate: requestedDeliveryDate.format("MM/dd/yyyy HH:mm XXX"),
                lastUpdated          : lastUpdated?.format("dd/MMM/yyyy hh:mm a"),
                status               : status?.name(),
                type                 : type?.name(),
                originId             : origin?.id,
                originName           : origin?.name,
                destinationId        : destination?.id,
                destinationName      : destination?.name,
                recipientProgram     : recipientProgram,
                requisitionTemplate  : requisitionTemplate?.toJson(),
                requisitionItems     : requisitionItems?.sort()?.collect { it?.toJson() }
        ]
    }
}
