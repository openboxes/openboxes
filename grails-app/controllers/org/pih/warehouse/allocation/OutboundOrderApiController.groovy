package org.pih.warehouse.allocation

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.pih.warehouse.api.StockMovement
import org.springframework.http.HttpStatus

@Transactional
class OutboundOrderApiController {

    AllocationService allocationService
    OutboundOrderService outboundOrderService

    def read() {
        StockMovement outboundOrder = allocationService.getOutboundOrder(params.id)
        if (!outboundOrder) {
            render (status: HttpStatus.NOT_FOUND.value(), [errorCode: 404, message: "Outbound order not found"] as JSON)
            return
        }

        render([data: outboundOrder] as JSON)
    }

    def allocate() {
        def jsonBody = request.JSON ?: [:]
        AllocationMode mode = jsonBody.mode as AllocationMode
        List<AllocationDto> allocations = jsonBody.allocations as List<AllocationDto>
        try {
            List<AllocationSourceStrategy> strategies = parseStrategies(jsonBody.strategies)
            def result = allocationService.allocate(params.itemId, mode, allocations, strategies)
            render(result as JSON)
        } catch (Exception e) {
            render(status: 500, [errorCode: 500, errorMessage: e.message] as JSON)
        }
    }

    def amend() {
        def jsonBody = request.JSON ?: [:]
        Integer quantity = jsonBody.quantity != null ? jsonBody.quantity as Integer : null
        Boolean crossDock = jsonBody.crossDock != null ? jsonBody.crossDock as Boolean : null

        Map result = outboundOrderService.amend(params.id, params.itemId, quantity,
                jsonBody.reasonCode as String, crossDock, parseStrategies(jsonBody.strategies))
        render([data: result] as JSON)
    }

    private static List<AllocationSourceStrategy> parseStrategies(def strategies) {
        if (!strategies) {
            return []
        }
        return strategies.collect { String strategy ->
            try {
                return AllocationSourceStrategy.valueOf(strategy)
            } catch (IllegalArgumentException e) {
                return null
            }
        }.findAll { it != null }
    }
}
