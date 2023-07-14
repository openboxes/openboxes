package org.pih.warehouse.jobs

import org.pih.warehouse.product.Product

class AssignIdentifierJob {

    def identifierService

    def sessionRequired = false

    static concurrent = false

    static triggers = {
        cron name: JobUtils.getCronName(AssignIdentifierJob),
            cronExpression: JobUtils.getCronExpression(AssignIdentifierJob)
    }

    void execute() {
        if (!JobUtils.shouldExecute(AssignIdentifierJob)) {
            return
        }

        Product.withNewSession {
            identifierService.assignProductIdentifiers()
            identifierService.assignShipmentIdentifiers()
            identifierService.assignReceiptIdentifiers()
            identifierService.assignOrderIdentifiers()
            identifierService.assignRequisitionIdentifiers()
            identifierService.assignTransactionIdentifiers()
        }
    }
}
