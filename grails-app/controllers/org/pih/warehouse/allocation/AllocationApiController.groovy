package org.pih.warehouse.allocation

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus
import org.springframework.http.HttpStatus

@Transactional
class AllocationApiController {

    AllocationService allocationService
    StockMovementService stockMovementService

    def allocate() {
        Requisition requisition = Requisition.get(params.id)
        if (!requisition) {
            render(status: HttpStatus.NOT_FOUND.value(),
                    [errorCode: 404, errorMessage: "Requisition not found for id: ${params.id}"] as JSON)
            return
        }

        try {
            def jsonBody = request.JSON ?: [:]
            AllocationMode mode = (jsonBody.mode as AllocationMode) ?: AllocationMode.AUTO
            List<AllocationStrategy> strategies = parseStrategies(jsonBody.strategies)

            List<AllocationResult> results = allocationService.allocate(requisition, mode, strategies)
            if (results && !results.empty) {
                stockMovementService.updateRequisitionStatus(requisition.id, RequisitionStatus.PICKING)
            }

            render([data: results] as JSON)
        } catch (Exception e) {
            render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    [errorCode: 500, errorMessage: e.message] as JSON)
        }
    }

    def deallocate() {
        Requisition requisition = Requisition.get(params.id)
        if (!requisition) {
            render(status: HttpStatus.NOT_FOUND.value(),
                    [errorCode: 404, errorMessage: "Requisition not found for id: ${params.id}"] as JSON)
            return
        }

        try {
            allocationService.deallocate(requisition)
            render(status: HttpStatus.NO_CONTENT.value())
        } catch (Exception e) {
            render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    [errorCode: 500, errorMessage: e.message] as JSON)
        }
    }

    private static List<AllocationStrategy> parseStrategies(def strategies) {
        if (!strategies) {
            return []
        }
        return strategies.collect { String strategy ->
            try {
                return AllocationStrategy.valueOf(strategy)
            } catch (IllegalArgumentException e) {
                return null
            }
        }.findAll { it != null }
    }
}
