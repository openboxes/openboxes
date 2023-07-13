package org.pih.warehouse.jobs

class DataCleaningJob extends SessionlessJob {

    def shipmentService

    static concurrent = false

    static triggers = {
        cron name: JobUtils.getCronName(DataCleaningJob),
            cronExpression: JobUtils.getCronExpression(DataCleaningJob)
    }

    void execute() {
        if (!JobUtils.shouldExecute(DataCleaningJob)) {
            return
        }

        log.info "Starting data cleaning job at ${new Date()}"
        def startTime = System.currentTimeMillis()
        shipmentService.bulkUpdateShipments()
        log.info "Finished data cleaning job in " + (System.currentTimeMillis() - startTime) + " ms"
    }
}
