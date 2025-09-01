package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.receiving.ReceiptStatusCode
import org.pih.warehouse.shipping.Shipment
import org.quartz.JobExecutionContext

class AutomaticReceiptJob {

    def shipmentService
    def receiptService
    def locationService
    def putawayService
    def inboundSortationService

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

                if (!shipment.hasShipped()) {
                    log.warn("Shipment ${shipmentId} has no SHIPPED event associated")
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

                if (hasPendingReceipt(shipment)) {
                    log.debug "Shipment ${shipmentId} has a pending receipt"
                    return
                }

                log.info("Creating automatic receipt for shipment ${shipmentId}")
                receiptService.createAutomaticReceipt(shipment)
                if (Holders.config.openboxes.receiving.inboundSortation.enabled) {
                    inboundSortationService.createPutawayOrdersFromReceipt(shipment.receipt)
                }
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
                    if (it.isFullyReceived()) {
                        log.debug("Shipment ${it.id} already fully received")
                        return
                    }

                    if (!it.hasShipped()) {
                        log.warn("Shipment ${it.id} has no SHIPPED event associated")
                        return
                    }

                    if (hasPendingReceipt(it)) {
                        log.debug "Shipment ${it.id} has a pending receipt"
                        return
                    }

                    receiptService.createAutomaticReceipt(it)
                    if (Holders.config.openboxes.receiving.inboundSortation.enabled) {
                        inboundSortationService.createPutawayOrdersFromReceipt(it.receipt)
                    }
                }
            }
        }
    }

    private static boolean hasPendingReceipt(Shipment shipment) {
        shipment.receipts.any { it.receiptStatusCode == ReceiptStatusCode.PENDING }
    }
}
