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

class MegamenuService {

    def grailsApplication

    private getMessageTagLib() {
        return grailsApplication.mainContext.getBean('org.pih.warehouse.MessageTagLib')
    }

    Map buildAndTranslateSections(section) {
        def label = getMessageTagLib().message(code: section.label, default: section.defaultLabel)
        def translatedSection
        if (section.href) {
            translatedSection = [
                    label: label,
                    href: section.href
            ]
            return translatedSection
        }
        if (section.subsections) {
            translatedSection = [
                    label: label,
                    subsections: buildAndTranslateSubsections(section.subsections)
            ]
            return translatedSection
        }
        if (section.menuItems) {
            translatedSection = [
                    label: label,
                    menuItems: buildAndTranslateMenuItems(section.menuItems)
            ]
            return translatedSection
        }
        return [:]
    }

    List buildAndTranslateSubsections(List subsections) {
        def builtSubsections = []
        subsections.each {
            def label = getMessageTagLib().message(code: it.label, default: it.defaultLabel)
            builtSubsections << [
                label: label,
                menuItems: buildAndTranslateMenuItems(it.menuItems)
            ]
        }
        return builtSubsections
    }

    List buildAndTranslateMenuItems(List menuItems) {
        def builtMenuItems = []
        menuItems.each {
            def label = getMessageTagLib().message(code: it.label, default: it.defaultLabel)
            if (it.href) {
                builtMenuItems << [
                    label: label,
                    href: it.href
                ]
            }
            if (it.subsections) {
                builtMenuItems << [
                    label: label,
                    subsections: buildAndTranslateSubsections(it.subsections)
                ]
            }
        }
        return builtMenuItems
    }

    List buildAndTranslateMenu(Map menuConfig) {
        def parsedMenuConfig = []
        menuConfig.each { key, value ->
            if (value.enabled) {
                parsedMenuConfig << buildAndTranslateSections(value)
            }
        }
        return parsedMenuConfig
    }
}
