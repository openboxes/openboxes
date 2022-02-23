package org.pih.warehouse.core

import com.icegreen.greenmail.user.GreenMailUser
import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetupTest
import com.sun.mail.imap.IMAPStore
import grails.test.GrailsUnitTestCase
import org.junit.Test

import javax.mail.Message
import javax.mail.Quota

class MailServiceTests extends GrailsUnitTestCase {

	MailService mailService
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
	void sendHtmlMailShouldHandleAccentedCharactersCorrectly() {
		def sent = mailService.sendHtmlMail("sübĵéçt", "This Is Spın̈al Tap", "anybody@anywhere.com", null, true)
		assertTrue(sent)
		testSmtpService.waitForIncomingEmail(10000, 1)
		Message[] messages = testSmtpService.receivedMessages
		assertEquals(1, messages.length)
		assertEquals("[OpenBoxes] sübĵéçt", messages[0].subject)
		assertTrue(GreenMailUtil.getBody(messages[0]).contains("This Is Sp=C4=B1n=CC=88al Tap"))
	}

	@Test
	void sendHtmlMailShouldNotRaiseExceptionIfSmtpServerIsDown() {
		testSmtpService.stop()
		// should not raise org.apache.commons.mail.EmailException
		def sent = mailService.sendHtmlMail("undeliverable", "this message will not send", "anybody@anywhere.com", null, true)
		assertFalse(sent)
	}

	@Test
	void sendHtmlMailShouldNotRaiseExceptionIfPortIsInvalid() {
		def oldPort = grailsApplication.config.grails.mail.port = ServerSetupTest.SMTP.port
		grailsApplication.config.grails.mail.port = -1
		try {
			// should not raise java.lang.IllegalArgumentException
			def sent = mailService.sendHtmlMail("invalid port", "this message will not send", "anybody@anywhere.com", null, true)
			assertFalse(sent)
		} finally {
			grailsApplication.config.grails.mail.port = oldPort
		}
	}

	@Test
	void sendMailShouldNotRaiseExceptionIfSmtpServerIsDown() {
		Boolean oldEnabled = grailsApplication.config.grails.mail.enabled
		grailsApplication.config.grails.mail.enabled = true

		try {
			def shouldSend = mailService.sendMail("deliverable", "this message will send", "anybody@anywhere.com")
			assertTrue(shouldSend)
			testSmtpService.stop()
			// should not raise org.apache.commons.mail.EmailException
			def shouldNotSend = mailService.sendMail("undeliverable", "this message will not send", "anybody@anywhere.com")
			assertFalse(shouldNotSend)
		} finally {
			grailsApplication.config.grails.mail.enabled = oldEnabled
		}
	}

	@Test
	void sendHtmlMailShouldNotRaiseExceptionIfCredentialsAreWrong() {
		String oldUsername = grailsApplication.config.grails.mail.username
		grailsApplication.config.grails.mail.username = "wrong_" + grailsApplication.config.grails.mail.username
		String oldPassword = grailsApplication.config.grails.mail.password
		grailsApplication.config.grails.mail.password = "wrong_" + grailsApplication.config.grails.mail.password

		try {
			GreenMailUser user = testSmtpService.setUser(
				grailsApplication.config.grails.mail.from,
				"right_" + grailsApplication.config.grails.mail.username,
				"right_" + grailsApplication.config.grails.mail.password
			)
			// should not raise org.apache.commons.mail.EmailException
			def sent = mailService.sendHtmlMail("unauthorized", "this message will not send", "anybody@anywhere.com", null, true)
			assertFalse(sent)
		} finally {
			grailsApplication.config.grails.mail.username = oldUsername
			grailsApplication.config.grails.mail.password = oldPassword
		}
	}

	@Test
	void sendMailShouldNotRaiseExceptionIfSmtpServerIsFull() {
		Boolean oldEnabled = grailsApplication.config.grails.mail.enabled
		grailsApplication.config.grails.mail.enabled = true

		try {
			testSmtpService.setQuotaSupported(true)
			GreenMailUser recipient = testSmtpService.setUser(
				"anybody@anywhere.com",
				"recipient"
			)

			IMAPStore store = testSmtpService.imap.createStore()
			store.connect(
				"anybody@anywhere.com",
				"recipient"
			)

			Quota testQuota = new Quota("INBOX")
			testQuota.setResourceLimit("MESSAGES", 1)

			store.quota = testQuota
			GreenMailUtil.setQuota(recipient, testQuota)

			def shouldSend = mailService.sendMail("under_quota", "this message will send", "anybody@anywhere.com")
			assertTrue(shouldSend)
			def shouldAlsoSend = mailService.sendMail("over_quota", "this message will send but not arrive", "anybody@anywhere.com")
			assertTrue(shouldAlsoSend)
		} finally {
			grailsApplication.config.grails.mail.enabled = oldEnabled
		}
	}
}
