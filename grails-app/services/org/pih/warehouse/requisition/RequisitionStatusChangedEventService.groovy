/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.requisition

import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Records every requisition status transition (see RequisitionEventManager#recordStatusChange) so
 * requisition.events stays in sync with requisition.status and can never drift from it. Triggered from
 * Requisition#beforeInsert/#beforeUpdate via RequisitionStatusChangedEvent (GORM dirty-checking, for updates)
 * rather than from a custom status setter, so this fires exactly once per persisted change.
 */
class RequisitionStatusChangedEventService {

    RequisitionEventManager requisitionEventManager

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void onRequisitionStatusChanged(RequisitionStatusChangedEvent event) {
        // The original transaction/session has already committed and closed by AFTER_COMMIT time, but the
        // thread may still have that (now inert) session bound - withNewSession forces a genuinely fresh
        // Hibernate session/transaction rather than silently reusing the stale, already-committed one (which
        // fails with "no transaction is in progress" on flush).
        Requisition.withNewSession {
            Requisition.withTransaction {
                Requisition requisition = Requisition.get(event.requisition.id)
                requisitionEventManager.recordStatusChange(requisition, event.oldStatus, event.newStatus, requisition.origin)
            }
        }
    }
}
