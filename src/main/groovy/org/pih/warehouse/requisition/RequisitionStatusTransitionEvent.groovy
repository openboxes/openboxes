package org.pih.warehouse.requisition

import org.springframework.context.ApplicationEvent

class RequisitionStatusTransitionEvent extends ApplicationEvent {
    Requisition requisition

    RequisitionStatusTransitionEvent(Requisition requisition) {
        super(requisition)
        this.requisition = requisition
    }
}
