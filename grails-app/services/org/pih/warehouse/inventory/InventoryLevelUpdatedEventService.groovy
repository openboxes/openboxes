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

import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

class InventoryLevelUpdatedEventService {

    def productAvailabilityService
    def inventorySnapshotService

    // AFTER_COMMIT: refresh runs once the upsert transaction has committed, so the data is guaranteed visible and
    // the refresh jobs don't need to be scheduled with a delay
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void onInventoryLevelUpdated(InventoryLevelUpdatedEvent event) {
        productAvailabilityService.triggerRefreshProductAvailability(event.facilityId, [event.productId], event.forceRefresh)
        inventorySnapshotService.triggerRefreshInventorySnapshot(event.facilityId, [event.productId], event.forceRefresh)
    }
}
