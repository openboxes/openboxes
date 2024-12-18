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

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.hibernate.sql.JoinType

import grails.plugins.csv.CSVMapReader
import org.hibernate.criterion.CriteriaSpecification
import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ProductPrice
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.SynonymTypeCode
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.UpdateUnitPriceMethodCode
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService
import org.pih.warehouse.data.DataService
import org.pih.warehouse.data.PersonService
import org.pih.warehouse.data.ProductSupplierService
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.jobs.RefreshOrderSummaryJob
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductException
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentException
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentService
import util.ReportUtil

import java.text.SimpleDateFormat

@Transactional
class OrderService {

    UserService userService
    DataService dataService
    ShipmentService shipmentService
    PurchaseOrderIdentifierService purchaseOrderIdentifierService
    InventoryService inventoryService
    ProductSupplierService productSupplierService
    PersonService personService
    GrailsApplication grailsApplication

    def getApplicationTagLib() {
        return Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
    }

    def getPurchaseOrders(Map params) {
        // Parse pagination parameters
        def max = params.max ? params.int("max") : null
        def offset = params.offset ? params.int("offset") : null

        // Parse date parameters
        Date statusStartDate = params.statusStartDate ? Date.parse("MM/dd/yyyy", params.statusStartDate) : null
        Date statusEndDate = params.statusEndDate ? Date.parse("MM/dd/yyyy", params.statusEndDate) : null

        // OrderSummery contains only Purchase Orders, hence no need to filter by orderType.
        return OrderSummary.createCriteria().list(max: max, offset: offset) {
            if (params.status) {
                'in'("derivedStatus", params.list("status"))
            }

            order {
                and {
                    if (params.searchTerm) {
                        or {
                            ilike("name", "%" + params.searchTerm + "%")
                            ilike("description", "%" + params.searchTerm + "%")
                            ilike("orderNumber", "%" + params.searchTerm + "%")
                        }
                    }
                    if (params.destination) {
                        eq("destination.id", params.destination)
                    }
                    if (params.destinationParty) {
                        destinationParty {
                            eq("id", params.destinationParty)
                        }
                    }
                    if (params.origin) {
                        eq("origin.id", params.origin)
                    }
                    if (statusStartDate) {
                        ge("dateOrdered", statusStartDate)
                    }
                    if (statusEndDate) {
                        le("dateOrdered", statusEndDate)
                    }
                    if (params.orderedBy) {
                        eq("orderedBy.id", params.orderedBy)
                    }
                    if (params.createdBy) {
                        eq("createdBy.id", params.createdBy)
                    }
                    paymentTerm(JoinType.LEFT_OUTER_JOIN.joinTypeValue) {
                        if (params.paymentTerm) {
                            or {
                                if (params.list("paymentTerm").contains("null")) {
                                    isNull("id")
                                }
                                'in'("id", params.list("paymentTerm"))
                            }
                        }
                    }
                }

                if (params.sort && params.sort != 'status') {
                    if (params.sort == 'origin') {
                        origin {
                            order("name", params.order ?: 'asc')
                        }
                    } else if (params.sort == 'destination') {
                        destination {
                            order("name", params.order ?: 'asc')
                        }
                    } else if (params.sort == 'orderedBy') {
                        orderedBy {
                            order("firstName", params.order ?: 'asc')
                            order("lastName", params.order ?: 'asc')
                        }
                    } else if (params.sort == 'paymentTerm') {
                        paymentTerm {
                            order("name", params.order ?: 'asc')
                        }
                    } else {
                        order(params.sort, params.order ?: 'asc')
                    }
                }
            }

            if (params.sort && params.sort == 'status') {
                order('derivedStatus', params.order ?: 'asc')
            }
        }
    }

