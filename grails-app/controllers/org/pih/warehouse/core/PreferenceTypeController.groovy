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

class PreferenceTypeController {

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [preferenceTypes: PreferenceType.list(params), preferenceTypesTotal: PreferenceType.count()]
    }

    def create = {
        def preferenceType = new PreferenceType()
        preferenceType.properties = params
        return [preferenceType: preferenceType]
    }

    def edit = {
        def preferenceType = PreferenceType.get(params.id)
        return [preferenceType: preferenceType]
    }

    def save = {
        def preferenceType = new PreferenceType(params)
        if (preferenceType.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'preferenceType.label', default: 'Preference Type'), preferenceType.id])}"
            redirect(controller: "preferenceType", action: "edit", id: preferenceType?.id)
        } else {
            render(view: "create", model: [preferenceType: preferenceType])
        }
    }

    def update = {
        def preferenceType = PreferenceType.get(params.id)
        if (preferenceType) {
            preferenceType.properties = params
            if (!preferenceType.hasErrors() && preferenceType.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'preferenceType.label', default: 'Preference Type'), preferenceType.id])}"
                redirect(action: "list")
            } else {
                render(view: "edit", model: [preferenceType: preferenceType])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'preferenceType.label', default: 'Preference Type'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def preferenceType = PreferenceType.get(params.id)
        if (preferenceType) {
            try {
                preferenceType.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'preferenceType.label', default: 'Preference Type'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'preferenceType.label', default: 'Preference Type'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'preferenceType.label', default: 'Preference Type'), params.id])}"
            redirect(action: "list")
        }
    }
}
