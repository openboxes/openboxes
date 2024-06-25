package org.pih.warehouse.core

import grails.validation.Validateable
import org.pih.warehouse.api.PaginationCommand

class StockMovementItemsParamsCommand extends PaginationCommand implements Validateable {

    String id

    Integer stepNumber

    Boolean refreshPicklistItems = true

    @Override
    Integer getMax() {
        // Overriding getter for max value from pagination command to be able
        // to get all of the items, instead of just 10 (10 was a default value in pagination command)
        return super.originalMax
    }

    static constraints = {
        stepNumber nullable: true
        refreshPicklistItems nullable: true
    }

}
