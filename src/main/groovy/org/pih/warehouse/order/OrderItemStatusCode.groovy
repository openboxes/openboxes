package org.pih.warehouse.order

enum OrderItemStatusCode {

    PENDING(1),
    STARTED(2),
    IN_PROGRESS(3),
    COMPLETED(4),   // Meaning depends on context i.e. picked, putaway, delivered, received
    CANCELED(5),
    BACKORDER(6)

    final Integer sortOrder

    OrderItemStatusCode(Integer sortOrder) {
        this.sortOrder = sortOrder
    }

    static list() {
        [PENDING, STARTED, IN_PROGRESS, COMPLETED, CANCELED, BACKORDER]
    }

}