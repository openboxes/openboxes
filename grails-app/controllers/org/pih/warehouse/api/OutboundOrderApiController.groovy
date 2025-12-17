package org.pih.warehouse.api

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.requisition.RequisitionItem

@Transactional
class OutboundOrderApiController {

    StockMovementService stockMovementService

    def read() {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)

        if (!stockMovement) {
            throw new IllegalArgumentException("No outbound order found for id ${params.id}")
        }

        stockMovement.lineItems.each { StockMovementItem item ->
            item.availableItems = stockMovementService.getAvailableItems(stockMovement.origin, RequisitionItem.get(item.id), false)
        }

        render([data: stockMovement] as JSON)
    }

    def items() {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        if (!stockMovement) {
            throw new IllegalArgumentException("No outbound order found for id ${params.id}")
        }

        render([data: stockMovement.lineItems] as JSON)
    }
}
