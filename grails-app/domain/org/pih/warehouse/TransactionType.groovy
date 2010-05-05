package org.pih.warehouse

class TransactionType {

    String name
    String description

    
    String toString() { return "$name"; }

    static constraints = {
	name(nullable:false)
	description(nullable:true)
    }



}
