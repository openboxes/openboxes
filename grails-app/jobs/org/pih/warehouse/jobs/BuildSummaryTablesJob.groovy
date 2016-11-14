package org.pih.warehouse.jobs

import org.codehaus.groovy.grails.commons.ConfigurationHolder as ConfigHolder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

class BuildSummaryTablesJob {

    def mailService
    def dataService

    // cron job needs to be triggered after the staging deployment
    static triggers = {
        simple startDelay: 60000, repeatInterval: 300000 // run every five minutes
		//cron name:'cronTrigger', cronExpression: ConfigHolder.config.openboxes.jobs.calculateQuantityJob.cronExpression
    }

	def execute(context) {

        dataService.rebuildInventoryItemSummaryTable()
    }


}
