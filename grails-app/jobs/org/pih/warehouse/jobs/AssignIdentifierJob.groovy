package org.pih.warehouse.jobs

import util.LiquibaseUtil

class AssignIdentifierJob {

    def identifierService

    static triggers = {
        simple startDelay: 60000l, repeatInterval: 300000l   // run every 5 minutes
	}

	def execute() {

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
