package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.core.Location

class CycleCountStartRecountBatchCommand implements Validateable {

    List<CycleCountStartRecountCommand> cycleCounts

    Location facility

    def beforeValidate() {
        String locationId = RequestContextHolder.getRequestAttributes().params?.facility
        facility = Location.read(locationId)
    }

    static constraints = {
        cycleCounts(validator: { List<CycleCountStartRecountCommand> cycleCounts ->
            // Validate every element of the batch
            cycleCounts.each { CycleCountStartRecountCommand command -> command.validate() }
            if (cycleCounts.any { it.hasErrors() }) {
                return false
            }
        })
    }
}
