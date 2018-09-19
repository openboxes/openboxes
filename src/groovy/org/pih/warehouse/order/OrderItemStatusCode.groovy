package org.pih.warehouse.order

enum OrderItemStatusCode {

    PENDING(1),
    READY(2),
    COMPLETED(3),   // Meaning depends on context i.e. picked, putaway, delivered, received
    CANCELED(4),
    BACKORDER(5)


    final Integer sortOrder

    OrderItemStatusCode(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    static list() {
        [PENDING, READY, COMPLETED, CANCELED, BACKORDER]
    }

}