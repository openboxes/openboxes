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

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.*
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode

class Order implements Serializable {

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
    OrderStatus status = OrderStatus.PENDING
    OrderTypeCode orderTypeCode
    String name
    String description        // a user-defined, searchable name for the order
    String orderNumber        // an auto-generated shipment number


    Location origin           // the vendor
    Party originParty

    Location destination      // the customer location
    Party destinationParty

    Person recipient
    Person approvedBy
    Person orderedBy
    Person completedBy

    Date dateApproved
    Date dateOrdered
    Date dateCompleted

    PaymentMethodType paymentMethodType
    PaymentTerm paymentTerm

    // Currency conversion
    String currencyCode
    BigDecimal exchangeRate

    Person createdBy
    Person updatedBy

    // Audit fields
    Date dateCreated
    Date lastUpdated

    static transients = [
            "isApprovalRequired",
            "displayStatus",
            "orderedOrderItems",
            "pendingShipment",
            "receivedOrderItems",
            "shipments",
            "shippedOrderItems",
            "subtotal",
            "totalAdjustments",
            "totalOrderAdjustments",
            "totalOrderItemAdjustments",
            "total",
            "totalNormalized",
            // Statuses
            "pending",
            "placed",
            "partiallyReceived",
            "received",
            "canceled",
            "completed",

    ]

    static hasMany = [
            orderItems: OrderItem,
            comments: Comment,
            documents: Document,
            events: Event,
            orderAdjustments: OrderAdjustment,
    ]
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
        orderNumber(nullable: true, maxSize: 255, unique: true)
        currencyCode(nullable:true)
        exchangeRate(nullable:true)
        origin(nullable: false, validator: { Location origin, Order obj ->
            return !origin?.organization ? ['validator.organization.required'] : true
        })
        originParty(nullable:true)
        destination(nullable: false, validator: { Location destination, Order obj ->
            return !destination?.organization ? ['validator.organization.required'] : true
        })
        destinationParty(nullable:true)
        recipient(nullable: true)
        orderedBy(nullable: false)
        dateOrdered(nullable: true)
        approvedBy(nullable: true)
        dateApproved(nullable: true)
        completedBy(nullable: true)
        dateCompleted(nullable: true)
        paymentMethodType(nullable: true)
        paymentTerm(nullable: true)
        dateCreated(nullable: true)
        lastUpdated(nullable: true)
        createdBy(nullable: true)
        updatedBy(nullable: true)
    }

    /**
     * Override the status getter so that we return pending if no state set
     */
    OrderStatus getStatus() {
        return status
    }

    def getDisplayStatus() {
        for (ShipmentStatusCode statusCode in
                [ShipmentStatusCode.RECEIVED, ShipmentStatusCode.PARTIALLY_RECEIVED, ShipmentStatusCode.SHIPPED]) {
            if (shipments.any { Shipment shipment -> shipment?.currentStatus == statusCode}) {
                return statusCode
            }
        }
        return status
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

    Boolean getIsApprovalRequired() {
        BigDecimal minimumAmount = ConfigurationHolder.config.openboxes.purchasing.approval.minimumAmount
        return (origin?.supports([ActivityCode.APPROVE_ORDER]) ||
                destination?.supports(ActivityCode.APPROVE_ORDER)) && total > minimumAmount
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

    def getShipments() {
        return orderItems.collect { it.listShipments() }.flatten().unique() { it?.id }
    }

    List getShipmentsByStatus(ShipmentStatusCode statusCode) {
        return shipments.findAll { Shipment shipment -> shipment.currentStatus == statusCode }
    }

    Shipment getPendingShipment() {
        def pendingShipments = getShipmentsByStatus(ShipmentStatusCode.PENDING)
        if (pendingShipments.size() > 1) {
            throw new IllegalStateException("An order can only have one pending shipment")
        }
        pendingShipments ? pendingShipments?.first() : null
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

    def getOrderedOrderItems() {
        return orderItems?.findAll { it.order.status >= OrderStatus.PLACED &&
                it.orderItemStatusCode != OrderItemStatusCode.CANCELED }
    }

    def getShippedOrderItems() {
        return orderItems?.findAll { it.completelyFulfilled }
    }

    def getReceivedOrderItems() {
        return orderItems?.findAll { it.completelyReceived }
    }

    /**
     * @deprecated should use total
     * @return
     */
    def totalPrice() {
        return total
    }

    def getTotalAdjustments() {
        return totalOrderItemAdjustments + totalOrderAdjustments
    }

    def getTotalOrderAdjustments() {
        return orderAdjustments?.findAll { !it.orderItem } ?.sum {
            return it.amount ?: it.percentage ? (it.percentage/100) * subtotal : 0
        }?:0
    }
    def getTotalOrderItemAdjustments() {
        return orderItems?.sum { it?.totalAdjustments }?:0
    }

    def getSubtotal() {
        return orderItems?.sum { it?.subtotal } ?: 0
    }

    def getTotal() {
        return (subtotal + totalAdjustments)?:0
    }

    def getTotalNormalized() {
        total * lookupCurrentExchangeRate()
    }

    def lookupCurrentExchangeRate() {

        // Use fixed exchange rate if it exists on order
        if (exchangeRate) { return exchangeRate }

        // Otherwise find a suitable exchange rate from the UomConversion table (or default to 1.0)
        BigDecimal currentExchangeRate
        String defaultCurrencyCode = ConfigurationHolder.config.openboxes.locale.defaultCurrencyCode
        if (currencyCode != defaultCurrencyCode) {
            currentExchangeRate = UnitOfMeasureConversion.conversionRateLookup(defaultCurrencyCode, currencyCode).list()
        }
        return currentExchangeRate?:1.0
    }


    String generateName() {
        final String separator =
                ConfigurationHolder.config.openboxes.generateName.separator ?: Constants.DEFAULT_NAME_SEPARATOR

        String name = "${orderNumber}"
        if (dateCompleted) name += "${separator}${dateCompleted?.format("MMMMM d, yyyy")}"
        if (completedBy) name += "${separator}${completedBy.name}"
        return name
    }


    Map toJson() {
        return [
                id         : id,
                orderNumber: orderNumber,
                name       : name,
                status     : status,
                origin     : origin,
                destination: destination,
                orderItems : orderItems,
        ]
    }

}
