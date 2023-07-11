package org.pih.warehouse.jobs

import org.quartz.JobExecutionContext

class RefreshDemandDataJob {

    def reportService

    static concurrent = false

    // By default this is true on QuartzDisplayJob, which invokes execute()
    // and if sessionRequired is true, then QuartzDisplayJob tries to do session flush
    // even if there is no session
    static sessionRequired = false

    static triggers = {
        cron name: JobUtils.getCronName(RefreshDemandDataJob),
            cronExpression: JobUtils.getCronExpression(RefreshDemandDataJob)
    }

    void execute(JobExecutionContext context) {
        if (JobUtils.shouldExecute(RefreshDemandDataJob)) {
            def startTime = System.currentTimeMillis()
            log.info("Refreshing demand data: " + context.mergedJobDataMap)
            reportService.refreshProductDemandData()
            log.info "Finished refreshing demand data in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }
}
