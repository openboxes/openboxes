package org.pih.warehouse

class TransactionItem {

    Transaction transaction;	// Reference to the parent tranaction.
    String lotNumber		// Should be a real entity
    Product product		// Should actually be a batch or lot
    Integer quantity		// Convention: negative number means OUT, positive number means IN



    static constraints = {
    }
}
