package org.pih.warehouse

class TransactionEntry {

    // Core data elements
    Integer id
    //String lotNumber		// Should be a real entity
    Product product		// Should actually be a batch or lot
    Integer quantityChange	// Convention: negative number means OUT, positive number means IN
    Date confirmDate		// Confirmation date (of what receiving?)

    // Core associations
    Transaction transaction	// Reference to the parent tranaction.


    static belongsTo = [ Transaction ]

    static constraints = {
	

    }
}
