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