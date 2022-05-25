/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

package org.pih.warehouse.inventory

import org.pih.warehouse.api.StockMovementDirection
import org.pih.warehouse.core.Location
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.shipping.Shipment

class StockTransferController {

    def stockTransferService
    def shipmentService

    def index = {
        redirect(action: "list", params: params)
    }

    def create = {
        render(template: "/common/react", params: params)
    }

    def edit = {
        Location currentLocation = Location.get(session.warehouse.id)
        def orderInstance = Order.get(params.id)

        boolean isSameOrigin = orderInstance?.origin?.id == currentLocation?.id
        boolean isSameDestination = orderInstance?.destination?.id == currentLocation?.id
        if (!(isSameOrigin || isSameDestination)) {
            flash.error = g.message(code: "retrunOrder.isDifferentLocation.message")
            redirect(controller: "stockMovement", action: "show", id: params.id)
            return
        }

        if(orderInstance?.getStockMovementDirection(currentLocation) == StockMovementDirection.INBOUND) {
            redirect(action: "createInboundReturn", params: params)
        } else if(orderInstance?.getStockMovementDirection(currentLocation) == StockMovementDirection.OUTBOUND) {
            redirect(action: "createOutboundReturn", params: params)
        }
    }

    def createOutboundReturn = {
        render(template: "/common/react", params: params)
    }

    def createInboundReturn = {
        render(template: "/common/react", params: params)
    }

    def list = {
        Location currentLocation = Location.get(session.warehouse.id)
        params.origin = currentLocation
        params.destination = currentLocation

        params.status = params.status ?: null

        // Parse date parameters
        Date lastUpdatedStartDate = params.lastUpdatedStartDate ? Date.parse("MM/dd/yyyy", params.lastUpdatedStartDate) : null
        Date lastUpdatedEndDate = params.lastUpdatedEndDate ? Date.parse("MM/dd/yyyy", params.lastUpdatedEndDate) : null

        // Pagination parameters
        params.max = params.max ?: 10
        params.offset = params.offset ?: 0

        OrderType orderType = OrderType.get(OrderTypeCode.TRANSFER_ORDER.name())

        def orderTemplate = new Order(params)
        orderTemplate.orderType = orderType

        def orders = stockTransferService.getStockTransfers(orderTemplate, lastUpdatedStartDate, lastUpdatedEndDate, params)

        [ orders : orders ]
    }

    def show = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.stockTransfer.label', default: 'Stock Transfer'), params.id])}"
            redirect(action: "list")
        } else {
            [orderInstance: orderInstance]
        }
    }

    def print = {
        Order stockTransfer = Order.get(params.id)

        [stockTransfer: stockTransfer]
    }

    def eraseStockTransfer = {
        try {
            stockTransferService.deleteStockTransfer(params.id)
        } catch (IllegalArgumentException e) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.stockTransfer.label', default: 'Stock Transfer'), params.id])}"
            redirect(action: "list")
        }
        redirect(action: "list")
    }

    def remove = {
        Location currentLocation = Location.get(session.warehouse.id)
        Order orderInstance = Order.get(params.orderId ?: params.id)
        StockMovementDirection direction = orderInstance?.getStockMovementDirection(currentLocation)

        if (!orderInstance) {
            throw new IllegalArgumentException("Order instance not found for this stock transfer")
        }

        try {
            stockTransferService.deleteStockTransfer(params.orderId ?: params.id)
        } catch (IllegalArgumentException e) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.stockTransfer.label', default: 'Stock Transfer'), params.id])}"
        }

        if (direction == StockMovementDirection.INBOUND) {
            redirect(controller: "stockMovement", action: "list", params: ['direction': StockMovementDirection.INBOUND])
        } else if (direction == StockMovementDirection.OUTBOUND) {
            redirect(controller: "stockMovement", action: "list", params: ['direction': StockMovementDirection.OUTBOUND])
        } else {
            redirect(action: "list")
        }

    }

    def rollback = {
        Location currentLocation = Location.get(session.warehouse.id)

        try {
            stockTransferService.rollbackReturnOrder(params.id as String, currentLocation)
            flash.message = "Successfully rolled back return order with ID ${params.id}"
        } catch (IllegalArgumentException e) {
            log.error("Unable to rollback return order with ID ${params.id}: " + e.message)
            flash.message = e.message
        }

        redirect( controller: "stockMovement", action: "show", id: params.id)
    }
}
