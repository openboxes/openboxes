package org.pih.warehouse.order

enum OrderItemStatusCode {

    PENDING(1),
    COMPLETED(2),   // Meaning depends on context i.e. picked, putaway, delivered, received
    CANCELED(3),
    BACKORDER(4)


    final Integer sortOrder

    OrderItemStatusCode(Integer sortOrder) {
        this.sortOrder = sortOrder
    }

    static list() {
        [PENDING, COMPLETED, CANCELED, BACKORDER]
    }

}