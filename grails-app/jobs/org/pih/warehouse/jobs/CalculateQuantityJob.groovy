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
		//cron name:'cronTrigger', startDelay:300000, cronExpression: '0 0 * * * *' // cronExpression: at midnight
        //cron name:'cronTrigger', startDelay:300000, cronExpression: '0/6 * 15 * * ?' // cronExpression:
	}

	def execute(context) {

        def startTime = System.currentTimeMillis()
        def date = context.mergedJobDataMap.get('date')
        def product = Product.get(context.mergedJobDataMap.get('productId'))
        def location = Location.get(context.mergedJobDataMap.get('locationId'))
        def user = User.get(context.mergedJobDataMap.get('userId'))

        log.info "Execute calculate quantity job " + context.mergedJobDataMap
        log.info "User: " + user
        log.info "Product: " + product
        log.info "Location: " + location
        log.info "Date: " + date

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
        }
        else if (date) {
            println "Triggered calculate quantity job for all locations, products on ${date}"
            inventoryService.createOrUpdateInventorySnapshot(date)
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
