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

class GlAccountTypeController {

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [glAccountTypes: GlAccountType.list(params), glAccountTypesTotal: GlAccountType.count()]
    }

    def create = {
        def glAccountType = new GlAccountType()
        glAccountType.properties = params
        return [glAccountType: glAccountType]
    }

    def edit = {
        def glAccountType = GlAccountType.get(params.id)
        return [glAccountType: glAccountType]
    }

    def save = {
        def glAccountType = new GlAccountType(params)
        if (glAccountType.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'glAccountType.label', default: 'GL Account Type'), glAccountType.id])}"
            redirect(controller: "glAccountType", action: "edit", id: glAccountType?.id)
        } else {
            render(view: "create", model: [glAccountType: glAccountType])
        }
    }

    def update = {
        def glAccountType = GlAccountType.get(params.id)
        if (glAccountType) {
            glAccountType.properties = params
            if (!glAccountType.hasErrors() && glAccountType.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'glAccountType.label', default: 'GL Account Type'), glAccountType.id])}"
                redirect(action: "list")
            } else {
                render(view: "edit", model: [glAccountType: glAccountType])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'glAccountType.label', default: 'GL Account Type'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def glAccountType = GlAccountType.get(params.id)
        if (glAccountType) {
            try {
                glAccountType.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'glAccountType.label', default: 'GL Account Type'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'glAccountType.label', default: 'GL Account Type'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'glAccountType.label', default: 'GL Account Type'), params.id])}"
            redirect(action: "list")
        }
    }
}
