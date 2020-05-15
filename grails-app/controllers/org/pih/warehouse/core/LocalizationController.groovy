/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import org.springframework.web.multipart.MultipartFile

class LocalizationController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST", upload: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        def localizationInstanceList
        def localizationInstanceTotal

        def defaultLocale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
        def currentLocale = session?.user?.locale ?: session.locale ?: defaultLocale
        params.locale = params.locale ?: currentLocale?.language

        localizationInstanceList = Localization.createCriteria().list(params) {
            if (params.locale) {
                eq("locale", params.locale)
            }
            if (params.q) {
                or {
                    ilike("code", params.q + "%")
                    ilike("text", "%" + params.q + "%")
                }
            }
        }
        localizationInstanceTotal = localizationInstanceList.totalCount

        [localizationInstanceList: localizationInstanceList, localizationInstanceTotal: localizationInstanceTotal]
    }

    def create = {
        def localizationInstance = new Localization()
        localizationInstance.properties = params
        return [localizationInstance: localizationInstance]
    }

    def save = {

        log.info "save localization: " + params

        def localizationInstance = Localization.get(params.id)
        if (!localizationInstance) {
            localizationInstance = new Localization(params)
        } else {

            if (params.version) {
                def version = params.version.toLong()
                if (localizationInstance.version > version) {

                    localizationInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'localization.label', default: 'Localization')] as Object[], "Another user has updated this Localization while you were editing")
                    render(view: "edit", model: [localizationInstance: localizationInstance])
                    return
                }
            }
            localizationInstance.properties = params
        }

        if (!localizationInstance.hasErrors() && localizationInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.saved.message', args: [warehouse.message(code: 'localization.label', default: 'Localization'), localizationInstance.id])}"
            redirect(action: "list", id: localizationInstance.id)
        } else {
            if (localizationInstance?.id) {
                render(view: "edit", model: [localizationInstance: localizationInstance])
            } else {
                render(view: "create", model: [localizationInstance: localizationInstance])
            }
        }
    }

    def show = {
        def localizationInstance = Localization.get(params.id)
        if (!localizationInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'localization.label', default: 'Localization'), params.id])}"
            redirect(action: "list")
        } else {
            [localizationInstance: localizationInstance]
        }
    }

    def edit = {
        def localizationInstance = Localization.get(params.id)
        if (!localizationInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'localization.label', default: 'Localization'), params.id])}"
            redirect(action: "list")
        } else {
            return [localizationInstance: localizationInstance]
        }
    }

    def update = {
        def localizationInstance = Localization.get(params.id)
        if (localizationInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (localizationInstance.version > version) {

                    localizationInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'localization.label', default: 'Localization')] as Object[], "Another user has updated this Localization while you were editing")
                    render(view: "edit", model: [localizationInstance: localizationInstance])
                    return
                }
            }
            localizationInstance.properties = params
            if (!localizationInstance.hasErrors() && localizationInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'localization.label', default: 'Localization'), localizationInstance.id])}"
                redirect(action: "list", id: localizationInstance.id)
            } else {
                render(view: "edit", model: [localizationInstance: localizationInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'localization.label', default: 'Localization'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def localizationInstance = Localization.get(params.id)
        if (localizationInstance) {
            try {
                localizationInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'localization.label', default: 'Localization'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'localization.label', default: 'Localization'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'localization.label', default: 'Localization'), params.id])}"            
            redirect(action: "list")
        }
    }


    def export = {
        log.info("Locale: " + session.user.locale)
        Locale locale = params.locale ? new Locale(params.locale) : session.user.locale
        def filename = locale.language == 'en' ? "messages.properties" : "messages_${locale.language}.properties"
        def localizationInstanceList = Localization.findAllByLocale(locale.language)
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
        response.contentType = "text/plan"
        String output = localizationInstanceList.sort { it.code }.collect {
            it.code + " = " + it?.text?.trim()
        }.join("\n")
        output = "# ${filename} for ${locale.displayName}\n" +
                "# Exported ${new Date()}\n" + output
        render output
    }

    def upload = { LocalizationCommand command ->

        try {

            if (command.messageProperties.empty) {
                command.errors.rejectValue("messageProperties", "default.invalid.file.message")
            }

            if (command.validate() && !command.hasErrors()) {
                Properties properties = new Properties()
                properties.load(command.messageProperties.inputStream)
                properties.stringPropertyNames().each { String property ->
                    String text = properties.getProperty(property)
                    log.info "Property " + property + " = " + text
                    Localization localization = Localization.findByCodeAndLocale(property, command.locale.language)
                    if (!localization) {
                        localization = new Localization(code: property, locale: command.locale.language, text: text)
                    }
                    localization.text = text
                    localization.save(flush: true)

                }
                flash.message = "${warehouse.message(code: 'default.uploaded.message', args: [warehouse.message(code: 'localizations.label')])}"
            } else {
                chain(action: "list", model: [command: command])
                return
            }

        } catch (Exception e) {
            log.error("Failed to import message.properties due to the following error: " + e.message, e)
            flash.message = "Failed to import message.properties due to the following error: " + e.message
        }
        redirect(action: "list")
    }

}

class LocalizationCommand {

    Locale locale
    MultipartFile messageProperties


    static constraints = {
        locale(nullable: false)
        messageProperties(nullable: false)
    }
}
