package org.pih.warehouse.jobs

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.quartz.DisallowConcurrentExecution
import util.LiquibaseUtil

@DisallowConcurrentExecution
class AssignIdentifierJob {

    def identifierService

    static triggers = {
        cron name: 'assignIdentifierCronTrigger',
                cronExpression: CH.config.openboxes.jobs.assignIdentifierJob.cronExpression
    }

    def execute() {

        Boolean enabled = CH.config.openboxes.jobs.assignIdentifierJob.enabled
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
