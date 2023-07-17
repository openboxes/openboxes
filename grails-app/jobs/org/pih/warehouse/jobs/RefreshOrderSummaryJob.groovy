package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.order.OrderService
import org.quartz.JobExecutionContext

class RefreshOrderSummaryJob {

    OrderService orderService

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
            orderService.refreshOrderSummary()
            log.info "Finished refreshing order summary in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }
}
