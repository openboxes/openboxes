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

import groovy.time.TimeCategory
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext

@DisallowConcurrentExecution
class RefreshProductAvailabilityJob {

    def concurrent = false
    def grailsApplication
    def productAvailabilityService
    def notificationService

    // Should never be triggered on a schedule - should only be triggered by persistence event listener
    static triggers = {}

    def execute(JobExecutionContext context) {
        Boolean enabled = grailsApplication.config.openboxes.jobs.refreshProductAvailabilityJob.enabled
        if (enabled) {
            def startTime = System.currentTimeMillis()
            log.info("Refreshing product availability data: " + context.mergedJobDataMap)
            boolean forceRefresh = context.mergedJobDataMap.getBoolean("forceRefresh")?:false
            Object productIds = context.mergedJobDataMap.get("productIds")
            String locationId = context.mergedJobDataMap.get("locationId")

            productAvailabilityService.refreshProductsAvailability(locationId, productIds, forceRefresh)
            log.info "Finished refreshing product availability data in " + (System.currentTimeMillis() - startTime) + " ms"
            boolean sendNotification = context.mergedJobDataMap.getBoolean("sendNotification")?:false
            if (sendNotification && locationId && !productIds?.empty){
//                startTime = System.currentTimeMillis()
//                notificationService.sendProductAvailabilityMessages(locationId, productIds)
//                log.info "Successfully published product availability messages ${(System.currentTimeMillis() - startTime)} ms"
                use(TimeCategory) {
                    def delayStartInMilliseconds = grailsApplication.config.openboxes.jobs.sendProductAvailabilityMessagesJob.delayStartInMilliseconds?:0
                    Date runAt = new Date() + delayStartInMilliseconds.milliseconds
                    log.info "Scheduling trigger for SendProductAvailabilityMessagesJob to run at ${runAt}"
                    SendProductAvailabilityMessagesJob.schedule(runAt, [locationId:locationId, productIds:productIds])
                }
            }else{
                if(!sendNotification) {
                    log.info "Send Notification is disabled from RefreshProductAvailabilityJob"
                }
            }
        }
    }

}
