/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import grails.plugin.springcache.annotations.CacheFlush
import grails.plugin.springcache.annotations.Cacheable
import grails.util.GrailsUtil
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionType

import java.text.DateFormat
import java.text.SimpleDateFormat

class ApiController {

    def dataSource
    def userService
    def localizationService
    def megamenuService
    def grailsApplication
    def messageSource

    def login = {
        def username = request.JSON.username
        def password = request.JSON.password
        if (userService.authenticate(username, password)) {
            session.user = User.findByUsernameOrEmail(username, username)
            if (request.JSON.location) {
                session.warehouse = Location.get(request.JSON.location)
            }
            render([status: 200, text: "Authentication was successful"])
            return
        }
        render([status: 401, text: "Authentication failed"])
    }

    def chooseLocation = {
        Location location = Location.get(params.id)
        if (!location) {
            throw new ObjectNotFoundException(params.id, Location.class.toString())
        }
        session.warehouse = location
        render([status: 200, text: "User ${session.user} is now logged into ${location.name}"])
    }

    @CacheFlush(["megamenuCache"])
    def chooseLocale = {
        Locale locale = localizationService.getLocale(params.id)
        if (!locale) {
            throw new ObjectNotFoundException(params.id, Locale.class.toString())
        }
        session.user.locale = locale
        render([status: 200, text: "Current language is ${locale}"])
    }

    def getMenuConfig = {
        Map menuConfig = grailsApplication.config.openboxes.megamenu
        User user = User.get(session?.user?.id)
        Location location = Location.get(session.warehouse?.id)
        List translatedMenu = megamenuService.buildAndTranslateMenu(menuConfig, user, location)
        render([data: [menuConfig: translatedMenu]] as JSON)
    }

    def getAppContext = {

        def localizationMode
        def locale = localizationService.getCurrentLocale()
        Object[] emptyArgs = [] as Object []
        if (session.useDebugLocale) {

            localizationMode = [
                "label"      : messageSource.getMessage('localization.disable.label', emptyArgs, 'Disable translation mode', locale),
                "linkIcon"   : "${request.contextPath}/images/icons/silk/world_delete.png",
                "linkAction" : "${request.contextPath}/user/disableLocalizationMode"
            ]
        }
        else {

            localizationMode = [
                    "label"     : messageSource.getMessage('localization.enable.label', emptyArgs, 'Enable translation mode', locale),
                    "linkIcon"  : "${request.contextPath}/images/icons/silk/world_add.png",
                    "linkAction": "${request.contextPath}/user/enableLocalizationMode"
            ]
        }
        List<Map> menuItems = [
            [
                "label"      : messageSource.getMessage('default.edit.label',
                        [messageSource.getMessage('user.profile.label', emptyArgs, 'Profile', locale)] as Object[],
                        'Enable translation mode', locale),
                "linkIcon"   : "${request.contextPath}/images/icons/silk/user.png",
                "linkAction" : "${request.contextPath}/user/edit/${session?.user?.id}",
            ],
            localizationMode,
            [
                "label"      : messageSource.getMessage('cache.flush.label', emptyArgs, 'Refresh caches', locale),
                "linkIcon"   : "${request.contextPath}/images/icons/silk/database_wrench.png",
                "linkAction" : "${request.contextPath}/dashboard/flushCache",
            ],
            [
                "label"      : messageSource.getMessage('default.logout.label', emptyArgs, 'Logout', locale),
                "linkIcon"   : "${request.contextPath}/images/icons/silk/door.png",
                "linkAction" : "${request.contextPath}/auth/logout",
            ]
        ]

        User user = User.get(session?.user?.id)
        Location location = Location.get(session.warehouse?.id)
        String highestRole = user.getHighestRole(location)
        boolean isSuperuser = userService.isSuperuser(session?.user)
        boolean isUserAdmin = userService.isUserAdmin(session?.user)
        def supportedActivities = location.supportedActivities ?: location.locationType.supportedActivities
        boolean isImpersonated = session.impersonateUserId ? true : false
        def buildNumber = grailsApplication.metadata.'app.revisionNumber'
        def buildDate = grailsApplication.metadata.'app.buildDate'
        def branchName = grailsApplication.metadata.'app.branchName'
        def grailsVersion = grailsApplication.metadata.'app.grails.version'
        def appVersion = grailsApplication.metadata.'app.version'
        def environment = GrailsUtil.environment
        def ipAddress = request?.getRemoteAddr()
        def hostname = session.hostname ?: "Unknown"
        def timezone = session?.timezone?.ID
        def isPaginated = grailsApplication.config.openboxes.api.pagination.enabled
        def tablero = grailsApplication.config.openboxes.tablero
        DateFormat dateFormat = new SimpleDateFormat("MM/DD/YYYY");
        String minimumExpirationDate = dateFormat.format(grailsApplication.config.openboxes.expirationDate.minValue)
        def logoLabel = grailsApplication.config.openboxes.logo.label
        def pageSize = grailsApplication.config.openboxes.api.pagination.pageSize
        def logoUrl = "/openboxes/location/viewLogo/${session.warehouse?.id}"
        def locales = grailsApplication.config.openboxes.locale.supportedLocales
        def supportedLocales = locales.collect {
            def name = new Locale(it).getDisplayName()
            [code: it, name: name]
        }
        String currencyCode = grailsApplication.config.openboxes.locale.defaultCurrencyCode
        render([
                data: [
                        user                 : user,
                        location             : location,
                        isSuperuser          : isSuperuser,
                        isUserAdmin          : isUserAdmin,
                        supportedActivities  : supportedActivities,
                        isImpersonated       : isImpersonated,
                        grailsVersion        : grailsVersion,
                        appVersion           : appVersion,
                        branchName           : branchName,
                        buildNumber          : buildNumber,
                        environment          : environment,
                        buildDate            : buildDate,
                        ipAddress            : ipAddress,
                        hostname             : hostname,
                        timezone             : timezone,
                        tablero              : tablero,
                        minimumExpirationDate: minimumExpirationDate,
                        activeLanguage       : locale.language,
                        isPaginated          : isPaginated,
                        logoLabel            : logoLabel,
                        menuItems            : menuItems,
                        highestRole          : highestRole,
                        pageSize             : pageSize,
                        logoUrl              : logoUrl,
                        supportedLocales     : supportedLocales,
                        currencyCode         : currencyCode,
                ],
        ] as JSON)
    }


    def logout = {
        if (session.impersonateUserId) {
            session.user = User.get(session.activeUserId)
            session.impersonateUserId = null
            session.activeUserId = null
            render([status: 200, text: "Logout was successful"])
        } else {
            session.invalidate()
            render([status: 200, text: "Logout was successful"])
        }
    }

    def status = {
        boolean databaseStatus = true
        String databaseStatusMessage = "Database is available"

        try {
            Product.count()
        } catch (Exception e) {
            databaseStatus = false
            databaseStatusMessage = "Error: " + e.message
        }
        render([status: "OK", database: [status: databaseStatus, message: databaseStatusMessage ?: ""]] as JSON)
    }

    def getRequestTypes = {
        def requestTypes = []
        Locale defaultLocale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale ?: "en")
        Locale locale = session?.user?.locale ?: defaultLocale
        RequisitionType.listRequestTypes().each {
            def id = it.name()
            def name = messageSource.getMessage("enum.RequisitionType.${it.name()}", null, null, locale)
            requestTypes << [id: id, name: name]
        }
        render([data: requestTypes] as JSON)
    }
}
