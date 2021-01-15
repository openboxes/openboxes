package org.pih.warehouse.jobs

import grails.core.GrailsApplication
import grails.util.Holders
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext

@DisallowConcurrentExecution
class RefreshStockoutDataJob {

    GrailsApplication grailsApplication
    def reportService

    static triggers = {
        cron name: 'refreshProductStockoutDataJobCronTrigger',
                cronExpression: Holders.config.openboxes.jobs.refreshStockoutDataJob.cronExpression
    }

    def execute(JobExecutionContext context) {
        Boolean enabled = grailsApplication.config.openboxes.jobs.refreshStockoutDataJob.enabled
        if (enabled) {
            def startTime = System.currentTimeMillis()
            log.info("Refreshing stockout data: " + context.mergedJobDataMap)
            Date yesterday = new Date()-1
            reportService.buildStockoutFact(yesterday)
            log.info "Refreshed stockout data in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }
}
