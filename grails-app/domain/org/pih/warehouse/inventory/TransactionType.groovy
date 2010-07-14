package org.pih.warehouse.inventory;

class TransactionType {

    String name
    String description

    
    String toString() { return "$name"; }

    static constraints = {
	name(nullable:false)
	description(nullable:true)
    }



}
