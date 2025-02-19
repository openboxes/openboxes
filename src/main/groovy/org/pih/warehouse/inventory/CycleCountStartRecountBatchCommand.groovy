package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.core.Location

class CycleCountStartRecountBatchCommand implements Validateable {

    List<CycleCountStartRecountCommand> requests

    Location facility

    def beforeValidate() {
        String locationId = RequestContextHolder.getRequestAttributes().params?.facility
        facility = Location.read(locationId)
    }

    static constraints = {
        requests(validator: { List<CycleCountStartRecountCommand> requests ->
            // Validate every element of the batch
            requests.each { CycleCountStartRecountCommand command -> command.validate() }
            if (requests.any { it.hasErrors() }) {
                return false
            }
        })
    }
}
