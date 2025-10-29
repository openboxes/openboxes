package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.core.PaginationParams

class ExpirationHistoryReportFilterCommand extends PaginationParams implements Validateable {
    Date dateFrom
    Date dateTo
}
