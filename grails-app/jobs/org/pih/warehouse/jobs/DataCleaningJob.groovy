package org.pih.warehouse.jobs

import org.codehaus.groovy.grails.commons.ConfigurationHolder as ConfigHolder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product
import util.LiquibaseUtil

class DataCleaningJob {

    def shipmentService

    // cron job needs to be triggered after the staging deployment
    static triggers = {
		cron cronExpression: ConfigHolder.config.openboxes.jobs.dataCleaningJob.cronExpression
    }

	def execute(context) {

        if (LiquibaseUtil.isRunningMigrations()) {
            log.info "Postponing job execution until liquibase migrations are complete"
            return
        }

        log.debug "Starting data cleaning job at ${new Date()}"
        def startTime = System.currentTimeMillis()
        shipmentService.bulkUpdateShipments()
        log.debug "Finished data cleaning job in " + (System.currentTimeMillis() - startTime) + " ms"
    }


}
