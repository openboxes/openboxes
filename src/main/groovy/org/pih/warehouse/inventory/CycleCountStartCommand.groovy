package org.pih.warehouse.inventory

import grails.validation.Validateable

class CycleCountStartCommand implements Validateable {

    CycleCountRequest cycleCountRequest

    Integer countIndex

    static constraints = {
        countIndex(min: 0, max: 1, validator: { Integer countIndex, CycleCountStartCommand obj ->
            CycleCount cycleCount = obj.cycleCountRequest.cycleCount
            if (!cycleCount && countIndex != 0) {
                return ['mustEqualZero']
            }
            return true
        })
    }
}
