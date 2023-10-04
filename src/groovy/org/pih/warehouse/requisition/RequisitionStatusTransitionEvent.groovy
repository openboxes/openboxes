package org.pih.warehouse.requisition

import org.springframework.context.ApplicationEvent

class RequisitionStatusTransitionEvent extends ApplicationEvent {
    Requisition requisition

    RequisitionStatus status

    RequisitionStatusTransitionEvent(Requisition requisition, RequisitionStatus status) {
        super(requisition)
        this.requisition = requisition
        this.status = status
    }
}
