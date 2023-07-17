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
import org.pih.warehouse.core.RoleType
import org.quartz.JobExecutionContext

class SendStockAlertsJob {

    def gparsService
    def locationService
    def notificationService

    static concurrent = false

    def sessionRequired = false

    static triggers = {
        cron name: JobUtils.getCronName(SendStockAlertsJob),
            cronExpression: JobUtils.getCronExpression(SendStockAlertsJob)
    }

    void execute(JobExecutionContext context) {

        Boolean skipOnEmpty = Boolean.valueOf(Holders.config.openboxes.jobs.sendStockAlertsJob.skipOnEmpty)
        Integer daysUntilExpiry = Integer.valueOf(Holders.config.openboxes.jobs.sendStockAlertsJob.daysUntilExpiry ?: 60)

        if (JobUtils.shouldExecute(SendStockAlertsJob)) {
            def startTime = System.currentTimeMillis()
            log.info("Send stock alerts: " + context.mergedJobDataMap)
            gparsService.withPool('SendStockAlerts') {
                def depotLocations = locationService.getDepots()
                depotLocations.eachParallel { Location location ->
                    if (location.active && location.supports(ActivityCode.ENABLE_NOTIFICATIONS)) {
                        notificationService.sendExpiryAlerts(location, daysUntilExpiry,
                                [RoleType.ROLE_ITEM_ALL_NOTIFICATION, RoleType.ROLE_ITEM_EXPIRY_NOTIFICATION], skipOnEmpty)
                        notificationService.sendStockAlerts(location, "out_of_stock",
                                [RoleType.ROLE_ITEM_ALL_NOTIFICATION, RoleType.ROLE_ITEM_OUT_OF_STOCK_NOTIFICATION], skipOnEmpty)
                        notificationService.sendStockAlerts(location, "low_stock",
                                [RoleType.ROLE_ITEM_ALL_NOTIFICATION, RoleType.ROLE_ITEM_LOW_STOCK_NOTIFICATION], skipOnEmpty)
                        notificationService.sendStockAlerts(location, "reorder_stock",
                                [RoleType.ROLE_ITEM_ALL_NOTIFICATION, RoleType.ROLE_ITEM_REORDER_NOTIFICATION], skipOnEmpty)
                        notificationService.sendStockAlerts(location, "over_stock",
                                [RoleType.ROLE_ITEM_ALL_NOTIFICATION, RoleType.ROLE_ITEM_OVERSTOCK_NOTIFICATION], skipOnEmpty)
                    } else {
                        log.warn "Notifications disabled for ${location.name}"
                    }
                }
            }
            log.info "Sent stock alerts ${(System.currentTimeMillis() - startTime)} ms"
        }
    }
}
