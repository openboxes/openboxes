package org.pih.warehouse.inventory

import grails.validation.Validateable

class CycleCountStartRecountCommand implements Validateable {

    CycleCountRequest cycleCountRequest

    Integer countIndex

    static constraints = {
        cycleCountRequest(validator: { CycleCountRequest cycleCountRequest ->
            CycleCount cycleCount = cycleCountRequest.cycleCount
            if (!cycleCount) {
                return ['noCycleCountFound']
            }
            if (!cycleCount.status.isRecounting()) {
                return ['invalidCycleCountStatus', cycleCount.status]
            }
        })
        countIndex(min: 1, validator: { Integer countIndex ->
            if (countIndex != 1) {
                // We only allow for a single recount in the initial version of this feature so we know countIndex
                // should always be 1. In the future, we may want to support multiple recounts. If so, this validation
                // will need to change to countIndex == max(cycleCountRequest.cycleCount.cycleCountItems.countIndex) + 1
                return ['invalid', countIndex]
            }
            return true
        })
    }
}
