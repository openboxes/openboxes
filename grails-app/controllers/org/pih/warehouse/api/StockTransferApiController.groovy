/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import grails.validation.ValidationException
import org.grails.web.json.JSONObject
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentType

import java.text.SimpleDateFormat

class StockTransferApiController {

    def identifierService
    def inventoryService
    def orderService
    def shipmentService
    def stockTransferService

    def list() {
        if (!params.location) {
            def message = "Location parameter is required"
            response.status = 400
            render([errorMessage: message] as JSON)
            return
        }

        def stockTransfers = stockTransferService.getStockTransfers(params)
        render([
            data: stockTransfers?.collect { it.toJson(it.orderType.orderTypeCode) },
            totalCount: stockTransfers.totalCount
        ] as JSON)
    }

    def read() {
        Order order = Order.get(params.id)
        if (!order) {
            throw new IllegalArgumentException("No stock transfer found for order ID ${params.id}")
        }

        StockTransfer stockTransfer = StockTransfer.createFromOrder(order)
        stockTransferService.setQuantityOnHand(stockTransfer)
        if (order?.picklist) {
            stockTransferService.getDocuments(stockTransfer)
        }
        render([data: stockTransfer?.toJson()] as JSON)
    }

    def create() {
        JSONObject jsonObject = request.JSON

        User currentUser = User.get(session.user.id)
        Location currentLocation = Location.get(session.warehouse.id)
        if (!currentLocation || !currentUser) {
            throw new IllegalArgumentException("User must be logged into a location to update stock transfer")
        }

        StockTransfer stockTransfer = new StockTransfer()

        bindStockTransferData(stockTransfer, currentUser, currentLocation, jsonObject)

        Order order = stockTransferService.createOrUpdateOrderFromStockTransfer(stockTransfer)

        // TODO: Refactor - Return only status
        stockTransfer = StockTransfer.createFromOrder(order)
        stockTransferService.setQuantityOnHand(stockTransfer)
        render([data: stockTransfer?.toJson()] as JSON)
    }

    def update() {
        JSONObject jsonObject = request.JSON

        User currentUser = User.get(session.user.id)
        Location currentLocation = Location.get(session.warehouse.id)
        if (!currentLocation || !currentUser) {
            throw new IllegalArgumentException("User must be logged into a location to update stock transfer")
        }

        Order order = Order.get(params.id)
        if (!order) {
            throw new IllegalArgumentException("No stock transfer found for order ID ${params.id}")
        }

        StockTransfer stockTransfer = new StockTransfer()

        bindStockTransferData(stockTransfer, currentUser, currentLocation, jsonObject)

        Boolean isReturnType = stockTransfer.type == OrderType.findByCode(Constants.RETURN_ORDER)
        if (isReturnType && (stockTransfer?.status == StockTransferStatus.PLACED)) {
            order = stockTransferService.createOrUpdateOrderFromStockTransfer(stockTransfer)
            shipmentService.createOrUpdateShipment(stockTransfer)
        } else if (!isReturnType && stockTransfer?.status == StockTransferStatus.COMPLETED) {
            order = stockTransferService.completeStockTransfer(stockTransfer)
        } else {
            order = stockTransferService.createOrUpdateOrderFromStockTransfer(stockTransfer)
        }

        // TODO: Refactor - Return only status
        stockTransfer = StockTransfer.createFromOrder(order)
        stockTransferService.setQuantityOnHand(stockTransfer)
        render([data: stockTransfer?.toJson()] as JSON)
    }

