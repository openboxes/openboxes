package org.pih.warehouse.inventory

enum ExpirationFilter {
    INCLUDE_EXPIRED_STOCK(null),
    REMOVE_EXPIRED_STOCK(null),
    EXPIRING_WITHIN_MONTH(30),
    EXPIRING_WITHIN_QUARTER(90),
    EXPIRING_WITHIN_HALF_YEAR(180),
    EXPIRING_WITHIN_YEAR(365)

    Integer days

    private ExpirationFilter(Integer days) {
        this.days = days
    }
}
