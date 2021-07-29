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

import grails.validation.ValidationException
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.grails.plugins.csv.CSVMapReader
import org.hibernate.criterion.CriteriaSpecification
import org.pih.warehouse.core.*
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductException
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentException
import org.pih.warehouse.shipping.ShipmentItem
import util.ReportUtil

import java.math.RoundingMode

class OrderService {

    boolean transactional = true

    def userService
    def dataService
    def shipmentService
    def identifierService
    def inventoryService
    def productSupplierDataService
    def personDataService
    def stockMovementService
    def grailsApplication

    def getOrders(Order orderTemplate, Date dateOrderedFrom, Date dateOrderedTo, Map params) {
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
                if (dateOrderedFrom) {
                    ge("dateOrdered", dateOrderedFrom)
                }
                if (dateOrderedTo) {
                    le("dateOrdered", dateOrderedTo)
                }
                if (orderTemplate.orderedBy) {
                    eq("orderedBy", orderTemplate.orderedBy)
                }
                if (orderTemplate.createdBy) {
                    eq("createdBy", orderTemplate.createdBy)
                }
                if (orderTemplate.destinationParty) {
                    destinationParty {
                        eq("id", params.destinationParty)
                    }
                }
            }
            order("dateOrdered", "desc")
        }
        return orders
    }

    Order createNewPurchaseOrder(Location currentLocation, User user, Boolean isCentralPurchasingEnabled) {
        Order order = new Order()
        if (!isCentralPurchasingEnabled) {
            order.destination = currentLocation
        }
        order.destinationParty = currentLocation?.organization
        order.orderedBy = user
        return order
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
     * @param origin (vendor)
     * @param destination
     * @return a list of purchase orders with given origin and destination
     */
    List<Order> getOrdersForCombinedShipment(Location origin, Location destination) {
        return Order.createCriteria().list() {
            and {
                eq("origin", origin)
                eq("destination", destination)
                eq("orderType", OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name()))
            }
        }
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
        def shipments = orderCommand?.order?.shipments
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

    int getNextSequenceNumber(String partyId) {
        Organization organization = Organization.get(partyId)
        Integer sequenceNumber = Integer.valueOf(organization.sequences.get(IdentifierTypeCode.PURCHASE_ORDER_NUMBER.toString())?:0)
        Integer nextSequenceNumber = sequenceNumber + 1
        organization.sequences.put(IdentifierTypeCode.PURCHASE_ORDER_NUMBER.toString(), nextSequenceNumber.toString())
        organization.save()
        return nextSequenceNumber
    }

    String generatePurchaseOrderSequenceNumber(Order order) {
        try {
            Integer sequenceNumber = getNextSequenceNumber(order.destinationParty.id)
            String sequenceNumberStr = identifierService.generateSequenceNumber(sequenceNumber.toString())

            // Properties to be used to get argument values for the template
            Map properties = ConfigurationHolder.config.openboxes.identifier.purchaseOrder.properties
            Map model = dataService.transformObject(order, properties)
            model.put("sequenceNumber", sequenceNumberStr)
            String template = ConfigurationHolder.config.openboxes.identifier.purchaseOrder.format
            return identifierService.renderTemplate(template, model)
        } catch(Exception e) {
            log.error("Error " + e.message, e)
            throw e;
        }
    }

    Order saveOrder(Order order) {
        // update the status of the order before saving
        order.updateStatus()

        order.originParty = order?.origin?.organization

        if (!order.orderNumber) {
            IdentifierGeneratorTypeCode identifierGeneratorTypeCode =
                    ConfigurationHolder.config.openboxes.identifier.purchaseOrder.generatorType

            if (identifierGeneratorTypeCode == IdentifierGeneratorTypeCode.SEQUENCE) {
                order.orderNumber = generatePurchaseOrderSequenceNumber(order)
            }
            else if (identifierGeneratorTypeCode == IdentifierGeneratorTypeCode.RANDOM) {
                order.orderNumber = identifierService.generatePurchaseOrderIdentifier()
            }
            else {
                throw new IllegalArgumentException("No identifier generator type associated with " + identifierGeneratorTypeCode)
            }
        }

        if (!order.hasErrors() && order.save()) {
            return order
        } else {
            throw new ValidationException("Unable to save order due to errors", order.errors)
        }
    }

    Order placeOrder(String id, String userId) {
        def orderInstance = Order.get(id)
        def userInstance = User.get(userId)
        if (orderInstance) {
            if (orderInstance?.status >= OrderStatus.PLACED) {
                orderInstance.errors.rejectValue("status", "order.hasAlreadyBeenPlaced.message")
            } else {
                if (orderInstance?.orderItems?.size() > 0 || orderInstance?.orderAdjustments?.size() > 0) {
                    if (canApproveOrder(orderInstance, userInstance)) {
                        orderInstance?.orderItems?.each { orderItem ->
                            orderItem.actualReadyDate = orderItem.estimatedReadyDate
                        }
                        orderInstance.status = OrderStatus.PLACED
                        orderInstance.dateApproved = new Date()
                        orderInstance.approvedBy = userInstance
                        if (!orderInstance.hasErrors() && orderInstance.merge()) {
                            grailsApplication.mainContext.publishEvent(new OrderStatusEvent(OrderStatus.PLACED, orderInstance))
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
                orderInstance?.shipments?.each { Shipment shipmentInstance ->
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

            grailsApplication.mainContext.publishEvent(new OrderStatusEvent(orderInstance.status, orderInstance))

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

    void updateProductUnitPrice(OrderItem orderItem) {
        Boolean enabled = ConfigurationHolder.config.openboxes.purchasing.updateUnitPrice.enabled
        if (enabled) {
            UpdateUnitPriceMethodCode method = ConfigurationHolder.config.openboxes.purchasing.updateUnitPrice.method
            if (method == UpdateUnitPriceMethodCode.LAST_PURCHASE_PRICE) {
                BigDecimal pricePerPackage = orderItem.unitPrice * orderItem?.order?.lookupCurrentExchangeRate()
                BigDecimal pricePerUnit = pricePerPackage / orderItem?.quantityPerUom
                orderItem.product.pricePerUnit = pricePerUnit
                orderItem.product.save()
            }
            else {
                log.warn("Cannot update unit price because method ${method} is not currently supported")
            }
        }
    }

    void updateProductPackage(OrderItem orderItem) {
        // Convert package price to default currency
        BigDecimal packagePrice = orderItem.unitPrice * orderItem?.order?.lookupCurrentExchangeRate()

        // If there's no product package already or the existing one changed we create a new one (or update)
        def uomChanged = orderItem.productPackage?.uom != orderItem.quantityUom
        def quantityPerUomChanged = orderItem.productPackage?.quantity != orderItem.quantityPerUom
        if (!orderItem.productPackage || uomChanged || quantityPerUomChanged) {
            // Find an existing product package associated with a specific supplier
            ProductPackage productPackage = orderItem?.productSupplier?.productPackages.find { ProductPackage productPackage ->
                return productPackage.product == orderItem.product &&
                        productPackage.uom == orderItem.quantityUom &&
                        productPackage.quantity == orderItem.quantityPerUom
            }

            // If not found, then we look for a product package associated with the product
            if (!productPackage) {
                productPackage = orderItem.product.packages.find { ProductPackage productPackage1 ->
                    return productPackage1.product == orderItem.product &&
                            productPackage1.uom == orderItem.quantityUom &&
                            productPackage1.quantity == orderItem.quantityPerUom
                }
            }

            // If we cannot find an existing product package, create a new one
            if (!productPackage) {
                productPackage = new ProductPackage()
                productPackage.product = orderItem.product
                productPackage.productSupplier = orderItem.productSupplier
                productPackage.name = "${orderItem?.quantityUom?.code}/${orderItem?.quantityPerUom as Integer}"
                productPackage.uom = orderItem.quantityUom
                productPackage.quantity = orderItem.quantityPerUom as Integer
                ProductPrice productPrice = new ProductPrice()
                productPrice.price = packagePrice
                productPackage.productPrice = productPrice
                productPackage.save()
            }
            // Otherwise update the price
            else {
                if (productPackage.productPrice) {
                    productPackage.productPrice.price = packagePrice
                } else {
                    ProductPrice productPrice = new ProductPrice()
                    productPrice.price = packagePrice
                    productPackage.productPrice = productPrice
                }
                productPackage.lastUpdated = new Date()
            }
            // Associate product package with order item
            orderItem.productPackage = productPackage
            if (orderItem.productSupplier && !orderItem.productSupplier?.productPackages?.find { it.id == productPackage.id }) {
                orderItem.productSupplier.addToProductPackages(productPackage)
            }
        }
        // Otherwise we update the existing price
        else {
            if (orderItem.productPackage.productPrice) {
                orderItem.productPackage.productPrice.price = packagePrice
            } else {
                ProductPrice productPrice = new ProductPrice()
                productPrice.price = packagePrice
                orderItem.productPackage.productPrice = productPrice
            }

        }
    }

    /**
     * Import the order items into the order represented by the given order ID.
     *
     * @param orderId
     * @param supplierId
     * @param orderItems
     * @return
     */
    boolean importOrderItems(String orderId, String supplierId, List orderItems, Location currentLocation) {

        int count = 0
        try {
            log.info "Order line items " + orderItems

            Order order = Order.get(orderId)

            if (validateOrderItems(orderItems, order)) {

                orderItems.each { item ->

                    log.info "Order item: " + item
                    def orderItemId = item["id"]
                    def productCode = item["productCode"]
                    def sourceCode = item["sourceCode"]
                    def sourceName = item["sourceName"]
                    def supplierCode = item["supplierCode"]
                    def manufacturerName = item["manufacturer"]
                    def manufacturerCode = item["manufacturerCode"]
                    def quantity = item["quantity"]
                    String recipient = item["recipient"]
                    def unitPrice = item["unitPrice"]
                    def unitOfMeasure = item["unitOfMeasure"]
                    def estimatedReadyDate = item["estimatedReadyDate"]
                    def actualReadyDate = item["actualReadyDate"]
                    def code = item["budgetCode"]

                    OrderItem orderItem
                    if (orderItemId) {
                        orderItem = OrderItem.get(orderItemId)
                        if (orderItem.order.id != orderId) {
                            throw new UnsupportedOperationException("You can not edit items from another order!")
                        }
                    } else {
                        orderItem = new OrderItem()
                        orderItem.orderIndex = order.orderItems ? order.orderItems.size() : 0
                    }

                    Product product
                    if (productCode) {
                        product = Product.findByProductCode(productCode)
                        if (!product) {
                            throw new ProductException("Unable to locate product with product code ${productCode}")
                        }
                        if (currentLocation.isAccountingRequired() && !product.glAccount) {
                            throw new ProductException("Product ${productCode}: Cannot add order item without a valid general ledger code")
                        }
                        orderItem.product = product
                    } else {
                        throw new ProductException("No product code specified")
                    }

                    if (sourceCode) {
                        ProductSupplier productSource = ProductSupplier.findByCode(sourceCode)
                        if (productSource && productSource.product != orderItem.product) {
                            throw new ProductException("Wrong product source for given product")
                        }
                        if (productSource) {
                            orderItem.productSupplier = productSource
                        }
                    } else {
                        Organization supplier = Organization.get(supplierId)
                        Organization manufacturer = null
                        if (manufacturerName) {
                            manufacturer = Organization.findByName(manufacturerName)
                        }
                        def supplierParams = [manufacturer: manufacturer?.id,
                                              product: product,
                                              supplierCode: supplierCode ?: null,
                                              manufacturerCode: manufacturerCode ?: null,
                                              supplier: supplier,
                                              sourceName: sourceName]
                        ProductSupplier productSupplier = productSupplierDataService.getOrCreateNew(supplierParams, false)

                        if (productSupplier) {
                            orderItem.productSupplier = productSupplier
                        }
                    }

                    if (unitOfMeasure) {
                        String[] uomParts = unitOfMeasure.split("/")
                        if (uomParts.length <= 1 || !UnitOfMeasure.findByCodeOrName(uomParts[0], uomParts[0])) {
                            throw new IllegalArgumentException("Could not find provided Unit of Measure: ${unitOfMeasure}.")
                        }
                        UnitOfMeasure uom = uomParts.length > 1 ? UnitOfMeasure.findByCodeOrName(uomParts[0], uomParts[0]) : null
                        BigDecimal qtyPerUom = uomParts.length > 1 ? BigDecimal.valueOf(Double.valueOf(uomParts[1])) : null
                        orderItem.quantityUom = uom
                        orderItem.quantityPerUom = qtyPerUom
                    } else {
                        throw new IllegalArgumentException("Missing unit of measure.")
                    }

                    if (quantity == "") {
                        throw new IllegalArgumentException("Missing quantity.")
                    }
                    Integer parsedQty = Integer.valueOf(quantity)
                    if (parsedQty <= 0) {
                        throw new IllegalArgumentException("Wrong quantity value: ${parsedQty}.")
                    }

                    if (unitPrice == "") {
                        throw new IllegalArgumentException("Missing unit price.")
                    }
                    BigDecimal parsedUnitPrice
                    try {
                        parsedUnitPrice = new BigDecimal(unitPrice).setScale(2, RoundingMode.FLOOR)
                    } catch (Exception e) {
                        log.error("Unable to parse unit price: " + e.message, e)
                        throw new IllegalArgumentException("Could not parse unit price with value: ${unitPrice}.")
                    }
                    if (parsedUnitPrice < 0) {
                        throw new IllegalArgumentException("Wrong unit price value: ${parsedUnitPrice}.")
                    }

                    orderItem.quantity = parsedQty
                    orderItem.unitPrice = parsedUnitPrice
                    orderItem.recipient = recipient ? personDataService.getPersonByNames(recipient) : null

                    def estReadyDate = null
                    if (estimatedReadyDate) {
                        try {
                            estReadyDate = new Date(estimatedReadyDate)
                        } catch (Exception e) {
                            log.error("Unable to parse date: " + e.message, e)
                            throw new IllegalArgumentException("Could not parse estimated ready date with value: ${estimatedReadyDate}.")
                        }
                    }
                    orderItem.estimatedReadyDate = estReadyDate

                    def actReadyDate = null
                    if (actualReadyDate) {
                        try {
                            actReadyDate = new Date(actualReadyDate)
                        } catch (Exception e) {
                            log.error("Unable to parse date: " + e.message, e)
                            throw new IllegalArgumentException("Could not parse actual ready date with value: ${actualReadyDate}.")
                        }
                    }
                    orderItem.actualReadyDate = actReadyDate

                    if (currentLocation.isAccountingRequired() && !code) {
                        throw new IllegalArgumentException("Budget code is required.")
                    }
                    BudgetCode budgetCode = BudgetCode.findByCode(code)
                    if (code && !budgetCode) {
                        throw new IllegalArgumentException("Could not find budget code with code: ${code}.")

                    }
                    orderItem.budgetCode = budgetCode

                    order.addToOrderItems(orderItem)
                    count++
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
            csvMapReader.fieldKeys = [
                'id',
                'productCode',
                'productName',
                'sourceCode',
                'sourceName',
                'supplierCode',
                'manufacturer',
                'manufacturerCode',
                'quantity',
                'unitOfMeasure',
                'unitPrice',
                'totalCost',
                'recipient',
                'estimatedReadyDate',
                'actualReadyDate',
                'budgetCode'
            ]
            orderItems = csvMapReader.toList()

        } catch (Exception e) {
            throw new RuntimeException("Error parsing order item CSV: " + e.message, e)

        }

        orderItems.each { orderItem ->
            if (orderItem.unitOfMeasure) {
                String[] uomParts = orderItem.unitOfMeasure.split("/")
                def quantityUom = (int)Double.parseDouble(uomParts[1])
                orderItem.unitOfMeasure = "${uomParts[0]}/${quantityUom}"
            }
            orderItem.unitPrice = orderItem.unitPrice ? new BigDecimal(orderItem.unitPrice).setScale(4, RoundingMode.FLOOR).toString() : ''
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
    boolean validateOrderItems(List orderItems, Order order) {
        def propertiesMap = grailsApplication.config.openboxes.purchaseOrder.editableProperties

        orderItems.each { orderItem ->
            OrderItem existingOrderItem = order.orderItems.find { it.id == orderItem.id }
            propertiesMap.each {
                def excludedProperties = it.deny
                excludedProperties.each { property ->
                    if (order.status == it.status && (existingOrderItem.toImport()."${property}" != orderItem."${property}")) {
                        throw new IllegalArgumentException("Can't edit the field ${property} of item ${orderItem.productCode} via import")
                    }
                }
            }
        }
    }

    List<OrderItem> getPendingInboundOrderItems(Location destination) {
        def orderItems = OrderItem.createCriteria().list() {
            order {
                eq("destination", destination)
                eq("orderType", OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name()))
                not {
                    'in'("status", OrderStatus.PENDING)
                }
            }
            not {
                'in'("orderItemStatusCode", OrderItemStatusCode.CANCELED)
            }
        }

        return orderItems.findAll { !it.isCompletelyFulfilled() }
    }

    List<OrderItem> getPendingInboundOrderItems(Location destination, Product product) {
        def orderItems = OrderItem.createCriteria().list() {
            order {
                eq("destination", destination)
                eq("orderType", OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name()))
                not {
                    'in'("status", OrderStatus.PENDING)
                }
            }
            eq("product", product)
        }

        return orderItems.findAll { !it.isCompletelyFulfilled() }
    }

    def getProductsInOrders(String[] terms, Location destination, Location vendor) {
        return OrderItem.createCriteria().list {
            not {
                'in'("orderItemStatusCode", OrderItemStatusCode.CANCELED)
            }
            order {
                eq("destination", destination)
                eq("origin", vendor)
            }
            product {
                if (terms) {
                    terms.each { term ->
                        term = term + "%"
                        or {
                            ilike("name", "%" + term)
                            ilike("productCode", term)
                            ilike("description", "%" + term)
                        }
                    }
                }
            }
        }
    }

    def canOrderItemBeEdited(OrderItem orderItem, User user) {
        def isPending = orderItem?.order?.status == OrderStatus.PENDING
        def isApprover = userService.hasRoleApprover(user)

        return isPending?:isApprover
    }

    String exportOrderItems(List<OrderItem> orderItems) {
        def rows = []
        orderItems.each { orderItem ->
            def row = [
                    'Order Item ID'       : orderItem?.id,
                    'Pack level 1'        : '',
                    'Pack level 2'        : '',
                    'Code'                : orderItem?.product?.productCode,
                    'Product'             : orderItem?.product?.name,
                    'UOM'                 : orderItem?.unitOfMeasure,
                    'Lot'                 : '',
                    'Expiry (mm/dd/yyyy)' : '',
                    'Qty to ship'         : orderItem.quantityRemaining,
            ]

            rows << row
        }
        return ReportUtil.getCsvForListOfMapEntries(rows)
    }

    def canManageAdjustments(Order order, User user) {
        return order.status == OrderStatus.PENDING || order?.status >= OrderStatus.PLACED && userService.hasRoleApprover(user)
    }

    def getOrderSummaryList(Map params) {
        return OrderSummary.createCriteria().list(params) {
            if (params.orderNumber) {
                ilike("orderNumber", "%${params.orderNumber}%")
            }
            if (params.orderStatus) {
                'in'("orderStatus", params.orderStatus)
            }
            if (params.shipmentStatus) {
                'in'("shipmentStatus", params.shipmentStatus)
            }
            if (params.receiptStatus) {
                'in'("receiptStatus", params.receiptStatus)
            }
            if (params.paymentStatus) {
                'in'("paymentStatus", params.paymentStatus)
            }
            if (params.derivedStatus) {
                'in'("derivedStatus", params.derivedStatus)
            }
        }
    }

    List<OrderItem> getOrderItemsForPriceHistory(Organization supplierOrganization, Product productInstance, String query) {
        def terms = "%" + query + "%"
        def orderItems = OrderItem.createCriteria().list() {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("unitPrice", "unitPrice")
                property("quantityPerUom", "quantityPerUom")
                order {
                    property("orderNumber", "orderNumber")
                    property("id", "orderId")
                    property("dateCreated", "dateCreated")
                    property("name", "description")
                }
                product {
                    property("productCode", "productCode")
                    property("name", "productName")
                }
                productSupplier {
                    property("code", "sourceCode")
                    property("supplierCode", "supplierCode")
                    property("manufacturerCode", "manufacturerCode")
                    manufacturer {
                        property("name", "manufacturerName")
                    }
                }
            }
            order {
                eq("orderType", OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name()))
                eq("originParty", supplierOrganization)
            }
            if (productInstance) {
                eq("product", productInstance)
            }
            if (terms) {
                or {
                    product {
                        or {
                            ilike("productCode", terms)
                            ilike("name", terms)
                         }
                    }
                    order {
                        or {
                            ilike("orderNumber", terms)
                            ilike("name", terms)
                        }
                    }
                    productSupplier {
                        or {
                            ilike("manufacturerCode", terms)
                            ilike("manufacturerName", terms)
                            ilike("code", terms)
                            ilike("supplierCode", terms)
                            ilike("supplierName", terms)
                        }
                    }
                }
            }
        }

        return orderItems
    }
}
