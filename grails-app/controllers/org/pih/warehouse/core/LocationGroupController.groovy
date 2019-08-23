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

class LocationGroupController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [locationGroupInstanceList: LocationGroup.list(params), locationGroupInstanceTotal: LocationGroup.count()]
    }

    def create = {
        def locationGroupInstance = new LocationGroup()
        locationGroupInstance.properties = params
        return [locationGroupInstance: locationGroupInstance]
    }

    def save = {
        def locationGroupInstance = new LocationGroup(params)
        if (locationGroupInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'locationGroup.label', default: 'LocationGroup'), locationGroupInstance.id])}"
            redirect(action: "list", id: locationGroupInstance.id)
        } else {
            render(view: "create", model: [locationGroupInstance: locationGroupInstance])
        }
    }

    def show = {
        def locationGroupInstance = LocationGroup.get(params.id)
        if (!locationGroupInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'locationGroup.label', default: 'LocationGroup'), params.id])}"
            redirect(action: "list")
        } else {
            [locationGroupInstance: locationGroupInstance]
        }
    }

    def edit = {
        def locationGroupInstance = LocationGroup.get(params.id)
        if (!locationGroupInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'locationGroup.label', default: 'LocationGroup'), params.id])}"
            redirect(action: "list")
        } else {
            return [locationGroupInstance: locationGroupInstance]
        }
    }

    def update = {
        def locationGroupInstance = LocationGroup.get(params.id)
        if (locationGroupInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (locationGroupInstance.version > version) {
                    locationGroupInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'locationGroup.label', default: 'LocationGroup')] as Object[], "Another user has updated this LocationGroup while you were editing")
                    render(view: "edit", model: [locationGroupInstance: locationGroupInstance])
                    return
                }
            }


            def address = Address.get(params.address.id)
            if (!address) {
                address = new Address(params.address)
            }
            address.save(flush: true)

            locationGroupInstance.properties = params
            if (address) {
                locationGroupInstance.address = address

            }
            if (!locationGroupInstance.hasErrors() && locationGroupInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'locationGroup.label', default: 'LocationGroup'), locationGroupInstance.id])}"
                redirect(action: "list", id: locationGroupInstance.id)
            } else {
                render(view: "edit", model: [locationGroupInstance: locationGroupInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'locationGroup.label', default: 'LocationGroup'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def locationGroupInstance = LocationGroup.get(params.id)
        if (locationGroupInstance) {
            try {
                locationGroupInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'locationGroup.label', default: 'LocationGroup'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'locationGroup.label', default: 'LocationGroup'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'locationGroup.label', default: 'LocationGroup'), params.id])}"
            redirect(action: "list")
        }
    }
}
