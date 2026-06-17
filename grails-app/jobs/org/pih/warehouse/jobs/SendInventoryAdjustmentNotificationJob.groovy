package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.WebhookPublisherService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.product.Product
import org.quartz.JobExecutionContext

class SendInventoryAdjustmentNotificationJob {

    WebhookPublisherService webhookPublisherService

    def sessionRequired = false

    static triggers = {
        /* trigger from an event occurrence, do not trigger from schedule */
    }

    def execute(JobExecutionContext context) {
        if (!Holders.config.openboxes.jobs.sendInventoryAdjustmentNotificationJob.enabled) {
            log.info"Sending inventory adjustment notification job is disabled"
            return
        }

        String productId = context.mergedJobDataMap.get("productId")
        String facilityId = context.mergedJobDataMap.get("facilityId")
        String baselineTransactionId = context.mergedJobDataMap.get("baselineTransactionId")
        String adjustmentTransactionId = context.mergedJobDataMap.get("adjustmentTransactionId")

        Product product = Product.get(productId)
        Location facility = Location.get(facilityId)
        Transaction baselineTransaction = Transaction.get(baselineTransactionId)
        Transaction adjustmentTransaction = Transaction.get(adjustmentTransactionId)

        if (!product || !facility || !(baselineTransaction || adjustmentTransaction)) {
            log.warn "Invalid data for SendInventoryAdjustmentNotificationJob, " +
                    "product: ${product?.id}, " +
                    "facility: ${facility?.id}, " +
                    "baselineTransaction: ${baselineTransaction?.id}, " +
                    "adjustmentTransaction: ${adjustmentTransaction?.id}"
            return
        }

        webhookPublisherService.publishInventoryAdjustmentEvent(
                product,
                facility,
                baselineTransaction,
                adjustmentTransaction
        )
    }
}
