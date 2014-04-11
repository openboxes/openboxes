package org.pih.warehouse.jobs

class AssignIdentifierJob {

    def identifierService

    static triggers = {
        simple startDelay: 60000, repeatInterval: 300000l   // run every 5 minutes
	}


	
	
	def execute() {
        identifierService.assignProductIdentifiers()
        identifierService.assignShipmentIdentifiers()
        identifierService.assignOrderIdentifiers()
        identifierService.assignRequisitionIdentifiers()
        identifierService.assignTransactionIdentifiers()
	}


}
