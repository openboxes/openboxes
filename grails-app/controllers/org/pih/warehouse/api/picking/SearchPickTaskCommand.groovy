package org.pih.warehouse.api.picking

import grails.validation.Validateable
import org.pih.warehouse.core.Location

class SearchPickTaskCommand implements Validateable {

    Location facility
    String pickType
    Integer ordersCount

    static constraints = {
        facility nullable: false
        pickType nullable: true
        ordersCount nullable: true, min: 1
    }
}
