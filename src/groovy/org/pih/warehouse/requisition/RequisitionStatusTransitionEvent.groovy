package org.pih.warehouse.requisition

import org.springframework.context.ApplicationEvent

class RequisitionStatusTransitionEvent extends ApplicationEvent {
    Requisition requisition

    RequisitionStatus oldStatus

    RequisitionStatus newStatus

    RequisitionStatusTransitionEvent(Requisition requisition, RequisitionStatus oldStatus, RequisitionStatus newStatus) {
        super(requisition)
        this.requisition = requisition
        this.oldStatus = oldStatus
        this.newStatus = newStatus
    }
}
