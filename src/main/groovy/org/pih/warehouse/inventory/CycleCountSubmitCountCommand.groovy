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
        cycleCount = CycleCount.read(cycleCountId)
    }

    static constraints = {
        cycleCount(validator: { CycleCount obj ->
            List<CycleCountStatus> validStatuses = [
                    CycleCountStatus.COUNTING,
                    CycleCountStatus.INVESTIGATING,
                    CycleCountStatus.REQUESTED,
                    CycleCountStatus.COUNTED,
            ]
            if (!validStatuses.contains(obj?.status)) {
                return ['invalidStatus']
            }
        })
    }
}
