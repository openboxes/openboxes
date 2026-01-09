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
        try {
            def result = allocationService.allocate(
                    params.itemId, jsonBody.mode as AllocationType, jsonBody.allocations as List<AllocationDto>)
            render(result as JSON)
        } catch (Exception e) {
            render(status: 500, [errorCode: 500, message: e.message] as JSON)
        }
    }
}