package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.Shipment
import org.quartz.JobExecutionContext

class AutomaticReceiptCreationJob {

    def shipmentService
    def receiptService
    def locationService

    def sessionRequired = false

    static triggers = {
        cron name: JobUtils.getCronName(AutomaticReceiptCreationJob),
        cronExpression: JobUtils.getCronExpression(AutomaticReceiptCreationJob)
    }

    def execute(JobExecutionContext context) {
        if (!Holders.config.openboxes.jobs.automaticReceiptCreationJob.enabled) {
            log.info"Automatic receipt creation job is disabled"
            return
        }

        log.info "Running automatic receipt creation job... "
        List<Location> autoReceivingLocations = locationService.getLocationsSupportingActivities([ActivityCode.AUTO_RECEIVING])
        autoReceivingLocations.each {
            List<Shipment> shippedShipments = shipmentService.getShippedShipmentsByDestination(it)
            shippedShipments.each {
                if (!it.isFullyReceived()) {
                    receiptService.createAutomaticReceipt(it)
                }
            }
        }
    }
}
