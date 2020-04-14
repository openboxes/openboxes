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

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.pih.warehouse.core.Location
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

@DisallowConcurrentExecution
class RefreshInventorySnapshotJob {

    def concurrent = false
    def grailsApplication
    def inventorySnapshotService

    // Should never be triggered on a schedule - should only be triggered by persistence event listener
    static triggers = {}

    def execute(JobExecutionContext context) {

        Boolean enabled = ConfigurationHolder.config.openboxes.jobs.refreshInventorySnapshotJob.enabled
        log.info("Refresh inventory snapshots with data (enabled=${enabled}): " + context.mergedJobDataMap)
        if (enabled) {

            def jobDataMap = context.jobDetail.jobDataMap
            Integer retryCount = jobDataMap.containsKey("retryCount") ? jobDataMap.getIntFromString("retryCount"):0
            Integer maxRetryAttempts = ConfigurationHolder.config.openboxes.jobs.refreshInventorySnapshotJob.maxRetryAttempts?:3
            Boolean retryOnError = ConfigurationHolder.config.openboxes.jobs.refreshInventorySnapshotJob.retryOnError?:false
            if (retryOnError) {
                log.info "Retry count: ${retryCount} / ${maxRetryAttempts}"
                if (retryOnError && retryCount >= maxRetryAttempts) {
                    JobExecutionException e = new JobExecutionException("Retries exceeded");
                    e.setUnscheduleAllTriggers(true);
                    throw e;
                }
            }

            boolean forceRefresh = context.mergedJobDataMap.getBoolean("forceRefresh")
            def startTime = System.currentTimeMillis()
            def userId = context.mergedJobDataMap.get('user')
            def startDate = context.mergedJobDataMap.get('startDate')
            def locationId = context.mergedJobDataMap.get('location')
            Location location = Location.get(locationId)

            try {
                log.info ("Refresh inventory snapshot " + retryCount + " out of " + maxRetryAttempts)
                if (forceRefresh) {
                    inventorySnapshotService.deleteInventorySnapshots(location)
                }

                // Refresh inventory snapshot for tomorrow
                inventorySnapshotService.populateInventorySnapshots(location)

                // Reset retry count to 0
                context.jobDetail.jobDataMap.putAsString("retryCount", 0);

            } catch (Exception e) {
                log.error("Exception occurred while executing refresh location=${locationId}, user=${userId}: " + e.message, e)
                if (retryOnError) {
                    context.jobDetail.jobDataMap.putAsString("retryCount", retryCount+1);
                    JobExecutionException jee = new JobExecutionException(e)
                    jee.setRefireImmediately(true)
                    throw jee;
                }
            }
            log.info "Refreshed inventory snapshot table for location ${location?.name} and start date ${startDate}: ${(System.currentTimeMillis() - startTime)} ms"
        }
    }
}
