package org.pih.warehouse.jobs

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.quartz.DisallowConcurrentExecution
import util.LiquibaseUtil

@DisallowConcurrentExecution
class DataCleaningJob {

    def shipmentService

    // cron job needs to be triggered after the staging deployment
    static triggers = {
        cron name: 'dataCleaningCronTrigger',
                cronExpression: CH.config.openboxes.jobs.dataCleaningJob.cronExpression
    }

    def execute(context) {

        Boolean enabled = CH.config.openboxes.jobs.dataCleaningJob.enabled
        if (!enabled) {
            return
        }

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
