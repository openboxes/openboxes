/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.stockTransfer

import org.pih.warehouse.api.StockTransfer
import org.pih.warehouse.api.StockTransferItem
import org.springframework.context.ApplicationListener

class StockTransferCompletedEventService implements ApplicationListener<StockTransferCompletedEvent> {

    boolean transactional = true
    def grailsApplication
    def productAvailabilityService

    void onApplicationEvent(StockTransferCompletedEvent event) {
        log.info "Application event $event has been published!"

        StockTransfer stockTransfer = event.source
        List locationIds = [stockTransfer?.origin?.id, stockTransfer?.destination?.id].unique()
        List productIds = stockTransfer.stockTransferItems.collect { StockTransferItem stockTransferItem -> stockTransferItem?.product?.id }.unique()
        Boolean forceRefresh = event?.forceRefresh

        locationIds.each { String locationId ->
            log.info "Refresh product availability records for " +
                    "location=${locationId}, productIds=${productIds}, forceRefresh=${forceRefresh}"
            productAvailabilityService.triggerRefreshProductAvailability(locationId, productIds, forceRefresh)
        }
    }
}
