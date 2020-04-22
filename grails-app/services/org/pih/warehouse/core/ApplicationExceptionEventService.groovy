/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import grails.gorm.transactions.Transactional
import org.springframework.context.ApplicationListener

@Transactional
class ApplicationExceptionEventService implements ApplicationListener<ApplicationExceptionEvent> {

    def notificationService

    void onApplicationEvent(ApplicationExceptionEvent event) {
        log.info "Application event ${event.source} has been published for location ${event.location.id}"
        Location location = Location.get(event.location.id)
        notificationService.sendApplicationErrorNotification(location, event.source)
    }
}
