package org.pih.warehouse.jobs

class AssignIdentifierJob {

    def identifierService

    static concurrent = false

    static triggers = {
        cron name: JobUtils.getCronName(AssignIdentifierJob),
            cronExpression: JobUtils.getCronExpression(AssignIdentifierJob)
    }

    void execute() {
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
