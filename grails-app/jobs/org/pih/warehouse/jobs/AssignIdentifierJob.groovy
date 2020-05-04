package org.pih.warehouse.jobs

import org.quartz.DisallowConcurrentExecution

@DisallowConcurrentExecution
class AssignIdentifierJob {

    def identifierService

    def concurrent = false  // make `static` in Grails 3
    static triggers = {
        cron name: JobUtils.getCronName(AssignIdentifierJob),
            cronExpression: JobUtils.getCronExpression(AssignIdentifierJob)
    }

    def execute() {
        if (!JobUtils.shouldExecute(AssignIdentifierJob)) {
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
