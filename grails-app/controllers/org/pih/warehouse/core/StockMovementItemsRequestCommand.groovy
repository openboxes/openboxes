package org.pih.warehouse.core

import grails.validation.Validateable
import org.pih.warehouse.api.PaginationCommand

class StockMovementItemsRequestCommand extends PaginationCommand implements Validateable {

    String id

    Integer stepNumber

    Boolean createNew = true

    static constraints = {
        id nullable: false
        stepNumber nullable: true
        createNew nullable: true
    }

}
