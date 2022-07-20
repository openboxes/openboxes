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
import org.pih.warehouse.api.DocumentGroupCode
import org.pih.warehouse.api.StockTransfer
import org.pih.warehouse.api.StockTransferItem
import org.pih.warehouse.api.StockTransferStatus
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.TransferStockCommand
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.ProductAvailability
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

class StockTransferService {

    def locationService
    def inventoryService
    def productAvailabilityService
    def picklistService
    def shipmentService
    def grailsApplication
    def orderService

    boolean transactional = true

    def getStockTransfers(Order orderTemplate, Date lastUpdatedStartDate, Date lastUpdatedEndDate, Map params) {
        def orders = Order.createCriteria().list(params) {
            and {
                if (params.q) {
                    or {
                        ilike("name", "%" + params.q + "%")
                        ilike("description", "%" + params.q + "%")
                        ilike("orderNumber", "%" + params.q + "%")
                    }
                }
                if (orderTemplate.orderType) {
                    eq("orderType", orderTemplate.orderType)
                }
                if (orderTemplate.destination) {
                    eq("destination", orderTemplate.destination)
                }
                if (orderTemplate.origin) {
                    eq("origin", orderTemplate.origin)
                }
                if (orderTemplate.status) {
                    eq("status", orderTemplate.status)
                }
                if (lastUpdatedStartDate) {
                    ge("lastUpdated", lastUpdatedStartDate)
                }
                if (lastUpdatedEndDate) {
                    le("lastUpdated", lastUpdatedEndDate)
                }
                if (orderTemplate.orderedBy) {
                    eq("orderedBy", orderTemplate.orderedBy)
                }
                if (orderTemplate.createdBy) {
                    eq("createdBy", orderTemplate.createdBy)
                }
            }
            order("dateCreated", "desc")
        }
        return orders
    }

