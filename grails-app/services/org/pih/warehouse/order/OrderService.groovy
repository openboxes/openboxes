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

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.grails.plugins.csv.CSVMapReader
import org.pih.warehouse.core.*
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductException
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentException
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentService

class OrderService {

    boolean transactional = true

    UserService userService
    ShipmentService shipmentService
    IdentifierService identifierService
    InventoryService inventoryService
    GrailsApplication grailsApplication

    def getOrders(Order orderTemplate, Date dateOrderedFrom, Date dateOrderedTo, Map params) {
        def orders = Order.createCriteria().list(params) {
            and {
                if (orderTemplate.name || orderTemplate.description) {
                    or {
                        ilike("name", "%" + orderTemplate.name + "%")
                        ilike("description", "%" + orderTemplate.name + "%")
                    }
                }
                if (orderTemplate.orderNumber) {
                    ilike("orderNumber", "%" + orderTemplate.orderNumber + "%")
                }
                if (orderTemplate.orderTypeCode) {
                    eq("orderTypeCode", orderTemplate.orderTypeCode)
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
                if (dateOrderedFrom) {
                    ge("dateOrdered", dateOrderedFrom)
                }
                if (dateOrderedTo) {
                    le("dateOrdered", dateOrderedTo)
                }
                if (orderTemplate.orderedBy) {
                    eq("orderedBy", orderTemplate.orderedBy)
                }
            }
            order("dateOrdered", "desc")
        }
        return orders
    }

    /**
     * @param location
     * @return a list of pending incoming order into the given location
     */
    List<Order> getIncomingOrders(Location location) {
        return Order.findAllByDestination(location)
    }


    /**
     * @param location
     * @return a list of pending outgoing order from the given location
     */
    List<Order> getOutgoingOrders(Location location) {
        return Order.findAllByOrigin(location)
    }

    /**
     * @return a list of suppliers
     */
    List<Location> getSuppliers() {
        def suppliers = []
        LocationType supplierType = LocationType.findById(Constants.SUPPLIER_LOCATION_TYPE_ID)
        if (supplierType) {
            suppliers = Location.findAllByLocationType(supplierType)
        }
        return suppliers

    }

    /**
     * @param id an identifier for the order
     * @param recipientId
     * @return an command object based on an order with the given
     */
    OrderCommand getOrder(String id, String recipientId) {
        def orderCommand = new OrderCommand()

        def orderInstance = Order.get(id)
        if (!orderInstance)
            throw new Exception("Unable to locate order with ID " + id)

        if (recipientId) {
            orderCommand.recipient = Person.get(recipientId)
        }

        orderCommand.origin = Location.get(orderInstance?.origin?.id)
        orderCommand.destination = Location.get(orderInstance?.destination?.id)
        orderCommand.orderedBy = Person.get(orderInstance?.orderedBy?.id)
        orderCommand.dateOrdered = orderInstance?.dateOrdered
        orderCommand.order = orderInstance
        orderInstance?.listOrderItems()?.each {
            def orderItemCommand = new OrderItemCommand()
            orderItemCommand.primary = true
            orderItemCommand.orderItem = it
            orderItemCommand.type = it.orderItemType
            orderItemCommand.description = it.description
            orderItemCommand.productReceived = it.product
            orderItemCommand.quantityOrdered = it.quantity
            orderCommand?.orderItems << orderItemCommand
        }
        return orderCommand
    }

    /**
     *
     * @param orderCommand
     * @return
     */
    OrderCommand saveOrderShipment(OrderCommand orderCommand) {
        def shipmentInstance = new Shipment()
        def shipments = orderCommand?.order?.listShipments()
        def numberOfShipments = (shipments) ? shipments?.size() + 1 : 1

        shipmentInstance.name = orderCommand?.order?.name + " - " + "Shipment #" + numberOfShipments
        shipmentInstance.shipmentType = orderCommand?.shipmentType
        shipmentInstance.origin = orderCommand?.order?.origin
        shipmentInstance.destination = orderCommand?.order?.destination
        shipmentInstance.expectedDeliveryDate = orderCommand?.deliveredOn
        shipmentInstance.expectedShippingDate = orderCommand?.shippedOn

        orderCommand?.shipment = shipmentInstance
        orderCommand?.orderItems.each { orderItemCommand ->

            // Ignores any null order items and makes sure that the order item has a product and quantity
            if (orderItemCommand && orderItemCommand.productReceived && orderItemCommand?.quantityReceived) {

                // Find or create a new inventory item based on the product and lot number provided
                def inventoryItem = null

                // Need to use withSession here otherwise it flushes the current session and causes an error
                InventoryItem.withSession { session ->
                    inventoryItem = inventoryService.findOrCreateInventoryItem(orderItemCommand.productReceived, orderItemCommand.lotNumber, orderItemCommand.expirationDate)
                    session.flush()
                    session.clear()
                }

                def shipmentItem = new ShipmentItem()
                shipmentItem.lotNumber = orderItemCommand.lotNumber
                shipmentItem.expirationDate = orderItemCommand.expirationDate
                shipmentItem.product = orderItemCommand.productReceived
                shipmentItem.quantity = orderItemCommand.quantityReceived
                shipmentItem.recipient = orderCommand?.recipient
                shipmentItem.inventoryItem = inventoryItem
                shipmentItem.addToOrderItems(orderItemCommand?.orderItem)
                shipmentInstance.addToShipmentItems(shipmentItem)
            }
        }

        // Validate the shipment and save it if there are no errors
        if (shipmentInstance.validate() && !shipmentInstance.hasErrors()) {
            shipmentService.saveShipment(shipmentInstance)
        } else {
            log.info("Errors with shipment " + shipmentInstance?.errors)
            throw new ShipmentException(message: "Validation errors on shipment ", shipment: shipmentInstance)
        }

        // Send shipment, receive shipment, and add
        if (shipmentInstance) {
            // Send shipment
            log.info "Sending shipment " + shipmentInstance?.name
            shipmentService.sendShipment(shipmentInstance, "", orderCommand?.currentUser, orderCommand?.currentLocation, orderCommand?.shippedOn)

            // Receive shipment
            log.info "Receiving shipment " + shipmentInstance?.name
            Receipt receiptInstance = shipmentService.createReceipt(shipmentInstance, orderCommand?.deliveredOn)

            // FIXME
            // receiptInstance.validate() && !receiptInstance.hasErrors()
            if (!receiptInstance.hasErrors() && receiptInstance.save()) {
                shipmentService.receiveShipment(shipmentInstance?.id, null, orderCommand?.currentUser?.id, orderCommand?.currentLocation?.id, true)
            } else {
                throw new ShipmentException(message: "Unable to save receipt ", shipment: shipmentInstance)
            }

            saveOrder(orderCommand?.order)
        }
        return orderCommand
    }

    /**
     *
     * @param order
     * @return
     */
    Order saveOrder(Order order) {
        // update the status of the order before saving
        order.updateStatus()

        if (!order.orderNumber) {
            order.orderNumber = identifierService.generateOrderIdentifier()
        }

        if (!order.hasErrors() && order.save()) {
            return order
        } else {
            println order.errors
            throw new OrderException(message: "Unable to save order due to errors", order: order)
        }
    }

    Order placeOrder(String id, String userId) {
        def orderInstance = Order.get(id)
        def userInstance = User.get(userId)
        if (orderInstance) {
            if (orderInstance?.status >= OrderStatus.PLACED) {
                orderInstance.errors.rejectValue("status", "order.hasAlreadyBeenPlaced.message")
            } else {
                if (orderInstance?.orderItems?.size() > 0) {
                    if (canApproveOrder(orderInstance, userInstance)) {
                        orderInstance.status = OrderStatus.PLACED
                        orderInstance.dateApproved = new Date()
                        orderInstance.approvedBy = userInstance
                        if (!orderInstance.hasErrors() && orderInstance.save(flush: true)) {
                            return orderInstance
                        }
                    }
                    else {
                        orderInstance.errors.reject("User does not have permission to approve order")
                    }

                } else {
                    orderInstance.errors.rejectValue("orderItems", "order.mustContainAtLeastOneItem.message")
                }
            }
        }
        return orderInstance
    }

    boolean canApproveOrder(Order order, User userInstance) {
        if (isApprovalRequired(order)) {
            List<RoleType> defaultRoleTypes = grailsApplication.config.openboxes.purchasing.approval.defaultRoleTypes
            return userService.hasAnyRoles(userInstance, defaultRoleTypes)
        }
        return Boolean.TRUE
    }

    boolean isApprovalRequired(Order order) {
        // FIXME this could take order into account (see Order.isApprovalRequired())
        return grailsApplication.config.openboxes.purchasing.approval.enabled
    }

    /**
     *
     * @param location
     * @return
     */
    Map getIncomingQuantityByProduct(Location location, List<Product> products) {
        return getQuantityByProduct(getIncomingOrders(location), products)
    }

    /**
     * Returns a list of outgoing quantity per product given location.
     * @param location
     * @return
     */
    Map getOutgoingQuantityByProduct(Location location, List<Product> products) {
        return getQuantityByProduct(getOutgoingOrders(location), products)
    }


    /**
     * Returns a map of order quantities per product given a list of orders.
     *
     * @param orders
     * @return
     */
    Map getQuantityByProduct(def orders, List<Product> products) {
        def quantityMap = [:]
        orders.each { order ->
            order.orderItems.each { orderItem ->
                def product = orderItem.product
                if (product) {
                    if (products.contains(product)) {
                        def quantity = quantityMap[product]
                        if (!quantity) quantity = 0
                        quantity += orderItem.quantity
                        quantityMap[product] = quantity
                    }
                }
            }
        }
        return quantityMap
    }

    /**
     * Rollback the latest status change for the given order.
     *
     * @param orderInstance
     */
    void rollbackOrderStatus(String orderId) {

        Order orderInstance = Order.get(orderId)
        if (!orderInstance) {
            throw new RuntimeException("Unable to locate order with order ID ${orderId}")
        }

        try {

            if (orderInstance.status in [OrderStatus.PLACED,  OrderStatus.PARTIALLY_RECEIVED, OrderStatus.RECEIVED]) {
                orderInstance?.listShipments().each { Shipment shipmentInstance ->
                    if (shipmentInstance) {

                        shipmentInstance.incomingTransactions.each { transactionInstance ->
                            if (transactionInstance) {
                                shipmentInstance.removeFromIncomingTransactions(transactionInstance)
                                transactionInstance?.delete()
                            }
                        }

                        shipmentInstance.shipmentItems.flatten().toArray().each { ShipmentItem shipmentItem ->

                            // Remove all order shipment records associated with this shipment item
                            shipmentItem.orderItems?.flatten().toArray().each { OrderItem orderItem ->
                                orderItem.removeFromShipmentItems(shipmentItem)
                                shipmentItem.removeFromOrderItems(orderItem)
                            }

                            // Remove the shipment item from the shipment
                            shipmentInstance.removeFromShipmentItems(shipmentItem)

                            // Delete the shipment item
                            shipmentItem.delete()
                        }

                        shipmentInstance.events.toArray().each { Event event ->
                            shipmentInstance.removeFromEvents(event)
                            shipmentInstance.currentEvent = null
                        }

                        // Delete all receipt items associated with the receipt
                        shipmentInstance?.receipt?.receiptItems?.toArray().each { receiptItem ->
                            shipmentInstance.receipt.removeFromReceiptItems(receiptItem)
                            receiptItem.delete()
                        }

                        // Delete the receipt from the shipment
                        if (shipmentInstance.receipt) {
                            shipmentInstance?.receipt?.delete()
                        }

                        // Delete the shipment
                        shipmentInstance.delete()

                    }
                }
                orderInstance.status = OrderStatus.PENDING
                orderInstance.approvedBy = null
                orderInstance.dateApproved = null

            } else if (orderInstance?.status == OrderStatus.COMPLETED) {
                deleteTransactions(orderInstance)
                orderInstance.status = OrderStatus.PENDING
            }


        } catch (Exception e) {
            log.error("Failed to rollback order status due to error: " + e.message, e)
            throw new RuntimeException("Failed to rollback order status for order ${orderId}" + e.message, e)
        }
    }


    void deleteOrder(Order orderInstance) {
        deleteTransactions(orderInstance)
        orderInstance.delete(flush: true)
    }

    void deleteTransactions(Order orderInstance) {
        Set<Transaction> transactions = Transaction.findAllByOrder(orderInstance)
        if (transactions) {
            transactions.toArray().each { Transaction transaction ->
                inventoryService.deleteLocalTransfer(transaction)
            }
        }
    }


    /**
     * Import the order items into the order represented by the given order ID.
     *
     * @param orderId
     * @param orderItems
     * @return
     */
    boolean importOrderItems(String orderId, List orderItems) {

        int count = 0
        try {
            log.info "Order line items " + orderItems

            Order order = Order.get(orderId)

            if (validateOrderItems(orderItems)) {

                orderItems.each { item ->

                    log.info "Order item: " + item
                    def productCode = item["productCode"]
                    def quantity = item["quantity"]
                    def unitPrice = item["unitPrice"]

                    if (productCode) {
                        def product = Product.findByProductCode(productCode)
                        if (!product) {
                            throw new ProductException("Unable to locate product with product code ${productCode}")
                        }
                        OrderItem orderItem = new OrderItem(product: product, quantity: quantity, unitPrice: unitPrice)
                        order.addToOrderItems(orderItem)
                        count++
                    }

                }

                if (count < orderItems?.size()) {
                    return false
                }
                order.save(flush: true)
                return true
            }


        } catch (Exception e) {
            log.warn("Unable to import packing list items due to exception: " + e.message, e)
            throw new RuntimeException(e.message)
        }

        return false
    }

    /**
     * Parse the given text into a list of maps.
     *
     * @param inputStream
     * @return
     */
    List parseOrderItems(String text) {

        List orderItems = []

        try {
            def settings = [skipLines: 1]
            def csvMapReader = new CSVMapReader(new StringReader(text), settings)
            csvMapReader.fieldKeys = ['productCode', 'productName', 'vendorCode', 'quantity', 'unitOfMeasure', 'unitPrice']
            orderItems = csvMapReader.toList()

        } catch (Exception e) {
            throw new RuntimeException("Error parsing order item CSV: " + e.message, e)

        }
        finally {
            if (inputStream) inputStream.close()
        }

        return orderItems
    }

    /**
     * Validates whether the order item details are valid.
     *
     * TODO Need to implement the validation logic :)
     *
     * @param orderItems
     * @return
     */
    boolean validateOrderItems(List orderItems) {
        return true
    }

    List<OrderItem> getPendingInboundOrderItems(Location destination, Product product) {
        def orderItems = OrderItem.createCriteria().list() {
            order {
                eq("destination", destination)
                eq("orderTypeCode", OrderTypeCode.PURCHASE_ORDER)
            }
            eq("product", product)
        }

        return orderItems.findAll { !it.isCompletelyFulfilled() }
    }
}
