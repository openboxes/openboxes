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

import org.pih.warehouse.core.Location
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode

class StockTransferController {

    def stockTransferService

    def index = {
        redirect(action: "list", params: params)
    }

    def create = {
        render(template: "/common/react", params: params)
    }

    def createReturns = {
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

    def delete = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.stockTransfer.label', default: 'Stock Transfer'), params.id])}"
        } else {
            stockTransferService.deleteStockTransfer(params.id)
        }
        redirect(action: "list")
    }
}
