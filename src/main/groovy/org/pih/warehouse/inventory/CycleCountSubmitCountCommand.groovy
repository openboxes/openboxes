package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.springframework.web.context.request.RequestContextHolder

class CycleCountSubmitCountCommand implements Validateable {

    CycleCount cycleCount

    Location facility

    boolean refreshQuantityOnHand

    boolean failOnOutdatedQuantity

    boolean requireRecountOnDiscrepancy

    def beforeValidate() {
        String locationId = RequestContextHolder.getRequestAttributes().params?.facility
        String cycleCountId = RequestContextHolder.getRequestAttributes().params?.cycleCountId
        facility = Location.read(locationId)
        cycleCount = CycleCount.get(cycleCountId)
    }

    static constraints = {
        cycleCount(validator: { CycleCount obj ->
            // TODO: Fix persisting valid status for cycle count
            CycleCountStatus currentStatus = obj?.recomputeStatus()
            if (currentStatus != CycleCountStatus.COUNTING) {
                return ['invalidStatus']
            }
        })
    }
}
