package org.pih.warehouse.outboundOrder

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.springframework.http.HttpStatus

@Transactional
class OutboundOrderApiController {

    OutboundOrderService outboundOrderService

    def read() {
        StockMovement outboundOrder = outboundOrderService.get(params.id)
        if (!outboundOrder) {
            render (status: HttpStatus.NOT_FOUND.value(), [errorCode: 404, message: "Outbound order not found"] as JSON)
            return
        }

        render([data: outboundOrder] as JSON)
    }

    def allocate() {
        def jsonBody = request.JSON ?: [:]
        StockMovement outboundOrder = outboundOrderService.get(params.id)
        if (!outboundOrder) {
            throw new IllegalArgumentException("Order with id ${params.id} not found")
        }

        StockMovementItem orderItem = outboundOrder.lineItems.find { StockMovementItem lineItem ->
            lineItem.id == params.itemId
        }

        if (!orderItem) {
            throw new IllegalArgumentException("Order item with id ${params.itemId} not found")
        }

        outboundOrderService.allocate(orderItem, jsonBody)

        render status: 200
    }
}