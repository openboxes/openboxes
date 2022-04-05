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
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Environment
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionType

import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.Produces
import java.text.DateFormat
import java.text.SimpleDateFormat

@Transactional
class ApiController {

    def dataSource
    def userService
    def localizationService
    GrailsApplication grailsApplication
    def megamenuService
    def messageSource

    class LoginRequest {
        @Schema(format = "email", required = true, type = "string")
        String username
        @Schema(format = "password", required = true, type = "string")
        String password
        @Schema(format = "id", type = "string")
        String location
    }

    @POST
    @Operation(
        summary = "retrieve a cookie for authentication",
        description = """\
Supply a username and password in the usual way to authenticate
(`application/x-www-form-urlencoded`, like `curl -d`). You may
optionally supply the identifier of a warehouse via a `location`
field; this is an implicit parameter to many subsequent queries.
If no location is specified, OpenBoxes will use the last one the
authenticating user selected via the web UI or /api/chooseLocation.

---

Note that SwaggerHub's UI may not report the `JSESSIONID`, depending
on browser settings (see
[this document](https://swagger.io/docs/specification/authentication/cookie-authentication/)
for more details). In that event, to authenticate this page and allow
the "Try it out" button to work elsewhere, please try the following.

1. Press the "Try it out" button below and to the right of this text.
2. Press the blue "Execute" button that appears beneath the json editor, below.
3. Copy the `curl` code snippet, add the `-i` flag, then run it in a local terminal.
4. Press the green "Authorize" button near the top of this page.
5. Copy the `JSESSIONID` field from `curl` output into the field that appears.
"""
    )
    @RequestBody(
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = LoginRequest)
        ),
        required = true
    )
    @ApiResponse(
        description = "the cookie to use when authentication is required",
        headers = [
            @Header(
                description = "the `JSESSIONID` cookie to use for subsequent requests",
                name = "Set-Cookie",
                schema = @Schema(type = "string")
            )
        ],
        responseCode = "200"
    )
    @Path("/api/login")
    @Tag(name = "Authentication")
    def login() {
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

    @PUT
    @Operation(
        summary = "specify a location",
        description = "specify a location to refer against when making requests",
        parameters = [@Parameter(ref = "location_id_in_path")]
    )
    @ApiResponse(
        content = @Content(
            schema = @Schema(type = "string")
        ),
        description = "current user name and location name",
        responseCode = "200"
    )
    @Path("/api/chooseLocation/{id}")
    @Produces("text/plain")
    @SecurityRequirement(name = "cookie")
    @Tag(name = "Configuration")
    def chooseLocation() {
        Location location = Location.get(params.id)
        if (!location) {
            throw new ObjectNotFoundException(params.id, Location.class.toString())
        }
        session.warehouse = location
        render([status: 200, text: "User ${session.user} is now logged into ${location.name}"])
    }

    def chooseLocale() {
        Locale locale = localizationService.getLocale(params.id)
        if (!locale) {
            throw new ObjectNotFoundException(params.id, Locale.class.toString())
        }
        session.user.locale = locale
        render([status: 200, text: "Current language is ${locale}"])
    }

    def getMenuConfig() {
        Map menuConfig = grailsApplication.config.openboxes.megamenu
        User user = User.get(session?.user?.id)
        Location location = Location.get(session.warehouse?.id)
        List translatedMenu = megamenuService.buildAndTranslateMenu(menuConfig, user, location)
        render([data: [menuConfig: translatedMenu]] as JSON)
    }

    def getAppContext() {

        def localizationMode
        def locale = localizationService.getCurrentLocale()
        Object[] emptyArgs = [] as Object []
        if (session.useDebugLocale) {

            localizationMode = [
                "label"      : messageSource.getMessage('localization.disable.label', emptyArgs, 'Disable translation mode', locale),
                "linkIcon"   : "${request.contextPath}/static/images/icons/silk/world_delete.png",
                "linkAction" : "${request.contextPath}/user/disableLocalizationMode"
            ]
        }
        else {

            localizationMode = [
                    "label"     : messageSource.getMessage('localization.enable.label', emptyArgs, 'Enable translation mode', locale),
                    "linkIcon"  : "${request.contextPath}/static/images/icons/silk/world_add.png",
                    "linkAction": "${request.contextPath}/user/enableLocalizationMode"
            ]
        }
        List<Map> menuItems = [
            [
                "label"      : messageSource.getMessage('default.edit.label',
                        [messageSource.getMessage('user.profile.label', emptyArgs, 'Profile', locale)] as Object[],
                        'Enable translation mode', locale),
                "linkIcon"   : "${request.contextPath}/static/images/icons/silk/user.png",
                "linkAction" : "${request.contextPath}/user/edit/${session?.user?.id}",
            ],
            localizationMode,
            [
                "label"      : messageSource.getMessage('cache.flush.label', emptyArgs, 'Refresh caches', locale),
                "linkIcon"   : "${request.contextPath}/static/images/icons/silk/database_wrench.png",
                "linkAction" : "${request.contextPath}/dashboard/flushCache",
            ],
            [
                "label"      : messageSource.getMessage('default.logout.label', emptyArgs, 'Logout', locale),
                "linkIcon"   : "${request.contextPath}/static/images/icons/silk/door.png",
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
        def buildNumber = grailsApplication.metadata.getProperty('app.revisionNumber')?:''
        def buildDate = grailsApplication.metadata.getProperty('app.buildDate')?:''
        def branchName = grailsApplication.metadata.getProperty('app.branchName')?:''
        def grailsVersion = grailsApplication.metadata.getProperty('app.grails.version')?:''
        def appVersion = grailsApplication.metadata.getProperty('app.version')?:''
        def environment = Environment.current
        def ipAddress = request?.getRemoteAddr()
        def hostname = session.hostname ?: "Unknown"
        def timezone = session?.timezone?.ID
        def isPaginated = grailsApplication.config.openboxes.api.pagination.enabled
        def tablero = grailsApplication.config.openboxes.tablero
        DateFormat dateFormat = new SimpleDateFormat("MM/DD/YYYY");
        String minValue = grailsApplication.getConfig().getProperty('openboxes.expirationDate.minValue')
        String minimumExpirationDate = dateFormat.format(new Date(minValue))
        def logoLabel = grailsApplication.config.openboxes.logo.label
        def pageSize = grailsApplication.config.openboxes.api.pagination.pageSize
        def logoUrl = "/openboxes/location/renderLogo/${session.warehouse?.id}"
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
                        environment        : environment.name,
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


    def logout() {
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

    def status() {
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

    def getRequestTypes() {
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
