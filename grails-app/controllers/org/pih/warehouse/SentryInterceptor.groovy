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
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User

/**
 * Intercepts HTTP requests, adding user information to the Sentry transaction that will be created for the request.
 *
 * If we ever switch to use the Spring Security plugin, this code will need to be moved to a Spring Component
 * that implements SentryUserProvider.
 */
@CompileStatic
class SentryInterceptor {

    // this interceptor depends on SecurityInterceptor setting user/location
    int order = LOWEST_PRECEDENCE

    AuthService authService

    SentryInterceptor() {
        matchAll().except(uri: '/static/**').except(uri: "/info").except(uri: "/health")
    }

    @Override
    boolean before() {
        long startTime = System.currentTimeMillis()
        try {
            if (!Sentry.isEnabled()) {
                return true
            }

            User user = authService.currentUser
            Location location = authService.currentLocation
            Map<String, String> additionalData = [:]

            Sentry.user = new SentryUser().with {
                if (user?.email) {
                    email = user.email
                }
                if (user?.id) {
                    id = user.id
                }
                if (user?.username) {
                    username = user.username
                }
                if (Environment?.current) {
                    additionalData['environment'] = Environment.current.name
                }
                if (user?.locale) {
                    additionalData['locale'] = user.locale.toString()
                }
                if (location?.name) {
                    additionalData['location'] = location.name
                }
                if (location?.id) {
                    additionalData['locationId'] = location.id
                }
                if (session?.id) {
                    additionalData['sessionId'] = session.id
                }
                if (user?.timezone) {
                    additionalData['timezone'] = user.timezone
                }

                data = additionalData
                delegate
            }

        } catch (Exception e) {
            log.warn("Error setting Sentry user data for ${request.requestURI}", e)
            return true
        }

        log.debug "updated Sentry context for ${request.requestURI} in ${System.currentTimeMillis() - startTime} ms"
        return true
    }
}
