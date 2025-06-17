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

class RefreshPicklistStatusEventService implements ApplicationListener<RefreshPicklistStatusEvent> {

    GrailsApplication grailsApplication

    void onApplicationEvent(RefreshPicklistStatusEvent event) {
        log.info "Application event $event has been published! " + event.properties

        // FIXME Need to delay automatic state transition
        use(TimeCategory) {
            def delayInMilliseconds = grailsApplication.config.openboxes.jobs.automaticStateTransitionJob.delayInMilliseconds ?: 0
            Date runAt = new Date() + delayInMilliseconds.milliseconds
            log.info "Schedule automatic state transition job for ${event.source} at ${runAt}"
            AutomaticStateTransitionJob.schedule(runAt, [id: event.source])
        }
    }
}
