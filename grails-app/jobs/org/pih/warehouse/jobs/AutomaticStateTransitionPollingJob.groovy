package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.requisition.RequisitionStatus
import org.quartz.JobExecutionContext

class AutomaticStateTransitionPollingJob {

    def stockMovementService

    def sessionRequired = false

    static triggers = {
        cron name: JobUtils.getCronName(AutomaticStateTransitionPollingJob),
        cronExpression: JobUtils.getCronExpression(AutomaticStateTransitionPollingJob)
    }

    def execute(JobExecutionContext context) {
        if (!Holders.config.openboxes.jobs.automaticStateTransitionPollingJob.enabled) {
            log.info "Automatic state transition job is disabled"
            return
        }

        log.info "Running automatic state transition job"
        StockMovement criteria = new StockMovement(requisitionStatusCodes: [RequisitionStatus.PICKING])
        def stockMovements = stockMovementService.getOutboundStockMovements(criteria, [:])
        log.info "Found ${stockMovements.size()} stock movements in PICKING "
        stockMovements.each { StockMovement stockMovement ->
            log.info "stock movement " + stockMovement.identifier + " " + stockMovement.status
            AutomaticStateTransitionJob.triggerNow([id: stockMovement.id])
        }
    }
}
