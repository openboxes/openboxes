/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.order

import grails.util.Holders
import org.pih.warehouse.core.*
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person

class Order implements Serializable {

    String id
    OrderStatus status
    OrderTypeCode orderTypeCode
    String name
    String description        // a user-defined, searchable name for the order
    String orderNumber        // an auto-generated shipment number
    Location origin            // the vendor
    Location destination    // the customer location
    Person recipient
    Person orderedBy
    Date dateOrdered
    Person completedBy
    Date dateCompleted


    // Audit fields
    Date dateCreated
    Date lastUpdated

    static hasMany = [orderItems: OrderItem, comments: Comment, documents: Document, events: Event]
    static mapping = {
        id generator: 'uuid'
        table "`order`"
        orderItems cascade: "all-delete-orphan"
        comments cascade: "all-delete-orphan"
        documents cascade: "all-delete-orphan"
        events cascade: "all-delete-orphan"
    }

    static constraints = {
        status(nullable: true)
        orderTypeCode(nullable: false)
        name(nullable: false)
        description(nullable: true, maxSize: 255)
        orderNumber(nullable: true, maxSize: 255)
        origin(nullable: false)
        destination(nullable: false)
        recipient(nullable: true)
        orderedBy(nullable: false)
        dateOrdered(nullable: true)
        completedBy(nullable: true)
        dateCompleted(nullable: true)
        dateCreated(nullable: true)
        lastUpdated(nullable: true)
    }

    /**
     * Override the status getter so that we return pending if no state set
     */
    OrderStatus getStatus() {
        return status ?: OrderStatus.PENDING
    }


    /**
     * Checks to see if this order has been received, or partially received, and
     * the update the status accordingly
     * (Note that does not know how to set to the PLACED state; this must be
     *  done manually)
     */
    OrderStatus updateStatus() {

        if (orderItems?.size() > 0 && orderItems?.size() == orderItems?.findAll {
            it.isCompletelyFulfilled()
        }?.size()) {
            status = OrderStatus.RECEIVED
        } else if (orderItems?.size() > 0 && orderItems?.find { it.isPartiallyFulfilled() }) {
            status = OrderStatus.PARTIALLY_RECEIVED
        } else if (!status) {
            status = OrderStatus.PENDING
        }

        return status
    }

    /**
     * @return a boolean indicating whether the order is pending
     */
    Boolean isPending() {
        return (status == null || status == OrderStatus.PENDING)
    }

    /**
     * @return a boolean indicating whether the order has been placed
     */
    Boolean isPlaced() {
        return (status == OrderStatus.PLACED)
    }

    /**
     * After an order is placed and before it is completed received, the order can
     * be partially received.  This occurs when the order contains items that have
     * been completely received and some that have not been completely received.
     *
     * @return
     */
    Boolean isPartiallyReceived() {
        return (status == OrderStatus.PARTIALLY_RECEIVED)
    }

    /**
     * @return a boolean indicating whether the order has been received
     */
    Boolean isReceived() {
        return (status == OrderStatus.RECEIVED)
    }

    Boolean isCompleted() {
        return (status == OrderStatus.COMPLETED)
    }

    Boolean isCanceled() {
        return (status == OrderStatus.CANCELED)
    }

    def listShipments() {
        return orderItems.collect { it.listShipments() }.flatten().unique() { it?.id }
    }


    def listOrderItems() {
        return orderItems ? orderItems.findAll {
            it.orderItemStatusCode != OrderItemStatusCode.CANCELED
        }.sort { a, b ->
            a.product?.category?.name <=> b.product?.category?.name ?:
                    a.product?.name <=> b.product?.name ?:
                            a.id <=> b.id
        } : []
    }

    def totalPrice() {
        def totalPrice = orderItems.collect { it.totalPrice() }.sum()
        return totalPrice ?: 0
    }


    String generateName() {
        final String separator =
                Holders.getConfig().getProperty("openboxes.generateName.separator") ?: Constants.DEFAULT_NAME_SEPARATOR

        String name = "${orderNumber}"
        if (dateCompleted) name += "${separator}${dateCompleted?.format("MMMMM d, yyyy")}"
        if (completedBy) name += "${separator}${completedBy.name}"
        return name
    }


}
