package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.PaginationParams

class ExpirationHistoryReportFilterCommand implements Validateable {
    // Pagination params have to be preinitialized to be properly bound when sent as query string
    PaginationParams paginationParams = new PaginationParams()
    Date startDate
    Date endDate
}
