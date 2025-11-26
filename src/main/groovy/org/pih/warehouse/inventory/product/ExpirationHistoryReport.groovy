package org.pih.warehouse.inventory.product

import org.pih.warehouse.PaginatedList
import org.pih.warehouse.inventory.ExpirationHistoryReportRow

class ExpirationHistoryReport {
    PaginatedList<ExpirationHistoryReportRow> rows
    Integer totalQuantityLostToExpiry
    BigDecimal totalValueLostToExpiry
}
