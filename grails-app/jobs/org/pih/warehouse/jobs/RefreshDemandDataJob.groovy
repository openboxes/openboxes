package org.pih.warehouse.jobs

import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext

@DisallowConcurrentExecution
class RefreshDemandDataJob {

    def concurrent = false  // make `static` in Grails 3
    def reportService

    static triggers = {
        cron name: JobUtils.getCronName(RefreshDemandDataJob),
            cronExpression: JobUtils.getCronExpression(RefreshDemandDataJob)
    }

    def execute(JobExecutionContext context) {
        if (JobUtils.shouldExecute(RefreshDemandDataJob)) {
            def startTime = System.currentTimeMillis()
            log.info("Refreshing demand data: " + context.mergedJobDataMap)
            reportService.refreshProductDemandData()
            log.info "Finished refreshing demand data in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }
}
