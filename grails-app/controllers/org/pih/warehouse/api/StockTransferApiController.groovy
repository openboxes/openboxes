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
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode

class StockTransferApiController {

    def identifierService
    def stockTransferService

    def list = {
        List<Order> stockTransfers = Order.findAllByOrderType(OrderType.get(OrderTypeCode.TRANSFER_ORDER.name()))
        render([data: stockTransfers.collect { it.toJson() }] as JSON)
    }

    def read = {
        Order order = Order.get(params.id)
        if (!order) {
            throw new IllegalArgumentException("No stock transfer found for order ID ${params.id}")
        }

        StockTransfer stockTransfer = StockTransfer.createFromOrder(order)
        stockTransferService.setQuantityOnHand(stockTransfer)
        render([data: stockTransfer?.toJson()] as JSON)
    }

    def create = {
        JSONObject jsonObject = request.JSON

        User currentUser = User.get(session.user.id)
        Location currentLocation = Location.get(session.warehouse.id)
        if (!currentLocation || !currentUser) {
            throw new IllegalArgumentException("User must be logged into a location to update stock transfer")
        }

        StockTransfer stockTransfer = new StockTransfer()

        bindStockTransferData(stockTransfer, currentUser, currentLocation, jsonObject)

        Order order
        if (stockTransfer?.status == StockTransferStatus.COMPLETED) {
            order = stockTransferService.completeStockTransfer(stockTransfer)
        } else {
            order = stockTransferService.createOrderFromStockTransfer(stockTransfer)
            if (order.hasErrors() || !order.save(flush: true)) {
                throw new ValidationException("Invalid order", order.errors)
            }
        }

        stockTransfer = StockTransfer.createFromOrder(order)
        stockTransferService.setQuantityOnHand(stockTransfer)
        render([data: stockTransfer?.toJson()] as JSON)
    }

    StockTransfer bindStockTransferData(StockTransfer stockTransfer, User currentUser, Location currentLocation, JSONObject jsonObject) {
        bindData(stockTransfer, jsonObject)

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

        jsonObject.stockTransferItems.each { stockTransferItemMap ->
            StockTransferItem stockTransferItem = new StockTransferItem()
            bindData(stockTransferItem, stockTransferItemMap)
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

            stockTransfer.stockTransferItems.add(stockTransferItem)
        }

        return stockTransfer
    }

    def stockTransferCandidates = {
        Location location = Location.get(params.location.id)
        if (!location) {
            throw new IllegalArgumentException("Can't find location with given id: ${params.location.id}")
        }

        List<StockTransferItem> stockTransferCandidates = stockTransferService.getStockTransferCandidates(location)
        render([data: stockTransferCandidates?.collect { it.toJson() }] as JSON)
    }

    def removeItem = {
        Order order = stockTransferService.deleteStockTransferItem(params.id)
        render([data: StockTransfer.createFromOrder(order)?.toJson()] as JSON)
    }
}
