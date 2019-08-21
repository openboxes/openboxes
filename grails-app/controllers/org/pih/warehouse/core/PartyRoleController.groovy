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

class PartyRoleController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [partyRoleInstanceList: PartyRole.list(params), partyRoleInstanceTotal: PartyRole.count()]
    }

    def create = {
        def partyRoleInstance = new PartyRole()
        partyRoleInstance.properties = params
        return [partyRoleInstance: partyRoleInstance]
    }

    def save = {
        def partyRoleInstance = new PartyRole(params)
        if (partyRoleInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'partyRole.label', default: 'PartyRole'), partyRoleInstance.id])}"
            redirect(action: "list", id: partyRoleInstance.id)
        } else {
            render(view: "create", model: [partyRoleInstance: partyRoleInstance])
        }
    }

    def show = {
        def partyRoleInstance = PartyRole.get(params.id)
        if (!partyRoleInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'partyRole.label', default: 'PartyRole'), params.id])}"
            redirect(action: "list")
        } else {
            [partyRoleInstance: partyRoleInstance]
        }
    }

    def edit = {
        def partyRoleInstance = PartyRole.get(params.id)
        if (!partyRoleInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'partyRole.label', default: 'PartyRole'), params.id])}"
            redirect(action: "list")
        } else {
            return [partyRoleInstance: partyRoleInstance]
        }
    }

    def update = {
        def partyRoleInstance = PartyRole.get(params.id)
        if (partyRoleInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (partyRoleInstance.version > version) {

                    partyRoleInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'partyRole.label', default: 'PartyRole')] as Object[], "Another user has updated this PartyRole while you were editing")
                    render(view: "edit", model: [partyRoleInstance: partyRoleInstance])
                    return
                }
            }
            partyRoleInstance.properties = params
            if (!partyRoleInstance.hasErrors() && partyRoleInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'partyRole.label', default: 'PartyRole'), partyRoleInstance.id])}"
                redirect(action: "list", id: partyRoleInstance.id)
            } else {
                render(view: "edit", model: [partyRoleInstance: partyRoleInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'partyRole.label', default: 'PartyRole'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def partyRoleInstance = PartyRole.get(params.id)
        if (partyRoleInstance) {
            try {
                partyRoleInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'partyRole.label', default: 'PartyRole'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'partyRole.label', default: 'PartyRole'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'partyRole.label', default: 'PartyRole'), params.id])}"
            redirect(action: "list")
        }
    }
}
