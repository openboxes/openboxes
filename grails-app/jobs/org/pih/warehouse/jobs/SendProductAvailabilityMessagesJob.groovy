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

import org.quartz.JobExecutionContext

class SendProductAvailabilityMessagesJob {

    def grailsApplication
    def locationService
    def notificationService
    def productAvailabilityService

    static triggers = { /* should only be triggered programmatically */ }

    def execute(JobExecutionContext context) {
        Boolean enabled = Boolean.valueOf(grailsApplication.config.openboxes.jobs.sendProductAvailabilityMessagesJob.enabled)
        if (enabled) {
            try {
                long startTime = System.currentTimeMillis()
                log.info("Sending product availability data: " + context.mergedJobDataMap)
                List productIds = context.mergedJobDataMap.get("productIds")
                String locationId = context.mergedJobDataMap.get("locationId")
                if (locationId && !productIds?.empty)
                    notificationService.sendProductAvailabilityMessages(locationId, productIds)
                log.info "Successfully published product availability messages ${(System.currentTimeMillis() - startTime)} ms"
            } catch (Exception e) {
                log.error("Unable to publish product availability message due to error: " + e.message, e)
            }
        }
    }

}
