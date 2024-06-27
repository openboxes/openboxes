package org.pih.warehouse.core

import grails.validation.Validateable

abstract class PaginationParams implements Validateable {

    Integer max

    Integer offset

    Integer getMax() {
        return max
    }

    Integer getOffset() {
        return offset
    }

    static constraints = {
        offset min: 0, nullable: true
        max min: 0, nullable: true
    }
}
