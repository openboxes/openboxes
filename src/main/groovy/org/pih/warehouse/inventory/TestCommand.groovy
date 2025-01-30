package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.api.PaginationCommand
import org.pih.warehouse.core.Location

class TestCommand extends PaginationCommand implements Validateable {

   Location facility

    static constraints = {
        facility(nullable: true)
    }
}
