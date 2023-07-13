package org.pih.warehouse.jobs

import org.quartz.JobExecutionContext

class RefreshStockoutDataJob extends SessionlessJob {

    def reportService

    static concurrent = false

    static triggers = {
        cron name: JobUtils.getCronName(RefreshStockoutDataJob),
            cronExpression: JobUtils.getCronExpression(RefreshStockoutDataJob)
    }

    void execute(JobExecutionContext context) {
        if (JobUtils.shouldExecute(RefreshStockoutDataJob)) {
            def startTime = System.currentTimeMillis()
            log.info("Refreshing stockout data: " + context.mergedJobDataMap)
            Date yesterday = new Date() - 1
            reportService.buildStockoutFact(yesterday)
            log.info "Refreshed stockout data in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }
}
