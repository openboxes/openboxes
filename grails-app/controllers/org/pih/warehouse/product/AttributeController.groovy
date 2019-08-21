/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.product

class AttributeController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [attributeInstanceList: Attribute.list(params), attributeInstanceTotal: Attribute.count()]
    }

    def show = {
        def attributeInstance = Attribute.get(params.id)
        if (!attributeInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'attribute.label', default: 'Attribute'), params.id])}"
            redirect(action: "list")
        } else {
            [attributeInstance: attributeInstance]
        }
    }

    def create = {
        def attributeInstance = new Attribute()
        attributeInstance.properties = params
        render(view: "edit", model: [attributeInstance: attributeInstance])
    }

    def edit = {
        def attributeInstance = Attribute.get(params.id)
        if (!attributeInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'attribute.label', default: 'Attribute'), params.id])}"
            redirect(action: "list")
        } else {
            return [attributeInstance: attributeInstance]
        }
    }

    def save = {
        def attributeInstance = null
        if (params.id) {
            attributeInstance = Attribute.get(params.id)
            attributeInstance.properties = params
        } else {
            params.id = null
            attributeInstance = new Attribute(params)
        }
        if (params.version) {
            def version = params.version.toLong()
            if (attributeInstance.version > version) {
                attributeInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'attribute.label', default: 'Attribute')] as Object[], "Another user has updated this Attribute while you were editing")
                render(view: "edit", model: [attributeInstance: attributeInstance])
                return
            }
        }

        // TODO: Add some validation in here (or better yet in the service) to make sure we do not remove options that are in use
        attributeInstance.options = new ArrayList()
        params.option.each { o ->
            if (o) {
                attributeInstance.options.add(o)
            }
        }

        if (attributeInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.saved.message', args: [warehouse.message(code: 'attribute.label', default: 'Attribute'), attributeInstance.id])}"
            redirect(action: "edit", id: attributeInstance.id)
        } else {
            render(view: (params.id ? "edit" : "create"), model: [attributeInstance: attributeInstance])
        }
    }

    def delete = {
        def attributeInstance = Attribute.get(params.id)
        if (attributeInstance) {
            try {
                attributeInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'attribute.label', default: 'Attribute'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'attribute.label', default: 'Attribute'), params.id])}"
                redirect(action: "edit", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'attribute.label', default: 'Attribute'), params.id])}"
            redirect(action: "list")
        }
    }
}
