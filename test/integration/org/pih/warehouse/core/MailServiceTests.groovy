package org.pih.warehouse.core

import grails.test.mixin.integration.Integration
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore

import org.junit.Test
import com.dumbster.smtp.SimpleSmtpServer

@Integration
class MailServiceTests {

	def mailService

	@Test
	void sendHtmlMail_shouldHandleAccentedCharactersCorrectly() {

        def mailServer = SimpleSmtpServer.start(2525)

        def subject = "The Véhicule shipment 'Soduim Chloride 0,9% for Boucan Carre' has been shipped on January 23 2013."
		def body = "The Véhicule shipment 'Soduim Chloride 0,9% for Boucan Carre' has been shipped on January 23 2013."

		//mailService.sendMail("[Text]" + subject, body, "justin.miranda@gmail.com")
		mailService.sendHtmlMail("[Prefix]" + subject, body, "justin@openboxes.com", "localhost", 2525, true)

        assert !mailServer.isStopped()
        mailServer.stop();

        // a little dump to see what's in the email :)
        mailServer.receivedEmail.each { println it }

		assert mailServer.receivedEmail.toList().size() == 1


        //assertTrue(server.getReceivedEmailSize() == 1);
		//Iterator emailIter = server.getReceivedEmail();
		//SmtpMessage email = (SmtpMessage)emailIter.next();
		//assertTrue(email.getHeaderValue("Subject").equals("Test"));
		//assertTrue(email.getBody().equals("Test Body"));
	}

    @Test
    void sendMail_shouldSendEmail() {

        def mailServer = SimpleSmtpServer.start(2525)

        mailService.sendMail("Subject", "Message body", ["justin@openboxes.com"], "localhost", 2525, true)

        assert !mailServer.isStopped()
        mailServer.stop()

        mailServer.receivedEmail.each { println it }
        assert mailServer.receivedEmail.toList().size() == 1


    }

}
