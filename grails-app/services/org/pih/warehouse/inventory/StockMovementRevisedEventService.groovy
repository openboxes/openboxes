/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.core.RoleType
import org.springframework.context.ApplicationListener

class StockMovementRevisedEventService implements ApplicationListener<StockMovementRevisedEvent> {

    boolean transactional = true
    def notificationService
    def tmsIntegrationService

    void onApplicationEvent(StockMovementRevisedEvent event) {
        log.info "Application event ${event.source} has been published"

        // Send notification for revised updates on outbound stock movements
        StockMovement stockMovement = event.source
        if (stockMovement?.requisition && stockMovement?.origin?.managedLocally) {
            notificationService.sendRequisitionRevisedNotifications(stockMovement?.requisition,
                    stockMovement?.origin, [RoleType.ROLE_SHIPMENT_NOTIFICATION])
        }
    }
}
