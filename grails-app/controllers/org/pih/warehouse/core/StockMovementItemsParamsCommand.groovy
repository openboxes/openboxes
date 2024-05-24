package org.pih.warehouse.core

import grails.validation.Validateable
import org.pih.warehouse.api.PaginationCommand

class StockMovementItemsParamsCommand extends PaginationCommand implements Validateable {

    String id

    Integer stepNumber

    Boolean refreshPicklistItems = true

    static constraints = {
        stepNumber nullable: true
        refreshPicklistItems nullable: true
    }

}
