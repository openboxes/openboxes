package org.pih.warehouse.jobs

import org.quartz.DisallowConcurrentExecution

@DisallowConcurrentExecution
class DataCleaningJob {

    def concurrent = false  // make `static` in Grails 3
    def shipmentService

    static triggers = {
        cron name: JobUtils.getCronName(DataCleaningJob),
            cronExpression: JobUtils.getCronExpression(DataCleaningJob)
    }

    def execute() {
        if (!JobUtils.shouldExecute(DataCleaningJob)) {
            return
        }

        log.info "Starting data cleaning job at ${new Date()}"
        def startTime = System.currentTimeMillis()
        shipmentService.bulkUpdateShipments()
        log.info "Finished data cleaning job in " + (System.currentTimeMillis() - startTime) + " ms"
    }
}
