/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.inventory

import org.pih.warehouse.core.Person
import org.pih.warehouse.report.NotificationService
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.requisition.RequisitionStatusTransitionEvent
import org.pih.warehouse.requisition.RequisitionStatus
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener

class RequisitionStatusTransitionEventService implements ApplicationListener<RequisitionStatusTransitionEvent> {

    RequisitionService requisitionService
    NotificationService notificationService


    void onApplicationEvent(RequisitionStatusTransitionEvent event) {
        if (event.newStatus == RequisitionStatus.PENDING_APPROVAL) {
            Requisition req = event.requisition
            List<Person> approvers = Person.findAllByIdInList(event.requisition.approvers.collect {it.id})
            notificationService.sendRequestPendingForApprovalNotification(req, approvers)
        }
        requisitionService.triggerRequisitionStatusTransition(event.requisition, event.newStatus, event.createdBy)
    }
}
