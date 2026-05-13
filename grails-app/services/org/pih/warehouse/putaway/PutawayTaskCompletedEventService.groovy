/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.putaway

import org.springframework.context.ApplicationListener
import org.springframework.transaction.support.TransactionSynchronizationAdapter
import org.springframework.transaction.support.TransactionSynchronizationManager

class PutawayTaskCompletedEventService implements ApplicationListener<PutawayTaskCompletedEvent> {

    def productAvailabilityService

    void onApplicationEvent(PutawayTaskCompletedEvent event) {
        PutawayTask task = (PutawayTask) event.source
        String facilityId = task?.facility?.id
        String productId = task?.product?.id
        Boolean forceRefresh = event?.forceRefresh
        log.info "Putaway ${task?.id} completed; scheduling product-availability refresh for facility=${facilityId}, product=${productId}"

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // Defer until commit so transferStock's two Transaction inserts are both flushed before the refresh reads transaction_entry.
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                void afterCommit() {
                    productAvailabilityService.refreshProductsAvailability(facilityId, [productId], forceRefresh)
                }
            })
        } else {
            productAvailabilityService.refreshProductsAvailability(facilityId, [productId], forceRefresh)
        }
    }
}
