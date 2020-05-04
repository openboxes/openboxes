package org.pih.warehouse.jobs

import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext

@DisallowConcurrentExecution
class RefreshTransactionFactJob {

    def concurrent = false  // make `static` in Grails 3
    def reportService

    static triggers = {
        cron name: JobUtils.getCronName(RefreshTransactionFactJob),
            cronExpression: JobUtils.getCronExpression(RefreshTransactionFactJob)
    }

    def execute(JobExecutionContext context) {
        if (JobUtils.shouldExecute(RefreshTransactionFactJob)) {
            log.info("Refresh transaction fact and dimensions: " + context.mergedJobDataMap)
            def startTime = System.currentTimeMillis()
            reportService.buildDimensions()
            reportService.buildFacts()
            log.info "Refreshed transaction fact and dimensions: ${(System.currentTimeMillis() - startTime)} ms"
        }
    }
}
