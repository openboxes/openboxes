package org.pih.warehouse.jobs

import grails.plugin.mail.MailService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.InventorySnapshot
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ShipmentItem

class CalculateQuantityJob {

    def inventoryService
    def mailService

    static triggers = {
        //simple startDelay: 300000, repeatInterval: 1000l * 60 * 10 * 1 * 60 * 24;  // startDelay: 5 minutes, repeatInterval: every 24 hours
		cron name:'cronTrigger', cronExpression: '0 0 3 * * ?' // cronExpression: at 4am
        //cron name:'cronTrigger', startDelay:300000, cronExpression: '0/6 * 15 * * ?' // cronExpression:
	}

	def execute(context) {
        def startTime = System.currentTimeMillis()
        def date = context.mergedJobDataMap.get('date')
        def product = Product.get(context.mergedJobDataMap.get('productId'))
        def location = Location.get(context.mergedJobDataMap.get('locationId'))
        def user = User.get(context.mergedJobDataMap.get('userId'))

        // System uses yesterday by default if a date is not provided
        if (!date) {
            log.info "Date is being set to yesterday"
            date = new Date() - 1
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
