package org.pih.warehouse.jobs

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext

@DisallowConcurrentExecution
class RefreshTransactionFactJob {

    def grailsApplication
    def reportService

    // Should never be triggered on a schedule - should only be triggered by persistence event listener
    static triggers = {
        cron name: 'refreshTransactionFactCronTrigger',
                cronExpression: ConfigurationHolder.config.openboxes.jobs.refreshTransactionFactJob.cronExpression
    }

    def execute(JobExecutionContext context) {

        Boolean enabled = grailsApplication.config.openboxes.jobs.refreshTransactionFactJob.enabled

        if (enabled) {
            log.info("Refresh transaction fact and dimensions: " + context.mergedJobDataMap)

            def startTime = System.currentTimeMillis()
            reportService.buildDimensions()
            reportService.buildFacts()

            log.info "Refreshed transaction fact and dimensions: ${(System.currentTimeMillis() - startTime)} ms"
        }
    }
}