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

import grails.core.GrailsApplication
import groovy.time.TimeCategory
import org.pih.warehouse.jobs.AutomaticStateTransitionJob
import org.springframework.context.ApplicationListener
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

class RefreshPicklistStatusEventService {

    GrailsApplication grailsApplication

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void onRefreshPicklistStatusEvent(RefreshPicklistStatusEvent event) {
        log.info "Application event $event has been published! " + event.properties

        use(TimeCategory) {
            log.info "Schedule automatic state transition job for ${event.source}"
            AutomaticStateTransitionJob.scheduleNow([id: event.source])
        }
    }
}
