/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package openboxes


import grails.util.Environment
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.pih.warehouse.core.User

class SentryFilters {
    def ravenClient

    def filters = {
        all(uri: '/**') {
            before = {
                if (session.user) {

                    try {
                        def serverUrl = ConfigurationHolder.config.grails.serverURL
                        def user = User.get(session.user.id)
                        def userData = [
                                id         : user.id, is_authenticated: true,
                                email      : user.email,
                                username   : user.username,
                                location   : session.warehouse ? session.warehouse.name : "No location",
                                environment: Environment.current,
                                timezone   : user.timezone,
                                locale     : session?.user?.locale?.toString() ?: "No locale",
                                sessionId  : session?.id,
                                server     : serverUrl ?: "No server URL"

                        ]
                        ravenClient.setUserData(userData)
                    } catch (Exception e) {
                        log.info("Unable to set the user data for sentry due to the following error " + e.message)
                    }
                } else {
                    ravenClient.setUserData([is_authenticated: false])
                }
            }
        }
    }
}
