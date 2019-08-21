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

class PartyController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [partyInstanceList: Party.list(params), partyInstanceTotal: Party.count()]
    }

    def create = {
        def partyInstance = new Party()
        partyInstance.properties = params
        return [partyInstance: partyInstance]
    }

    def save = {
        def partyInstance = new Party(params)
        if (partyInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'party.label', default: 'Party'), partyInstance.id])}"
            redirect(action: "list", id: partyInstance.id)
        } else {
            render(view: "create", model: [partyInstance: partyInstance])
        }
    }

    def show = {
        def partyInstance = Party.get(params.id)
        if (!partyInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'party.label', default: 'Party'), params.id])}"
            redirect(action: "list")
        } else {
            [partyInstance: partyInstance]
        }
    }

    def edit = {
        def partyInstance = Party.get(params.id)
        if (!partyInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'party.label', default: 'Party'), params.id])}"
            redirect(action: "list")
        } else {
            return [partyInstance: partyInstance]
        }
    }

    def update = {
        def partyInstance = Party.get(params.id)
        if (partyInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (partyInstance.version > version) {

                    partyInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'party.label', default: 'Party')] as Object[], "Another user has updated this Party while you were editing")
                    render(view: "edit", model: [partyInstance: partyInstance])
                    return
                }
            }
            partyInstance.properties = params
            if (!partyInstance.hasErrors() && partyInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'party.label', default: 'Party'), partyInstance.id])}"
                redirect(action: "list", id: partyInstance.id)
            } else {
                render(view: "edit", model: [partyInstance: partyInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'party.label', default: 'Party'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def partyInstance = Party.get(params.id)
        if (partyInstance) {
            try {
                partyInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'party.label', default: 'Party'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'party.label', default: 'Party'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'party.label', default: 'Party'), params.id])}"
            redirect(action: "list")
        }
    }
}
