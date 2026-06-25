package org.pih.warehouse.allocation

import grails.util.Holders
import org.pih.warehouse.jobs.AutomaticAllocationJob
import org.pih.warehouse.requisition.Requisition
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

class AutomaticAllocationEventService {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void onAutomaticAllocationEvent(AutomaticAllocationEvent event) {
        if (!Holders.config.openboxes.jobs.automaticAllocationJob.enabled) {
            log.info "Automatic allocation job is disabled"
            return
        }

        log.info "Application event $event has been published! " + event.properties
        Requisition.withNewSession {
            Requisition requisition = Requisition.get(event.source)
            if (!requisition) {
                log.warn "Requisition with id ${event.source} not found, cannot apply automatic allocation"
                return
            }
            if (!requisition.isEligibleForAutomaticAllocation()) {
                return
            }
            log.info "Triggering automatic allocation job for requisition ${requisition.requestNumber} (${requisition.id})"
            AutomaticAllocationJob.triggerNow([requisitionId: requisition.id])
        }
    }
}
