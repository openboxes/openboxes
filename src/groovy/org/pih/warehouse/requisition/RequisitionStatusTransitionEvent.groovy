package org.pih.warehouse.requisition

import org.pih.warehouse.core.User
import org.springframework.context.ApplicationEvent

class RequisitionStatusTransitionEvent extends ApplicationEvent {
    Requisition requisition

    RequisitionStatus newStatus

    User createdBy

    RequisitionStatusTransitionEvent(Requisition requisition, RequisitionStatus newStatus, User createdBy) {
        super(requisition)
        this.requisition = requisition
        this.newStatus = newStatus
        this.createdBy = createdBy
    }
}
