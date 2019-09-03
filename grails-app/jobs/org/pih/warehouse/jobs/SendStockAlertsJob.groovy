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

import groovyx.gpars.GParsPool
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.quartz.JobExecutionContext

class SendStockAlertsJob {

    def concurrent = false
    def grailsApplication
    def locationService
    def notificationService

    static triggers = {}

    def execute(JobExecutionContext context) {

        log.info "Executing ${this.class} at ${new Date()}"
        Boolean enabled = Boolean.valueOf(grailsApplication.config.openboxes.jobs.sendStockAlertsJob.enabled)
        Integer daysUntilExpiry = grailsApplication.config.openboxes.jobs.sendStockAlertsJob.daysUntilExpiry?:60

        if (enabled) {
            def startTime = System.currentTimeMillis()
            log.info("Send stock alerts: " + context.mergedJobDataMap)
            GParsPool.withPool {
                def depotLocations = locationService.getDepots()
                depotLocations.eachParallel { Location location ->
                    if (location.active && location.supports(ActivityCode.ENABLE_NOTIFICATIONS)) {
                        notificationService.sendExpiryAlertsByLocation(location, daysUntilExpiry)
                    }
                    else {
                        log.warn "Notifications disabled for ${location.name}"
                    }
                }
            }
            log.info "Sent stock alerts ${(System.currentTimeMillis() - startTime)} ms"
        }
    }
}