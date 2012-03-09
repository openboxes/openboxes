package org.pih.warehouse.core;

class ErrorsController {

	def mailService
	
	def handleException = { 
		log.info("handle exception" + params)
		render(view: "/error")
	}
	
	def processError = { 
		log.info "process error " + params
		def recipient = "jmiranda@pih.org"
		def userInstance = User.findByEmail(recipient);		
		def subject = "${warehouse.message(code: 'email.errorReportSubject.message')}"
		def body = "${g.render(template:'/email/errorReport', params:params)}"
		mailService.sendHtmlMailWithAttachment(userInstance, subject, body.toString(), params?.dom?.bytes, "error.html","text/html");
		//mailService.sendHtmlMail(subject, body, recipient)
		flash.message = "${warehouse.message(code: 'email.errorReportSuccess.message', args: [recipient])}"
		redirect(controller: "dashboard", action: "index")
	}
	
}