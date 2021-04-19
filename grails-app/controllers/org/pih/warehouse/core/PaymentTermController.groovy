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

class PaymentTermController {

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [paymentTerms: PaymentTerm.list(params), paymentTermsTotal: PaymentTerm.count()]
    }

    def create = {
        def paymentTerm = new PaymentTerm()
        paymentTerm.properties = params
        return [paymentTerm: paymentTerm]
    }

    def edit = {
        def paymentTerm = PaymentTerm.get(params.id)
        return [paymentTerm: paymentTerm]
    }

    def save = {
        def paymentTerm = PaymentTerm.get(params.id)
        if (paymentTerm) {
            paymentTerm.properties = params
            if (!paymentTerm.hasErrors() && paymentTerm.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'paymentTerm.label', default: 'Payment Term'), paymentTerm.id])}"
                redirect(action: "list")
            } else {
                render(view: "edit", model: [paymentTerm: paymentTerm])
            }
        } else {
            paymentTerm = new PaymentTerm(params)
            if (paymentTerm.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'paymentTerm.label', default: 'Payment Term'), paymentTerm.id])}"
                redirect(controller: "paymentTerm", action: "edit", id: paymentTerm?.id)
            } else {
                render(view: "create", model: [paymentTerm: paymentTerm])
            }
        }
    }

    def delete = {
        def paymentTerm = PaymentTerm.get(params.id)
        if (paymentTerm) {
            try {
                paymentTerm.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'paymentTerm.label', default: 'Payment Term'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'paymentTerm.label', default: 'Payment Term'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'paymentTerm.label', default: 'Payment Term'), params.id])}"
            redirect(action: "list")
        }
    }
}
