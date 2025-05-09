package org.pih.warehouse.inventory

import grails.validation.Validateable

class CycleCountUpdateItemBatchCommand implements Validateable {

    List<CycleCountUpdateItemCommand> itemsToUpdate

    static constraints = {
        itemsToUpdate(validator: { List<CycleCountUpdateItemCommand> items ->
            items.each { it.validate() }
            if (items.any { it.hasErrors() }) {
                return false
            }
        })
    }
}
