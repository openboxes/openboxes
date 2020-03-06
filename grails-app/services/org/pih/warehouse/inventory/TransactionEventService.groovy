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

import grails.gorm.transactions.Transactional
import groovy.time.TimeCategory
import org.pih.warehouse.jobs.RefreshProductAvailabilityJob
import org.springframework.context.ApplicationListener

@Transactional
class TransactionEventService implements ApplicationListener<TransactionEvent> {

    void onApplicationEvent(TransactionEvent event) {
        log.info "Application event $event has been published!"
        Transaction transaction = event?.source
        def transactionId = transaction?.id
        def transactionDate = transaction?.transactionDate
        def locationId = event.associatedLocation
        List productIds = event.associatedProducts
        Boolean forceRefresh = event.forceRefresh

        log.info "Refresh product availability in 10 seconds " +
                "date=$transactionDate, " +
                "location=$locationId, " +
                "transaction=$transactionId," +
                "productIds=$productIds, " +
                "forceRefresh=$forceRefresh"

        // FIXME Hack to allow the transaction to be persisted to the database. Otherwise, the
        // RefreshProductAvailabilityJob calculates product availability on all transactions
        // except this one, which defeats the purpose of running this job. I'm still at a loss
        // for why the transaction is not being returned in the query because the Hibernate
        // sessions should not be shared between the thread associated with the request and
        // the thread running the background job.
        //
        // The preferred approach would be to trigger the job now.
        //RefreshProductAvailabilityJob.triggerNow([
        //        locationId  : locationId,
        //        productIds  : productIds,
        //        forceRefresh: forceRefresh
        //])
        use(TimeCategory) {
            Date runAt = new Date() + 500.milliseconds
            RefreshProductAvailabilityJob.schedule(runAt, [
                    locationId  : locationId,
                    productIds  : productIds,
                    forceRefresh: forceRefresh
            ])
        }

    }
}
