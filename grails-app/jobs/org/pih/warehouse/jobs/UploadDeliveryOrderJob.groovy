package org.pih.warehouse.jobs

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.quartz.DisallowConcurrentExecution
import util.LiquibaseUtil

@DisallowConcurrentExecution
class UploadDeliveryOrdersJob {

    def tmsIntegrationService

    static triggers = {
        cron name: 'uploadDeliveryOrdersJobCronTrigger',
                cronExpression: ConfigurationHolder.config.openboxes.jobs.uploadDeliveryOrdersJob.cronExpression
    }

    def execute() {

        Boolean enabled = ConfigurationHolder.config.openboxes.jobs.uploadDeliveryOrdersJob.enabled
        if (!enabled) {
            return
        }

        if (LiquibaseUtil.isRunningMigrations()) {
            log.info "Postponing job execution until liquibase migrations are complete"
            return
        }

        Integer numberOfDaysInAdvance = ConfigurationHolder.config.openboxes.jobs.uploadDeliveryOrdersJob.numberOfDaysInAdvance?:7
        Date today = new Date()
        today.clearTime()
        Date requestedDeliveryDate = today + numberOfDaysInAdvance
        tmsIntegrationService.uploadDeliveryOrders(requestedDeliveryDate)
    }


}
