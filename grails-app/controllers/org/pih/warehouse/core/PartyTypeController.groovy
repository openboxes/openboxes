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

class PartyTypeController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [partyTypeInstanceList: PartyType.list(params), partyTypeInstanceTotal: PartyType.count()]
    }

    def create = {
        def partyTypeInstance = new PartyType()
        partyTypeInstance.properties = params
        return [partyTypeInstance: partyTypeInstance]
    }

    def save = {
        def partyTypeInstance = new PartyType(params)
        if (partyTypeInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'partyType.label', default: 'PartyType'), partyTypeInstance.id])}"
            redirect(action: "list", id: partyTypeInstance.id)
        } else {
            render(view: "create", model: [partyTypeInstance: partyTypeInstance])
        }
    }

    def show = {
        def partyTypeInstance = PartyType.get(params.id)
        if (!partyTypeInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'partyType.label', default: 'PartyType'), params.id])}"
            redirect(action: "list")
        } else {
            [partyTypeInstance: partyTypeInstance]
        }
    }

    def edit = {
        def partyTypeInstance = PartyType.get(params.id)
        if (!partyTypeInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'partyType.label', default: 'PartyType'), params.id])}"
            redirect(action: "list")
        } else {
            return [partyTypeInstance: partyTypeInstance]
        }
    }

    def update = {
        def partyTypeInstance = PartyType.get(params.id)
        if (partyTypeInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (partyTypeInstance.version > version) {

                    partyTypeInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'partyType.label', default: 'PartyType')] as Object[], "Another user has updated this PartyType while you were editing")
                    render(view: "edit", model: [partyTypeInstance: partyTypeInstance])
                    return
                }
            }
            partyTypeInstance.properties = params
            if (!partyTypeInstance.hasErrors() && partyTypeInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'partyType.label', default: 'PartyType'), partyTypeInstance.id])}"
                redirect(action: "list", id: partyTypeInstance.id)
            } else {
                render(view: "edit", model: [partyTypeInstance: partyTypeInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'partyType.label', default: 'PartyType'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def partyTypeInstance = PartyType.get(params.id)
        if (partyTypeInstance) {
            try {
                partyTypeInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'partyType.label', default: 'PartyType'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'partyType.label', default: 'PartyType'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'partyType.label', default: 'PartyType'), params.id])}"
            redirect(action: "list")
        }
    }
}
