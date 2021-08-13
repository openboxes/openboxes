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

import org.apache.commons.beanutils.BeanUtils
import org.pih.warehouse.api.StockTransfer
import org.pih.warehouse.api.StockTransferItem
import org.pih.warehouse.api.StockTransferStatus
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.TransferStockCommand
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

        OrderItem parentItem = orderItem?.parentOrderItem
        Order order = orderItem.order
        order.removeFromOrderItems(orderItem)
        if (parentItem) {
            parentItem.removeFromOrderItems(orderItem)
        }
        orderItem.delete()

        if (parentItem?.orderItems?.toArray()?.size() == 0 && parentItem.orderItemStatusCode == OrderItemStatusCode.CANCELED) {
            parentItem.orderItemStatusCode = OrderItemStatusCode.PENDING
            parentItem.save()
        }
        return order
    }

    OrderItem updateOrderItem(StockTransferItem stockTransferItem, OrderItem orderItem) {
        def emptySplitItems = stockTransferItem?.splitItems?.empty
        def statusCompleted = (stockTransferItem.status == StockTransferStatus.COMPLETED || orderItem?.order?.status == OrderStatus.COMPLETED)
        OrderItemStatusCode orderItemStatusCode = !emptySplitItems ? OrderItemStatusCode.CANCELED :
            (statusCompleted ? OrderItemStatusCode.COMPLETED : OrderItemStatusCode.PENDING)

        orderItem.orderItemStatusCode = orderItemStatusCode
        orderItem.product = stockTransferItem.product
        orderItem.inventoryItem = stockTransferItem.inventoryItem
        orderItem.quantity = stockTransferItem.quantity
        orderItem.originBinLocation = stockTransferItem.originBinLocation
        orderItem.destinationBinLocation = stockTransferItem.destinationBinLocation
        return orderItem
    }

    Order completeStockTransfer(StockTransfer stockTransfer) {
        validateStockTransfer(stockTransfer)

        // Save the stockTransfer as an order
        Order order = createOrderFromStockTransfer(stockTransfer)

        // Need to process the split items
        processSplitItems(stockTransfer)

        stockTransfer.stockTransferItems.each { StockTransferItem stockTransferItem ->
            TransferStockCommand command = new TransferStockCommand()
            command.location = stockTransferItem.location?:stockTransfer?.destination
            command.binLocation = stockTransferItem.originBinLocation
            command.inventoryItem = stockTransferItem.inventoryItem
            command.quantity = stockTransferItem.quantity
            command.otherLocation = stockTransferItem.location
            command.otherBinLocation = stockTransferItem.destinationBinLocation
            command.order = order
            command.transferOut = Boolean.TRUE
            inventoryService.transferStock(command)
        }

        return order
    }

    void processSplitItems(StockTransfer stockTransfer) {

        stockTransfer.stockTransferItems.toArray().each { StockTransferItem oldStockTransferItem ->

            if (oldStockTransferItem.splitItems) {
                // Iterate over split items and create new stock transfer items for them
                // NOTE: The only fields we change from the original are the stock transfer destination bin and quantity.
                oldStockTransferItem.splitItems.each { StockTransferItem splitSockTransferItem ->
                    StockTransferItem newStockTransferItemItem = new StockTransferItem()
                    BeanUtils.copyProperties(newStockTransferItemItem, oldStockTransferItem)
                    newStockTransferItemItem.quantity = splitSockTransferItem.quantity
                    newStockTransferItemItem.location = splitSockTransferItem.location
                    newStockTransferItemItem.destinationBinLocation = splitSockTransferItem.destinationBinLocation
                    stockTransfer.stockTransferItems.add(newStockTransferItemItem)
                }

                // Remove the original stock transfer item since it was replaced with the above
                stockTransfer.stockTransferItems.remove(oldStockTransferItem)
            }
        }
    }

    void validateStockTransfer(StockTransfer stockTransfer) {
        stockTransfer.stockTransferItems.toArray().each { StockTransferItem stockTransferItem ->
            validateStockTransferItem(stockTransferItem)
        }
    }

    void validateStockTransferItem(StockTransferItem stockTransferItem) {
        def quantity = stockTransferItem.quantity

        if (stockTransferItem.splitItems) {
            quantity = stockTransferItem.splitItems.sum { it.quantity }
        }

        validateQuantityAvailable(stockTransferItem.location, stockTransferItem.originBinLocation, stockTransferItem.inventoryItem, quantity)
    }

    void validateQuantityAvailable(Location location , Location originBinLocation, InventoryItem inventoryItem, BigDecimal quantity) {

        if (!location) {
            throw new IllegalArgumentException("Location is required")
        }

        Integer quantityAvailable = productAvailabilityService.getQuantityOnHandInBinLocation(inventoryItem, originBinLocation)
        log.info "Quantity: ${quantity} vs ${quantityAvailable}"

        if (quantityAvailable < 0) {
            throw new IllegalStateException("The inventory item is no longer available at the specified location ${location} and bin ${originBinLocation} ")
        }

        if (quantity > quantityAvailable) {
            throw new IllegalStateException("Quantity available ${quantityAvailable} is less than quantity to stock transfer ${quantity} for product ${inventoryItem.product.productCode} ${inventoryItem.product.name}")
        }
    }

    def setQuantityOnHand(StockTransfer stockTransfer) {
        stockTransfer?.stockTransferItems?.each { StockTransferItem stockTransferItem ->
            stockTransferItem.quantityOnHand = productAvailabilityService.getQuantityOnHandInBinLocation(stockTransferItem.inventoryItem, stockTransferItem.originBinLocation)
        }
    }
}
