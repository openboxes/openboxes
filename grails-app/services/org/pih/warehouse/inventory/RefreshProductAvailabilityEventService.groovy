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

import groovy.time.TimeCategory
import org.pih.warehouse.jobs.SendProductAvailabilityMessagesJob
import org.springframework.context.ApplicationListener
class RefreshProductAvailabilityEventService implements ApplicationListener<RefreshProductAvailabilityEvent> {

    boolean transactional = true
    def grailsApplication
    def productAvailabilityService
    def notificationService

    void onApplicationEvent(RefreshProductAvailabilityEvent event) {
        log.info "Application event $event has been published! " + event.properties

        // Some event publishers might want to trigger the events on their own
        // in order to allow the transaction to be saved to the database (e.g. Partial Receiving)
        if (!event?.disableRefresh) {
            log.warn "Refresh product availability (synchronous=${event?.synchronousRequired})"
            if (event?.synchronousRequired) {
                productAvailabilityService.refreshProductsAvailability(event.locationId,
                        event.productIds, event.forceRefresh)
            } else {
                productAvailabilityService.triggerRefreshProductAvailability(event.locationId,
                        event.productIds, event.forceRefresh)
            }
        }
        else {
            log.warn "Product availability refresh has been disabled by event publisher"
        }

        // All transaction events should publish a message to the product availability message queue
        if (event?.source instanceof Transaction) {
            use(TimeCategory) {
                def delayStartInMilliseconds = grailsApplication.config.openboxes.jobs.sendProductAvailabilityMessagesJob.delayStartInMilliseconds?:0
                Date runAt = new Date() + delayStartInMilliseconds.milliseconds
                log.info "Scheduling trigger for SendProductAvailabilityMessagesJob to run at ${runAt}"
                SendProductAvailabilityMessagesJob.schedule(runAt, [locationId:event.locationId, productIds:event.productIds])
            }
        }
    }
}
