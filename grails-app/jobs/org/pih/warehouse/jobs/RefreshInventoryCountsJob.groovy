package org.pih.warehouse.jobs

import org.pih.warehouse.inventory.InventoryCountService
import org.quartz.JobExecutionContext

class RefreshInventoryCountsJob {

    InventoryCountService inventoryCountService

    static concurrent = false

    def sessionRequired = false

    static triggers = {
        cron name: JobUtils.getCronName(RefreshInventoryCountsJob),
            cronExpression: JobUtils.getCronExpression(RefreshInventoryCountsJob)
    }

    void execute(JobExecutionContext context) {
        if (JobUtils.shouldExecute(RefreshInventoryCountsJob)) {
            def startTime = System.currentTimeMillis()
            log.info("Refreshing inventory count candidates: " + context.mergedJobDataMap)
            inventoryCountService.refreshCandidateTables()
            log.info "Finished refreshing inventory count candidates in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }
}
