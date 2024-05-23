package org.pih.warehouse.core

import grails.validation.Validateable

class StockMovementItemParamsCommand implements Validateable {

    String id

    Integer stepNumber

    Boolean refresh = true

    Boolean showDetails = false

    static constraints = {
        stepNumber nullable: true
        refresh nullable: true
        showDetails nullable: true
    }

}
