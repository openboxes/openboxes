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

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode

class OrderItem implements Serializable, Comparable<OrderItem> {

    String id
    String description
    Category category
    Product product
    InventoryItem inventoryItem
    Integer quantity
    UnitOfMeasure quantityUom
    BigDecimal quantityPerUom = 1

    BigDecimal unitPrice
    String currencyCode
    ProductSupplier productSupplier
    ProductPackage productPackage

    User requestedBy    // the person who actually requested the item
    Person recipient

    OrderItemStatusCode orderItemStatusCode = OrderItemStatusCode.PENDING

    // Transfer order
    Location originBinLocation
    Location destinationBinLocation

    Date estimatedReadyDate
    Date estimatedShipDate
    Date estimatedDeliveryDate

    Date actualReadyDate
    Date actualShipDate
    Date actualDeliveryDate

    // Audit fields
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id generator: 'uuid'
        shipmentItems joinTable: [name: 'order_shipment', key: 'order_item_id']
    }

    static transients = [
            "orderItemType",
            "quantityInStandardUom",
            "quantityRemaining",
            "quantityReceived",
            "quantityReceivedInStandardUom",
            "quantityShipped",
            "quantityShippedInStandardUom",
            "total",
            "shippedShipmentItems",
            "subtotal",
            "totalAdjustments",
            "unitOfMeasure",
            // Statuses
            "partiallyFulfilled",
            "completelyFulfilled",
            "completelyReceived",
            "pending",
    ]

    static belongsTo = [order: Order, parentOrderItem: OrderItem]

    static hasMany = [orderItems: OrderItem, shipmentItems: ShipmentItem, orderAdjustments: OrderAdjustment]

    static constraints = {
        description(nullable: true)
        category(nullable: true)
        product(nullable: true)
        inventoryItem(nullable: true)
        requestedBy(nullable: true)
        quantity(nullable: false, min: 1)
        quantityUom(nullable: true)
        quantityPerUom(nullable: false)
        productPackage(nullable: true)
        unitPrice(nullable: true)
        orderItemStatusCode(nullable: true)
        parentOrderItem(nullable: true)
        originBinLocation(nullable: true)
        destinationBinLocation(nullable: true)
        recipient(nullable: true)
        currencyCode(nullable: true)
        productSupplier(nullable: true)
        estimatedReadyDate(nullable: true)
        estimatedShipDate(nullable: true)
        estimatedDeliveryDate(nullable: true)
        actualReadyDate(nullable: true)
        actualShipDate(nullable: true)
        actualDeliveryDate(nullable: true)
    }

    String getUnitOfMeasure() {
        if (quantityUom) {
            return "${quantityUom?.code}/${quantityPerUom as Integer}"
        }
        else {
            def g = ApplicationHolder.application.mainContext.getBean( 'org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib' )
            return "${g.message(code:'default.ea.label').toUpperCase()}/1"
        }
    }

    def hasShipmentAssociated() {
        return shipmentItems ? shipmentItems.size() > 0 : false
    }

    def getShippedShipmentItems() {
        return shipmentItems.findAll { it.shipment.currentStatus >= ShipmentStatusCode.SHIPPED }
    }

    def hasShippedItems() {
        return shippedShipmentItems?shippedShipmentItems.size()>0:false
    }

    Integer getQuantityInStandardUom() {
        return quantity * quantityPerUom
    }

    Integer getQuantityShippedInStandardUom() {
        return shippedShipmentItems?.sum { ShipmentItem shipmentItem ->
            shipmentItem?.quantity
        }?:0
    }

    Integer getQuantityReceivedInStandardUom() {
        return shippedShipmentItems?.sum { ShipmentItem shipmentItem ->
            shipmentItem?.quantityReceived
        }?:0
    }

    Integer getQuantityShipped() {
        return quantityShippedInStandardUom / quantityPerUom
    }

    Integer getQuantityReceived() {
        return quantityReceivedInStandardUom / quantityPerUom
    }

    String getOrderItemType() {
        return "Product"
    }

    Integer getQuantityRemaining() {
        def quantityRemaining = quantity - quantityShipped
        return quantityRemaining > 0 ? quantityRemaining : 0
    }

    Boolean isPartiallyFulfilled() {
        return quantityShipped > 0 && quantityShipped < quantity
    }

    Boolean isCompletelyFulfilled() {
        return quantityShipped >= quantity
    }

    Boolean isCompletelyReceived() {
        return quantityReceived >= quantity
    }

    Boolean isPending() {
        return !isCompletelyFulfilled()
    }

    /**
     * Gets all shipment items related to this order item
     * (ignoring any orphaned shipment item references)
     *
     * @return
     */
    def shipmentItems() {
        return shipmentItems
    }

    /**
     * Gets all shipments related to this order item
     * (ignoring any orphaned shipment item references)
     *
     * @return
     */
    def listShipments() {
        return shipmentItems*.shipment
    }

    def totalPrice() {
        return total
    }

    def getTotalAdjustments() {
        return orderAdjustments?.sum {
            return it.amount ?: it.percentage ? (it.percentage/100) * subtotal : 0
        }?:0
    }

    def getSubtotal() {
        return (quantity ?: 0.0) * (unitPrice ?: 0.0)
    }

    def getTotal() {
        return (subtotal + totalAdjustments)?:0
    }

    String toString() {
        return product?.name
    }

    int compareTo(OrderItem orderItem) {
        def sortOrder =
                dateCreated <=> orderItem?.dateCreated ?:
                        product?.name <=> orderItem?.product?.name ?:
                                quantity <=> orderItem?.quantity ?:
                                        id <=> orderItem?.id
        return sortOrder
    }


    Map toJson() {
        return [
                id           : id,
                product      : product,
                quantity     : quantity,
                shipmentItems: shipmentItems,
        ]
    }



}
