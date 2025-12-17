package org.pih.warehouse.api

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.pih.warehouse.inventory.StockMovementService

@Transactional
class OutboundOrdersApiController {

    StockMovementService stockMovementService

    def read() {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        if (!stockMovement) {
            throw new IllegalArgumentException("No outbound order found for id ${params.id}")
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
