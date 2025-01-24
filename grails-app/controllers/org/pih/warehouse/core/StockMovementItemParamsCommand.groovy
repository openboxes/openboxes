package org.pih.warehouse.core

import grails.validation.Validateable

class StockMovementItemParamsCommand implements Validateable {

    String id

    Integer stepNumber

    Boolean refreshPicklistItems = true

    Boolean showDetails = false

    static constraints = {
        stepNumber nullable: true
        refreshPicklistItems nullable: true
        showDetails nullable: true
    }

}
