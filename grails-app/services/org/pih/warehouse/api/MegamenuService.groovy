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

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User

class MegamenuService {

    def userService
    def grailsApplication

    private getMessageTagLib() {
        return grailsApplication.mainContext.getBean('org.pih.warehouse.MessageTagLib')
    }

    Map buildAndTranslateSections(section, User user, Location location) {
        def label = getMessageTagLib().message(code: section.label, default: section.defaultLabel)
        def translatedSection
        if (section.href) {
            translatedSection = [
                    label: label,
                    href: section.href
            ]
            return translatedSection
        } else if (section.subsections) {
            translatedSection = [
                    label: label,
                    subsections: buildAndTranslateSubsections(section.subsections, user, location)
            ]
            return translatedSection
        } else if (section.menuItems) {
            translatedSection = [
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
            def roles = it.requiredRole
            if (roles && !userService.hasAnyRoles(user, roles)) {
                return
            }
            ActivityCode[] activities = it.requiredActivities ?: []
            if (activities && !location.supportsAny(activities)) {
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
            def roles = it.requiredRole
            if (roles && !userService.hasAnyRoles(user, roles)) {
                return
            }
            ActivityCode[] activities = it.requiredActivities ?: []
            if (activities && !location.supportsAny(activities)) {
                return
            }
            def label = getMessageTagLib().message(code: it.label, default: it.defaultLabel)
            if (it.href) {
                builtMenuItems << [
                    label: label,
                    href: it.href
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
            def roles = value.requiredRole
            if (roles && !userService.hasAnyRoles(user, roles)) {
                return
            }
            ActivityCode[] activities = value.requiredActivities ?: []
            if (activities && !location.supportsAny(activities)) {
                return
            }
            if (value.enabled) {
                parsedMenuConfig << buildAndTranslateSections(value, user, location)
            }
        }
        return parsedMenuConfig
    }
}
