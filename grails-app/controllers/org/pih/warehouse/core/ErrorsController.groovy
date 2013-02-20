/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core;

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
	
	def processError = { 		
		def toList = []
		def ccList = []
		
		toList.add("justin.miranda@gmail.com")
		//toList.add("emr-requests@pih.org")
		//ccList.add("jmiranda@pih.org")
		
		def reportedBy = User.findByUsername(params.reportedBy)
		if (params.ccMe && reportedBy) { 
			ccList << reportedBy?.email
		}		
		
		def dom = params.remove("dom")
		def subject = "${params.summary?:warehouse.message(code: 'email.errorReportSubject.message')}"
		def body = "${g.render(template:'/email/errorReport', params:params)}"
		mailService.sendHtmlMailWithAttachment(toList, ccList, subject, body.toString(), dom?.bytes, "error.html","text/html");
		flash.message = "${warehouse.message(code: 'email.errorReportSuccess.message', args: [toList])}"
		redirect(controller: "dashboard", action: "index")
	}
	
}