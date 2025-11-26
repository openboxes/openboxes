package org.pih.warehouse.inventory

import grails.validation.Validateable

class CycleCountRequestUpdateBulkCommand implements Validateable {
    List<CycleCountRequestUpdateCommand> commands = []
}
