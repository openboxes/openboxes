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

import org.springframework.context.ApplicationEvent

/**
 * Published from Requisition#beforeInsert (the very first status assignment) and #beforeUpdate (via GORM's
 * isDirty('status')/getPersistentValue('status')) whenever requisition.status changes. Handled by
 * RequisitionStatusChangedEventService, which records the transition via
 * RequisitionEventManager#recordStatusChange - this is the single path through which requisition.events stays
 * in sync with requisition.status, replacing the previous approach of doing this from a custom status setter.
 */
class RequisitionStatusChangedEvent extends ApplicationEvent {
    RequisitionStatus oldStatus
    RequisitionStatus newStatus

    RequisitionStatusChangedEvent(Requisition requisition, RequisitionStatus oldStatus, RequisitionStatus newStatus) {
        super(requisition)
        this.oldStatus = oldStatus
        this.newStatus = newStatus
    }

    Requisition getRequisition() {
        return (Requisition) source
    }
}
