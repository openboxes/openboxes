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

import org.springframework.context.ApplicationListener
class TransactionEventService implements ApplicationListener<TransactionEvent> {

    boolean transactional = true
    def grailsApplication
    def productAvailabilityService

    void onApplicationEvent(TransactionEvent event) {
        log.info "Application event $event has been published! " + event.properties
        Transaction transaction = event?.source

        // Some transaction event publishers might want to trigger the events on their own
        // in order to allow the transaction to be saved to the database (e.g. Partial Receiving)
        if (transaction?.disableRefresh) {
            log.warn "Product availability refresh has been disabled by event publisher"
            return
        }

        productAvailabilityService.triggerRefreshProductAvailability(transaction.associatedLocation,
                transaction.associatedProducts, event.forceRefresh)
    }
}
