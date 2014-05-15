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
        //def date = context.mergedJobDataMap.get('date')?:new Date()


        println "not started yet"
        def transactionDates = inventoryService.getTransactionDates()
        transactionDates.each { date ->

            log.info "Executing CalculateQuantityJob for date ${date}"
            log.info "Starting inventory snapshot process " + new Date()

            inventoryService.createOrUpdateInventorySnapshot(date)

            println "Finished inventory snapshot process " + new Date()
            println "Finished calculate quantity job ${date} in " + (System.currentTimeMillis() - startTime) + " ms"
            println "=".multiply(60)
        }

    }


}
