package org.pih.warehouse.jobs

import org.pih.warehouse.reporting.DateDimension
import org.quartz.JobExecutionContext

class RefreshTransactionFactJob {

    def reportService

    static concurrent = false

    def sessionRequired = false

    static triggers = {
        cron name: JobUtils.getCronName(RefreshTransactionFactJob),
            cronExpression: JobUtils.getCronExpression(RefreshTransactionFactJob)
    }

    void execute(JobExecutionContext context) {
        if (JobUtils.shouldExecute(RefreshTransactionFactJob)) {
            DateDimension.withNewSession {
                log.info("Refresh transaction fact and dimensions: " + context.mergedJobDataMap)
                def startTime = System.currentTimeMillis()
                reportService.buildDimensions()
                reportService.buildFacts()
                log.info "Refreshed transaction fact and dimensions: ${(System.currentTimeMillis() - startTime)} ms"
            }
        }
    }
}
