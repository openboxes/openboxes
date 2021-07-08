/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.stockTransfer

import org.pih.warehouse.api.StockTransfer
import org.pih.warehouse.api.StockTransferItem
import org.pih.warehouse.api.StockTransferStatus
import org.pih.warehouse.core.Location
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.product.ProductAvailability

class StockTransferService {

    def locationService
    def inventoryService
    def productAvailabilityService
    def grailsApplication

    boolean transactional = true

    def getStockTransferCandidates(Location location) {
        List stockTransferItems = []

        List stockTransferCandidates = productAvailabilityService.getStockTransferCandidates(location)

        stockTransferCandidates?.each { ProductAvailability productAvailability ->
            stockTransferItems << StockTransferItem.createFromProductAvailability(productAvailability)
        }

        List<StockTransferItem> pendingStockTransferItems = getPendingItems(location)

        stockTransferItems.removeAll { StockTransferItem item ->
            pendingStockTransferItems.find {
                item.location?.id == it.location?.id && item.inventoryItem?.id == it.inventoryItem?.id &&
                        item.product?.id == it.product?.id
            }
        }

        return stockTransferItems
    }

    List<StockTransferItem> getPendingItems(Location location) {
        List<Order> orders = Order.findAllByOriginAndOrderType(location, OrderType.findByCode(OrderTypeCode.TRANSFER_ORDER.name()))
        List<StockTransfer> stockTransfers = orders.collect { StockTransfer.createFromOrder(it) }
        List<StockTransferItem> stockTransferItems = []

        stockTransfers.each {
            stockTransferItems.addAll(it.stockTransferItems.findAll {
                it.status == StockTransferStatus.PENDING ||
                    (it.status == StockTransferStatus.CANCELED && it.splitItems?.any { item -> item.status == StockTransferStatus.PENDING })
            })
        }

        return stockTransferItems
    }

    Order createOrderFromStockTransfer(StockTransfer stockTransfer) {

        Order order = Order.get(stockTransfer.id)
        if (!order) {
            order = new Order()
        }

        OrderType orderType = OrderType.findByCode(OrderTypeCode.TRANSFER_ORDER.name())
        order.orderType = orderType
        order.status = OrderStatus.valueOf(stockTransfer.status.toString())
        if (!order.orderNumber) {
            order.orderNumber = stockTransfer.stockTransferNumber
        }
        order.orderedBy = stockTransfer.orderedBy
        order.dateOrdered = new Date()
        order.origin = stockTransfer.origin
        order.destination = stockTransfer.destination

        // Set auditing data on completion
        if (stockTransfer.status == StockTransferStatus.COMPLETED) {
            order.completedBy = stockTransfer.orderedBy
            order.dateCompleted = new Date()
        }

        // Generate name
        order.name = order.generateName()

        stockTransfer.stockTransferItems.toArray().each { StockTransferItem stockTransferItem ->

            OrderItem orderItem
            if (stockTransferItem.id) {
                orderItem = order.orderItems?.find { it.id == stockTransferItem.id }
            }

            if (!orderItem) {
                orderItem = new OrderItem()
                order.addToOrderItems(orderItem)
            }

            orderItem = updateOrderItem(stockTransferItem, orderItem)

            stockTransferItem.splitItems.each { StockTransferItem splitItem ->
                OrderItem childOrderItem
                if (splitItem.id) {
                    childOrderItem = order.orderItems?.find { it.id == splitItem.id }
                }

                if (!childOrderItem && !splitItem.delete) {
                    childOrderItem = new OrderItem()
                    order.addToOrderItems(childOrderItem)
                }

                if (childOrderItem && splitItem.delete) {
                    orderItem.removeFromOrderItems(childOrderItem)
                    order.removeFromOrderItems(childOrderItem)

                    childOrderItem.delete()
                } else if (childOrderItem) {
                    childOrderItem = updateOrderItem(splitItem, childOrderItem)
                    childOrderItem.parentOrderItem = orderItem
                }
            }
        }

        order.save(failOnError: true)
        return order
    }

    Order deleteStockTransferItem(String id) {
        OrderItem orderItem = OrderItem.get(id)
        if (!orderItem) {
            throw new IllegalArgumentException("No stockTransfer item found with ID ${id}")
        }

        def splitItems = orderItem.orderItems?.toArray()

        splitItems?.each { OrderItem item ->
            orderItem.removeFromOrderItems(item)
            item.order.removeFromOrderItems(item)
            item.delete()
        }

        Order order = orderItem.order
        order.removeFromOrderItems(orderItem)
        orderItem.delete()
        return order
    }

    OrderItem updateOrderItem(StockTransferItem stockTransferItem, OrderItem orderItem) {
        OrderItemStatusCode orderItemStatusCode =
                !stockTransferItem?.splitItems?.empty ? OrderItemStatusCode.CANCELED :
                        stockTransferItem.status == StockTransferStatus.COMPLETED ? OrderItemStatusCode.COMPLETED : OrderItemStatusCode.PENDING

        orderItem.orderItemStatusCode = orderItemStatusCode
        orderItem.product = stockTransferItem.product
        orderItem.inventoryItem = stockTransferItem.inventoryItem
        orderItem.quantity = stockTransferItem.quantity
        orderItem.originBinLocation = stockTransferItem.originBinLocation
        orderItem.destinationBinLocation = stockTransferItem.destinationBinLocation
        return orderItem
    }
}
