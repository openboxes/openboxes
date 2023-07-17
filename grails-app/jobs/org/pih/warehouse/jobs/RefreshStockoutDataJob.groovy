package org.pih.warehouse.jobs

import org.pih.warehouse.reporting.DateDimension
import org.quartz.JobExecutionContext

class RefreshStockoutDataJob {

    def reportService

    static concurrent = false

    def sessionRequired = false

    static triggers = {
        cron name: JobUtils.getCronName(RefreshStockoutDataJob),
            cronExpression: JobUtils.getCronExpression(RefreshStockoutDataJob)
    }

    void execute(JobExecutionContext context) {
        if (JobUtils.shouldExecute(RefreshStockoutDataJob)) {
            DateDimension.withNewSession {
                def startTime = System.currentTimeMillis()
                log.info("Refreshing stockout data: " + context.mergedJobDataMap)
                Date yesterday = new Date() - 1
                reportService.buildStockoutFact(yesterday)
                log.info "Refreshed stockout data in " + (System.currentTimeMillis() - startTime) + " ms"
            }
        }
    }
}
