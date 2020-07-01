/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import grails.converters.JSON
import org.pih.warehouse.util.RequestUtil
import org.springframework.validation.BeanPropertyBindingResult
import util.ConfigHelper

class ErrorsController {

    def messageSource
    MailService mailService
    def userService
    def grailsApplication

    def handleException = {
        if (RequestUtil.isAjax(request)) {
            def cause = request?.exception?.cause ?: request?.exception
            def message = cause?.message ?: ""

            render([errorCode: 500, cause: cause?.class, errorMessage: message] as JSON)
        } else {
            render(view: "/error")
        }
    }

    def handleNotFound = {
        log.info "Params " + params

        if (RequestUtil.isAjax(request)) {
            response.status = 404
            def errorMessage = "Resource not found"
            if (request?.exception?.message) {
                errorMessage = request.exception.message
            } else if (params.resource) {
                errorMessage = "${params.resource.capitalize()} with identifier ${params.id} not found"
            }
            render([errorCode: 404, errorMessage: errorMessage] as JSON)
        } else {
            render(view: "/errors/notFound")
        }
    }

    def handleUnauthorized = {
        log.info "Unauthorized user"
        if (RequestUtil.isAjax(request)) {
            response.status = 401
            render([errorCode: 401, errorMessage: "Unauthorized user: ${request?.exception?.message}"] as JSON)
        } else {
            redirect(controller: "auth", action: "login")
        }
    }

    def handleForbidden = {
        log.info "Access denied"
        if (RequestUtil.isAjax(request)) {
            response.status = 403
            render([errorCode: 403, errorMessage: "Access denied"] as JSON)
        } else {
            render(view: "/errors/accessDenied")
        }
    }


    def handleInvalidDataAccess = {
        if (RequestUtil.isAjax(request)) {
            render([errorCode: 500, errorMessage: "Illegal data access"] as JSON)
        } else {
            render(view: "/errors/dataAccess")
        }
    }

    def handleMethodNotAllowed = {
        if (RequestUtil.isAjax(request)) {
            render([errorCode: 405, errorMessage: "Method not allowed"] as JSON)
            return
        }
        render(view: "/errors/methodNotAllowed")
    }

    def handleValidationErrors = {
        if (RequestUtil.isAjax(request)) {
            response.status = 400
            BeanPropertyBindingResult errors = request?.exception?.cause?.errors
            def errorMessages = errors.allErrors.collect {
                return messageSource.getMessage(it.codes[0], it.arguments, it.defaultMessage, null)
            }
            render([errorCode: 400,
                    errorMessage: "Validation error. " + request?.exception?.cause?.fullMessage,
                    data: errors?.allErrors,
                    errorMessages: errorMessages
            ] as JSON)
            return
        }
        render(view: "/error")
    }


    def sendFeedback = {
        def enabled = Boolean.valueOf(grailsApplication.config.openboxes.mail.feedback.enabled ?: true)

        if (enabled) {
            def recipients = grailsApplication.config.openboxes.mail.feedback.recipients

            def jsonObject = JSON.parse(params.data)
            byte[] attachment = jsonObject[1].replace("data:image/png;base64,", "").decodeBase64()

            def emailMessage = [
                    from          : session?.user?.email,
                    to            : recipients,
                    cc            : [],
                    bcc           : [],
                    subject       : jsonObject[0]["summary"],
                    body          : jsonObject[0]["description"],
                    attachment    : attachment,
                    attachmentName: "screenshot.png",
                    mimeType      : "image/png"

            ]
            mailService.sendHtmlMailWithAttachment(emailMessage)
        }
        render "OK"

    }


    def processError = {

        def enabled = ConfigHelper.booleanValue(grailsApplication.config.openboxes.mail.errors.enabled)
        if (enabled) {
            def recipients = ConfigHelper.listValue(grailsApplication.config.openboxes.mail.errors.recipients) as List

            def errorNotificationList = userService.findUsersByRoleType(RoleType.ROLE_ERROR_NOTIFICATION)
            errorNotificationList.each { errorNotificationUser ->
                if (errorNotificationUser.email)
                    recipients.add(errorNotificationUser.email)
            }

            def ccList = []
            def reportedBy = User.findByUsername(params.reportedBy)
            if (params.ccMe && reportedBy) {
                ccList.add(reportedBy?.email)
            }

            def dom = params.remove("dom")
            def stacktrace = params.remove("stacktrace")
            def subject = "${params.summary ?: warehouse.message(code: 'email.errorReportSubject.message')}"
            def body = "${g.render(template: '/email/errorReport', model: [stacktrace: stacktrace], params: params)}"

            mailService.sendHtmlMailWithAttachment(reportedBy, recipients, ccList, subject, body.toString(), dom?.bytes, "error.html", "text/html")
            flash.message = "${warehouse.message(code: 'email.errorReportSuccess.message', args: [recipients])}"
        } else {
            flash.message = "${warehouse.message(code: 'email.errorReportDisabled.message')}"
        }
        redirect(controller: "dashboard", action: "index")
    }

}
