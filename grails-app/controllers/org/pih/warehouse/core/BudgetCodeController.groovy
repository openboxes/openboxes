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

class BudgetCodeController {

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [budgetCodes: BudgetCode.list(params), budgetCodesTotal: BudgetCode.count()]
    }

    def create = {
        def budgetCode = new BudgetCode()
        budgetCode.properties = params
        return [budgetCode: budgetCode]
    }

    def edit = {
        def budgetCode = BudgetCode.get(params.id)
        def organization = budgetCode?.organization ? Organization.get(budgetCode.organization.id) : null
        return [budgetCode: budgetCode, organizationId: organization?.id]
    }

    def save = {
        def budgetCode = new BudgetCode(params)
        if (budgetCode.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'budgetCode.label', default: 'Budget Code'), budgetCode.id])}"
            redirect(controller: "budgetCode", action: "edit", id: budgetCode?.id)
        } else {
            render(view: "create", model: [budgetCode: budgetCode])
        }
    }

    def update = {
        def budgetCode = BudgetCode.get(params.id)
        if (budgetCode) {
            budgetCode.properties = params
            if (!budgetCode.hasErrors() && budgetCode.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'budgetCode.label', default: 'Budget Code'), budgetCode.id])}"
                redirect(action: "list")
            } else {
                def organization = budgetCode?.organization ? Organization.get(budgetCode.organization.id) : null
                render(view: "edit", model: [budgetCode: budgetCode, organizationId: organization?.id])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'budgetCode.label', default: 'Budget Code'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def budgetCode = BudgetCode.get(params.id)
        if (budgetCode) {
            try {
                budgetCode.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'budgetCode.label', default: 'Budget Code'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'budgetCode.label', default: 'Budget Code'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'budgetCode.label', default: 'Budget Code'), params.id])}"
            redirect(action: "list")
        }
    }
}
