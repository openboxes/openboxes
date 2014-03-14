package org.pih.warehouse.jobs

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventorySnapshot
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ShipmentItem

class CalculateQuantityJob {


    def inventoryService

    static triggers = {

        //simple startDelay: 10000, repeatInterval: 86400000l
        //simple startDelay: 30000, repeatInterval: 86400000l
		//simple name:'simpleTrigger', startDelay:10000, repeatInterval: 30000, repeatCount: 10
		//cron name:'cronTrigger', startDelay:10000, cronExpression: '0/6 * 15 * * ?'
		//custom name:'customTrigger', triggerClass:MyTriggerClass, myParam:myValue, myAnotherParam:myAnotherValue
	}


	
	
	def execute() {
        def date = new Date()
        log.info "Executing CalculateQuantityJob on ${date}"
		def startTime = System.currentTimeMillis()
        log.info "Starting inventory snapshot process " + new Date()
        inventoryService.createOrUpdateInventorySnapshot(date)
		println "Finished inventory snapshot process " + new Date()
	}


}
