/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
import org.pih.warehouse.core.User

class InitializationFilters {
    def locationService
    def productService

    def filters = {

        sessionCheck(controller: '*', action: '*') {
            before = {
                try {

                    // Only initialize session if a user has logged in.
                    if (session.user) {

                        if (session.impersonateUserId && session.user.id != session.impersonateUserId) {
                            session.user = User.get(session.impersonateUserId)
                        }

                        if (!session.hasProperty("_showTime")) {
                            session._showTime = true
                        }

                        if (!session.hasProperty("hostname")) {
                            session.hostname = InetAddress.getLocalHost().getHostName() + " (" + request.getHeader('Host') + ")"
                        }
                    }
                } catch (Exception e) {
                    log.error "Unable to initialize session variables: " + e.message, e
                }
            }
        }

    }
}
