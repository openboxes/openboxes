package org.pih.warehouse.jobs

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product
import org.quartz.JobExecutionContext
import util.LiquibaseUtil

class CalculateQuantityJob {

    def inventoryService
    def mailService

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
        def date = context.mergedJobDataMap.get('date')
        def product = Product.get(context.mergedJobDataMap.get('productId'))
        def location = Location.get(context.mergedJobDataMap.get('locationId'))
        def user = User.get(context.mergedJobDataMap.get('userId'))
        boolean includeAllDates = context.mergedJobDataMap.get('includeAllDates')?
                Boolean.parseBoolean(context.mergedJobDataMap.get('includeAllDates')):false
        boolean includeInventoryItemSnapshot = context.mergedJobDataMap.get('includeInventoryItemSnapshot')?
                Boolean.parseBoolean(context.mergedJobDataMap.get('includeInventoryItemSnapshot')):true

        log.info "includeAllDates: " + includeAllDates
        log.info "includeInventoryItemSnapshot: " + includeInventoryItemSnapshot

        if (!date) {
            log.info "Date is being set to midnight tonight (tomorrow)"
            date = new Date() + 1
            date.clearTime()
        }
        log.info "Executing calculate quantity job for date=${includeAllDates ? 'ALL' : date}, user=${user}, location=${location}, product=${product}, mergedJobDataMap=${context.mergedJobDataMap}"
        if (includeAllDates) {
            inventoryService.createOrUpdateInventorySnapshot()
            if (includeInventoryItemSnapshot) {
                // FIXME Method doesn't exist yet
                //inventoryService.createOrUpdateInventoryItemSnapshot()
            }
        }
        else {
            // Triggered by ?
            if (product && date && location) {
                log.info "Triggered job for product ${product} at ${location} on ${date}"
                inventoryService.createOrUpdateInventorySnapshot(date, location, product)
                if (includeInventoryItemSnapshot) {
                    inventoryService.createOrUpdateInventoryItemSnapshot(date, location, product)
                }
            }
            // Triggered by the inventory snapshot tab off the product page
            else if (product && location) {
                log.info "Triggered job for product ${product} at ${location} on ${date}"
                inventoryService.createOrUpdateInventorySnapshot(location, product)
                if (includeInventoryItemSnapshot) {
                    inventoryService.createOrUpdateInventoryItemSnapshot(location, product)
                }
            }
            // Triggered by the Inventory Snapshot page
            else if (date && location) {
                log.info "Triggered calculate quantity job for all products at ${location} on ${date}"
                inventoryService.createOrUpdateInventorySnapshot(date, location)
                if (includeInventoryItemSnapshot) {
                    inventoryService.createOrUpdateInventoryItemSnapshot(date, location)
                }
            }
            // Triggered by the CalculateQuantityJob
            else if (date) {
                log.info "Triggered calculate quantity job for all locations and products on ${date}"
                inventoryService.createOrUpdateInventorySnapshot(date)
                if (includeInventoryItemSnapshot) {
                    inventoryService.createOrUpdateInventoryItemSnapshot(date)
                }
            // FIXME Removed for some reason?
            //} else if (location) {
            //    println "Triggered calculate quantity job for all products at location ${location} over all dates"
            //    inventoryService.createOrUpdateInventorySnapshot(location)
            } else {
                log.info "Triggered calculate quantity job for all dates, locations, products"
                def transactionDates = inventoryService.getTransactionDates()
                transactionDates.each { transactionDate ->
                    log.info "Triggered calculate quantity job for all products at all locations on date ${date}"
                    inventoryService.createOrUpdateInventorySnapshot(transactionDate)
                    if (includeInventoryItemSnapshot) {
                        inventoryService.createOrUpdateInventoryItemSnapshot(transactionDate)
                    }
                }
            }
        }

        def elapsedTime = (System.currentTimeMillis() - startTime)
        if (user?.email) {
            String subject = "Calculate quantity job completed in ${elapsedTime} ms"
            String message = """Location: ${location}\nProduct: ${product}\nDate: ${date}\n"""
            try {
                mailService.sendMail(subject, message, user.email)
            } catch (Exception e) {
                log.error("Unable to send email " + e.message, e)
            }
        }

        log.info "Successfully completed job for location=${location?:"ALL"}, product=${product?:"ALL"}, ${date?:"ALL"}): " + elapsedTime + " ms"
        println "=".multiply(60)
    }


}
