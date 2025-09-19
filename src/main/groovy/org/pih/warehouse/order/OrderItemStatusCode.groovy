package org.pih.warehouse.order

enum OrderItemStatusCode {

    PENDING(1),
    IN_PROGRESS(2),
    IN_TRANSIT(3),
    COMPLETED(4),   // Meaning depends on context i.e. picked, putaway, delivered, received
    CANCELED(5),
    BACKORDER(6)

    final Integer sortOrder

    OrderItemStatusCode(Integer sortOrder) {
        this.sortOrder = sortOrder
    }

    static list() {
        [PENDING, COMPLETED, CANCELED, BACKORDER]
    }

}