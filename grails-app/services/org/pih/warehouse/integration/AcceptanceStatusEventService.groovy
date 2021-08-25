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

import org.pih.warehouse.api.StockMovement
import org.springframework.context.ApplicationListener

class AcceptanceStatusEventService implements ApplicationListener<AcceptanceStatusEvent>  {

    boolean transactional = true

    def stockMovementService
    def notificationService

    void onApplicationEvent(AcceptanceStatusEvent acceptanceStatusEvent) {
        log.info "Acceptance status " + acceptanceStatusEvent.acceptanceStatus.tripDetails.toString()
        String trackingNumber = acceptanceStatusEvent.acceptanceStatus.tripDetails.tripId
        StockMovement stockMovement = stockMovementService.findByTrackingNumber(trackingNumber)
        if (!stockMovement) {
            throw new Exception("Unable to locate stock movement by tracking number ${trackingNumber}")
        }
        stockMovementService.acceptStockMovement(stockMovement)
    }
}
