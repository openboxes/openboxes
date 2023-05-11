package org.pih.warehouse.jobs

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext

@DisallowConcurrentExecution
class RefreshOrderSummaryJob {

    def concurrent = false  // make `static` in Grails 3

    def orderService

    static triggers = {
        cron name: 'refreshOrderSummaryJobCronTrigger',
                cronExpression: ConfigurationHolder.config.openboxes.jobs.refreshOrderSummaryJob.cronExpression
    }

    def execute(JobExecutionContext context) {
        Boolean enabled = ConfigurationHolder.config.openboxes.jobs.refreshOrderSummaryJob.enabled
        if (enabled) {
            def startTime = System.currentTimeMillis()
            log.info("Refreshing order summary: " + context.mergedJobDataMap)
            orderService.refreshOrderSummary()
            log.info "Finished refreshing order summary in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }
}
