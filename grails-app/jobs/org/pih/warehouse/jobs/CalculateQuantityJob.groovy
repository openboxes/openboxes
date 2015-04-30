package org.pih.warehouse.jobs

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventorySnapshot
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ShipmentItem

class CalculateQuantityJob {


    def inventoryService

    static triggers = {
        //simple startDelay: 30000, repeatInterval: 1000l * 60 * 10 * 1 //* 60 * 24;  // every 24 hours
		//cron name:'cronTrigger', startDelay:10000, cronExpression: '0/6 * 15 * * ?'
	}


	
	
	def execute(context) {

        def startTime = System.currentTimeMillis()
        def date = context.mergedJobDataMap.get('date')
        def product = Product.get(context.mergedJobDataMap.get('productId'))
        def location = Location.get(context.mergedJobDataMap.get('locationId'))

        println "Execute calculate quantity job " + context.mergedJobDataMap
        println "Product: " + product
        println "Location: " + location
        println "Date: " + date

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
        println "Finished calculate quantity on hand job for triple (${location}, ${product}, ${date}): " + (System.currentTimeMillis() - startTime) + " ms"
        println "=".multiply(60)
    }


}
