package org.pih.warehouse.receiving

import org.pih.warehouse.order.Order

class ReceiptOrderDto {
    String id

    static ReceiptOrderDto toDto(Order order) {
        if (!order) {
            return null
        }
        return new ReceiptOrderDto(id: order.id)
    }
}
