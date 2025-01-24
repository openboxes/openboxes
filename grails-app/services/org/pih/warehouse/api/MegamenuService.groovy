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

import grails.core.GrailsApplication
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User

class MegamenuService {

    def userService
    GrailsApplication grailsApplication
    def grailsLinkGenerator

    private getMessageTagLib() {
        return grailsApplication.mainContext.getBean('org.pih.warehouse.MessageTagLib')
    }

    Map buildAndTranslateSections(section, String key, User user, Location location) {
        def label = getMessageTagLib().message(code: section.label, default: section.defaultLabel)
        def translatedSection
        if (section.href) {
            translatedSection = [
                    id: key,
                    label: label,
                    href: grailsLinkGenerator.link(uri: section.href)
            ]
            return translatedSection
        } else if (section.subsections) {
            translatedSection = [
                    id: key,
                    label: label,
                    subsections: buildAndTranslateSubsections(section.subsections, user, location)
            ]
            return translatedSection
        } else if (section.menuItems) {
            translatedSection = [
                    id: key,
                    label: label,
                    menuItems: buildAndTranslateMenuItems(section.menuItems, user, location)
            ]
            return translatedSection
        }
        return [:]
    }

    List buildAndTranslateSubsections(List subsections, User user, Location location) {
        def builtSubsections = []
        subsections.each {
            def minRole = it.minimumRequiredRole
            if (it.enabled == false) {
                return
            }
            if (minRole && !userService.isUserInRole(user, minRole)) {
                return
            }
            def roles = it.supplementalRoles
            if (roles && !userService.hasAnyRoles(user, roles)) {
                return
            }
            ActivityCode[] activitiesAny = it.requiredActivitiesAny ?: []
            if (activitiesAny && !location.supportsAny(activitiesAny)) {
                return
            }

            ActivityCode[] activitiesAll = it.requiredActivitiesAll ?: []
            if (activitiesAll && !location.supportsAll(activitiesAll)) {
                return
            }
            def label = getMessageTagLib().message(code: it.label, default: it.defaultLabel)
            builtSubsections << [
                label: label,
                menuItems: buildAndTranslateMenuItems(it.menuItems, user, location)
            ]
        }
        return builtSubsections
    }

    List buildAndTranslateMenuItems(List menuItems, User user, Location location) {
        def builtMenuItems = []
        menuItems.each {
            if (it.enabled == false) {
                return
            }
            def minRole = it.minimumRequiredRole
            if (minRole && !userService.isUserInRole(user, minRole)) {
                return
            }
            def roles = it.supplementalRoles
            if (roles && !userService.hasAnyRoles(user, roles)) {
                return
            }
            ActivityCode[] activitiesAny = it.requiredActivitiesAny ?: []
            if (activitiesAny && !location.supportsAny(activitiesAny)) {
                return
            }

            ActivityCode[] activitiesAll = it.requiredActivitiesAll ?: []
            if (activitiesAll && !location.supportsAll(activitiesAll)) {
                return
            }
            def label = getMessageTagLib().message(code: it.label, default: it.defaultLabel)
            if (it.href) {
                builtMenuItems << [
                    label: label,
                    href: grailsLinkGenerator.link(uri: it.href)
                ]
            } else if (it.subsections) {
                builtMenuItems << [
                    label: label,
                    subsections: buildAndTranslateSubsections(it.subsections, user, location)
                ]
            }
        }
        return builtMenuItems
    }

    ArrayList buildAndTranslateMenu(Map menuConfig, User user, Location location) {
        def parsedMenuConfig = []
        menuConfig.each { key, value ->
            def minRole = value.minimumRequiredRole
            if (minRole && !userService.isUserInRole(user, minRole)) {
                return
            }
            def roles = value.supplementalRoles
            if (roles && !userService.hasAnyRoles(user, roles)) {
                return
            }
            ActivityCode[] activitiesAny = value.requiredActivitiesAny ?: []
            if (activitiesAny && !location.supportsAny(activitiesAny)) {
                return
            }

            ActivityCode[] activitiesAll = value.requiredActivitiesAll ?: []
            if (activitiesAll && !location.supportsAll(activitiesAll)) {
                return
            }
            if (value.enabled) {
                def translatedSections = buildAndTranslateSections(value, key, user, location)
                if (translatedSections) {
                    parsedMenuConfig << translatedSections
                }
            }
        }
        return parsedMenuConfig
    }
}
