package org.pih.warehouse.requisition

import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Records the requisition transition history (see {@link RequisitionEventManager}) for every persisted status
 * change.
 *
 * Runs in the BEFORE_COMMIT phase rather than on the {@link RequisitionStatusChangedEvent} synchronously:
 * the event is published from inside a GORM flush, and writing the Event through requisition.addToEvents()
 * at that point would mutate a collection Hibernate is in the middle of flushing. BEFORE_COMMIT runs once
 * the flush has completed but while the original transaction is still open, so the history row stays atomic
 * with the status change it describes.
 */
class RequisitionStatusChangedEventService {

    RequisitionEventManager requisitionEventManager

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    void onRequisitionStatusChanged(RequisitionStatusChangedEvent event) {
        Requisition requisition = Requisition.get(event.requisitionId)
        if (!requisition) {
            log.warn "Requisition with id ${event.requisitionId} not found, cannot record status change ${event}"
            return
        }

        requisitionEventManager.recordStatusChange(
                requisition, event.oldStatus, event.newStatus, requisition.origin)
    }
}
