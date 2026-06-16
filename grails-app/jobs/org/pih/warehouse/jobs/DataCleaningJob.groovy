package org.pih.warehouse.jobs

import org.pih.warehouse.auth.AuthService
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
            // Run as the system user so any records updated are stamped with a valid current user.
            AuthService.withSystemUser {
                log.info "Starting data cleaning job at ${new Date()}"
                def startTime = System.currentTimeMillis()
                shipmentService.bulkUpdateShipments()
                log.info "Finished data cleaning job in " + (System.currentTimeMillis() - startTime) + " ms"
            }
        }
    }
}
