/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse

import grails.util.Environment
import groovy.transform.CompileStatic
import io.sentry.Sentry
import io.sentry.protocol.User as SentryUser
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User

@CompileStatic
class SentryInterceptor {

    SentryInterceptor() {
        matchAll().except(uri: '/static/**').except(uri: '/info').except(uri: '/health')
    }

    @Override
    /**
     * Update user/location data for Sentry at each page load.
     *
     * This method doesn't actually send any events to Sentry; rather,
     * it attaches various data to the Sentry global for later use in
     * case an error or logging event occurs.
     *
     * N.B. Sentry also loads some user-agent/url information by itself
     * from Tomcat (see SentryServletContainerInitializer).
     */
    boolean before() {
        def startTime = System.currentTimeMillis()
        try {
            String userId = (session.getAttribute('user') as User)?.id

            if (userId) {
                String locationId = (session.getAttribute('warehouse') as Location)?.id
                User user = User.get(userId)
                SentryUser sentryUser = new SentryUser()
                sentryUser.with {
                    email = user?.email
                    id = userId
                    username = user?.username
                    data = [
                        environment: Environment.current.name,
                        locale: user?.locale?.toString() ?: 'unset',
                        location: Location.get(locationId)?.name ?: 'unset',
                        locationId: locationId ?: 'unset',
                        sessionId: session?.id ?: 'unset',
                        timezone: user.timezone ?: 'unset',
                    ] as Map<String, String>
                }
                Sentry.user = sentryUser
            }
        } catch (Exception e) {
            log.warn("Error setting Sentry user data for ${request.requestURI}", e)
        }

        log.debug "updated Sentry context for ${request.requestURI} in ${System.currentTimeMillis() - startTime} ms"
        return true
    }
}
