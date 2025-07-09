package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.Shipment
import org.quartz.JobExecutionContext

class AutomaticReceiptJob {

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

        String shipmentId = context.mergedJobDataMap.get('shipmentId')
        if (shipmentId) {
            try {
                Shipment shipment = shipmentService.getShipmentInstance(shipmentId)
                if (!shipment) {
                    log.warn("Shipment ${shipmentId} not found, skipping")
                    return
                }

                if (!shipment.destination?.supports(ActivityCode.AUTO_RECEIVING)) {
                    log.debug("Shipment ${shipmentId}: origin does not support AUTO_RECEIVING")
                    return
                }

                if (shipment.isFullyReceived()) {
                    log.debug("Shipment ${shipmentId} already fully received")
                    return
                }

                log.info("Creating automatic receipt for shipment ${shipmentId}")
                receiptService.createAutomaticReceipt(shipment)
            } catch (Exception e) {
                log.error("Error processing shipment ${shipmentId}", e)
            }
            return
        }

        if (Holders.config.openboxes.jobs.automaticReceiptJob.bulkShipmentAutoReceipt) {
            log.info "Running automatic receipt job for all shipped shipments... "
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
}
