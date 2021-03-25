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
import org.pih.warehouse.core.Location
import org.pih.warehouse.jobs.RefreshProductAvailabilityJob
import org.springframework.context.ApplicationListener
class TransactionEventService implements ApplicationListener<TransactionEvent> {

    boolean transactional = true
    def grailsApplication

    void onApplicationEvent(TransactionEvent event) {
        log.info "Application event $event has been published!"
        Transaction transaction = event?.source

        // Some transaction event publishers might want to trigger the events on their own
        // in order to allow the transaction to be saved to the database (e.g. Partial Receiving)
        if (transaction?.blockRefresh) {
            log.warn "Product availability refresh has been blocked by event publisher"
            return
        }

        Location location = Location.load(transaction.associatedLocation)
        def productIds = transaction.associatedProducts
        Boolean forceRefresh = event.forceRefresh

        log.info "Refresh product availability records for " +
                "location=$location.id, " +
                "transactionId=$transaction.id," +
                "transactionDate=$transaction.transactionDate," +
                "transactionNumber=$transaction.transactionNumber," +
                "productIds=$productIds, " +
                "forceRefresh=$forceRefresh"

        use(TimeCategory) {
            Boolean delayStart = grailsApplication.config.openboxes.jobs.refreshProductAvailabilityJob.delayStart
            def delayInMilliseconds = delayStart ?
                    grailsApplication.config.openboxes.jobs.refreshProductAvailabilityJob.delayInMilliseconds : 0
            Date runAt = new Date() + delayInMilliseconds.milliseconds
            log.info "Triggering refresh product availability with ${delayInMilliseconds} ms delay"
            RefreshProductAvailabilityJob.schedule(runAt, [
                    locationId  : location?.id,
                    productIds  : productIds,
                    forceRefresh: forceRefresh
            ])
        }

    }
}
