package org.pih.warehouse.report

import grails.validation.Validateable
import java.time.LocalDate

import org.pih.warehouse.api.PaginationCommand
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class CycleCountReportCommand extends PaginationCommand implements Validateable {

    Location facility
    List<Product> products
    LocalDate startDate
    LocalDate endDate

    static constraints = {
        facility(nullable: false)
        products(nullable: true)
        startDate(nullable: true)
        endDate(nullable: true)
    }

}
