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
import org.pih.warehouse.requisition.Requisition


class RequisitionStatusTransitionEventService {

    MailService mailService
    GrailsApplication grailsApplication


    void publishDefaultEmailNotifications(Requisition requisition, Collection receivers) {
        String redirectToRequestsList = "/openboxes/stockMovement/list?direction=OUTBOUND&sourceType=ELECTRONIC"
        String subject = "${requisition.requestNumber} ${requisition.name}"
        def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
        GString body = "${g.render(template: "/email/approvalsAlert", model: [requisition: requisition, redirectUrl: redirectToRequestsList])}"

        mailService.sendHtmlMail(subject, body, receivers)
    }
}
