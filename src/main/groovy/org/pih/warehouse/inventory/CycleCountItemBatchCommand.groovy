package org.pih.warehouse.inventory

import grails.validation.Validateable

class CycleCountItemBatchCommand implements Validateable {

    List<CycleCountItemCommand> itemsToCreate

    static constraints = {
        itemsToCreate(validator: { List<CycleCountItemCommand> items ->
            items.each { it.validate() }
            if (items.any { it.hasErrors() }) {
                return false
            }
        })
    }
}
