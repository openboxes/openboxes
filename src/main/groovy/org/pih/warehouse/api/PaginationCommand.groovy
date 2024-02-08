package org.pih.warehouse.api

import grails.validation.Validateable

class PaginationCommand implements Validateable {

    Integer offset

    Integer max

    static constraints = {
        offset min: 0, nullable: true
        max min: 0, nullable: true
    }


    Integer getMax() {
        return Math.min(max ?: 10, 100)
    }

    Integer getOffset() {
        return Math.max(offset ?: 0, 0)
    }

    Map<String, Integer> getPaginationParams() {
        return [
            max: getMax(),
            offset: getOffset(),
        ]
    }
}
