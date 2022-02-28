package org.pih.warehouse.inventory

import org.pih.warehouse.api.StockMovement
import org.springframework.context.ApplicationEvent

class StockMovementStatusEvent extends ApplicationEvent {
    StockMovementStatusCode stockMovementStatusCode
    Boolean rollback = Boolean.FALSE

    StockMovementStatusEvent(StockMovement stockMovement, StockMovementStatusCode stockMovementStatusCode, Boolean rollback) {
        super(stockMovement)
        this.stockMovementStatusCode = stockMovementStatusCode
        this.rollback = rollback
    }

}
