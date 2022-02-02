package org.pih.warehouse.core

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetupTest
import grails.test.GrailsUnitTestCase
import org.junit.Test

import javax.mail.Message

class MailServiceTests extends GrailsUnitTestCase {

	def mailService
	def grailsApplication

	static GreenMail testSmtpService

	@Override
	protected void setUp() {
		super.setUp()

		testSmtpService = new GreenMail(ServerSetupTest.ALL)
		testSmtpService.start()

		grailsApplication.config.grails.mail.host = ServerSetupTest.SMTP.bindAddress
		grailsApplication.config.grails.mail.port = ServerSetupTest.SMTP.port
	}

	@Override
	protected void tearDown() {
		super.tearDown()
		testSmtpService.stop()
	}

	@Test
	void sendHtmlMail_shouldHandleAccentedCharactersCorrectly() {
		mailService.sendHtmlMail("sübĵéçt", "This Is Spın̈al Tap", "anybody@anywhere.com", null, true)

		Message[] messages = testSmtpService.receivedMessages
		assertEquals(1, messages.length)
		assertEquals("[OpenBoxes] sübĵéçt", messages[0].subject)
		assertTrue(GreenMailUtil.getBody(messages[0]).contains("This Is Sp=C4=B1n=CC=88al Tap"))
	}
}
