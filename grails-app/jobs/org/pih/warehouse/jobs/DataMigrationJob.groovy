package org.pih.warehouse.jobs

import org.codehaus.groovy.grails.commons.ConfigurationHolder as ConfigHolder
import util.LiquibaseUtil

class DataMigrationJob {

    def migrationService

    //static triggers = {
	//	cron cronExpression: ConfigHolder.config.openboxes.jobs.dataMigrationJob.cronExpression
    //}

	def execute(context) {
        if (LiquibaseUtil.isRunningMigrations()) {
            log.info "Postponing job execution until liquibase migrations are complete"
            return
        }

        if (ConfigHolder.config.openboxes.jobs.dataMigrationJob.enabled?:false) {
            log.info "Starting data migration job at ${new Date()}"
            def startTime = System.currentTimeMillis()
            migrationService.migrateInventoryTransactions()
            log.info "Finished data migration job in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }


}
