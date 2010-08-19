package org.pih.warehouse.inventory;

import org.pih.warehouse.product.Product;

class TransactionEntry {

    Product product					// Should actually be a batch or lot
    Integer quantityChange			// Convention: negative number means OUT, positive number means IN

    String toString() { return "$quantityChange $product"; }


    static belongsTo = [ transaction : Transaction ]

    static constraints = {
		product(nullable:true)
		quantityChange(nullable:true)	

    }
}
