package org.pih.warehouse.jobs

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.OrderTypeCode
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.requisition.Requisition
import org.quartz.JobExecutionContext

class AutomaticIssuanceJob {

    StockMovementService stockMovementService
    AuthService authService

    def sessionRequired = false

    static concurrent = false

    static triggers = {
        cron name: JobUtils.getCronName(AutomaticIssuanceJob),
                cronExpression: JobUtils.getCronExpression(AutomaticIssuanceJob)
    }

    def execute(JobExecutionContext context) {
        if (!JobUtils.shouldExecute(AutomaticIssuanceJob)) {
            log.info "Automatic Issuance job is disabled"
            return
        }

        String requisitionId = context.mergedJobDataMap.get('requisitionId')
        if (requisitionId) {
            issueRequisition(requisitionId)
        } else {
            List<String> requisitionIds = stockMovementService.findStagedRequisitionIds()
            log.info "Found ${requisitionIds.size()} outbound STAGED requisitions to automatic issue"

            requisitionIds.each { String id ->
                try {
                    issueRequisition(id)
                } catch (Exception e) {
                    // Don't let one failing requisition stop the rest of the batch.
                    log.error("Error issuing requisition ${id}", e)
                }
            }
        }
    }

    void issueRequisition(String id) {
        authService.withSystemUser {
            Requisition requisition = Requisition.get(id)
            if (!requisition) {
                log.warn("Requisition ${id} not found, skipping")
                return
            }

            if (requisition.deliveryTypeCode in [DeliveryTypeCode.PICK_UP, DeliveryTypeCode.LOCAL_DELIVERY, DeliveryTypeCode.WILL_CALL, DeliveryTypeCode.SHIP_TO, DeliveryTypeCode.DEFAULT]
                || requisition.orderTypeCode in [OrderTypeCode.SERVICE_ORDER, OrderTypeCode.TRANSFER_ORDER, OrderTypeCode.SALES_ORDER]) {
                log.info "Automatic issuing requisition ${requisition.requestNumber} (${requisition.deliveryTypeCode})"
                stockMovementService.createShipment(requisition)
            }
        }
    }
}