    def getStockTransferCandidates(Location location, Map params) {
        List<StockTransferItem> stockTransferItems = []

        List stockTransferCandidates = productAvailabilityService.getStockTransferCandidates(location, params)

        stockTransferCandidates?.each { ProductAvailability productAvailability ->
            stockTransferItems << StockTransferItem.createFromProductAvailability(productAvailability)
        }

        if (!params) {
            List<StockTransferItem> pendingStockTransferItems = getPendingItems(location)

            stockTransferItems.removeAll { StockTransferItem item ->
                pendingStockTransferItems.find {
                    item.location?.id == it.location?.id && item.inventoryItem?.id == it.inventoryItem?.id &&
                            item.originBinLocation?.id == it.originBinLocation?.id &&
                            item.product?.id == it.product?.id
                }
            }
        }

        return stockTransferItems.sort { a, b ->
            a.product?.productCode <=> b.product?.productCode ?:
                a.inventoryItem?.lotNumber <=> b.inventoryItem?.lotNumber ?:
                    a.originBinLocation?.zone?.name <=> b.originBinLocation?.zone?.name ?:
                        a.originBinLocation?.name <=> b.originBinLocation?.name }
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

    Order createOrUpdateOrderFromStockTransfer(StockTransfer stockTransfer) {
        Order order = Order.get(stockTransfer.id)
        if (!order) {
            order = new Order()
        }

        if (!order.orderType) {
            order.orderType = stockTransfer.type ?: OrderType.findByCode(OrderTypeCode.TRANSFER_ORDER.name())
        }
        order.status = OrderStatus.valueOf(stockTransfer.status.toString())
        if (!order.orderNumber) {
            order.orderNumber = stockTransfer.stockTransferNumber
        }
        order.orderedBy = stockTransfer.orderedBy
        order.dateOrdered = new Date()
        order.origin = stockTransfer.origin
        order.destination = stockTransfer.destination
        order.description = stockTransfer.description

        // Set auditing data on completion
        if (stockTransfer.status == StockTransferStatus.COMPLETED) {
            order.completedBy = stockTransfer.orderedBy
            order.dateCompleted = new Date()
        }

        // Generate name
        order.name = order.generateName()

        // Remove order items that were removed from stock transfer items
        def itemsToRemove = order?.orderItems?.findAll { OrderItem item -> !stockTransfer?.stockTransferItems?.find { it.id == item.id } }
        itemsToRemove?.each { it -> deleteOrderItem(order, it) }

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
                OrderItem childOrderItem = null
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
                    orderItem.addToOrderItems(childOrderItem)
                    childOrderItem.parentOrderItem = orderItem
                }
            }
        }
        order.save(failOnError: true)

        def currentLocation = AuthService?.currentLocation?.get()
        if (order.orderType.isReturnOrder() && order.isOutbound(currentLocation) && order?.orderItems) {
            picklistService.createPicklistFromItem(order)
        }

        return order
    }

    Order deleteStockTransferItem(String id) {
        OrderItem orderItem = OrderItem.get(id)
        if (!orderItem) {
            throw new IllegalArgumentException("No stockTransfer item found with ID ${id}")
        }

        return deleteOrderItem(orderItem.order, orderItem)
    }

    Order deleteAllStockTransferItems(String id) {
        Order order = Order.get(id)
        if (!order) {
            throw new IllegalArgumentException("No stockTransfer found with ID ${id}")
        }

        def itemsToRemove = order.orderItems?.findAll { it }
        itemsToRemove?.each { it -> deleteOrderItem(order, it) }

        return order
    }

    Order deleteOrderItem(Order order, OrderItem orderItem) {
        def splitItems = orderItem.orderItems?.toArray()

        splitItems?.each { OrderItem item ->
            orderItem.removeFromOrderItems(item)
            item.order.removeFromOrderItems(item)
            item.delete()
        }

        List<PicklistItem> picklistItems = PicklistItem.findAllByOrderItem(orderItem)
        if (picklistItems) {
            picklistItems.each { PicklistItem picklistItem ->
                picklistItem.disableRefresh = Boolean.TRUE
                picklistItem.picklist?.removeFromPicklistItems(picklistItem)
                picklistItem.orderItem?.removeFromPicklistItems(picklistItem)
                picklistItem.delete(flush: true)
            }
            def productsToRefresh = picklistItems.collect {it.inventoryItem?.product?.id}.unique()
            productAvailabilityService.refreshProductsAvailability(orderItem?.order?.origin?.id, productsToRefresh, false)
        }

        OrderItem parentItem = orderItem?.parentOrderItem
        order.removeFromOrderItems(orderItem)
        if (parentItem) {
            parentItem.removeFromOrderItems(orderItem)
        }

        def shipmentItems = orderItem.shipmentItems
        if (shipmentItems) {
            shipmentItems.each { ShipmentItem shipmentItem ->
                Shipment shipment = shipmentItem.shipment
                shipment.removeFromShipmentItems(shipmentItem)
                orderItem.removeFromShipmentItems(shipmentItem)
                shipmentItem.delete()
            }
        }

        orderItem.delete()

        if (parentItem?.orderItems?.toArray()?.size() == 0 && parentItem.orderItemStatusCode == OrderItemStatusCode.CANCELED) {
            parentItem.orderItemStatusCode = OrderItemStatusCode.PENDING
            parentItem.save()
        }
        return order.save(flush: true)
    }

    void deleteStockTransfer(String id) {
        Order order = Order.get(id)
        if (!order) {
            throw new IllegalArgumentException("No stockTransfer item found with ID ${id}")
        }

        orderService.deleteOrder(order)
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
        orderItem.recipient = stockTransferItem.recipient
        orderItem.orderIndex = stockTransferItem.orderIndex
        return orderItem
    }

    Order completeStockTransfer(StockTransfer stockTransfer) {
        validateStockTransfer(stockTransfer)

        // Save the stockTransfer as an order
        Order order = createOrUpdateOrderFromStockTransfer(stockTransfer)

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

        // Get quantity available as not picked, we allow transferring from on hold bins or recalled in this workflow
        Integer quantityAvailable
        if (originBinLocation) {
            quantityAvailable = productAvailabilityService.getQuantityNotPickedInBinLocation(inventoryItem, location, originBinLocation)
        } else {
            quantityAvailable = productAvailabilityService.getQuantityNotPickedInLocation(inventoryItem.product, location)
        }

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
            ProductAvailability pa = ProductAvailability.findByInventoryItemAndBinLocation(stockTransferItem.inventoryItem, stockTransferItem.originBinLocation)
            stockTransferItem.quantityOnHand = pa ? pa.quantityOnHand : 0
            stockTransferItem.quantityNotPicked = pa && pa.quantityNotPicked > 0 ? pa.quantityNotPicked: 0
            stockTransferItem.productAvailabilityId = pa ? pa.id : stockTransferItem.id

            if (stockTransferItem.splitItems) {
                stockTransferItem.splitItems.each { splitItem ->
                    splitItem.quantityOnHand = stockTransferItem.quantityOnHand
                    splitItem.quantityNotPicked = stockTransferItem.quantityNotPicked
                    splitItem.productAvailabilityId = stockTransferItem.productAvailabilityId
                }
            }
        }
    }

    def getDocuments(StockTransfer stockTransfer) {
        def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
        stockTransfer.documents = [[
            name        : g.message(code: "picklist.button.download.label"),
            documentType: DocumentGroupCode.PICKLIST.name(),
            contentType : "application/pdf",
            uri         : g.createLink(controller: 'picklist', action: "returnPrint", id: stockTransfer?.id, absolute: true),
        ]]
    }

    def rollbackReturnOrder(String orderId, Location currentLocation) {
        Order order = Order.get(orderId)
        if (!order) {
            throw new IllegalArgumentException("Can't find order with given id: ${orderId}")
        }

        if(currentLocation != order?.origin && currentLocation != order?.destination) {
            throw new IllegalArgumentException("Can't rollback shipment from current location")
        }

        Shipment returnOrderShipment = order.shipments.first() as Shipment;
        if(!returnOrderShipment) {
            throw new IllegalArgumentException("Order with id: ${orderId} has no shipments")
        }

        // For returns there should be only one shipment for the given order
        shipmentService.rollbackLastEvent(returnOrderShipment)
        order.status = OrderStatus.PLACED;

    }
}
