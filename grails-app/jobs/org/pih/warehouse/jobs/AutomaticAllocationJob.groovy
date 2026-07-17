package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.allocation.AllocationMode
import org.pih.warehouse.allocation.AllocationStrategy
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus
import org.quartz.JobExecutionContext

class AutomaticAllocationJob {

    def allocationService
    def requisitionService
    def locationService

    def sessionRequired = false

    static concurrent = false

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
            allocationService.allocateRequisition(requisitionId)
            return
        }

        if (Holders.config.openboxes.jobs.automaticAllocationJob.bulkAutoAllocation) {
            List<Location> facilities =
                    locationService.getLocationsSupportingActivities([ActivityCode.AUTOMATIC_ALLOCATION_ENABLED])
            log.info "Running automatic allocation job for all pending requisitions... "
            facilities.each { Location facility ->
                requisitionService.getRequisitionsPendingAutoAllocation(facility).each { Requisition requisition ->
                    allocationService.allocateRequisition(requisition.id)
                }
            }
        }
    }
}
