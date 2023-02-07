/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.order

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import org.pih.warehouse.order.RefreshOrderSummaryEvent
import org.springframework.context.ApplicationListener

@Transactional
class RefreshOrderSummaryEventService implements ApplicationListener<RefreshOrderSummaryEvent> {

    GrailsApplication grailsApplication
    def orderService

    void onApplicationEvent(RefreshOrderSummaryEvent event) {
        log.info "Application event ${event} has been published! " + event.properties

        if (event?.orderIds && !event.disableRefresh) {
            log.info "Refreshing order summary ${event.isDelete ? 'before delete action' : ''} for orders with ids : ${event.orderIds}"
            orderService.refreshOrderSummary(event.orderIds, event.isDelete)
        } else {
            log.info "Event not processed because ${!event?.orderIds ? 'lack of purchase order ids.' : 'it was disabled by event publisher.'}"
        }
    }
}
