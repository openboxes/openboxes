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

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.pih.warehouse.core.MailService
import org.pih.warehouse.core.Person
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.requisition.RequisitionStatusTransitionEvent
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener

class RequisitionStatusTransitionEventService implements ApplicationListener<RequisitionStatusTransitionEvent> {

    static transactional = true

    MailService mailService
    GrailsApplication grailsApplication
    RequisitionService requisitionService


    void publishDefaultEmailNotifications(Requisition requisition, List<Person> receivers) {
        def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
        String subject = "${requisition.requestNumber} ${requisition.name}"
        String redirectToRequestsList = "${g.createLink(uri: "/stockMovement/list?direction=OUTBOUND&sourceType=ELECTRONIC", absolute: true)}"
        GString body = "${g.render(template: "/email/approvalsAlert", model: [requisition: requisition, redirectUrl: redirectToRequestsList])}"

        receivers.each {receiver ->
            if (receiver.email) {
                mailService.sendHtmlMail(subject, body, receiver.email)
            }
        }
    }

    void onApplicationEvent(RequisitionStatusTransitionEvent event) {
        requisitionService.triggerRequisitionStatusTransition(event.requisition, event.newStatus)

    }
}
