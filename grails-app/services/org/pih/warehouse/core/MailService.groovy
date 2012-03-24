package org.pih.warehouse.core;

import grails.util.GrailsUtil;

import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail
import org.apache.commons.mail.HtmlEmail
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class MailService {

	boolean transactional = false
	def userService
	def grailsApplication 
	def config = ConfigurationHolder.config
	def prefix = "${config.grails.mail.prefix}" //[OpenBoxes
	def from = "${config.grails.mail.from}" // openboxes@pih.org
	def host= "${config.grails.mail.host}" // localhost
	def port = Integer.parseInt ("${config.grails.mail.port}") // 23; 
	
	
	def sendMail(String subject, String msg, String to) {
		sendMail(subject, msg, [to])
	}
	def sendMail(String subject, String msg, Collection to) {	
		log.info("Is email enabled? '" + grailsApplication.config.grails.mail.enabled + "'")
		log.info(Boolean.valueOf(grailsApplication.config.grails.mail.enabled))
		if (Boolean.valueOf(grailsApplication.config.grails.mail.enabled)) { 			
			log.info "Sending text email '" + subject + "' to " + to; 
			try { 
				//SimpleEmail is the class which will do all the hard work for you				
				SimpleEmail email = new SimpleEmail()
				email.setHostName(host)				
				to.each {
					email.addTo(it)
				}
				email.setFrom(from)
				email.setSubject("${prefix} " + subject)
				email.setMsg(msg)				
				email.send()
			} catch (Exception e) { 
				log.error("Error sending plaintext email message with subject " + subject + " to " + to, e);
				throw e;
			}
		}
	}
	
	
	
	def sendHtmlMail(String subject, String htmlMessage, String to) {
		sendHtmlMail(subject, htmlMessage, [to])
	}
	
	def sendHtmlMail(String subject, String body, Collection to) { 	
		log.info("Is email enabled? '" + grailsApplication.config.grails.mail.enabled + "'")
		log.info(Boolean.valueOf(grailsApplication.config.grails.mail.enabled))
		if (Boolean.valueOf(grailsApplication.config.grails.mail.enabled)) { 
			log.info "Sending html email '" + subject + "' to " + to; 
			try { 			
				// Create the email message
				HtmlEmail email = new HtmlEmail();
				email.setHostName(host)
				to.each { 
					email.addTo(it)
				}
				email.setFrom(from)
				email.setSubject("${prefix} " + subject)		
				email.setHtmlMsg(body);
				email.setTextMsg(subject);
				email.send();	  
			} catch (Exception e) { 
				log.error("Error sending HTML email message with subject " + subject + " to " + to, e);	
				throw e	
			}
		}
	}

	
	
	def sendHtmlMailWithAttachment(String to, String subject, String body, byte [] bytes, String name, String mimeType) {
		def toList = new ArrayList();
		toList.add(to)
		sendHtmlMailWithAttachment(toList, subject, body, bytes, name, mimeType)
	}

	def sendHtmlMailWithAttachment(User userInstance, String subject, String body, byte [] bytes, String name, String mimeType) { 
		sendHtmlMailWithAttachment(userInstance?.email, subject, body, bytes, name, mimeType)
	}	


	def sendHtmlMailWithAttachment(Collection toList, String subject, String body, byte [] bytes, String name, String mimeType) { 
		log.info ("Sending email with attachment " + toList)
		if (Boolean.valueOf(grailsApplication.config.grails.mail.enabled)) {
			try {
				// Create the email message
				HtmlEmail email = new HtmlEmail();
				email.setHostName(host);
				email.setFrom(from);
				toList.each { to ->
					email.addTo(to)
				}
				email.setSubject("${prefix} " + subject);
				email.setHtmlMsg(body);
				email.setTextMsg(subject);
			  
				// Create the attachment
				//EmailAttachment attachment = new EmailAttachment();
				//attachment.setPath("mypictures/john.jpg");
				//attachment.setDisposition(EmailAttachment.ATTACHMENT);
				//attachment.setDescription("Picture of John");
				//attachment.setName("John");
				//email.attach(attachment);
				
				// add the attachment
				email.attach(new ByteArrayDataSource(bytes, mimeType), 
					name, name, EmailAttachment.ATTACHMENT);
				
				// send the email
				email.send();
			} catch(Exception e) {
				log.error "Problem sending email $e.message", e
				//flash.message = “Confirmation email NOT sent”
			}
		}
	}	
	
	def sendAlertMail(String subject, Throwable throwable) {
		if (grailsApplication.config.grails.mail.enabled) {
			log.info "Sending HTML email '" + subject;
			try {
				HtmlEmail email = new HtmlEmail();
				email.setSubject("${prefix} " + subject)
				// add more information to email
				email.send();
			} catch (Exception e) {
				log.error("Error sending HTML email message with subject " + subject, e);
			}
		}
	}

	/*
	def sendMailWithAttachment(User userInstance) { 
		log.info ("Sending email with attachment " + userInstance?.email)
		if (Boolean.valueOf(grailsApplication.config.grails.mail.enabled)) {
			try {
				
				sendMail {
					multipart true
					to "${userInstance.email}"
					subject "The issue you watch has been updated"
					body "Hello World!"
					//html g.render(template:"/email/userConfirmed", model:[userInstance:userInstance])
					//attachBytes "Some-File-Name.xml", "text/xml", contentOrder.getBytes("UTF-8")
					//To get started quickly, try the following
					attachBytes './web-app/images/grails_logo.jpg','image/jpg', new File('./web-app/images/grails_logo.jpg').readBytes()
				}
				//flash.message = “Confirmation email sent to ${userInstance.emailAddress}”
			} catch(Exception e) {
				log.error "Problem sending email $e.message", e
				//flash.message = “Confirmation email NOT sent”
			}
		}
	}
	*/
		
	
}
