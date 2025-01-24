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
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionType
import org.springframework.boot.info.GitProperties
import util.ConfigHelper

import java.text.DateFormat
import java.text.SimpleDateFormat

@Transactional
class ApiController {

    def userService
    def helpScoutService
    def localizationService
    GrailsApplication grailsApplication
    def megamenuService
    def messageSource
    GitProperties gitProperties

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
        session.locale = locale
        render([
            data: [
                activeLanguage: locale.toString(),
                activeLanguageTag: locale.toLanguageTag()
            ]
        ] as JSON)
    }

    def getMenuConfig() {
        Location location = Location.get(session.warehouse?.id)

        if (!location.supports(ActivityCode.MANAGE_INVENTORY) && location.supports(ActivityCode.SUBMIT_REQUEST)) {
            render([data: [menuConfig: []]] as JSON)
            return
        }

        Map menuConfig = grailsApplication.config.openboxes.megamenu
        Map menuSectionsUrlParts = grailsApplication.config.openboxes.menuSectionsUrlParts
        User user = User.get(session?.user?.id)

        if (userService.hasHighestRole(user, session?.warehouse?.id, RoleType.ROLE_AUTHENTICATED)) {
            menuConfig = grailsApplication.config.openboxes.requestorMegamenu;
        }
        List translatedMenu = megamenuService.buildAndTranslateMenu(menuConfig, user, location)
        render([data: [menuConfig: translatedMenu, menuSectionsUrlParts: menuSectionsUrlParts]] as JSON)
    }

    def getAppContext() {

        def localizationMode
        def currentLocale = localizationService.getCurrentLocale()
        Object[] emptyArgs = [] as Object []
        def localizationModeLocale = grailsApplication.config.openboxes.locale.localizationModeLocale
        def displayDateFormat = grailsApplication.config.openboxes.display.date.format
        def displayDateDefaultValue = grailsApplication.config.openboxes.display.date.defaultValue
        // Notification disappear time in miliseconds
        def notificationAutohideDelay = grailsApplication.config.openboxes.client.notification.autohide.delay

        if (session.useDebugLocale) {

            localizationMode = [
                "label"      : messageSource.getMessage('localization.disable.label', emptyArgs, 'Disable translation mode', currentLocale),
                "linkIcon"   : resource(dir: 'images/icons/silk', file: 'world_delete.png'),
                "linkAction" : "${request.contextPath}/user/disableLocalizationMode",
                "linkReactIcon" : "localization-mode",
            ]
        }
        else {

            localizationMode = [
                    "label"     : messageSource.getMessage('localization.enable.label', emptyArgs, 'Enable translation mode', currentLocale),
                    "linkIcon"  : resource(dir: 'images/icons/silk', file: 'world_add.png'),
                    "linkAction": "${request.contextPath}/user/enableLocalizationMode",
                    "linkReactIcon" : "localization-mode",
            ]
        }
        List<Map> menuItems = [
            [
                "label"      : messageSource.getMessage('default.edit.label',
                        [messageSource.getMessage('user.profile.label', emptyArgs, 'Profile', currentLocale)] as Object[],
                        'Enable translation mode', currentLocale),
                "linkIcon"   : resource(dir: 'images/icons/silk', file: 'user.png'),
                "linkAction" : "${request.contextPath}/user/edit/${session?.user?.id}",
                "linkReactIcon" : "profile",
            ],
            localizationMode,
            [
                "label"      : messageSource.getMessage('cache.flush.label', emptyArgs, 'Refresh caches', currentLocale),
                "linkIcon"   : resource(dir: 'images/icons/silk', file: 'database_wrench.png'),
                "linkAction" : "${request.contextPath}/dashboard/flushCache",
                "linkReactIcon" : "flush-cache",
            ],
            [
                "label"      : messageSource.getMessage('default.logout.label', emptyArgs, 'Logout', currentLocale),
                "linkIcon"   : resource(dir: 'images/icons/silk', file: 'door.png'),
                "linkAction" : "${request.contextPath}/auth/logout",
                "linkReactIcon" : "logout",
            ]
        ]

        User user = User.get(session?.user?.id)
        Location location = Location.get(session.warehouse?.id)
        String highestRole = user.getHighestRole(location)
        List currentLocationRoles = user.getRolesByCurrentLocation(location)?.roleType*.name()
        boolean isSuperuser = userService.isSuperuser(session?.user)
        boolean isUserAdmin = userService.isUserAdmin(session?.user)
        boolean isUserApprover = userService.hasRolePurchaseApprover(session?.user)
        boolean isUserRequestApprover = userService.isUserInAllRoles(session?.user?.id, [RoleType.ROLE_REQUISITION_APPROVER], session.warehouse?.id)
        // TODO: investigate why in isUserManager method in userService there is Assistant role included
        ArrayList<RoleType> managerRoles = [RoleType.ROLE_SUPERUSER, RoleType.ROLE_ADMIN, RoleType.ROLE_MANAGER]
        boolean isUserManager = userService.getEffectiveRoles(user).any { managerRoles.contains(it.roleType) }
        def supportedActivities = location.supportedActivities ?: location.locationType.supportedActivities
        boolean isImpersonated = session.impersonateUserId ? true : false
        def buildNumber = gitProperties.shortCommitId
        def buildDate = grailsApplication.metadata.getProperty('build.time') ?: messageSource.getMessage('application.realTimeBuild.label', null, currentLocale)
        def branchName = ConfigHelper.getBranchName(gitProperties)
        def grailsVersion = grailsApplication.metadata.getProperty('info.app.grailsVersion')
        def appVersion = grailsApplication.metadata.getProperty('info.app.version')
        def environment = Environment.current
        def ipAddress = request?.getRemoteAddr()
        def hostname = session.hostname ?: "Unknown"
        def timezone = session?.timezone?.ID
        def isPaginated = grailsApplication.config.openboxes.api.pagination.enabled
        DateFormat dateFormat = new SimpleDateFormat("MM/DD/YYYY");
        String minValue = grailsApplication.getConfig().getProperty('openboxes.expirationDate.minValue')
        String minimumExpirationDate = dateFormat.format(new Date(minValue))
        def logoLabel = grailsApplication.config.openboxes.logo.label
        def pageSize = grailsApplication.config.openboxes.api.pagination.pageSize
        def logoUrl = location?.logo ? "${createLink(controller: 'location', action: 'viewLogo', id: location?.id)}" : grailsApplication.config.openboxes.logo.url
        def locales = grailsApplication.config.openboxes.locale.supportedLocales
        def browserConnectionTimeout = grailsApplication.config.openboxes.browser.connection.status.timeout
        def isAutosaveEnabled = grailsApplication.config.openboxes.client.autosave.enabled &&
                supportedActivities.contains(ActivityCode.AUTOSAVE.name())
        def defaultLocale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale ?: "en")
        def supportedLocales = locales.collect {
            def defaultName = LocalizationUtil.getLocale(it)?.getDisplayName(currentLocale ?: defaultLocale)
            def name =  "${warehouse.message(code: "locale.${it}.label", default: defaultName)}"
            [code: it, name: name]
        }
        String currencyCode = grailsApplication.config.openboxes.locale.defaultCurrencyCode
        String localizedHelpScoutKey = helpScoutService.localizedHelpScoutKey
        boolean isHelpScoutEnabled = grailsApplication.config.openboxes.helpscout.widget.enabled
        boolean localizationModeEnabled = session.useDebugLocale ?: false

        render([
            data: [
                user                 : user,
                location             : location,
                currentLocationRoles: currentLocationRoles,
                isSuperuser          : isSuperuser,
                isUserAdmin          : isUserAdmin,
                isUserApprover       : isUserApprover,
                isUserRequestApprover: isUserRequestApprover,
                isUserManager        : isUserManager,
                supportedActivities  : supportedActivities,
                isImpersonated       : isImpersonated,
                grailsVersion        : grailsVersion,
                appVersion           : appVersion,
                branchName           : branchName,
                buildNumber          : buildNumber,
                environment          : environment.name,
                buildDate            : buildDate,
                ipAddress            : ipAddress,
                hostname             : hostname,
                timezone             : timezone,
                minimumExpirationDate: minimumExpirationDate,
                activeLanguage       : currentLocale.toString(),
                activeLanguageTag    : currentLocale.toLanguageTag(),
                isPaginated          : isPaginated,
                logoLabel            : logoLabel,
                menuItems            : menuItems,
                highestRole          : highestRole,
                pageSize             : pageSize,
                logoUrl              : logoUrl,
                supportedLocales     : supportedLocales,
                currencyCode         : currencyCode,
                localizedHelpScoutKey: localizedHelpScoutKey,
                isHelpScoutEnabled   : isHelpScoutEnabled,
                localizationModeEnabled  : localizationModeEnabled,
                localizationModeLocale   : localizationModeLocale,
                displayDateFormat        : displayDateFormat,
                displayDateDefaultValue  : displayDateDefaultValue,
                notificationAutohideDelay: notificationAutohideDelay,
                browserConnectionTimeout : browserConnectionTimeout,
                isAutosaveEnabled        : isAutosaveEnabled
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

    def getSupportLinks() {
        def supportLinks = grailsApplication.config.openboxes.supportLinks

        render([data: supportLinks] as JSON)
    }

    def getResettingInstanceCommand() {
        def resettingInstanceCommand = grailsApplication.config.openboxes.resettingInstance.command

        render([data: resettingInstanceCommand] as JSON)
    }
}
