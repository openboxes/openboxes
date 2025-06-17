/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.integration

import org.grails.web.json.JSONObject
import org.pih.warehouse.api.StockMovement

import org.pih.warehouse.inventory.StockMovementStatusCode
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus

class StateTransitionService {

    def stockMovementService

    def triggerStockMovementStatusUpdate(StockMovement stockMovement) {

        Requisition requisition = stockMovement?.requisition
        Picklist picklist = stockMovement?.requisition?.picklist

        // Picklist and/or requisition is not ready to be checked
        if (!picklist || !requisition) {
            return
        }

        // If requisition is in picking and is fully picked, then transition to PICKED status
        if (requisition.status == RequisitionStatus.PICKING) {
            if (!picklist?.picklistItems?.empty) {
                if (picklist.isFullyPicked) {
                    log.info "Stock movement ${stockMovement.identifier} has been picked"
                    stockMovementService.transitionRequisitionBasedStockMovement(stockMovement, [status: StockMovementStatusCode.PICKED] as JSONObject)
                }
            }
        }
    }
}

