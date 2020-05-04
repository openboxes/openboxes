package org.pih.warehouse.jobs

import grails.util.Holders
import org.quartz.DisallowConcurrentExecution
import util.LiquibaseUtil

@DisallowConcurrentExecution
class AssignIdentifierJob {

    def identifierService

    static triggers = {
        cron name: 'assignIdentifierCronTrigger',
                cronExpression: Holders.getConfig().getProperty("openboxes.jobs.assignIdentifierJob.cronExpression")
    }

    def execute() {

        Boolean enabled = Holders.getConfig().getProperty("openboxes.jobs.assignIdentifierJob.enabled")
        if (!enabled) {
            return
        }

        if (LiquibaseUtil.isRunningMigrations()) {
            log.info "Postponing job execution until liquibase migrations are complete"
            return
        }

        identifierService.assignProductIdentifiers()
        identifierService.assignShipmentIdentifiers()
        identifierService.assignReceiptIdentifiers()
        identifierService.assignOrderIdentifiers()
        identifierService.assignRequisitionIdentifiers()
        identifierService.assignTransactionIdentifiers()
    }


}
