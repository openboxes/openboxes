package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.inventory.StockMovementStatusCode
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext

@DisallowConcurrentExecution
class AutomaticStateTransitionPollingJob {

    def stockMovementService

    static triggers = {
        cron name: 'autoTransitionRequisitionStatusJobCronTrigger',
                cronExpression: Holders.config.openboxes.jobs.automaticStateTransitionPollingJob.cronExpression
    }

    def execute(JobExecutionContext context) {

        if (!Holders.config.openboxes.jobs.automaticStateTransitionPollingJob.enabled) {
            log.info "Automatic state transition job is disabled"
            return
        }
        log.info "Running automatic state transition job ... "
        StockMovement criteria = new StockMovement(stockMovementStatusCode: StockMovementStatusCode.PICKING)
        def stockMovements = stockMovementService.getOutboundStockMovements(criteria, [:])
        log.info "Found ${stockMovements.size()} stock movements in PICKING "
        stockMovements.each { StockMovement stockMovement ->
            log.info "stock movement " + stockMovement.identifier + " " + stockMovement.status
            AutomaticStateTransitionJob.triggerNow([id: stockMovement.id])
        }
    }
}
