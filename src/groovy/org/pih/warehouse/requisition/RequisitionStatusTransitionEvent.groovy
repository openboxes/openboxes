package org.pih.warehouse.requisition

import org.springframework.context.ApplicationEvent

class RequisitionStatusTransitionEvent extends ApplicationEvent {
    Requisition requisition

    RequisitionStatus newStatus

    RequisitionStatusTransitionEvent(Requisition requisition, RequisitionStatus newStatus) {
        super(requisition)
        this.requisition = requisition
        this.newStatus = newStatus
    }
}
