package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.order.OrderSummaryService
import org.quartz.JobExecutionContext

class RefreshOrderSummaryJob {

    OrderSummaryService orderSummaryService

    static concurrent = false

    def sessionRequired = false

    static triggers = {
        cron name: 'refreshOrderSummaryJobCronTrigger',
                cronExpression: Holders.config.openboxes.jobs.refreshOrderSummaryJob.cronExpression
    }

    void execute(JobExecutionContext context) {
        if (JobUtils.shouldExecute(RefreshOrderSummaryJob)) {
            def startTime = System.currentTimeMillis()
            log.info("Refreshing order summary: " + context.mergedJobDataMap)
            orderSummaryService.refreshOrderSummary()
            log.info "Finished refreshing order summary in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }
}
