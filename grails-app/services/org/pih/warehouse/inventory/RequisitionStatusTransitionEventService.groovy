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

import org.pih.warehouse.report.NotificationService
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatusTransitionEvent
import org.springframework.context.ApplicationListener

class RequisitionStatusTransitionEventService implements ApplicationListener<RequisitionStatusTransitionEvent> {

    NotificationService notificationService

    void onApplicationEvent(RequisitionStatusTransitionEvent event) {
        // Fetch the Requisition again to avoid LazyInitializationException when building the email template
        // In some of the templates we access some deeply nested values like event comments and statuses
        // TODO in grails 3 create a @service method which would fetch all of the necessary data without the need to refetch Requisition
        Requisition requisition = Requisition.get(event.requisition.id)
        if (requisition.shouldSendApprovalNotification()) {
            notificationService.publishRequisitionStatusTransitionNotifications(requisition)
        }
    }
}
