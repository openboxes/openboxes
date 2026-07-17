/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.requisition.Requisition
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
