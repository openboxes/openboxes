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

import grails.gorm.transactions.Transactional

@Transactional
class PersonController {
    def userService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]


    def redirect() {
        redirect(action: "show", id: params.id)
    }

    def index() {
        redirect(action: "list", params:params)
    }


    def list() {
        def personInstanceList = []
        def personInstanceTotal = 0

        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        if (params.q) {
            String[] terms = ["%" + params.q + "%"]
            personInstanceList = userService.findPersons(terms, params)
            personInstanceTotal = personInstanceList.totalCount

        } else {
            personInstanceList = Person.list(params)
            personInstanceTotal = Person.count()
        }

        [personInstanceList: personInstanceList, personInstanceTotal: personInstanceTotal]
    }

    def create() {
        def personInstance = new Person()
        personInstance.properties = params
        return [personInstance: personInstance]
    }

    def save() {
        def personInstance = new Person(params)
        if (personInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'person.label', default: 'Person'), personInstance.id])}"
            redirect(action: "list", id: personInstance.id)
        }
        else {
            render(view: "create", model: [personInstance: personInstance])
        }
    }

    def show() {
        def personInstance = Person.get(params.id)
        if (!personInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'person.label', default: 'Person'), params.id])}"
            redirect(action: "list")
        }
        else {
            [personInstance: personInstance]
        }
    }

    def edit() {
        def personInstance = Person.get(params.id)
        if (!personInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'person.label', default: 'Person'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [personInstance: personInstance]
        }
    }

    def update() {
        def personInstance = Person.get(params.id)
        if (personInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (personInstance.version > version) {

                    personInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'person.label', default: 'Person')] as Object[], "Another user has updated this Person while you were editing")
                    render(view: "edit", model: [personInstance: personInstance])
                    return
                }
            }
            personInstance.properties = params
            if (!personInstance.hasErrors() && personInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'person.label', default: 'Person'), personInstance.id])}"
                redirect(action: "list", id: personInstance.id)
            }
            else {
                render(view: "edit", model: [personInstance: personInstance])
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'person.label', default: 'Person'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete() {
        def personInstance = Person.get(params.id)
        if (personInstance) {
            try {
                personInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'person.label', default: 'Person'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'person.label', default: 'Person'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'person.label', default: 'Person'), params.id])}"
            redirect(action: "list")
        }
    }

    def convertPersonToUser() {
        userService.convertPersonToUser(params.id)
        redirect(controller: "user", action: "edit", id: params.id)
    }

    def convertUserToPerson() {
        userService.convertUserToPerson(params.id)
        redirect(controller: "person", action: "show", id: params.id)
    }

}
