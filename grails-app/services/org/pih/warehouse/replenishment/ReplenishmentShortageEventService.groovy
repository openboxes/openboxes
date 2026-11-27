/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.replenishment

import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.event.TransactionalEventListener

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.WebhookPublisherService
import org.pih.warehouse.picklist.PicklistItem

/**
 * Sends a replenishment shortage as a pick.shortage webhook, once the short pick is committed: a short pick that is
 * rolled back is never announced, and a failure to send cannot undo it.
 */
class ReplenishmentShortageEventService {

    WebhookPublisherService webhookPublisherService

    @Async
    @TransactionalEventListener
    void onReplenishmentShortageEvent(ReplenishmentShortageEvent event) {
        PicklistItem.withNewTransaction {
            PicklistItem picklistItem = PicklistItem.get(event.picklistItemId)
            if (!picklistItem) {
                log.warn "Picklist item ${event.picklistItemId} not found, cannot publish its pick shortage"
                return
            }
            Location holdLocation = event.holdLocationId ? Location.get(event.holdLocationId) : null
            List<PicklistItem> reallocatedItems = event.reallocatedPicklistItemIds.collect { PicklistItem.get(it) }.findAll()

            webhookPublisherService.publishPickShortageEvent(picklistItem, event.quantityHeld, holdLocation, reallocatedItems)
        }
    }
}
