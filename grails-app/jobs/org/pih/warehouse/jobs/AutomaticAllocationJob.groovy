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
import org.springframework.beans.factory.annotation.Value

class AutomaticAllocationJob {

    def allocationService
    def requisitionService
    def locationService

    def sessionRequired = false

    // Never run two allocation passes at once - allocation reads availability that a concurrent pass would be
    // mutating. Together with the staggered scheduling below this keeps auto-allocation effectively serialized
    // so two outbounds cannot allocate the same stock (OBLS-919).
    static concurrent = false

    // Delay between successive per-requisition allocation passes. Should be at least as long as it takes the
    // asynchronous product-availability refresh to settle after an allocation/issuance (i.e. >=
    // openboxes.jobs.refreshProductAvailabilityJob.delayInMilliseconds plus a buffer), so the next requisition
    // is evaluated against up-to-date availability. Defaults to 7000ms if unset.
    @Value('${openboxes.jobs.automaticAllocationJob.delayInMilliseconds:7000}')
    Long delayInMilliseconds

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

        // OBLS-919: rather than allocate every pending requisition in a single pass (where later requisitions
        // would be evaluated against availability the earlier ones have not yet refreshed), schedule a separate
        // per-requisition pass for each, staggered by delayInMilliseconds. concurrent=false serializes them and
        // the stagger gives each requisition's asynchronous product-availability refresh time to settle before
        // the next is evaluated - so a second outbound cannot allocate stock the first one already issued.
        List<Location> facilities =
                locationService.getLocationsSupportingActivities([ActivityCode.AUTOMATIC_ALLOCATION_ENABLED])
        log.info "Scheduling automatic allocation for all pending requisitions... "
        facilities.each { Location facility ->
            requisitionService.getRequisitionsPendingAutoAllocation(facility)
                    .eachWithIndex { Requisition requisition, int index ->
                        scheduleAllocation(requisition.id, index)
                    }
        }
    }

    /**
     * Schedule a delayed, single-requisition allocation pass. The delay is staggered by position so that
     * pending requisitions are processed one at a time, spaced by delayInMilliseconds.
     */
    private void scheduleAllocation(String requisitionId, int position) {
        Long delay = (delayInMilliseconds ?: 0L) * position
        Date runAt = new Date(System.currentTimeMillis() + delay)
        AutomaticAllocationJob.schedule(runAt, [requisitionId: requisitionId])
    }
}
