package org.pih.warehouse.inventory

import grails.validation.Validateable

class CycleCountRequestUpdateBulkCommand implements Validateable {
    List<CycleCountRequestUpdateCommand> commands = []

    static constraints = {
        commands(validator: { List<CycleCountRequestUpdateCommand> commands ->
            // The individual commands are not automatically validated so we have to do it manually.
            commands.each { it.validate() }
            return !commands.any { it.hasErrors() }
        })
    }
}
