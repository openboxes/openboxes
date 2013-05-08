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

import org.pih.warehouse.auth.AuthService;
import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.Event;


import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.fulfillment.Fulfillment
import org.pih.warehouse.inventory.Transaction;

class Requisition implements Comparable<Requisition>, Serializable {

    def beforeInsert = {
        if (!createdBy) createdBy = AuthService.currentUser.get()
    }
    def beforeUpdate = {
        if (!updatedBy) updatedBy = AuthService.currentUser.get()
    }

    String id
    String name
    String description         // a user-defined, searchable name for the order
    String requestNumber     // an auto-generated reference number

    // Dates
    Date dateRequested = new Date()
    Date requestedDeliveryDate = new Date()

    // Frequency - for stock requisitions we should know how often (monthly, weekly, daily)

    // Requisition type, status, and commodity class
    RequisitionType type;
    RequisitionStatus status;
    CommodityClass commodityClass

    Location origin            // where the requisition came from
    Location destination     // who the requisition will be fulfilled by

    Person requestedBy
    Person recipient
    String recipientProgram

    // Stock requisitions will need to be handled through a template version of a requisition
    Boolean isTemplate = false
    Boolean isPublished = false
    Date datePublished

    // Not used yet
    Date dateValidFrom
    Date dateValidTo

    Fulfillment fulfillment;

    List requisitionItems

    // Audit fields
    Date dateCreated
    Date lastUpdated
    User createdBy
    User updatedBy

    // Removed comments, documents, events for the time being.
    //static hasMany = [ requisitionItems: RequisitionItem, comments : Comment, documents : Document, events : Event ]
    static hasMany = [requisitionItems: RequisitionItem]
    static mapping = {
        id generator: 'uuid'
        requisitionItems cascade: "all-delete-orphan", sort: "id"
//		comments cascade: "all-delete-orphan"
//		documents cascade: "all-delete-orphan"
        //events cascade: "all-delete-orphan"
    }

    static constraints = {
        status(nullable: true)
        type(nullable: true)
        name(nullable: true)
        description(nullable: true)
        requestNumber(nullable: true, maxSize: 255)
        origin(nullable: false)
        destination(nullable: false)
        recipient(nullable: true)
        requestedBy(nullable: false)
        dateRequested(nullable: false)
        //validator: { value -> value <= new Date()})
        requestedDeliveryDate(nullable: false)
        //validator: { value ->
        //    def tomorrow = new Date().plus(1)
        //    tomorrow.clearTime()
        //    return value >= tomorrow
        //})
        fulfillment(nullable: true)
        dateCreated(nullable: true)
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
    }

    def getTransactions() {
        return Transaction.findAllByRequisition(this)
    }


    def calculatePercentageCompleted() {
        def numerator = getCompleteRequisitionItems()?.size()?:0
        def denominator = getInitialRequisitionItems()?.size()?:1
        if (denominator) {
            return (numerator / denominator)*100
        }
        else {
            return 0;
        }
    }

    /**
     * @return  all requisition items that have been completed (canceled or fulfilled)
     */
    def getCompleteRequisitionItems() {
        return initialRequisitionItems?.findAll { it.isCompleted() }
    }

    /**
     * @return  all requisition items that have not been completed
     */
    def getIncompleteRequisitionItems() {
        return initialRequisitionItems?.findAll { !it.isCompleted() }
    }

    /**
     * @return  all requisition items that were apart of the original requisition
     */
    def getInitialRequisitionItems() {
        return requisitionItems?.findAll { !it.parentRequisitionItem }
    }

    /**
     * @return  all requisition items that have been added as substitutions or supplements
     */
    def getAdditionalRequisitionItems() {
        return requisitionItems?.findAll { it.parentRequisitionItem }
    }

    Boolean isWardRequisition() {
        return (type in [RequisitionType.WARD_NON_STOCK, RequisitionType.WARD_STOCK, RequisitionType.WARD_ADHOC])
    }

    Boolean isDepotRequisition() {
        return (type in [RequisitionType.DEPOT_NON_STOCK, RequisitionType.DEPOT_STOCK, RequisitionType.DEPOT_TO_DEPOT])
    }

    Boolean isStockRequisition() {
        return (type in [RequisitionType.WARD_STOCK, RequisitionType.DEPOT_STOCK])
    }

    Boolean isPending() {
        return (status in [RequisitionStatus.CREATED]);
    }

    Boolean isOpen() {
        return (status == RequisitionStatus.CREATED)
    }

    Boolean isRequested() {
        return (status in [RequisitionStatus.FULFILLED, RequisitionStatus.ISSUED, RequisitionStatus.RECEIVED, RequisitionStatus.PICKED])
    }

    /**
     * Sort by sort order, name
     *
     * Sort requisitions by receiving location (alphabetical), requisition type, commodity class (consumables or medications), then date requested, then date created,
     */
    int compareTo(Requisition requisition) {
        return origin <=> requisition.origin ?:
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
            requisition.addToRequisitionItems(requisitionItem)
        }

        return requisition
    }

    Map toJson() {
        [
                id: id,
                name: name,
                version: version,
                requestedById: requestedBy?.id,
                requestedByName: requestedBy?.name,
                description: description,
                dateRequested: dateRequested.format("MM/dd/yyyy"),
                requestedDeliveryDate: requestedDeliveryDate.format("MM/dd/yyyy"),
                lastUpdated: lastUpdated?.format("dd/MMM/yyyy hh:mm a"),
                status: status?.name(),
                type: type?.name(),
                originId: origin?.id,
                originName: origin?.name,
                destinationId: destination?.id,
                destinationName: destination?.name,
                recipientProgram: recipientProgram,
                requisitionItems: requisitionItems?.sort()?.collect { it?.toJson() }
        ]
    }
}
