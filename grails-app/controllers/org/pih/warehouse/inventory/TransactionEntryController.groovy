package org.pih.warehouse.inventory;

class TransactionEntryController {

	def scaffold = TransactionEntry

	def delete = { 
		def transactionEntryInstance = TransactionEntry.get(params.id)
		if (!transactionEntryInstance) { 
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'transactionEntry.label'), params.id])}"
		}
		else { 
			flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'transactionEntry.label'), params.id])}"
			transactionEntryInstance.transaction.removeFromTransactionEntries(transactionEntryInstance)
			transactionEntryInstance.delete()			
		}
		redirect(action: "list")
	}
	

	def edit = {
		def transactionEntryInstance = TransactionEntry.get(params.id)
		log.info "get transaction entry " + transactionEntryInstance
		if (!transactionEntryInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'transactionEntry.label'), params.id])}"
			redirect(action: "create")
			return;
		}
		[transactionEntryInstance: transactionEntryInstance]
	}
}
