package org.pih.warehouse.jobs

class DataCleaningJob {

    def shipmentService

    static concurrent = false

    // By default this is true on QuartzDisplayJob, which invokes execute()
    // and if sessionRequired is true, then QuartzDisplayJob tries to do session flush
    // even if there is no session
    static sessionRequired = false

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
