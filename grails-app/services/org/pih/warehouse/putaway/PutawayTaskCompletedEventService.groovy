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

import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

class PutawayTaskCompletedEventService {

    def productAvailabilityService

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void onPutawayTaskCompleted(PutawayTaskCompletedEvent event) {
        PutawayTask task = (PutawayTask) event.source
        log.info "Putaway ${task?.id} completed; refreshing PA for facility=${task?.facility?.id}, product=${task?.product?.id}"
        productAvailabilityService.triggerRefreshProductAvailability(task?.facility?.id, [task?.product?.id], event?.forceRefresh)
    }
}
