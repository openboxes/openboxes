package org.pih.warehouse.outboundOrder

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.pih.warehouse.api.StockMovement
import org.springframework.http.HttpStatus

@Transactional
class OutboundOrderApiController {

    AllocationService allocationService

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
            List<AllocationStrategy> strategies = []
            if (jsonBody.strategies) {
                strategies = jsonBody.strategies.collect { String strategy ->
                    try {
                        return AllocationStrategy.valueOf(strategy)
                    } catch (IllegalArgumentException e) {
                        return null
                    }
                }.findAll { it != null }
            }

            def result = allocationService.allocate(params.itemId, mode, allocations, strategies)
            render(result as JSON)
        } catch (Exception e) {
            render(status: 500, [errorCode: 500, errorMessage: e.message] as JSON)
        }
    }
}