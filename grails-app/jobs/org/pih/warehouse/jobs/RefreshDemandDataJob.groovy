package org.pih.warehouse.jobs

import grails.util.Holders
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext

@DisallowConcurrentExecution
class RefreshDemandDataJob {

    def reportService

    // Should never be triggered on a schedule - should only be triggered by persistence event listener
    static triggers = {
        cron name: 'refreshDemandDataJobCronTrigger',
                cronExpression: Holders.grailsApplication.config.openboxes.jobs.refreshDemandDataJob.cronExpression
    }

    def execute(JobExecutionContext context) {

        Boolean enabled = Holders.grailsApplication.config.openboxes.jobs.refreshDemandDataJob.enabled
        if (enabled) {
            def startTime = System.currentTimeMillis()
            log.info("Refreshing demand data: " + context.mergedJobDataMap)
            reportService.refreshDemandData()
            log.info "Finished refreshing demand data in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }
}
