package org.pih.warehouse.jobs

import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext

@DisallowConcurrentExecution
class CalculateHistoricalQuantityJob {

    def concurrent = false  // make `static` in Grails 3
    static dates = []
    static enabled = true
    def inventorySnapshotService

    static triggers = {
        cron name: JobUtils.getCronName(CalculateHistoricalQuantityJob),
            cronExpression: JobUtils.getCronExpression(CalculateHistoricalQuantityJob)
    }

    def execute(JobExecutionContext context) {

        if (JobUtils.shouldExecute(CalculateHistoricalQuantityJob)) {

            log.info "Executing calculate historical quantity job at ${new Date()} with context ${context}"
            if (!dates) {
                // Filter down to the transaction dates within the last 18 months
                def daysToProcess = CH.config.openboxes.jobs.calculateHistoricalQuantityJob.daysToProcess
                def startDate = new Date() - daysToProcess
                def transactionDates = inventorySnapshotService.getTransactionDates()
                transactionDates = transactionDates.findAll { it >= startDate }
                dates = transactionDates.reverse()
                log.info "Refreshing ${dates.size()} dates"

            } else {
                log.info "There are ${dates.size()} remaining to be processed"
            }

            Date nextDate = dates.pop()
            // We need the next date that has not already been processed
            // FIXME This could get stuck if there's a date that generates 0 inventory snapshot records (but that should not happen)
            log.info "Triggering inventory snapshot for date ${nextDate}"
            inventorySnapshotService.populateInventorySnapshots(nextDate, Boolean.FALSE)
        }
    }
}
