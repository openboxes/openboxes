package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.Shipment
import org.quartz.JobExecutionContext

class AutomaticReceiptJob {

    def authService
    def shipmentService
    def receiptService
    def locationService

    def sessionRequired = false

    static triggers = {
        cron name: JobUtils.getCronName(AutomaticReceiptJob),
        cronExpression: JobUtils.getCronExpression(AutomaticReceiptJob)
    }

    def execute(JobExecutionContext context) {
        if (!Holders.config.openboxes.jobs.automaticReceiptJob.enabled) {
            log.info"Automatic receipt creation job is disabled"
            return
        }

        // Jobs run without a logged-in user. Authenticate as the system user so that audited records
        // created during the receipt (e.g. EventLog, whose created_by_id/updated_by_id are NOT NULL) get
        // a valid author.
        authService.withSystemUser {
            String shipmentId = context.mergedJobDataMap.get('shipmentId')
            if (shipmentId) {
                try {
                    Shipment shipment = shipmentService.getShipmentInstance(shipmentId)
                    if (!shipment) {
                        log.warn("Shipment ${shipmentId} not found, skipping")
                        return
                    }
                    log.info("Creating automatic receipt for shipment ${shipmentId}")
                    receiptService.receiveInboundShipment(shipment)

                } catch (Exception e) {
                    log.error("Error processing shipment ${shipmentId}", e)
                }
                return
            }
            // FIXME This probably shouldn't be run during the same execution as the above code as it has the potential
            //  to create a race condition or duplicate receipts
            // Fallback in case the auto receipt job was not triggered for a specific shipment
            if (Holders.config.openboxes.jobs.automaticReceiptJob.bulkShipmentAutoReceipt) {
                List<Location> autoReceiptFacilities = locationService.getLocationsSupportingActivities([ActivityCode.AUTO_RECEIVING]) as List<Location>
                log.info "Running automatic receipt job for all shipped shipments... "
                autoReceiptFacilities.each { Location facility ->
                    receiptService.receiveInboundShipments(facility)
                }
            }
        }
    }

}
