package org.pih.warehouse.inventory

enum ExpirationFilter {
    SUBTRACT_EXPIRED_STOCK(null),
    DO_NOT_SUBTRACT_EXPIRED_STOCK(null),
    SUBTRACT_EXPIRING_WITHIN_MONTH(30),
    SUBTRACT_EXPIRING_WITHIN_QUARTER(90),
    SUBTRACT_EXPIRING_WITHIN_HALF_YEAR(180),
    SUBTRACT_EXPIRING_WITHIN_YEAR(365)

    Integer days

    private ExpirationFilter(Integer days) {
        this.days = days
    }
}
