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
import grails.util.GrailsUtil
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

import java.text.DateFormat
import java.text.SimpleDateFormat

class ApiController {

    def dataSource
    def userService
    def localizationService
    def grailsApplication

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

    def chooseLocale = {
        Locale locale = localizationService.getLocale(params.id)
        if (!locale) {
            throw new ObjectNotFoundException(params.id, Locale.class.toString())
        }
        session.user.locale = locale
        render([status: 200, text: "Current language is ${locale}"])
    }

    def getAppContext = {
        User user = User.get(session?.user?.id)
        Location location = Location.get(session.warehouse?.id)
        boolean isSuperuser = userService.isSuperuser(session?.user)
        boolean isUserAdmin = userService.isUserAdmin(session?.user)
        def locale = localizationService.getCurrentLocale()
        def supportedActivities = location.supportedActivities ?: location.locationType.supportedActivities
        def menuConfig = grailsApplication.config.openboxes.megamenu
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
        render([
                data: [
                        user                 : user,
                        location             : location,
                        isSuperuser          : isSuperuser,
                        isUserAdmin          : isUserAdmin,
                        supportedActivities  : supportedActivities,
                        menuConfig           : menuConfig,
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
}
