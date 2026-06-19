package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus
import org.quartz.JobExecutionContext

class AutoissuanceJob {

    StockMovementService stockMovementService
    AuthService authService

    def sessionRequired = false

    static triggers = {
        cron name: JobUtils.getCronName(AutoissuanceJob),
                cronExpression: JobUtils.getCronExpression(AutoissuanceJob)
    }

    def execute(JobExecutionContext context) {
        if (!Holders.config.openboxes.jobs.autoissuanceJob.enabled) {
            log.info "Autoissuance job is disabled"
            return
        }

        // Only auto-issue outbound requisitions, i.e. those fulfilled from an internal inventory
        // location. Inbound requisitions are fulfilled from a SUPPLIER location (purchasing/receiving)
        // and must not be issued here.
        List<Requisition> requisitions = Requisition.createCriteria().list {
            eq("isTemplate", Boolean.FALSE)
            eq("status", RequisitionStatus.STAGED)
            origin {
                locationType {
                    ne("locationTypeCode", LocationTypeCode.SUPPLIER)
                }
            }
        }

        log.info "Found ${requisitions.size()} outbound STAGED requisitions to issue"

        requisitions.each { Requisition requisition ->
            authService.withSystemUser {
                switch (requisition.deliveryTypeCode) {
                    case DeliveryTypeCode.LOCAL_DELIVERY:
                    case DeliveryTypeCode.SHIP_TO:
                    case DeliveryTypeCode.STOCK_TRANSFER_IBT:
                        try {
                            log.info "Issuing requisition ${requisition.requestNumber} (${requisition.deliveryTypeCode})"
                            stockMovementService.issueRequisition(requisition)
                        } catch (Exception e) {
                            // Don't let one failing requisition stop the rest of the batch.
                            log.error("Error issuing requisition ${requisition.requestNumber}", e)
                        }
                        break
                    case DeliveryTypeCode.PICK_UP:
                    case DeliveryTypeCode.WILL_CALL:
                    case DeliveryTypeCode.SERVICE:
                    case DeliveryTypeCode.DEFAULT:
                        // no handling for now
                        break
                }
            }
        }
    }
}
