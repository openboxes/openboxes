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
import org.apache.catalina.util.Base64
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.pih.warehouse.user.DashboardController
import util.ClickstreamUtil
import util.ConfigHelper;

class ErrorsController {

	MailService mailService
	def userService
    def grailsApplication

	def handleException = {
        if (isAjax(request)) {
            render([errorCode: 500, errorMessage: request?.exception?.message?:""] as JSON)
        }
        else {
            render(view: "/error")
        }
	}
	
	def handleNotFound = {
        if (isAjax(request)) {
            render([errorCode: 404, errorMessage: "Resource not found"] as JSON)
        }
        else {
            render(view: "/errors/notFound")
        }
	}
	
	def handleUnauthorized = {
        if (isAjax(request)) {
            render([errorCode: 401, errorMessage: "Access denied"] as JSON)
        }
        else {
            render(view:"/errors/accessDenied")
        }
	}

    def handleInvalidDataAccess = {
        if (isAjax(request)) {
            render([errorCode: 500, errorMessage: "Illegal data access"] as JSON)
        }
        else {
            render(view:"/errors/dataAccess")
        }
    }

    def handleMethodNotAllowed = {
        if (isAjax(request)) {
            render([errorCode: 405, errorMessage: "Method not allowed"] as JSON)
            return
        }
        render(view:"/errors/methodNotAllowed")
    }

    boolean isAjax(request) {
        def contentType = request.getHeader("Content-Type")
        return request.isXhr() || contentType.equals("application/json")
    }

    def sendFeedback = {
        def enabled = Boolean.parseBoolean(grailsApplication.config.openboxes.mail.feedback.enabled?:"true");

        if (enabled) {
            def recipients = grailsApplication.config.openboxes.mail.feedback.recipients

            def jsonObject = JSON.parse(params.data)
            byte[] attachment = jsonObject[1].replace("data:image/png;base64,","").decodeBase64()

            def emailMessage = [
                from: session?.user?.email,
                to: recipients,
                cc: [],
                bcc: [],
                subject: jsonObject[0]["summary"],
                body: jsonObject[0]["description"],
                attachment: attachment,
                attachmentName: "screenshot.png",
                mimeType: "image/png"

            ]
            mailService.sendHtmlMailWithAttachment(emailMessage);
        }
        render "OK"

    }


	def processError = {

        //def enabled = Boolean.valueOf(grailsApplication.config.openboxes.mail.errors.enabled)
        //def enabled = Boolean.parseBoolean(grailsApplication.config.openboxes.mail.errors.enabled?:"true");
        def enabled = ConfigHelper.booleanValue(grailsApplication.config.openboxes.mail.errors.enabled)
        if (enabled) {
            def recipients = ConfigHelper.listValue(grailsApplication.config.openboxes.mail.errors.recipients) as List

            def ccList = []
            def reportedBy = User.findByUsername(params.reportedBy)
            if (params.ccMe && reportedBy) {
                ccList.add(reportedBy?.email)
            }

            def dom = params.remove("dom")
            def sessionId = session?.id
            def stacktrace = params.remove("stacktrace")
            def clickstream = params.remove("clickstream")
            def serverUrl = ConfigurationHolder.config.grails.serverURL
            def clickstreamUrl = "${serverUrl}/stream/view/${sessionId}"
            def subject = "${params.summary?:warehouse.message(code: 'email.errorReportSubject.message')}"
            def body = "${g.render(template:'/email/errorReport', model:[stacktrace:stacktrace, clickstream:clickstream, clickstreamUrl:clickstreamUrl], params:params)}"

            //def clickstreamAsCsv = ClickstreamUtil.getClickstreamAsCsv(session.clickstream)
            mailService.sendHtmlMailWithAttachment(reportedBy, recipients, ccList, subject, body.toString(), dom?.bytes, "error.html", "text/html");
            //mailService.sendHtmlMailWithAttachment(reportedBy, toList, ccList, subject, body.toString(), clickstreamAsCsv, "clickstream.csv", "text/csv");
            flash.message = "${warehouse.message(code: 'email.errorReportSuccess.message', args: [recipients])}"
        }
        else {
            flash.message = "${warehouse.message(code: 'email.errorReportDisabled.message')}"
        }
		redirect(controller: "dashboard", action: "index")
	}
	
}