package org.pih.warehouse.jobs

import org.codehaus.groovy.grails.commons.ConfigurationHolder as ConfigHolder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventorySnapshot
import org.pih.warehouse.product.Product
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.PersistJobDataAfterExecution
import org.quartz.Scheduler

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import util.LiquibaseUtil

class CalculateHistoricalQuantityJob {

    static dates = []
    static enabled = true
    def inventorySnapshotService

    // cron job needs to be triggered after the staging deployment
    static triggers = {
		cron name:'cronHistoricalTrigger', cronExpression: ConfigHolder.config.openboxes.jobs.calculateHistoricalQuantityJob.cronExpression
    }

    def execute(JobExecutionContext context) {

        if (LiquibaseUtil.isRunningMigrations()) {
            log.info "Postponing job execution until liquibase migrations are complete"
            return
        }

        enabled = ConfigHolder.config.openboxes.jobs.calculateHistoricalQuantityJob.enabled
        if (enabled) {
            log.info "Executing calculate historical quantity job at ${new Date()} with context ${context}"
            if (!dates) {
                // Filter down to the transaction dates within the last 18 months
                def daysToProcess = ConfigHolder.config.openboxes.jobs.calculateHistoricalQuantityJob.daysToProcess
                def startDate = new Date() - daysToProcess
                def transactionDates = inventorySnapshotService.getTransactionDates()
                transactionDates = transactionDates.findAll { it >= startDate }
                dates = transactionDates.reverse()
                log.info "Refreshing ${dates.size()} dates"

            } else {
                log.info "There are ${dates.size()} remaining to be processed"
            }

            def nextDate = dates.pop()
            // We need the next date that has not already been processed
            // FIXME This could get stuck if there's a date that generates 0 inventory snapshot records (but that should not happen)
            log.info "Triggering inventory snapshot for date ${nextDate}"
            //CalculateQuantityJob.triggerNow([date: nextDate, includeInventoryItemSnapshot: false])
            inventorySnapshotService.populateInventorySnapshots(nextDate)
        }
    }


}
