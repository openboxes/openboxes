package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.allocation.AllocationMode
import org.pih.warehouse.allocation.AllocationStrategy
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus
import org.quartz.JobExecutionContext

class AutomaticAllocationJob {

    def allocationService
    def stockMovementService

    def sessionRequired = false

    static triggers = {
        cron name: JobUtils.getCronName(AutomaticAllocationJob),
        cronExpression: JobUtils.getCronExpression(AutomaticAllocationJob)
    }

    def execute(JobExecutionContext context) {
        if (!Holders.config.openboxes.jobs.automaticAllocationJob.enabled) {
            log.info"Automatic allocation job is disabled"
            return
        }

        String requisitionId = context.mergedJobDataMap.get('requisitionId')
        if (requisitionId) {
            try {
                Requisition requisition = Requisition.get(requisitionId)
                if (!requisition) {
                    log.warn("Requisition ${requisitionId} not found, skipping")
                    return
                }
                log.info("Automatic allocation for requisition ${requisitionId} ...")
                allocationService.allocate(requisition, AllocationMode.AUTO, [AllocationStrategy.WAREHOUSE_FIRST])
                stockMovementService.updateRequisitionStatus(requisitionId, RequisitionStatus.PICKING)
            } catch (Exception e) {
                log.error("Error processing requisition ${requisitionId}", e)
            }
        }
    }

}
