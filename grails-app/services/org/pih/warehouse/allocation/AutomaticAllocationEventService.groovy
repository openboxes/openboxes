package org.pih.warehouse.allocation

import grails.core.GrailsApplication
import grails.util.Holders
import org.pih.warehouse.jobs.AutomaticAllocationJob
import org.pih.warehouse.requisition.Requisition
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

class AutomaticAllocationEventService {

    GrailsApplication grailsApplication

    // The event is published from Requisition.afterInsert(), which fires during the flush while the creating
    // transaction is still open. Handling it synchronously (the previous ApplicationListener) scheduled the
    // allocation job before that transaction committed, so the job - which runs in a separate transaction after a
    // short delay - frequently queried for the requisition before it was committed and logged
    // "Requisition not found, skipping" (OBLS-821). Reacting AFTER_COMMIT guarantees the requisition has been
    // committed and is visible before the job is even scheduled. fallbackExecution = true keeps the listener working
    // if the event is ever published outside of a transaction.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void onAutomaticAllocationEvent(AutomaticAllocationEvent event) {
        if (!Holders.config.openboxes.jobs.automaticAllocationJob.enabled) {
            log.info"Automatic allocation job is disabled"
            return
        }

        log.info "Application event $event has been published! " + event.properties

        // The event fires after commit and on a thread that no longer has an active transaction/session, so open one
        // explicitly to read the (now committed) requisition.
        Requisition.withNewTransaction {
            Requisition requisition = Requisition.get(event.source)
            if (!requisition) {
                log.warn "Requisition with id ${event.source} not found, cannot apply automatic allocation"
                return
            }

            if (requisition.autoAllocationEnabled) {
                def delayInMilliseconds =
                        Integer.valueOf(grailsApplication.config.openboxes.jobs.automaticAllocationJob.delayInMilliseconds) ?: 0
                Date runAt = new Date(System.currentTimeMillis() + delayInMilliseconds)
                log.info "Triggering automatic allocation job with ${delayInMilliseconds} ms delay"
                AutomaticAllocationJob.schedule(runAt, [requisitionId: event.source])
            }
        }
    }
}
