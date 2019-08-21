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

class TransactionEntryController {

    def scaffold = TransactionEntry

    def delete = {
        def transactionEntryInstance = TransactionEntry.get(params.id)
        if (!transactionEntryInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'transactionEntry.label'), params.id])}"
        } else {
            flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'transactionEntry.label'), params.id])}"
            transactionEntryInstance.transaction.removeFromTransactionEntries(transactionEntryInstance)
            transactionEntryInstance.delete()
        }
        redirect(action: "list")
    }
}
