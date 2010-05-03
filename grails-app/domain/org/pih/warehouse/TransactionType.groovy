package org.pih.warehouse

class TransactionType {

    String name
    String description

    static constraints = {
	name(nullable:false)
	description(nullable:true)
    }
}
