package org.pih.warehouse.requisition

import org.springframework.context.ApplicationEvent

/**
 * Published whenever a requisition's status is actually persisted as changed - see Requisition#beforeUpdate
 * and Requisition#afterInsert, which detect the change via GORM dirty checking rather than by intercepting
 * assignment. Carries the requisition id rather than the instance because the event is published from inside
 * a flush; listeners re-load the requisition in whatever session/transaction they run in.
 */
class RequisitionStatusChangedEvent extends ApplicationEvent {

    final String requisitionId
    final RequisitionStatus oldStatus
    final RequisitionStatus newStatus

    RequisitionStatusChangedEvent(String requisitionId, RequisitionStatus oldStatus, RequisitionStatus newStatus) {
        super(requisitionId)
        this.requisitionId = requisitionId
        this.oldStatus = oldStatus
        this.newStatus = newStatus
    }

    @Override
    String toString() {
        return "RequisitionStatusChangedEvent[requisition=${requisitionId}, ${oldStatus} -> ${newStatus}]"
    }
}
