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

class GlAccountController {

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [glAccounts: GlAccount.list(params), glAccountsTotal: GlAccount.count()]
    }

    def create = {
        def glAccount = new GlAccount()
        glAccount.properties = params
        return [glAccount: glAccount]
    }

    def edit = {
        def glAccount = GlAccount.get(params.id)
        def glAccountType = glAccount?.glAccountType ? GlAccountType.get(glAccount.glAccountType.id) : null
        return [glAccount: glAccount, glAccountTypeId: glAccountType?.id]
    }

    def save = {
        def glAccount = new GlAccount(params)
        if (glAccount.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'glAccount.label', default: 'GL Account'), glAccount.id])}"
            redirect(controller: "glAccount", action: "edit", id: glAccount?.id)
        } else {
            render(view: "create", model: [glAccount: glAccount])
        }
    }

    def update = {
        def glAccount = GlAccount.get(params.id)
        if (glAccount) {
            glAccount.properties = params
            if (!glAccount.hasErrors() && glAccount.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'glAccount.label', default: 'GL Account'), glAccount.id])}"
                redirect(action: "list")
            } else {
                def glAccountType = glAccount?.glAccountType ? GlAccountType.get(glAccount?.glAccountType?.id) : null
                render(view: "edit", id: glAccount.id, model: [glAccount: glAccount, glAccountTypeId: glAccountType?.id])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'glAccount.label', default: 'GL Account'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def glAccount = GlAccount.get(params.id)
        if (glAccount) {
            try {
                glAccount.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'glAccount.label', default: 'GL Account'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'glAccount.label', default: 'GL Account'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'glAccount.label', default: 'GL Account'), params.id])}"
            redirect(action: "list")
        }
    }
}
