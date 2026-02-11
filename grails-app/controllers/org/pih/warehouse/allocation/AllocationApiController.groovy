package org.pih.warehouse.allocation

import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus

class AllocationApiController {
    AllocationService allocationService
    StockMovementService stockMovementService

    def allocate() {
        try {
            Requisition requisition = Requisition.get(params.id)
            List<AllocationStrategy> strategyList = [AllocationStrategy.WAREHOUSE_FIRST]
            def result = allocationService.allocate(requisition, AllocationMode.AUTO, strategyList)
            if (result && !result.empty) {
                stockMovementService.updateRequisitionStatus(params.id, RequisitionStatus.PICKING)
            }
            redirect(controller: "stockMovement", action: "show", id: params.id)
        } catch (Exception e) {
            response.status = 404
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
