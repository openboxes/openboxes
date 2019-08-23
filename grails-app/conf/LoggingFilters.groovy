/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/


import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.slf4j.MDC

class LoggingFilters {
    def filters = {
        all(controller: '*', action: '*') {
            before = {
                try {
                    def sessionId = session?.id
                    def userId = session?.user?.username
                    def serverUrl = CH.config.grails.serverURL
                    MDC.put('sessionId', session?.id ?: "No session ID")
                    MDC.put('username', userId ?: "No user")
                    MDC.put('location', session?.warehouse?.name ?: "No location")
                    MDC.put('locale', session?.user?.locale?.toString() ?: "No locale")
                    MDC.put('ipAddress', request?.remoteAddr ?: "No IP address")
                    MDC.put('requestUri', request?.requestURI?.toString() ?: "No request URI")
                    MDC.put('requestUrl', request?.requestURL?.toString() ?: "No request URL")
                    MDC.put('queryString', request?.queryString ?: "No query string")
                    MDC.put('serverUrl', CH?.config?.grails?.serverURL ?: "No server URL")
                } catch (Exception e) {
                    log.warn("Error occurred while adding attributes to Mapped Diagnostic Context: ${e.message}", e)

                }
            }
            after = {
            }
            afterView = {

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
    }
}