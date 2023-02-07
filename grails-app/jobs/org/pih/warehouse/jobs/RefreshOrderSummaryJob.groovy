package org.pih.warehouse.jobs

import grails.util.Holders
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext

@DisallowConcurrentExecution
class RefreshOrderSummaryJob {

    def orderService

    static triggers = {
        cron name: 'refreshOrderSummaryJobCronTrigger',
                cronExpression: Holders.config.openboxes.jobs.refreshOrderSummaryJob.cronExpression
    }

    def execute(JobExecutionContext context) {
        Boolean enabled = Holders.config.openboxes.jobs.refreshOrderSummaryJob.enabled
        if (enabled) {
            def startTime = System.currentTimeMillis()
            log.info("Refreshing order summary: " + context.mergedJobDataMap)
            orderService.refreshOrderSummary()
            log.info "Finished refreshing order summary in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }
}