    def getOrders(Order orderTemplate, Date dateOrderedFrom = null, Date dateOrderedTo = null, Map params = [:]) {
        def orders = Order.createCriteria().list(params) {
            and {
                if (params.q) {
                    or {
                        ilike("name", "%" + params.q + "%")
                        ilike("description", "%" + params.q + "%")
                        ilike("orderNumber", "%" + params.q + "%")
                    }

                    orderItems {
                        product {
                            or {
                                ilike("name", "%" + params.q + "%")
                                ilike("productCode", "%" + params.q + "%")
                            }
                        }
                    }

                    orderItems {
                        inventoryItem {
                            ilike("lotNumber", "%" + params.q + "%")
                        }
                    }

                    orderItems {
                        originBinLocation {
                            or {
                                ilike("name", "%" + params.q + "%")
                                ilike("locationNumber", "%" + params.q + "%")
                            }
                        }
                    }

                    orderItems {
                        destinationBinLocation {
                            or {
                                ilike("name", "%" + params.q + "%")
                                ilike("locationNumber", "%" + params.q + "%")
                            }
                        }
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
                'in'("status", [OrderStatus.PLACED, OrderStatus.PARTIALLY_RECEIVED])
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

    Order saveOrder(Order order) {
        // update the status of the order before saving
        order.updateStatus()

        order.originParty = order?.origin?.organization

        if (!order.orderNumber) {
            order.orderNumber = purchaseOrderIdentifierService.generate(order)
        }

        if (!order.hasErrors() && order.save(flush: true)) {
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
        if (orderInstance.shipments?.size() > 0) {
            throw new IllegalArgumentException("Cannot rollback order with associated shipments")
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
        orderInstance.shipments?.each { Shipment it ->
            shipmentService.deleteShipment(it)
        }
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
        Boolean enabled = Holders.grailsApplication.config.openboxes.purchasing.updateUnitPrice.enabled
        if (enabled) {
            UpdateUnitPriceMethodCode method = Holders.grailsApplication.config.openboxes.purchasing.updateUnitPrice.method
            if (method == UpdateUnitPriceMethodCode.LAST_PURCHASE_PRICE) {
                BigDecimal pricePerPackage = orderItem.unitPrice * orderItem?.order?.lookupCurrentExchangeRate()
                BigDecimal pricePerUnit = pricePerPackage / orderItem?.quantityPerUom
                if (pricePerUnit != 0) {
                    orderItem.product.pricePerUnit = pricePerUnit
                    orderItem.product.save()
                }
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
        boolean uomChanged = orderItem.productPackage?.uom != orderItem.quantityUom
        boolean quantityPerUomChanged = orderItem.productPackage?.quantity != orderItem.quantityPerUom
        boolean productSupplierChanged = orderItem.productPackage?.productSupplier?.id != orderItem.productSupplier?.id

        if (!orderItem.productPackage || productSupplierChanged || uomChanged || quantityPerUomChanged) {
            // Find an existing product package associated with a specific supplier
            ProductPackage productPackage = orderItem?.productSupplier?.productPackages.find { ProductPackage productPackage ->
                return productPackage.product == orderItem.product &&
                        productPackage.uom == orderItem.quantityUom &&
                        productPackage.quantity == orderItem.quantityPerUom
            }

            // If not found, then we look for a product package associated with the product
            if (!productPackage && !orderItem?.productSupplier) {
                productPackage = orderItem.product.packages.find { ProductPackage productPackage1 ->
                    return productPackage1.product == orderItem.product &&
                            productPackage1.uom == orderItem.quantityUom &&
                            productPackage1.quantity == orderItem.quantityPerUom &&
                            !productPackage1.productSupplier
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
                productPackage.save(failOnError: true)
            }
            // Otherwise update the price
            else if (packagePrice > 0) {
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
        else if (packagePrice > 0) {
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
    boolean importOrderItems(String orderId, String supplierId, List orderItems, Location currentLocation, User user) {

        int count = 0
        try {
            log.info "Order line items " + orderItems

            Order order = Order.get(orderId)
            if (!isOrderEditable(order, user)) {
                throw new UnsupportedOperationException(applicationTagLib.message(code: "errors.noPermissions.label", default: "You do not have permissions to perform this action"))
            }

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
                        if (productSource) {
                            if (productSource.product != orderItem.product) {
                                throw new ProductException("Wrong product source for given product")
                            }
                            if (!productSource.active) {
                                throw new ProductException("Product source ${sourceCode} for product ${productCode} is inactive")
                            }
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
                        ProductSupplier productSupplier = productSupplierService.getOrCreateNew(supplierParams, false)
                        // Check if any of search term fields for productSupplier are filled
                        def supplierParamFilled = supplierCode || manufacturerName || manufacturerCode

                        if (productSupplier) {
                            if (!productSupplier.active && supplierParamFilled) {
                                throw new ProductException("Product source ${productSupplier.code} for product ${productCode} is inactive")
                            }
                            // If it matches a product source for empty params (rare case), but it's inactive, treat it as if there was not a product source
                            if (productSupplier.active) {
                                orderItem.productSupplier = productSupplier
                            }
                        }
                    }

                    if (unitOfMeasure) {
                        String[] uomParts = unitOfMeasure.split("/")
                        if (uomParts.length <= 1 || !UnitOfMeasure.findByCodeOrName(uomParts[0], uomParts[0])) {
                            throw new IllegalArgumentException("Could not find provided Unit of Measure: ${unitOfMeasure}.")
                        }
                        UnitOfMeasure uom = uomParts.length > 1 ? UnitOfMeasure.findByCodeOrName(uomParts[0], uomParts[0]) : null
                        orderItem.quantityUom = uom
                        BigDecimal qtyPerUom = uomParts.length > 1 ? CSVUtils.parseNumber(uomParts[1], "unitOfMeasure"): null
                        if (uom?.id == Constants.UOM_EACH_ID && qtyPerUom != 1) {
                            throw new IllegalArgumentException("Quantity per UoM must be 1, if selected UoM is Each")
                        }
                        if (!qtyPerUom || qtyPerUom < 1) {
                            throw new IllegalArgumentException("Quantity per UoM cannot be empty or less than 1")
                        }
                        orderItem.quantityPerUom = qtyPerUom
                    } else {
                        throw new IllegalArgumentException("Missing unit of measure.")
                    }

                    Integer parsedQty = CSVUtils.parseInteger(quantity, "quantity")
                    if (parsedQty <= 0) {
                        throw new IllegalArgumentException("Wrong quantity value: ${parsedQty}.")
                    }

                    BigDecimal parsedUnitPrice = CSVUtils.parseNumber(unitPrice, "unitPrice")
                    if (orderItem.id && orderItem.hasRegularInvoice && orderItem.unitPrice != parsedUnitPrice) {
                        throw new IllegalArgumentException("Cannot update the unit price on a line that is already invoiced.")
                    }
                    if (parsedUnitPrice < 0) {
                        throw new IllegalArgumentException("Wrong unit price value: ${parsedUnitPrice}.")
                    }

                    orderItem.quantity = parsedQty
                    orderItem.unitPrice = parsedUnitPrice

                    if (recipient) {
                        Person person = personService.getPersonByNames(recipient)
                        if (!person?.active) {
                            throw new IllegalArgumentException("Cannot set a recipient who is non-existant or inactive: ${recipient}")
                        }
                        orderItem.recipient = person
                    }

                    def estReadyDate = null
                    Locale locale = LocalizationUtil.currentLocale
                    SimpleDateFormat readyDateFormat = new SimpleDateFormat(LocalizationUtil.getLocalizedOrderImportDateFormat(locale))
                    if (estimatedReadyDate) {
                        try {
                            estReadyDate = readyDateFormat.parse(estimatedReadyDate)
                        } catch (Exception e) {
                            log.error("Unable to parse date: " + e.message, e)
                            throw new IllegalArgumentException("Could not parse estimated ready date with value: ${estimatedReadyDate}.")
                        }
                    }
                    orderItem.estimatedReadyDate = estReadyDate

                    def actReadyDate = null
                    if (actualReadyDate) {
                        try {
                            actReadyDate = readyDateFormat.parse(actualReadyDate)
                        } catch (Exception e) {
                            log.error("Unable to parse date: " + e.message, e)
                            throw new IllegalArgumentException("Could not parse actual ready date with value: ${actualReadyDate}.")
                        }
                    }
                    orderItem.actualReadyDate = actReadyDate

                    if (currentLocation.isAccountingRequired() && !code) {
                        throw new IllegalArgumentException("Budget code is required.")
                    }

                    // There can be more than one budget code with the same code, despite the fact that the code is unique from the domain perspective.
                    // As a fix for that we would like to have only one active budget code with the same code, so we have to find only active
                    // budget codes. In case when there is only one budget code, and this one is inactive we have to throw a validation error,
                    // so we can't just look for BudgetCode.findAllByCodeAndActive(code, true);
                    List<BudgetCode> foundBudgetCodes = BudgetCode.findAllByCode(code)
                    List<BudgetCode> activeBudgetCodes = foundBudgetCodes.findAll { it.active }

                    if (activeBudgetCodes.size() > 1) {
                        throw new IllegalArgumentException("Found more than one active budget code with the same code.")
                    }

                    BudgetCode budgetCode = activeBudgetCodes.size() == 0 ? foundBudgetCodes[0] : activeBudgetCodes.first()

                    if (orderItem.id && orderItem.hasRegularInvoice && orderItem.budgetCode?.id != budgetCode?.id) {
                        throw new IllegalArgumentException("Cannot update the budget code on a line that is already invoiced.")
                    }
                    if (code) {
                        if (!budgetCode) {
                            throw new IllegalArgumentException("Could not find budget code with code: ${code}.")
                        }
                        if (!budgetCode.active) {
                            throw new IllegalArgumentException("Budget code ${code} is inactive.")
                        }
                    }
                    orderItem.budgetCode = budgetCode

                    order.addToOrderItems(orderItem)

                    if (order.status >= OrderStatus.PLACED) {
                        updateProductPackage(orderItem)
                        updateProductUnitPrice(orderItem)
                    }

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

        def fieldKeys = [
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

        char dataSeparator =  CSVUtils.getSeparator(text, fieldKeys.size())

        try {
            def settings = [skipLines: 1, separatorChar: dataSeparator]
            def csvMapReader = new CSVMapReader(new StringReader(text), settings)
            csvMapReader.fieldKeys = fieldKeys
            orderItems = csvMapReader.toList()

        } catch (Exception e) {
            throw new RuntimeException("Error parsing order item CSV: " + e.message, e)
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
        def excludedProperties = propertiesMap?.deny
        orderItems.each { orderItem ->
            OrderItem existingOrderItem = order.orderItems.find { it.id == orderItem.id }
            excludedProperties?.each { property ->
                if (order.status == propertiesMap.status) {
                    def existingValue = existingOrderItem.toImport()."${property}"
                    def importedValue = orderItem."${property}"
                    if (existingValue != importedValue) {
                        throw new IllegalArgumentException("Import must not change ${property} of item ${orderItem.productCode}, before: ${existingValue}, after: ${importedValue}")
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

    List<OrderItem> getPendingInboundOrderItems(Location destination, List<Product> products) {
        def orderItems = OrderItem.createCriteria().list() {
            order {
                eq("destination", destination)
                eq("orderType", OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name()))
                not {
                    'in'("status", OrderStatus.PENDING)
                }
            }
            'in'("product", products)
            not {
                'in'("orderItemStatusCode", OrderItemStatusCode.CANCELED)
            }
        }

        return orderItems.findAll { !it.isCompletelyFulfilled() }
    }

    def getProductsInOrders(String[] terms, Location destination, Location vendor) {
        return OrderItem.createCriteria().list {
            createAlias('product', 'product', JoinType.LEFT_OUTER_JOIN)
            createAlias('product.synonyms', 'synonym', JoinType.LEFT_OUTER_JOIN)

            not {
                'in'("orderItemStatusCode", OrderItemStatusCode.CANCELED)
            }
            order {
                eq("destination", destination)
                eq("origin", vendor)
            }
            if (terms) {
                terms.each { term ->
                    term = term + "%"
                    or {
                        ilike("product.name", "%" + term)
                        and {
                            ilike("synonym.name", "%" + term)
                            eq("synonym.synonymTypeCode", SynonymTypeCode.DISPLAY_NAME)
                        }
                        ilike("product.productCode", term)
                        ilike("product.description", "%" + term)
                    }
                }
            }
        }
    }

    boolean isOrderEditable(Order order, User user) {
        return order?.pending ?: userService.hasRolePurchaseApprover(user)
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
        return order.status == OrderStatus.PENDING || order?.status >= OrderStatus.PLACED && userService.hasRolePurchaseApprover(user)
    }

    def getOrderSummaryList(Map params) {
        return OrderSummary.createCriteria().list(params) {
            if (params.orderNumber) {
                order {
                    ilike("orderNumber", "%${params.orderNumber}%")
                }
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

    OrderItem getOrderItemByOrderAndProduct(String orderNumber, String productCode) {
        Product product = productCode ? Product.findByProductCode(productCode) : null
        Order order = orderNumber ? Order.findByOrderNumber(orderNumber) : null

        getOrderItemByOrderAndProduct(order, product)
    }

    OrderItem getOrderItemByOrderAndProduct(Order order, Product product) {
        if (!order) {
            throw new IllegalArgumentException("Missing Order")
        }

        if (!product) {
            throw new IllegalArgumentException("Missing Product")
        }

        List<OrderItem> matchedOrderItems = OrderItem.findAllByOrderAndProduct(order, product)

        if (matchedOrderItems.size() > 1) {
            throw new RuntimeException("Found more than one candidates for OrderItem")
        }
        if (matchedOrderItems.size() == 1) {
            return matchedOrderItems.first();
        }
    }

    def getOrderItemSummaryList(Map params) {
        return OrderItemSummary.createCriteria().list(params) {
            if (params.orderNumber) {
                ilike("orderNumber", "%${params.orderNumber}%")
            }
            if (params.derivedStatus) {
                'in'("derivedStatus", params.derivedStatus)
            }
        }
    }

    def getOrderItemDetailsList(Map params) {
        return OrderItemDetails.createCriteria().list(params) {
            if (params.orderNumber) {
                ilike("orderNumber", "%${params.orderNumber}%")
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
                productSupplier(JoinType.LEFT_OUTER_JOIN.joinTypeValue) {
                    property("code", "sourceCode")
                    property("supplierCode", "supplierCode")
                    property("manufacturerCode", "manufacturerCode")
                    manufacturer(JoinType.LEFT_OUTER_JOIN.joinTypeValue) {
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

    def deleteAdjustment(OrderAdjustment orderAdjustment, User user) {
        Order order = orderAdjustment.order;

        if (!canManageAdjustments(order, user) || orderAdjustment.hasInvoices){
            throw new UnsupportedOperationException(applicationTagLib.message(code: "errors.noPermissions.label", default: "You do not have permissions to perform this action"))
        }

        order.removeFromOrderAdjustments(orderAdjustment)
        orderAdjustment.delete()

        if (order.hasErrors()) {
            throw new ValidationException("Invalid order", order.errors)
        }
        order.save(flush: true)
    }

    def removeOrderItem(OrderItem orderItem, User user) {

        if (orderItem.hasShipmentAssociated() || !isOrderEditable(orderItem.order, user) || orderItem.hasInvoices) {
            throw new UnsupportedOperationException(applicationTagLib.message(code: "errors.noPermissions.label", default: "You do not have permissions to perform this action"))
        }

        Order order = orderItem.order
        order.removeFromOrderItems(orderItem)
        orderItem.delete()

        if (order.hasErrors()) {
            throw new ValidationException("Invalid order", order.errors)
        }
        order.save(flush:true)
    }

    /**
     * Gets map of derived status for orders (Order id as a key and derived status as a value)
     * Done to improve performance of getting orders derived statuses on order list page
     * */
    def getOrdersDerivedStatus(List orderIds) {
        if (!orderIds) {
            return [:]
        }

        def orderSummaryList =  OrderSummary.findAllByIdInList(orderIds)
        def results = orderSummaryList.inject([:]) { map, OrderSummary orderSummary ->
            map << [(orderSummary?.id): applicationTagLib.message(code: "enum.OrderSummaryStatus.${orderSummary?.derivedStatus}")]
        }

        // Check if any order was not fetched from OrderSummary, then get derived status from the old Order.displayStatus
        def summaryIds = orderSummaryList?.collect { it?.id }
        (orderIds - summaryIds).each { String orderId ->
            Order order = Order.get(orderId)
            if (order && !results[order.id]) {
                results[order.id] = applicationTagLib.message(code: "enum.OrderSummaryStatus.${order.displayStatus?.name()}")
            }
        }

        return results
    }

    Map getOrderSummary(String orderId) {
        Order order = Order.get(orderId)
        def orderItems = order?.orderItems
        return [
            isPurchaseOrder: order.isPurchaseOrder,
            isPutawayOrder: order.isPutawayOrder,
            orderItems: orderItems,
            hasSupplierCode: orderItems?.any { it.productSupplier?.supplierCode },
            hasManufacturerName: orderItems?.any { it.productSupplier?.manufacturerName },
            hasManufacturerCode: orderItems?.any { it.productSupplier?.manufacturerCode },
            currencyCode: order.currencyCode ?: grailsApplication.config.openboxes.locale.defaultCurrencyCode,
            subtotal: order.subtotal,
            totalAdjustments: order.totalAdjustments,
            total: order.total,
        ]
    }

    Map getOrderItemStatus(String orderId) {
        Order order = Order.get(orderId)
        def orderItems = order?.listOrderItems()
        return [
            isPurchaseOrder: order.isPurchaseOrder,
            isPutawayOrder: order.isPutawayOrder,
            orderItems: orderItems,
            currencyCode: order.currencyCode ?: grailsApplication.config.openboxes.locale.defaultCurrencyCode,
            total: order.total,
        ]
    }
}
