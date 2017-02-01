package org.pih.warehouse.jobs

import grails.util.Holders
import org.quartz.JobExecutionContext

class CalculateHistoricalQuantityJob {

    static dates = []
    static enabled = true
    def inventoryService

    // cron job needs to be triggered after the staging deployment
    static triggers = {
		cron name:'cronHistoricalTrigger', cronExpression: Holders.grailsApplication.config.openboxes.jobs.calculateHistoricalQuantityJob.cronExpression
    }

    def execute(JobExecutionContext context) {
        enabled = ConfigHolder.config.openboxes.jobs.calculateHistoricalQuantityJob.enabled
        if (enabled) {
            log.info "Executing calculate historical quantity job at ${new Date()} with context ${context}"
            if (!dates) {
                // Filter down to the transaction dates within the last 18 months
                def daysToProcess = Holders.grailsApplication.config.openboxes.jobs.calculateHistoricalQuantityJob.daysToProcess
                def startDate = new Date() - daysToProcess
                def transactionDates = inventoryService.getTransactionDates()
                transactionDates = transactionDates.findAll { it >= startDate }
                dates = transactionDates.reverse()
                log.info "Refreshing ${dates.size()} dates"

            } else {
                log.info "There are ${dates.size()} remaining to be processed"
            }

            def nextDate = dates.pop()
            // We need the next date that has not already been processed
            // FIXME This could get stuck if there's a date that generates 0 inventory snapshot records (but that should not happen)
            log.info "Triggering inventory snapshot for date ${nextDate}"
            //CalculateQuantityJob.triggerNow([date: nextDate, includeInventoryItemSnapshot: false])
            inventoryService.createOrUpdateInventorySnapshot(nextDate)
        }
    }


}
