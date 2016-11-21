package org.pih.warehouse.jobs

import grails.plugin.mail.MailService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.InventorySnapshot
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ShipmentItem
import org.codehaus.groovy.grails.commons.ConfigurationHolder as ConfigHolder

class CalculateQuantityJob {

    def mailService
    def inventoryService
    def grailsApplication

    // cron job needs to be triggered after the staging deployment
    static triggers = {
		cron name:'calculateQuantityTrigger', cronExpression: ConfigHolder.config.openboxes.jobs.calculateQuantityJob.cronExpression
    }

	def execute(context) {

        // Allow admins to disable the job
        if(!grailsApplication.config.openboxes.jobs.calculateQuantityJob.enabled) {
            return
        }

        def startTime = System.currentTimeMillis()
        def date = context.mergedJobDataMap.get('date')
        def product = Product.get(context.mergedJobDataMap.get('productId'))
        def location = Location.get(context.mergedJobDataMap.get('locationId'))
        def user = User.get(context.mergedJobDataMap.get('userId'))

        // System uses yesterday by default if a date is not provided
        if (!date) {
            log.info "Date is being set to yesterday"
            date = new Date()
            date.clearTime()
        }

        log.info "Executing calculate quantity job for date=${date}, user=${user}, location=${location}, product=${product}, mergedJobDataMap=${context.mergedJobDataMap}"


        if (product && date && location) {
            println "Triggered calculate quantity job for product ${product} at ${location} on ${date}"
            inventoryService.createOrUpdateInventorySnapshot(date, location, product)
        }
        else if (product && location) {
            println "Triggered calculate quantity job for product ${product} at ${location} on ${date}"
            inventoryService.createOrUpdateInventorySnapshot(location, product)
        }
        else if (date && location) {
            println "Triggered calculate quantity job for all products at ${location} on ${date}"
            inventoryService.createOrUpdateInventorySnapshot(date, location)
            inventoryService.createOrUpdateInventoryItemSnapshot(date, location)
        }
        else if (date) {
            println "Triggered calculate quantity job for all locations and products on ${date}"
            inventoryService.createOrUpdateInventorySnapshot(date)
            inventoryService.createOrUpdateInventoryItemSnapshot(date)
        }
        else if (location) {
            println "Triggered calculate quantity job for all products at location ${location} over all dates"
            inventoryService.createOrUpdateInventorySnapshot(location)
        }
        else {
            println "Triggered calculate quantity job for all dates, locations, products"
            def transactionDates = inventoryService.getTransactionDates()
            transactionDates.each { transactionDate ->
                log.info "Triggered calculate quantity job for all products at all locations on date ${date}"
                inventoryService.createOrUpdateInventorySnapshot(transactionDate)
                inventoryService.createOrUpdateInventoryItemSnapshot(transactionDate)
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

        println "Finished calculate quantity job for triple (${location}, ${product}, ${date}): " + elapsedTime + " ms"
        println "=".multiply(60)
    }


}
