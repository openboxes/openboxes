package org.pih.warehouse.api

import grails.converters.JSON
import org.pih.warehouse.fulfillment.FulfillmentService
import org.pih.warehouse.outbound.ImportPackingListCommand
import org.pih.warehouse.outbound.ImportPackingListItem

class FulfillmentApiController {

    FulfillmentService fulfillmentService

    def validate(ImportPackingListCommand command) {
        if (command.hasErrors()) {
            response.status = 400
        }
        List<Map> tableData = command.packingList.collect { ImportPackingListItem item -> item.toTableJson() }
        render([errors: command.errorMessages, data: tableData] as JSON)
    }

    def save(ImportPackingListCommand command) {
        if (command.hasErrors()) {
            response.status = 400
            List<Map> tableData = command.packingList.collect { ImportPackingListItem item -> item.toTableJson() }
            render([errors: command.errorMessages, data: tableData] as JSON)
            return
        }
        StockMovement stockMovement = fulfillmentService.save(command)

        render([data: stockMovement] as JSON)
    }
}
