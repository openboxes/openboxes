package org.pih.warehouse

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

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

import org.pih.warehouse.core.localization.LocaleManager

/**
 * Hook-in for initializing a user session.
 */
class InitializationInterceptor {

    @Autowired
    LocaleManager localeManager

    @Value('${server.session.timeout}')
    Integer sessionTimeoutInterval

    int order = HIGHEST_PRECEDENCE

    InitializationInterceptor() {
        // TODO: This will get triggered for EVERY request. See if we can restrict this at all. Ideally this will only
        //       be triggered when login is called but we need to double check.
        matchAll()
    }

    boolean before() {
        try {
            // Only initialize the session if a user has logged in.
            if (!session?.user) {
                return true
            }

            setLocale()
            setUser()
            setCustomProperties()

            // Note that we're only setting a custom session timeout for actual logged in users. Tomcat creates a
            // session when first fetching the login page, which will use the Tomcat default session timeout (30 mins).
            // This temporary session is only used to store simple info such as post-login redirects, and will be
            // replaced with a real session object once the user logs in.
            setSessionTimeout()

        } catch (Exception e) {
            log.error "Unable to initialize session variables: " + e.message, e
        }
        return true
    }

    void setUser() {
        // If we're trying to impersonate a given user, change our user to them so that we can see things as they do.
        if (session.impersonateUserId && session.user.id != session.impersonateUserId) {
            session.user = User.get(session.impersonateUserId)
        }
    }

    void setLocale() {
        // It might seem weird to get the current locale and then set it immediately after, but getCurrentLocale
        // returns the user or system default locale when the session locale is not yet initialized (which it won't be
        // at the time that auth is called). We then set that default into the session so that we don't need to lookup
        // the defaults again next time we fetch the locale. Subsequent changes to the locale can be made via the ?lang
        // query param (when making API requests), or via any locale-updating controller actions.
        Locale locale = localeManager.getCurrentLocale()
        localeManager.setCurrentLocale(locale)
    }

    void setCustomProperties() {
        // Controls whether or not to display certain timestamp fields in the UI. Default to true for all sessions.
        if (!session.hasProperty("_showTime")) {
            session._showTime = true
        }

        if (!session.hasProperty("hostname")) {
            session.hostname = "${InetAddress.getLocalHost().getHostName()} (${request.getHeader('Host')})"
        }
    }

    /**
     * Set the TTL for each session. Simply defining the application property is enough when running an embedded
     * server, but when deploying the app to an external Servlet, we need to set the timeout directly on the session
     * for it to get picked up by the Servlet.
     *
     * https://schneide.blog/2019/07/08/setting-grails-session-timeout-in-production/
     */
    void setSessionTimeout() {
        session.maxInactiveInterval = sessionTimeoutInterval
    }
}
