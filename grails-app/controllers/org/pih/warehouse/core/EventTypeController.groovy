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

class EventTypeController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [eventTypeInstanceList: EventType.list(params), eventTypeInstanceTotal: EventType.count()]
    }

    def create = {
        def eventTypeInstance = new EventType()
        eventTypeInstance.properties = params
        return [eventTypeInstance: eventTypeInstance]
    }

    def save = {
        def eventTypeInstance = new EventType(params)
        if (eventTypeInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'eventType.label', default: 'EventType'), eventTypeInstance.id])}"
            redirect(action: "list", id: eventTypeInstance.id)
        } else {
            render(view: "create", model: [eventTypeInstance: eventTypeInstance])
        }
    }

    def show = {
        def eventTypeInstance = EventType.get(params.id)
        if (!eventTypeInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'eventType.label', default: 'EventType'), params.id])}"
            redirect(action: "list")
        } else {
            [eventTypeInstance: eventTypeInstance]
        }
    }

    def edit = {
        def eventTypeInstance = EventType.get(params.id)
        if (!eventTypeInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'eventType.label', default: 'EventType'), params.id])}"
            redirect(action: "list")
        } else {
            return [eventTypeInstance: eventTypeInstance]
        }
    }

    def update = {
        def eventTypeInstance = EventType.get(params.id)
        if (eventTypeInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (eventTypeInstance.version > version) {

                    eventTypeInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'eventType.label', default: 'EventType')] as Object[], "Another user has updated this EventType while you were editing")
                    render(view: "edit", model: [eventTypeInstance: eventTypeInstance])
                    return
                }
            }
            eventTypeInstance.properties = params
            if (!eventTypeInstance.hasErrors() && eventTypeInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'eventType.label', default: 'EventType'), eventTypeInstance.id])}"
                redirect(action: "list", id: eventTypeInstance.id)
            } else {
                render(view: "edit", model: [eventTypeInstance: eventTypeInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'eventType.label', default: 'EventType'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def eventTypeInstance = EventType.get(params.id)
        if (eventTypeInstance) {
            try {
                eventTypeInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'eventType.label', default: 'EventType'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'eventType.label', default: 'EventType'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'eventType.label', default: 'EventType'), params.id])}"
            redirect(action: "list")
        }
    }
}
