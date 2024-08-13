package org.pih.warehouse.api

import grails.converters.JSON
import org.pih.warehouse.CommandUtils
import org.pih.warehouse.fulfillment.FulfillmentService
import org.pih.warehouse.outbound.ImportPackingListCommand
import org.pih.warehouse.outbound.ImportPackingListErrors
import org.pih.warehouse.outbound.ImportPackingListItem

class FulfillmentApiController {

    FulfillmentService fulfillmentService

    def validate(ImportPackingListCommand command) {
        ImportPackingListErrors errors = new ImportPackingListErrors()
        if (command.hasErrors()) {
            response.status = 400
            buildErrors(command, errors)
        }
        render([errors: errors] as JSON)
    }

    def createOutbound(ImportPackingListCommand command) {
        ImportPackingListErrors errors = new ImportPackingListErrors()
        if (command.hasErrors()) {
            response.status = 400
            buildErrors(command, errors)
            render([errors: errors] as JSON)
            return
        }
        StockMovement stockMovement = fulfillmentService.createOutbound(command)

        render([data: stockMovement] as JSON)
    }

    static void buildErrors(ImportPackingListCommand command, ImportPackingListErrors errors) {
        // Build errors instance for each sub-step (fulfillmentDetails, sendingOptions, list of items)
        // Errors will be grouped by the sub-step and by fields, e.g.: { fulfillmentDetails: { origin: ["Cannot be null"] } }
        errors.fulfillmentDetails = CommandUtils.buildErrorsGroupedByField(command.fulfillmentDetails.errors)
        errors.sendingOptions = CommandUtils.buildErrorsGroupedByField(command.sendingOptions.errors)

        command.packingList.each { ImportPackingListItem item ->
            if (item.hasErrors()) {
                // Build errors for each row separately. Row id is the key.
                // e.g. { 2: { binLocation: ["Bin location cannot be null"] } }
                errors.packingList[item.rowId] = CommandUtils.buildErrorsGroupedByField(item.errors)
            }
        }
    }
}
