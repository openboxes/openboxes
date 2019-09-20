package org.pih.warehouse.order

enum OrderTypeCode {

    SALES_ORDER(1),
    PRODUCTION_ORDER(2),
    PURCHASE_ORDER(3),
    TRANSFER_ORDER(4),
    WORK_ORDER(5)

    final Integer sortOrder

    OrderTypeCode(Integer sortOrder) {
        this.sortOrder = sortOrder
    }

    static list() {
        [PRODUCTION_ORDER, PURCHASE_ORDER, SALES_ORDER, TRANSFER_ORDER, WORK_ORDER]
    }


}
