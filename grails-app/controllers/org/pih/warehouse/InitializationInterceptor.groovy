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
import org.pih.warehouse.core.User
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.servlet.support.RequestContextUtils

class InitializationInterceptor {

    int order = HIGHEST_PRECEDENCE

    public InitializationInterceptor() {
        matchAll()
    }

    boolean before() {
        try {

            // Only initialize session if a user has logged in.
            if (session.user) {
                // Determine whether enable/disable localization mode
                def locale = session?.locale ?: session.user?.locale ?: new Locale(grailsApplication.config.openboxes.locale.defaultLocale ?: "en")
                def localizationModeLocale = grailsApplication.config.openboxes.locale.localizationModeLocale
                // If current locale is equal to translation mode locale, we are in localization mode
                session.useDebugLocale = locale == new Locale(localizationModeLocale)
                // We want to set the locale for grails (equivalent to passing ?lang as param)
                // so grails' g:message "understands" current language so that is translatable with the crowdin
                RequestContextUtils.getLocaleResolver(request).setLocale(request, response, locale)
                // TODO (OBPIH-5452): If we get rid of our own logic to handle locale, this line or the RCU one might be removed
                LocaleContextHolder.setLocale(locale)

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
        return true
    }
}
