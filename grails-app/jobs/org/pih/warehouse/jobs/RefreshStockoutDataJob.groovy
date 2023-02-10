package org.pih.warehouse.jobs

import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext

@DisallowConcurrentExecution
class RefreshStockoutDataJob {

    def concurrent = false  // make `static` in Grails 3
    def reportService

    static triggers = {
        cron name: JobUtils.getCronName(RefreshStockoutDataJob),
            cronExpression: JobUtils.getCronExpression(RefreshStockoutDataJob)
    }

    def execute(JobExecutionContext context) {
        if (JobUtils.shouldExecute(RefreshStockoutDataJob)) {
            def startTime = System.currentTimeMillis()
            log.info("Refreshing stockout data: " + context.mergedJobDataMap)
            Date yesterday = new Date() - 1
            reportService.buildStockoutFact(yesterday)
            log.info "Refreshed stockout data in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }
}
