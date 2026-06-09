package org.pih.warehouse.api.receiving.v2

import org.pih.warehouse.requisition.Requisition

class ReceiptRequisitionDto {
    String id

    static ReceiptRequisitionDto toDto(Requisition requisition) {
        if (!requisition) {
            return null
        }
        return new ReceiptRequisitionDto(id: requisition.id)
    }
}
