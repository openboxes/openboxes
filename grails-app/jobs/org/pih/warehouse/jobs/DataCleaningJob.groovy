package org.pih.warehouse.jobs

import org.pih.warehouse.shipping.Shipment

class DataCleaningJob {

    def shipmentService

    static concurrent = false

    def sessionRequired = false

    static triggers = {
        cron name: JobUtils.getCronName(DataCleaningJob),
            cronExpression: JobUtils.getCronExpression(DataCleaningJob)
    }

    void execute() {
        if (!JobUtils.shouldExecute(DataCleaningJob)) {
            return
        }

        Shipment.withNewSession {
            log.info "Starting data cleaning job at ${new Date()}"
            def startTime = System.currentTimeMillis()
            shipmentService.bulkUpdateShipments()
            log.info "Finished data cleaning job in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }
}
