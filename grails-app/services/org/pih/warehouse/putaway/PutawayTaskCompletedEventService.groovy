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

import grails.gorm.transactions.Transactional
import org.springframework.context.ApplicationListener

@Transactional
class PutawayTaskCompletedEventService implements ApplicationListener<PutawayTaskCompletedEvent> {

    def productAvailabilityService

    void onApplicationEvent(PutawayTaskCompletedEvent event) {
        PutawayTask task = (PutawayTask) event.source;
        log.info "Putaway ${task.id} into ${task.facility}:${task.destination} completed by ${task.completedBy} at ${task.dateCompleted} "
        productAvailabilityService.triggerRefreshProductAvailability(task?.facility?.id, [task.product.id], event?.forceRefresh)
    }
}
