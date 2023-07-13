package org.pih.warehouse.jobs

import org.quartz.JobExecutionContext

class RefreshTransactionFactJob extends SessionlessJob {

    def reportService

    static concurrent = false

    static triggers = {
        cron name: JobUtils.getCronName(RefreshTransactionFactJob),
            cronExpression: JobUtils.getCronExpression(RefreshTransactionFactJob)
    }

    void execute(JobExecutionContext context) {
        if (JobUtils.shouldExecute(RefreshTransactionFactJob)) {
            log.info("Refresh transaction fact and dimensions: " + context.mergedJobDataMap)
            def startTime = System.currentTimeMillis()
            reportService.buildDimensions()
            reportService.buildFacts()
            log.info "Refreshed transaction fact and dimensions: ${(System.currentTimeMillis() - startTime)} ms"
        }
    }
}
