package org.pih.warehouse.core

// import groovy.util.GroovyTestCase;
import org.junit.Test
import com.dumbster.smtp.SimpleSmtpServer

class MailServiceTests extends GroovyTestCase{
  
	def mailService
	
	@Test
	void sendHtmlMail_shouldHandleAccentedCharactersCorrectly() { 
		def server = SimpleSmtpServer.start(2525)
		
		def subject = "The Véhicule shipment 'Soduim Chloride 0,9% for Boucan Carre' has been shipped on January 23 2013."
		def body = "The Véhicule shipment 'Soduim Chloride 0,9% for Boucan Carre' has been shipped on January 23 2013."
				
		//mailService.sendMail("[Text]" + subject, body, "justin.miranda@gmail.com")
		mailService.sendHtmlMail("[Html]" + subject, body, "justin@openboxes.com", 2525, true)

		server.stop()
		
		assert server.receivedEmail.toList().size() == 1
	
		// a little dump to see what's in the email :)
		server.receivedEmail.each { println it }

		//assertTrue(server.getReceivedEmailSize() == 1);
		//Iterator emailIter = server.getReceivedEmail();
		//SmtpMessage email = (SmtpMessage)emailIter.next();
		//assertTrue(email.getHeaderValue("Subject").equals("Test"));
		//assertTrue(email.getBody().equals("Test Body"));
	}	
}
