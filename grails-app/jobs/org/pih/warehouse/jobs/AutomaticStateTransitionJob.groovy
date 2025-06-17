package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.integration.StateTransitionService
import org.pih.warehouse.requisition.Requisition
import org.quartz.JobExecutionContext

class AutomaticStateTransitionJob {

    StateTransitionService stateTransitionService

    def sessionRequired = false

    static triggers = {
        /* should only be triggered programmatically */
    }

    def execute(JobExecutionContext context) {
        log.info "Automatic state transition job " + context.mergedJobDataMap.get("id")
        if (!Holders.config.openboxes.jobs.automaticStateTransitionJob.enabled) {
            return
        }
        String id = context.mergedJobDataMap.get("id")
        Requisition requisition = Requisition.get(id)
        if (!requisition) {
            log.warn "Requisition with id ${id} not found, cannot transition state"
            return
        }

        StockMovement stockMovement = StockMovement.createFromRequisition(requisition, true)
        log.info "Transition stock movement " + stockMovement.identifier + " status = " + stockMovement.status
        stateTransitionService.triggerStockMovementStatusUpdate(stockMovement)
    }
}
