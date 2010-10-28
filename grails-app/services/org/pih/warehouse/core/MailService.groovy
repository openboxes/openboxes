package org.pih.warehouse.core;

import org.apache.commons.mail.SimpleEmail
import org.apache.commons.mail.HtmlEmail
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class MailService {

	boolean transactional = false

	def config = ConfigurationHolder.config
	String from = "${config.grails.mail.from}" 						// warehouse@pih-emr.org
	String host= "${config.grails.mail.host}" 						// localhost
	Integer port = Integer.parseInt ("${config.grails.mail.port}") 	// 23; 
	
	def sendMail(String subject, String msg, String to) {	
		//SimpleEmail is the class which will do all the hard work for you
		SimpleEmail email = new SimpleEmail()
		email.setHostName(host)
		email.addTo(to)
		email.setFrom(from)
		email.setSubject("[OpenBoxes] " + subject)
		email.setMsg(msg)		
		//email.setAuthentication(username,password)		
		//email.setSmtpPort(port)		
		email.send()
	}
	
	
	def sendHtmlMail(String subject, String htmlMessage, String textMessage, String to) { 		
		// Create the email message
		HtmlEmail email = new HtmlEmail();
		email.setHostName(host)
		email.addTo(to)
		email.setFrom(from)
		email.setSubject("[OpenBoxes] " + subject)		
		// embed the image and get the content id
		//URL url = new URL("http://www.apache.org/images/asf_logo_wide.gif");
		//String cid = email.embed(url, "Apache logo");
		email.setHtmlMsg(htmlMessage);
		email.setTextMsg(textMessage);
		email.send();	  
	}
	
	
	
	def sendAlertMail(String subject, Throwable throwable) { 
		
		HtmlEmail email = new HtmlEmail();
		email.send();
		
	}
	
	
	
}
