package org.pih.warehouse.allocation

import grails.converters.JSON
import org.pih.warehouse.requisition.Requisition

class AllocationController {
    AllocationService allocationService

    def allocate() {
        try {
            def jsonBody = request.JSON ?: [:]
            AllocationMode mode = jsonBody.mode as AllocationMode
            Requisition requisition = Requisition.get(params.id)
            List<AllocationSourceStrategy> strategies = []
            if (jsonBody.strategies) {
                strategies = jsonBody.strategies.collect { String strategy ->
                    try {
                        return AllocationSourceStrategy.valueOf(strategy)
                    } catch (IllegalArgumentException e) {
                        return null
                    }
                }.findAll { it != null }
            }
            def result = allocationService.allocate(requisition, mode ?: AllocationMode.AUTO, strategies)
            allocationService.completeAllocation(requisition, result?.any { it.suggestedItems })
            redirect(controller: "stockMovement", action: "show", id: params.id)
        } catch (Exception e) {
            render(status: 500, [errorCode: 500, errorMessage: e.message] as JSON)
        }
    }

    def deallocate() {
        try {
            Requisition requisition = Requisition.get(params.id)
            if (!requisition) {
                flash.error = "Requisition not found for id: ${params.id}"
                redirect(controller: "stockMovement", action: "show", id: params.id)
                return
            }
            allocationService.deallocate(requisition)
            redirect(controller: "stockMovement", action: "show", id: params.id)
        } catch (Exception e) {
            flash.error = "Error while clearing allocation for stock movement: ${e.message}"
            redirect(controller: "stockMovement", action: "show", id: params.id)
        }
    }

    def redoAutopick() {
        try {
            Requisition requisition = Requisition.get(params.id)
            if (!requisition) {
                flash.error = "Requisition not found for id: ${params.id}"
                redirect(controller: "stockMovement", action: "show", id: params.id)
                return
            }
            allocationService.deallocate(requisition)
            redirect(action: "allocate", id: params.id)
        } catch (Exception e) {
            flash.error = "Error while redoing picklist for stock movement: ${e.message}"
            redirect(controller: "stockMovement", action: "show", id: params.id)
        }
    }
}
