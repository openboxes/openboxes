package org.pih.warehouse.core;

import grails.util.GrailsUtil;
import org.apache.commons.mail.SimpleEmail
import org.apache.commons.mail.HtmlEmail
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class MailService {

	boolean transactional = false
	
	def grailsApplication 
	def config = ConfigurationHolder.config
	def from = "${config.grails.mail.from}" 						// warehouse@pih-emr.org
	def host= "${config.grails.mail.host}" 						// localhost
	def port = Integer.parseInt ("${config.grails.mail.port}") 	// 23; 
	
	def sendMail(String subject, String msg, String to) {
		sendMail(subject, msg, [to])
	}
	
	def sendMail(String subject, String msg, ArrayList to) {	
		
		if (Boolean.valueOf(grailsApplication.config.grails.mail.enabled)) { 			
			log.info "Sending HTML email '" + subject + "' to " + to; 
			try { 
				//SimpleEmail is the class which will do all the hard work for you				
				SimpleEmail email = new SimpleEmail()
				email.setHostName(host)
				email.addTo(to)
				email.setFrom(from)
				email.setSubject("[ OpenBoxes " + GrailsUtil.environment + " ] " + subject)
				email.setMsg(msg)		
				//email.setAuthentication(username,password)		
				//email.setSmtpPort(port)		
				email.send()
			} catch (Exception e) { 
				log.error("Error sending plaintext email message with subject " + subject + " to " + to, e);
			}
		}
		else { 
			log.warn "Email has been disabled for '" + GrailsUtil.environment + "' environment";
		}
	}
	
	def sendHtmlMail(String subject, String htmlMessage, String textMessage, String to) {
		sendHtmlMail(subject, htmlMessage, textMessage, [to])
	}
	
	def sendHtmlMail(String subject, String htmlMessage, String textMessage, ArrayList to) { 		
		if (Boolean.valueOf(grailsApplication.config.grails.mail.enabled)) { 
			log.info "Sending HTML email '" + subject + "' to " + to; 
			try { 			
				// Create the email message
				HtmlEmail email = new HtmlEmail();
				email.setHostName(host)
				email.addTo(to)
				email.setFrom(from)
				email.setSubject("[ OpenBoxes " + GrailsUtil.environment + " ] " + subject)		
				// embed the image and get the content id
				//URL url = new URL("http://www.apache.org/images/asf_logo_wide.gif");
				//String cid = email.embed(url, "Apache logo");
				email.setHtmlMsg(htmlMessage);
				email.setTextMsg(textMessage);
				email.send();	  
			} catch (Exception e) { 
				log.error("Error sending HTML email message with subject " + subject + " to " + to, e);		
			}
		}
		else { 
			log.warn "Email has been disabled for '" + GrailsUtil.environment + "' environment";
		}
	}
	
	
	
	def sendAlertMail(String subject, Throwable throwable) { 
		if (grailsApplication.config.grails.mail.enabled) { 
			log.info "Sending HTML email '" + subject;
			try { 			
				HtmlEmail email = new HtmlEmail();
				// add more information to email
				email.send();
			} catch (Exception e) { 
				log.error("Error sending HTML email message with subject " + subject, e);		
			}
		}		
		else { 
			log.warn "Email has been disabled for '" + GrailsUtil.environment + "' environment";
		}
	}
	
	
	
}
