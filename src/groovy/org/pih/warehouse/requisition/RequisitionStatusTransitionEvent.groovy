package org.pih.warehouse.requisition

import org.pih.warehouse.core.User
import org.springframework.context.ApplicationEvent

class RequisitionStatusTransitionEvent extends ApplicationEvent {
    Requisition requisition

    User createdBy

    RequisitionStatusTransitionEvent(Requisition requisition, User createdBy) {
        super(requisition)
        this.requisition = requisition
        this.createdBy = createdBy
    }
}
