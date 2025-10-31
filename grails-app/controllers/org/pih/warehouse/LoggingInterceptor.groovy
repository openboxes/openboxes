package org.pih.warehouse
/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

import grails.util.Holders
import org.apache.commons.lang.StringUtils
import org.slf4j.MDC

/**
 * Adds custom information to Logback events via MDC (Mapped Diagnostic Context) so that they can be displayed in logs.
 * Threads are shared across user requests, so we need to reset the MDC for each new request.
 *
 * Sentry will automatically pick up all MDC tags, but to have them appear in console logs or in New Relic,
 * they must be added explicitly to the log output.
 */
class LoggingInterceptor {

    LoggingInterceptor() {
        matchAll().except(uri: "/static/**").except(uri: "/info").except(uri: "/health")
    }

    boolean before() {
        try {
            MDC.put('sessionId', session?.id ?: "No session ID")
            MDC.put('username', session?.user?.username ?: "No user")
            MDC.put('location', session?.warehouse?.name ?: "No location")
            MDC.put('locale', session?.user?.locale?.toString() ?: "No locale")
            MDC.put('ipAddress', request?.remoteAddr ?: "No IP address")
            MDC.put('requestUri', request?.requestURI?.toString() ?: "No request URI")
            MDC.put('requestUrl', request?.requestURL?.toString() ?: "No request URL")
            MDC.put('queryString', request?.queryString ?: "No query string")
            MDC.put('serverUrl', Holders.grailsApplication.config?.grails?.serverURL ?: "No server URL")
        } catch (Exception e) {
            log.warn("Error occurred while adding attributes to Mapped Diagnostic Context: ${e.message}", e)
        }

        // Now that we've populated the MDC, log the request itself, including query parameters.
        String queryString = StringUtils.isBlank(request?.queryString) ? "" : "?${request?.queryString}"
        log.info("${request.requestURI}${queryString} [user:${session?.user?.username}, location:${session?.warehouse?.name}]")

        return true
    }

    boolean after() {
        return true
    }

    void afterView() {
        try {
            MDC.remove('sessionId')
            MDC.remove('username')
            MDC.remove('location')
            MDC.remove('locale')
            MDC.remove('ipAddress')
            MDC.remove('requestUri')
            MDC.remove('requestUri')
            MDC.remove('serverUrl')
            MDC.remove('queryString')
        } catch (Exception e) {
            log.warn("Error occurred while removing attributes from Mapped Diagnostic Context: ${e.message}", e)
        }
    }
}
