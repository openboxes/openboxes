package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.springframework.web.context.request.RequestContextHolder

class CycleCountStartBatchCommand implements Validateable {

    List<CycleCountStartCommand> requests

    Location facility

    def beforeValidate() {
        String locationId = RequestContextHolder.getRequestAttributes().params?.facility
        facility = Location.read(locationId)
    }

    static constraints = {
        requests(validator: { List<CycleCountStartCommand> requests ->
            // Elements of a list are not validated by default, so proceed manual validation of every element in the list
            requests.each { CycleCountStartCommand command -> command.validate() }
            // If any of elements have validation errors, throw an exception
            if (requests.any { it.hasErrors() }) {
                return false
            }
        })
    }
}
