package org.pih.warehouse.core

import org.junit.Test
import com.dumbster.smtp.SimpleSmtpServer

class MailServiceTests extends GroovyTestCase{
  
	def mailService
	def grailsApplication

	protected void setUp() {
		super.setUp()

		// Reset mail server settings
		grailsApplication.config.grails.mail.host = "localhost"
		grailsApplication.config.grails.mail.port = 2525
	}

	@Test
	void sendHtmlMail_shouldHandleAccentedCharactersCorrectly() { 
		def server = SimpleSmtpServer.start(2525)

		mailService.sendHtmlMail("subject", "body", "anybody@anywhere.com")

		server.stop()
		
		assert server.receivedEmail.toList().size() == 1
	
		// a little dump to see what's in the email :)
		server.receivedEmail.each { println it }
	}	
}
