package org.pih.warehouse.core

import grails.validation.Validateable

class PaginationParams implements Validateable {

    Integer max

    Integer offset

    static constraints = {
        offset min: 0, nullable: true
        max min: 0, nullable: true
    }
}
