/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.report

import grails.core.GrailsApplication
import grails.util.GrailsWebMockUtil
import org.apache.commons.mail.EmailException
import org.apache.commons.validator.EmailValidator
import org.grails.plugins.web.taglib.RenderTagLib
import grails.web.context.ServletContextHolder
import org.grails.web.errors.GrailsWrappedRuntimeException
import org.pih.warehouse.api.PartialReceipt
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.MailService
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.support.WebApplicationContextUtils

class NotificationService {

    def dataService
    def userService
    MailService mailService
    GrailsApplication grailsApplication
    def messageSource

    def renderTemplate(String template, Map model) {
        def webRequest = RequestContextHolder.getRequestAttributes()
        if(!webRequest) {
            def servletContext = ServletContextHolder.getServletContext()
            def applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext)
            GrailsWebMockUtil.bindMockWebRequest(applicationContext)
        }
        return new RenderTagLib().render(template: template, model: model)
    }

    def getExpiryAlertsByLocation(Location location, Integer daysUntilExpiry = 0) {
        String query = """
            select * 
            from product_inventory_expiry_view 
            where days_until_expiry <= ${daysUntilExpiry} 
            and location_id = '${location.id}'
            and quantity_on_hand > 0
            order by days_until_expiry asc
            """
        return dataService.executeQuery(query)
    }

    def getStockAlertsByLocation(Location location) {
        String query = """
            select * 
            from product_inventory_compare_view 
            where location_id = '${location.id}'
            order by product_name asc
            """
        return dataService.executeQuery(query)
    }

    def sendExpiryAlerts(Location location, Integer daysUntilExpiry = 60, List<RoleType> roleTypes, Boolean skipOnEmpty) {
        def subject = "Expiry Alerts - ${location.name}"
        def expiryAlerts = getExpiryAlertsByLocation(location, daysUntilExpiry)
        if (expiryAlerts.isEmpty() && skipOnEmpty) {
            log.info "Skipped ${subject} email for location ${location} because there are no alerts"
            return
        }
        def subscribers = userService.findUsersByRoleTypes(location, roleTypes)
        def csv = dataService.generateCsv(expiryAlerts)
        def expired = expiryAlerts.findAll { it.days_until_expiry <= 0 }
        def expiring = expiryAlerts.findAll { it.days_until_expiry > 0 }
        def model = [location: location, expiring: expiring, expired: expired, daysUntilExpiry: daysUntilExpiry]
        log.info "Sending ${expiryAlerts.size()} ${subject} alerts and ${subscribers.size()} subscribers for location ${location}"
        sendAlerts(subject, "/email/expiryAlerts", model, subscribers, csv)
    }

    def sendStockAlerts(Location location, String status, List<RoleType> roleTypes, Boolean skipOnEmpty) {
        def subject = "Stock Alerts - Status ${status} - ${location.name}"
        def stockAlerts = getStockAlertsByLocation(location)
        def products = stockAlerts.findAll { it.status == status }
        if (products.isEmpty() && skipOnEmpty) {
            log.info "Skipped ${subject} email for location ${location} because there are no alerts"
            return
        }
        def subscribers = userService.findUsersByRoleTypes(location, roleTypes)
        def model = [location: location, status: status, products: products]
        def csv = dataService.generateCsv(products)
        log.info "Sending ${products.size()} ${subject} alerts and ${subscribers.size()} subscribers for location ${location} "
        sendAlerts(subject, "/email/stockAlerts", model, subscribers, csv)
    }

    def sendAlerts(String subject, String template, Map model, List<User> subscribers, String csv) {

        Collection toList = subscribers.collect { it.email }.findAll{ it != null }.toArray()
        if (toList.isEmpty()) {
            log.info("Skipped ${subject} email because there are no subscribers")
            return
        }

        String body = renderTemplate(template, model)

        // Send email with attachment (if csv exists)
        if (csv) {
            mailService.sendHtmlMailWithAttachment(toList, [], subject, body, csv.bytes, "${subject}.csv", "text/csv")
        }
        else {
            mailService.sendHtmlMail(subject, body, toList)
        }

    }


    def sendShipmentCreatedNotification(Shipment shipmentInstance, Location location, List<RoleType> roleTypes) {
        def users = userService.findUsersByRoleTypes(location, roleTypes)
        String subject = "Shipment ${shipmentInstance?.shipmentNumber} has been created"
        String template = "/email/shipmentCreated"
        sendShipmentNotifications(shipmentInstance, users, template, subject)
    }

    def sendShipmentIssuedNotification(Shipment shipmentInstance, Location location, List<RoleType> roleTypes) {
        def users = userService.findUsersByRoleTypes(location, roleTypes)
        String subject = "Shipment ${shipmentInstance?.shipmentNumber} has been shipped"
        String template = "/email/shipmentShipped"
        sendShipmentNotifications(shipmentInstance, users, template, subject)
    }

    def sendShipmentReceiptNotification(Shipment shipmentInstance, Location location, List<RoleType> roleTypes) {
        List<User> users = userService.findUsersByRoleTypes(location, roleTypes)
        String subject = "Shipment ${shipmentInstance?.shipmentNumber} has been received"
        String template = "/email/shipmentReceived"
        sendShipmentNotifications(shipmentInstance, users, template, subject)
    }

    def sendShipmentItemsShippedNotification(Shipment shipmentInstance) {
        def emailValidator = EmailValidator.getInstance()
        def g = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')
        def recipientItems = shipmentInstance.shipmentItems.groupBy {it.recipient }
        recipientItems.each { Person recipient, items ->
            if (emailValidator.isValid(recipient?.email)) {
                def subject = g.message(code: "email.yourItemShipped.message", args: [shipmentInstance.origin.name, shipmentInstance.destination.name, shipmentInstance.shipmentNumber])
                def body = "${g.render(template: "/email/shipmentItemShipped", model: [shipmentInstance: shipmentInstance, shipmentItems: items, recipient:recipient])}"
                mailService.sendHtmlMail(subject, body.toString(), recipient.email)
            }
        }
    }

    def sendShipmentNotifications(Shipment shipmentInstance, List<User> users, String template, String subject) {
        String body = renderTemplate(template, [shipmentInstance: shipmentInstance])
        List emails = users.collect { it.email }
        if (!emails.empty) {
            mailService.sendHtmlMail(subject, body, emails)
        }
    }

    def sendReceiptNotifications(PartialReceipt partialReceipt) {
        Shipment shipment = partialReceipt?.shipment
        def emailValidator = EmailValidator.getInstance()
        def recipientItems = partialReceipt.partialReceiptItems.groupBy {it.recipient }
        recipientItems.each { Person recipient, items ->
            if (emailValidator.isValid(recipient?.email)) {
                def locale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
                def subject = messageSource.getMessage('email.yourItemReceived.message', [shipment.destination.name, shipment.shipmentNumber].toArray(), locale)
                def body = renderTemplate("/email/shipmentItemReceived", [shipmentInstance: shipment, receiptItems: items, recipient: recipient, receivedBy: partialReceipt.recipient])
                mailService.sendHtmlMail(subject, body.toString(), recipient.email)
            }
        }
    }

    def sendApplicationErrorNotification(Location location, Exception exception) {
        log.info "Sending application error notification"
        if (location.active && location.supports(org.pih.warehouse.core.ActivityCode.ENABLE_NOTIFICATIONS)) {
            List<RoleType> roleTypes = [RoleType.ROLE_ERROR_NOTIFICATION]
            List subscribers = userService.findUsersByRoleTypes(location, roleTypes)
            List emails = subscribers.collect { it.email }

            GrailsWrappedRuntimeException grailsException = new GrailsWrappedRuntimeException(ServletContextHolder.servletContext, exception)
            String body = renderTemplate("/email/applicationError",
                    [exception: grailsException, location: location])
            mailService.sendHtmlMail("Application Error: ${exception?.message}", body, emails)
        }
        else {
            log.warn("Unable to send notification because location ${location.name} is inactive or has not enabled notifications")
        }
    }

    def sendUserAccountCreation(User userInstance, Map additionalQuestions) {
        try {
            // Send email to user notification recipients
            def recipients = userService.findUsersByRoleType(RoleType.ROLE_USER_NOTIFICATION)
            if (recipients) {
                def locale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
                def to = recipients?.collect { it.email }?.unique()
                def subject = messageSource.getMessage('email.userAccountCreated.message', [userInstance.username].toArray(), locale)
                def body = renderTemplate("/email/userAccountCreated", [userInstance: userInstance, additionalQuestions: additionalQuestions])
                mailService.sendHtmlMail(subject, body.toString(), to)
            }

        } catch (EmailException e) {
            log.error("Unable to send creation email: " + e.message, e)
        }
    }

    def sendUserAccountConfirmation(User userInstance, Map additionalQuestions) {
        try {
            // Send confirmation email to user
            if (userInstance?.email) {
                def locale = userInstance?.locale ?: new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
                def subject = messageSource.getMessage('email.userAccountConfirmed.message', [userInstance?.email].toArray(), locale)
                def body = renderTemplate("/email/userAccountConfirmed", [userInstance: userInstance, additionalQuestions: additionalQuestions])
                mailService.sendHtmlMail(subject, body.toString(), userInstance?.email)
            }
        } catch (EmailException e) {
            log.error("Unable to send confirmation email: " + e.message, e)
        }
    }


    void publishRequisitionStatusTransitionNotifications(Requisition requisition) {
        switch(requisition.status) {
            case RequisitionStatus.PENDING_APPROVAL:
                List<Person> recipients = requisition.approvers ? Person.getAll(requisition.approvers.id) : []
                publishRequisitionPendingApprovalNotifications(requisition, recipients)
                break
            case RequisitionStatus.APPROVED:
                publishRequisitionStatusUpdateNotification(requisition, requisition.requestedBy)
                break
            case RequisitionStatus.REJECTED:
                publishRequisitionStatusUpdateNotification(requisition, requisition.requestedBy)
                break
            case RequisitionStatus.ISSUED:
                if (requisition?.sourceType == RequisitionSourceType.ELECTRONIC) {
                    publishFulfillmentNotification(requisition.requestedBy, requisition)
                }
                break
            default:
                break
        }
    }

    void publishRequisitionPendingApprovalNotifications(Requisition requisition, List<Person> recipients) {
        String subject = "${requisition.requestNumber} ${requisition.name}"
        String template = "/email/approvalsAlert"

        recipients.each { recipient ->
            if (recipient?.email) {
                String redirectToRequestsList = "/stockMovement/list?direction=OUTBOUND&sourceType=ELECTRONIC&approver=${recipient.id}"
                String body = renderTemplate(template, [requisition: requisition, redirectUrl: redirectToRequestsList])
                mailService.sendHtmlMail(subject, body, recipient.email)
            }
        }
    }

    void publishRequisitionStatusUpdateNotification(Requisition requisition, Person recipient) {
        if (!recipient.email) {
            return
        }
        String subject = "${requisition.requestNumber} ${requisition.name}"
        String template = "/email/approvalsStatusChanged"
        String body = renderTemplate(template, [requisition: requisition])
        mailService.sendHtmlMail(subject, body, recipient.email)
    }


    void publishFulfillmentNotification(Person requestor, Requisition requisition) {
        String subject = "${requisition.requestNumber} ${requisition.name}"
        String template = "/email/fulfillmentAlert"

        if (requestor.email) {
            String body = renderTemplate(template, [requisition: requisition])
            mailService.sendHtmlMail(subject, body, requestor.email)
        }
    }
}
