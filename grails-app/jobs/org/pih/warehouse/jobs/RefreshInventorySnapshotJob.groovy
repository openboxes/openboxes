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

import grails.core.GrailsApplication
import org.pih.warehouse.core.Location
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext

@DisallowConcurrentExecution
class RefreshInventorySnapshotJob {

    def concurrent = false
    GrailsApplication grailsApplication
    def inventorySnapshotService

    // Should never be triggered on a schedule - should only be triggered by persistence event listener
    static triggers = {}

    def execute(JobExecutionContext context) {

        Boolean enabled = grailsApplication.config.openboxes.jobs.refreshInventorySnapshotJob.enabled
        if (enabled) {
            log.info("Refresh inventory snapshots with data: " + context.mergedJobDataMap)

            boolean forceRefresh = context.mergedJobDataMap.getBoolean("forceRefresh")
            def startTime = System.currentTimeMillis()
            def startDate = context.mergedJobDataMap.get('startDate')
            def locationId = context.mergedJobDataMap.get('location')

            Location location = Location.get(locationId)

            if (forceRefresh) {
                inventorySnapshotService.deleteInventorySnapshots(location)
            }

            // Refresh inventory snapshot for tomorrow
            inventorySnapshotService.populateInventorySnapshots(location)

            log.info "Refreshed inventory snapshot table for location ${location?.name} and start date ${startDate}: ${(System.currentTimeMillis() - startTime)} ms"
        }
    }
}