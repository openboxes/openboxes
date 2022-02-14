package org.pih.warehouse.jobs

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.inventory.StockMovementStatusCode
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext

@DisallowConcurrentExecution
class AutomaticStateTransitionJob {

    def tmsIntegrationService
    def stockMovementService

    static triggers = {
        /* should only be triggered programmatically */
    }

    def execute(JobExecutionContext context) {
        log.info "Automatic state transition job " + context.mergedJobDataMap.get("id")
        if(!ConfigurationHolder.config.openboxes.jobs.automaticStateTransitionJob.enabled) {
            return
        }
        String id = context.mergedJobDataMap.get("id")
        StockMovement stockMovement = stockMovementService.getStockMovement(id, false)
        if (stockMovement) {
            log.info "Transition stock movement " + stockMovement.identifier + " status = " + stockMovement.status
            tmsIntegrationService.triggerStockMovementStatusUpdate(stockMovement)
        }
    }
}
