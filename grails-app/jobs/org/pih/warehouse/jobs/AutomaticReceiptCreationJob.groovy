package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.shipping.Shipment
import org.quartz.JobExecutionContext

class AutomaticReceiptCreationJob {

    def shipmentService
    def receiptService

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

        log.info "Running automatic receipt creation job ... "
        List<Shipment> shippedShipments = shipmentService.getAllShippedShipments()
        shippedShipments.each {
            if (!it.isFullyReceived()) {
//                receiptService.createAutomaticReceipt(it)
            }
        }
    }
}
