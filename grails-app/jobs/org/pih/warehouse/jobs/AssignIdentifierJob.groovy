package org.pih.warehouse.jobs

import util.LiquibaseUtil

class AssignIdentifierJob {

    def identifierService

    static triggers = {
        simple startDelay: 10000l, repeatInterval: 10000l   // run every 5 minutes
	}

	def execute() {

        if (LiquibaseUtil.isRunningMigrations()) {
            log.info "Postponing job execution until liquibase migrations are complete"
            return
        }

        identifierService.assignProductIdentifiers()
        identifierService.assignShipmentIdentifiers()
        identifierService.assignOrderIdentifiers()
        identifierService.assignRequisitionIdentifiers()
        identifierService.assignTransactionIdentifiers()
	}


}
