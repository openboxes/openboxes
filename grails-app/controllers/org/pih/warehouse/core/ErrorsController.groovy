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
import util.ClickstreamUtil;

class ErrorsController {

	def mailService
	def userService
	
	def handleException = {
		render(view: "/error")
	}
	
	def handleNotFound = { 
		render(view:"/errors/notFound")
	}
	
	def handleUnauthorized = { 
		render(view:"/errors/accessDenied")
	}

    def handleInvalidDataAccess = {
        render(view:"/errors/dataAccess")
    }

    def handleMethodNotAllowed = {
        render(view:"/errors/methodNotAllowed")
    }

    def sendFeedback = {
        def jsonObject = JSON.parse(params.data)

        byte[] attachment = jsonObject[1].replace("data:image/png;base64,","").decodeBase64()

        def emailMessage = [
            from: session?.user?.email,
            to: ["jcm62@columbia.edu"],
            cc: [],
            bcc: [],
            subject: jsonObject[0]["summary"],
            body: jsonObject[0]["description"],
            attachment: attachment,
            attachmentName: "screenshot.png",
            mimeType: "image/png"

        ]
        mailService.sendHtmlMailWithAttachment(emailMessage);
        render "OK"

    }


	def processError = { 		
		def toList = ["justin@openboxes.com"]  //"support@openboxes.com"
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
		mailService.sendHtmlMailWithAttachment(reportedBy, toList, ccList, subject, body.toString(), dom?.bytes, "error.html", "text/html");
        //mailService.sendHtmlMailWithAttachment(reportedBy, toList, ccList, subject, body.toString(), clickstreamAsCsv, "clickstream.csv", "text/csv");
        flash.message = "${warehouse.message(code: 'email.errorReportSuccess.message', args: [toList])}"
		redirect(controller: "dashboard", action: "index")
	}
	
}