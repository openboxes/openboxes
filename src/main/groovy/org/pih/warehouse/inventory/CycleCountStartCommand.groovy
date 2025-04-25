package org.pih.warehouse.inventory

import grails.validation.Validateable

class CycleCountStartCommand implements Validateable {

    CycleCountRequest cycleCountRequest

    static constraints = {
        cycleCountRequest(validator: { CycleCountRequest cycleCountRequest ->
            CycleCount cycleCount = cycleCountRequest.cycleCount
            if (!cycleCount) {
                // When first starting a count, the cycle count object won't exist yet, so this is valid.
                return true
            }
            if (!cycleCount.status.isCounting()) {
                return ['invalidCycleCountStatus', cycleCount.status]
            }
        })
    }
}
