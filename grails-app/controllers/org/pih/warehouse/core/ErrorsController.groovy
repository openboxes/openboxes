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
import grails.core.GrailsApplication
import org.grails.exceptions.ExceptionUtils
import org.grails.web.errors.GrailsWrappedRuntimeException
import org.pih.warehouse.RequestUtil
import org.springframework.http.HttpMethod
import org.springframework.validation.BeanPropertyBindingResult
import util.ConfigHelper

class ErrorsController {

    def messageService
    MailService mailService
    def userService
    GrailsApplication grailsApplication
    def userAgentIdentService
    def localizationService

    def handleException() {
        if (RequestUtil.isAjax(request)) {
            Throwable exception = request.getAttribute('exception') ?: request.getAttribute("javax.servlet.error.exception")
            Throwable root = exception ? ExceptionUtils.getRootCause(exception) : null
            String message = root?.message ?: ""
            render([errorCode: 500, cause: root?.class, errorMessage: message] as JSON)
        } else {
            if (userAgentIdentService.isMobile()) {
                render(view: "/mobile/error")
                return
            }

            render(view: "/error")
        }
    }

    def handleNotFound() {
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

    def handleUnauthorized() {
        log.info "Unauthorized user"
        if (RequestUtil.isAjax(request)) {
            response.status = 401
            render([errorCode: 401, errorMessage: "Unauthorized user: ${request?.exception?.message}"] as JSON)
        } else {
            redirect(controller: "auth", action: "login")
        }
    }

    def handleForbidden() {
        log.info "Access denied"
        if (RequestUtil.isAjax(request)) {
            response.status = 403
            render([errorCode: 403, errorMessage: "Access denied"] as JSON)
        } else {
            render(view: "/errors/accessDenied")
        }
    }


    def handleInvalidDataAccess() {
        if (RequestUtil.isAjax(request)) {
            render([errorCode: 500, errorMessage: "Illegal data access"] as JSON)
        } else {
            render(view: "/errors/dataAccess")
        }
    }

    def handleMethodNotAllowed() {
        if (RequestUtil.isAjax(request)) {
            render([errorCode: 405, errorMessage: "Method ${request.method} not allowed for ${controllerName}:${actionName}"] as JSON)
            return
        }
        render(view: "/errors/methodNotAllowed")
    }

    def handleValidationErrors() {
        if (RequestUtil.isAjax(request)) {
            response.status = 400
            Throwable exception = request.getAttribute('exception')
            def root = ExceptionUtils.getRootCause(exception)
            BeanPropertyBindingResult errors = root.getErrors()
            List<String> errorMessages = errors.allErrors.collect {
                return g.message(error: it, locale: localizationService.currentLocale)
            }
            render([errorCode: 400,
                    errorMessage: "Validation error. " + root.fullMessage,
                    errorMessages: errorMessages
            ] as JSON)
            return
        }
        render(view: "/error")
    }

    def handleConstraintViolation() {
        if (RequestUtil.isAjax(request)) {
            if (request?.method == HttpMethod.DELETE.name()) {
                String message = g.message(
                        code: "errors.existingAssociation.message",
                        default: "Resource could not be deleted because of an existing association"
                )

                render([errorCode: 500, errorMessage: message] as JSON)
                return
            }

            Throwable root = ExceptionUtils.getRootCause(request.getAttribute('exception'))
            render([errorCode: 500, errorMessage: root.getMessage()] as JSON)
            return
        }

        render(view: '/error')
    }

    def sendFeedback() {
        def enabled = Boolean.valueOf(grailsApplication.config.openboxes.mail.feedback.enabled ?: true)

        if (enabled) {
            def recipients = grailsApplication.config.openboxes.mail.feedback.recipients
            def jsonObject = JSON.parse(params.data)
            byte[] attachment = jsonObject[1].replace("data:image/png;base64,", "").decodeBase64()
            mailService.sendHtmlMailWithAttachment(
                    session?.user,
                    recipients,
                    null,
                    jsonObject[0]["summary"],
                    jsonObject[0]["description"],
                    attachment,
                    "screenshot.png",
                    "image/png"
            )
        }
        render "OK"
    }


    def processError() {

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