    StockTransfer bindStockTransferData(StockTransfer stockTransfer, User currentUser, Location currentLocation, JSONObject jsonObject) {
        bindData(stockTransfer, jsonObject, [exclude: ['stockTransferItems']])

        if (!stockTransfer.origin) {
            stockTransfer.origin = currentLocation
        }

        if (!stockTransfer.destination) {
            stockTransfer.destination = currentLocation
        }

        if (!stockTransfer.orderedBy) {
            stockTransfer.orderedBy = currentUser
        }

        if (!stockTransfer.stockTransferNumber) {
            stockTransfer.stockTransferNumber = identifierService.generateOrderIdentifier()
        }

        if (jsonObject.type) {
            stockTransfer.type = OrderType.get(jsonObject.type)
        }

        if (jsonObject.shipmentType) {
            stockTransfer.shipmentType = ShipmentType.get(jsonObject.shipmentType?.id)
        }

        def dateFormat = new SimpleDateFormat("MM/dd/yyyy")
        if (jsonObject.dateShipped) {
            stockTransfer.dateShipped = dateFormat.parse(jsonObject.dateShipped)
        }

        if (jsonObject.expectedDeliveryDate) {
            stockTransfer.expectedDeliveryDate = dateFormat.parse(jsonObject.expectedDeliveryDate)
        }

        jsonObject.stockTransferItems.each { stockTransferItemMap ->
            StockTransferItem stockTransferItem = new StockTransferItem()
            stockTransferItem.id = stockTransferItemMap["id"] ? stockTransferItemMap["id"] : null
            stockTransferItem.productAvailabilityId = stockTransferItemMap["productAvailabilityId"] ? stockTransferItemMap["productAvailabilityId"] : null
            stockTransferItem.product = stockTransferItemMap?.product?.id ? Product.load(stockTransferItemMap?.product?.id) : null
            stockTransferItem.originBinLocation = stockTransferItemMap?.originBinLocation?.id ? Location.load(stockTransferItemMap?.originBinLocation?.id) : null
            stockTransferItem.destinationBinLocation = stockTransferItemMap?.destinationBinLocation?.id ? Location.load(stockTransferItemMap?.destinationBinLocation?.id) : null
            stockTransferItem.inventoryItem = stockTransferItemMap?.inventoryItem?.id ? InventoryItem.load(stockTransferItemMap?.inventoryItem?.id) : null
            stockTransferItem.quantityOnHand = stockTransferItemMap["quantityOnHand"] ? stockTransferItemMap["quantityOnHand"] : 0
            stockTransferItem.quantityNotPicked = stockTransferItemMap["quantityNotPicked"] ? stockTransferItemMap["quantityNotPicked"] : 0
            stockTransferItem.quantity = stockTransferItemMap["quantity"] ? new BigDecimal(stockTransferItemMap["quantity"]) : 0
            stockTransferItem.status = stockTransferItemMap["status"] ? stockTransferItemMap["status"] : null
            stockTransferItem.recipient = stockTransferItemMap?.recipient?.id ? Person.load(stockTransferItemMap?.recipient?.id) : null

            if (!stockTransferItem.location) {
                stockTransferItem.location = stockTransfer.origin
            }

            stockTransferItemMap.splitItems.each { splitItemMap ->
                StockTransferItem splitItem = new StockTransferItem()
                bindData(splitItem, splitItemMap)
                if (!splitItem.location) {
                    splitItem.location = stockTransfer.origin
                }
                stockTransferItem.splitItems.add(splitItem)
            }

            // For inbound returns
            Date expirationDate = stockTransferItemMap.expirationDate ? Constants.EXPIRATION_DATE_FORMATTER.parse(stockTransferItemMap.expirationDate) : null
            String lotNumber = stockTransferItemMap.lotNumber ? stockTransferItemMap.lotNumber : null
            stockTransferItem.inventoryItem = inventoryService.findAndUpdateOrCreateInventoryItem(
                    stockTransferItem.product,
                    lotNumber,
                    expirationDate
            )

            if (stockTransferItemMap.sortOrder) {
                stockTransferItem.orderIndex = stockTransferItemMap.sortOrder
            }

            stockTransfer.stockTransferItems.add(stockTransferItem)
        }

        return stockTransfer
    }

    def stockTransferCandidates() {
        Location location = Location.get(params.location.id)
        if (!location) {
            throw new IllegalArgumentException("Can't find location with given id: ${params.location.id}")
        }

        List<StockTransferItem> stockTransferCandidates = stockTransferService.getStockTransferCandidates(location, null)
        render([data: stockTransferCandidates?.collect { it.toJson() }] as JSON)
    }

    def returnCandidates() {
        Location location = Location.get(request?.JSON?.locationId)
        if (!location) {
            throw new IllegalArgumentException("Can't find location with given id: ${request?.JSON?.locationId}")
        }

        List<StockTransferItem> stockTransferCandidates = stockTransferService.getStockTransferCandidates(location, request?.JSON)
        render([data: stockTransferCandidates?.collect { it.toJson() }] as JSON)
    }

    def removeItem() {
        Order order = stockTransferService.deleteStockTransferItem(params.id)
        StockTransfer stockTransfer = StockTransfer.createFromOrder(order)
        stockTransferService.setQuantityOnHand(stockTransfer)
        render([data: stockTransfer?.toJson()] as JSON)
    }

    def removeAllItems() {
        Order order = stockTransferService.deleteAllStockTransferItems(params.id)
        render([data: StockTransfer.createFromOrder(order)?.toJson()] as JSON)
    }

    def sendShipment() {
        Order order = Order.get(params.id)
        if (!order) {
            throw new IllegalArgumentException("Can't find order with given id: ${params.id}")
        }

        shipmentService.sendShipment(order)
        render status: 200
    }

    def rollback() {
        Location currentLocation = Location.get(session.warehouse.id)

        stockTransferService.rollbackReturnOrder(params.id as String, currentLocation)
        render status: 200
    }

    def delete() {
        def order = Order.get(params.id)
        if (!order) {
            def message = "Order does not exist"
            response.status = 404
            render([errorMessage: message] as JSON)
            return
        }

        if (order.status > OrderStatus.APPROVED || order.orderType.orderTypeCode != OrderTypeCode.TRANSFER_ORDER) {
            def message = "Cannot delete this order"
            response.status = 400
            render([errorMessage: message] as JSON)
            return
        }

        orderService.deleteOrder(order)
        render status: 204
    }

    def statusOptions() {
        def statusOptions = OrderStatus.listStockTransfer().collect{
            [ id: it.name(), value: it.name(), label: "${g.message(code: 'enum.OrderStatus.' + it.name())}", variant: it.variant?.name()]
        }
        render([data: statusOptions] as JSON)
    }
}
