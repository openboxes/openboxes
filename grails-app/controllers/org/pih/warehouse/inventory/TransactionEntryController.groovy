/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.inventory



import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class TransactionEntryController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond TransactionEntry.list(params), model:[transactionEntryInstanceCount: TransactionEntry.count()]
    }

    def show(TransactionEntry transactionEntryInstance) {
        respond transactionEntryInstance
    }

    def create() {
        respond new TransactionEntry(params)
    }

    @Transactional
    def save(TransactionEntry transactionEntryInstance) {
        if (transactionEntryInstance == null) {
            notFound()
            return
        }

        if (transactionEntryInstance.hasErrors()) {
            respond transactionEntryInstance.errors, view:'create'
            return
        }

        transactionEntryInstance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'transactionEntry.label', default: 'TransactionEntry'), transactionEntryInstance.id])
                redirect transactionEntryInstance
            }
            '*' { respond transactionEntryInstance, [status: CREATED] }
        }
    }

    def edit(TransactionEntry transactionEntryInstance) {
        respond transactionEntryInstance
    }

    @Transactional
    def update(TransactionEntry transactionEntryInstance) {
        if (transactionEntryInstance == null) {
            notFound()
            return
        }

        if (transactionEntryInstance.hasErrors()) {
            respond transactionEntryInstance.errors, view:'edit'
            return
        }

        transactionEntryInstance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'TransactionEntry.label', default: 'TransactionEntry'), transactionEntryInstance.id])
                redirect transactionEntryInstance
            }
            '*'{ respond transactionEntryInstance, [status: OK] }
        }
    }

    @Transactional
    def delete(TransactionEntry transactionEntryInstance) {

        if (transactionEntryInstance == null) {
            notFound()
            return
        }

        transactionEntryInstance.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'TransactionEntry.label', default: 'TransactionEntry'), transactionEntryInstance.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'transactionEntry.label', default: 'TransactionEntry'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
