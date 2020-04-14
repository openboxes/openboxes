package org.pih.warehouse.jobs

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext
import util.LiquibaseUtil

@DisallowConcurrentExecution
class CalculateQuantityJob {

    def concurrent = false
    def inventorySnapshotService

    // cron job needs to be triggered after the staging deployment
    static triggers = {
        cron name: 'calculateQuantityCronTrigger',
                cronExpression: CH.config.openboxes.jobs.calculateQuantityJob.cronExpression
    }

    def execute(JobExecutionContext context) {

        Boolean enabled = CH.config.openboxes.jobs.calculateQuantityJob.enabled
        if (!enabled) {
            return
        }

        if (LiquibaseUtil.isRunningMigrations()) {
            log.info "Postponing job execution until liquibase migrations are complete"
            return
        }

        def startTime = System.currentTimeMillis()
        Date date = context.mergedJobDataMap.get('date')
        def product = Product.get(context.mergedJobDataMap.get('productId'))
        def location = Location.get(context.mergedJobDataMap.get('locationId'))
        def user = User.get(context.mergedJobDataMap.get('userId'))
        boolean forceRefresh = context.mergedJobDataMap.getBoolean("forceRefresh") ?: false
        boolean includeAllDates = context.mergedJobDataMap.get('includeAllDates') ?
                Boolean.valueOf(context.mergedJobDataMap.get('includeAllDates')) : false

        log.info "includeAllDates: " + includeAllDates

        if (!date) {
            date = new Date() + 1
        }

        // Make sure to set time to midnight
        date.clearTime()

        log.info "Executing calculate quantity job for date=${includeAllDates ? 'ALL' : date}, user=${user?.id}, location=${location?.id}, product=${product?.id}, mergedJobDataMap=${context.mergedJobDataMap}"
        if (includeAllDates) {
            inventorySnapshotService.populateInventorySnapshots(location, product)
        } else {
            // Triggered by ?
            if (product && location) {
                log.info "Triggered for product ${product?.id} at ${location?.id} on ${date}"
                if (forceRefresh) {
                    inventorySnapshotService.deleteInventorySnapshots(date, location, product)
                }
                inventorySnapshotService.populateInventorySnapshots(date, location, product)
            }
            // Triggered by the Inventory Snapshot page
            else if (location) {
                log.info "Triggered for all products at ${location?.id} on ${date}"
                if (forceRefresh) {
                    inventorySnapshotService.deleteInventorySnapshots(date, location)
                }
                inventorySnapshotService.populateInventorySnapshots(date, location)
            }
            // Triggered by the CalculateQuantityJob
            else {
                log.info "Triggered for all locations and products on ${date}"
                if (forceRefresh) {
                    inventorySnapshotService.deleteInventorySnapshots(date)
                }
                boolean enableOptimization = CH.config.openboxes.jobs.calculateQuantityJob.enableOptimization
                inventorySnapshotService.populateInventorySnapshots(date, enableOptimization)
            }
        }

        def elapsedTime = (System.currentTimeMillis() - startTime)
        log.info "Successfully completed job for location=${location ?: "ALL"}, product=${product?.id ?: "ALL"}, ${date ?: "ALL"}): " + elapsedTime + " ms"
        log.info "=".multiply(100)
    }


}
