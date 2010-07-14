package org.pih.warehouse.inventory;

import org.pih.warehouse.product.Product;

class TransactionEntry {

    // Core data elements
    //Integer id
    //String lotNumber		// Should be a real entity
    Product product		// Should actually be a batch or lot
    Integer quantityChange	// Convention: negative number means OUT, positive number means IN
    Date confirmDate		// Confirmation date (of what receiving?)

    // Core associations
    //Transaction transaction	// Reference to the parent tranaction.


    String toString() { return "$quantityChange units of $product on $confirmDate"; }


    static belongsTo = [ transaction : Transaction ]

    static constraints = {
	

    }
}
