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

import grails.converters.JSON

import org.pih.warehouse.api.StockMovementDirection
import org.pih.warehouse.core.Location
import org.pih.warehouse.order.Order

class StockTransferController {

    def stockTransferService
    def shipmentService

    def index() {
        redirect(action: "list", params: params)
    }

    def create() {
        render(view: "/common/react", params: params)
    }

    def edit() {
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

    def createOutboundReturn() {
        render(view: "/common/react", params: params)
    }

    def createInboundReturn() {
        render(view: "/common/react", params: params)
    }

    def list() {
        render(view: "/common/react")
    }

    def show() {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.stockTransfer.label', default: 'Stock Transfer'), params.id])}"
            redirect(action: "list")
        } else {
            [orderInstance: orderInstance]
        }
    }

    def print() {
        Order stockTransfer = Order.get(params.id)

        [stockTransfer: stockTransfer]
    }

    def eraseStockTransfer() {
        try {
            stockTransferService.deleteStockTransfer(params.id)
        } catch (IllegalArgumentException e) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.stockTransfer.label', default: 'Stock Transfer'), params.id])}"
            redirect(action: "list")
        }
        redirect(action: "list")
    }

    def remove() {
        Location currentLocation = Location.get(session.warehouse.id)
        Order orderInstance = Order.get(params.orderId ?: params.id)
        StockMovementDirection direction = orderInstance?.getStockMovementDirection(currentLocation)

        if (!orderInstance) {
            throw new IllegalArgumentException("Order instance not found for this stock transfer")
        }

        try {
            stockTransferService.deleteStockTransfer(params.orderId ?: params.id)
            flash.message = g.message(
                    code: 'react.stockMovement.deleted.success.message.label',
                    default: 'Stock Movement has been deleted successfully',
            )
        } catch (Exception e) {
            flash.message = "${g.message(code: 'stockMovement.delete.error.message', default: 'The Stock Movement could not be deleted')}"
            redirect(controller: "stockMovement", action: "show", id: params.id)
            return
        }

        if (direction == StockMovementDirection.INBOUND) {
            params.direction = StockMovementDirection.INBOUND
            redirect(controller: "stockMovement", action: "list", params: params << ["flash": flash as JSON])
        } else if (direction == StockMovementDirection.OUTBOUND) {
            params.direction = StockMovementDirection.OUTBOUND
            redirect(controller: "stockMovement", action: "list", params: params << ["flash": flash as JSON])
        } else {
            redirect(action: "list", params: params << ["flash": flash as JSON])
        }
    }

    def rollback() {
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
