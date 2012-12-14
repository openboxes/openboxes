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
		log.info "process error " + params
		def toList = []

		def adminUsers = userService.findUsersByRoleType(RoleType.ROLE_ADMIN);	
		adminUsers.each { admin ->
			toList << admin?.email
		}
		
		def reportedBy = User.findByUsername(params.reportedBy)
		if (params.ccMe && reportedBy) { 
			toList << reportedBy?.email
		}		
		
		def subject = "${warehouse.message(code: 'email.errorReportSubject.message')}"
		def body = "${g.render(template:'/email/errorReport', params:params)}"
		mailService.sendHtmlMailWithAttachment(toList, [], subject, body.toString(), params?.dom?.bytes, "error.html","text/html");
		flash.message = "${warehouse.message(code: 'email.errorReportSuccess.message', args: [toList])}"
		redirect(controller: "dashboard", action: "index")
	}
	
}