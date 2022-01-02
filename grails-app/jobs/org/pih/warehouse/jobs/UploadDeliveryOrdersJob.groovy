package org.pih.warehouse.jobs

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext
import util.LiquibaseUtil

import java.text.ParseException
import java.text.SimpleDateFormat

@DisallowConcurrentExecution
class UploadDeliveryOrdersJob {

    def tmsIntegrationService

    static triggers = {
        cron name: 'uploadDeliveryOrdersJobCronTrigger',
                cronExpression: ConfigurationHolder.config.openboxes.jobs.uploadDeliveryOrdersJob.cronExpression
    }

    def execute(JobExecutionContext context) {

        Boolean enabled = ConfigurationHolder.config.openboxes.jobs.uploadDeliveryOrdersJob.enabled
        if (!enabled) {
            return
        }

        if (LiquibaseUtil.isRunningMigrations()) {
            log.info "Postponing job execution until liquibase migrations are complete"
            return
        }

        Integer numberOfDaysInAdvance = ConfigurationHolder.config.openboxes.jobs.uploadDeliveryOrdersJob.numberOfDaysInAdvance?:7
        if (context.mergedJobDataMap.get('numberOfDaysInAdvance')) {
            numberOfDaysInAdvance = context.mergedJobDataMap.get('numberOfDaysInAdvance')
        }

        // Calculate default requested delivery date
        Date today = new Date()
        today.clearTime()
        Date requestedDeliveryDate = today + numberOfDaysInAdvance

        // Check if the requested delivery date is overriden
        def requestedDeliveryDateOverride = context.mergedJobDataMap.get('requestedDeliveryDate')
        if (requestedDeliveryDateOverride) {
            log.info "Overriding requested delivery date with " + requestedDeliveryDateOverride
            if (requestedDeliveryDateOverride instanceof String) {
                try {
                    requestedDeliveryDate =
                            new SimpleDateFormat("dd/MM//yyyy").parse()
                } catch (ParseException e) { /* use default date calculated above */ }
            }
            else if (requestedDeliveryDateOverride instanceof Date) {
                requestedDeliveryDate = requestedDeliveryDateOverride
            }
        }

        // Trigger upload for all orders with given requested delivery date
        tmsIntegrationService.uploadDeliveryOrders(requestedDeliveryDate)
    }


}
