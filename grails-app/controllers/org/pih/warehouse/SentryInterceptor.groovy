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
import io.sentry.SentryClient
import io.sentry.event.UserBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User

class SentryInterceptor {
    SentryClient sentryClient

    public SentryInterceptor() {
        matchAll().except(uri: '/static/**').except(uri: "/info")
    }

    boolean before() {
        if (session.user && sentryClient) {
            try {
                def user = User.get(session.user.id)
                def warehouse = Location.get(session.warehouse.id)
                def data = [
                        id         : user?.id,
                        email      : user?.email,
                        username   : user?.username,
                        location   : warehouse?.name,
                        environment: Environment.current,
                        timezone   : user?.timezone,
                        locale     : user?.locale?.toString(),
                        sessionId  : session?.id
                ]
                def userBuilder = new UserBuilder().setData(data)
                sentryClient.context.setUser(userBuilder.build())

            } catch (Exception e) {
                log.info("Unable to set the user data for ${request.requestURI} due to the following error: " + e.message)
            }
        }
        return true
    }
}
