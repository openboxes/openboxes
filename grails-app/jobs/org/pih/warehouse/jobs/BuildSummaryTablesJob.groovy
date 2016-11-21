package org.pih.warehouse.jobs

import org.codehaus.groovy.grails.commons.ConfigurationHolder as ConfigHolder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

class BuildSummaryTablesJob {

    def mailService
    def dataService
    def grailsApplication

    // cron job needs to be triggered after the staging deployment
    static triggers = {
		cron name:'buildSummaryTablesTrigger', cronExpression: ConfigHolder.config.openboxes.jobs.buildSummaryTablesJob.cronExpression
    }

	def execute(context) {
        boolean enabled = grailsApplication.config.openboxes.jobs.buildSummaryTablesJob.enabled
        boolean force = context.mergedJobDataMap.get('force')
        if(enabled || force) {
            dataService.rebuildInventoryItemSummaryTable()
        }
    }

}
