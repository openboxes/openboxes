package org.pih.warehouse.regression.platform.core

import org.pih.warehouse.core.*
import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetup
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

import javax.mail.Address
import javax.mail.Message
import javax.mail.Part
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

/**
 * Mail is delivered to an in-process SMTP server bound to a free loopback port, so nothing leaves the machine.
 */
class MailServiceSpec extends Specification implements GrailsUnitTest {

    GreenMail greenMail
    MailService service

    void setup() {
        greenMail = new GreenMail(new ServerSetup(0, '127.0.0.1', ServerSetup.PROTOCOL_SMTP).dynamicPort())
        greenMail.start()

        config.grails.mail.enabled = true
        config.grails.mail.from = 'info@example.org'
        config.grails.mail.prefix = '[OpenBoxes]'
        config.grails.mail.host = '127.0.0.1'
        config.grails.mail.port = String.valueOf(greenMail.smtp.port)
        config.grails.mail.username = null
        config.grails.mail.password = null
        config.grails.mail.props.mail.smtp.starttls.enable = false

        service = new MailService()
        service.config = config
    }

    void cleanup() {
        greenMail?.stop()
    }

    private MimeMessage onlyMessage() {
        return receivedMessages(1)[0]
    }

    /** The server stores one copy of a message per recipient. */
    private List<MimeMessage> receivedMessages(int count) {
        assert greenMail.waitForIncomingEmail(5000, count)
        List<MimeMessage> messages = greenMail.receivedMessages as List
        assert messages.size() == count
        return messages
    }

    private static List<String> addresses(Address[] addresses) {
        return addresses.collect { ((InternetAddress) it).address }
    }

    private static int freePort() {
        ServerSocket socket = new ServerSocket(0)
        try {
            return socket.localPort
        } finally {
            socket.close()
        }
    }

    void 'the connection settings are read from the grails.mail config'() {
        given:
        config.grails.mail.username = 'mailer'
        config.grails.mail.password = 'secret'
        config.grails.mail.port = '2525'

        expect:
        service.defaultFrom == 'info@example.org'
        service.defaultHost == '127.0.0.1'
        service.defaultPort == 2525
        service.username == 'mailer'
        service.password == 'secret'
        service.prefix == '[OpenBoxes]'
        service.isMailEnabled
        !service.startTlsEnabled
    }

    void 'mail is neither enabled nor sent over STARTTLS when the flags are missing'() {
        given:
        service.config = new org.grails.config.PropertySourcesConfig([:])

        expect:
        !service.isMailEnabled
        !service.startTlsEnabled
    }

    void 'nothing is sent while mail is disabled'() {
        given:
        config.grails.mail.enabled = false

        when:
        Boolean sent = service.sendMail('Hello', 'Plain text', 'to@example.org')

        then:
        !sent
        !greenMail.waitForIncomingEmail(500, 1)
    }

    void 'override sends even while mail is disabled'() {
        given:
        config.grails.mail.enabled = false

        when:
        Boolean sent = service.sendHtmlMail('Hello', '<p>Hi</p>', 'to@example.org', null, true)

        then:
        sent
        addresses(onlyMessage().getRecipients(Message.RecipientType.TO)) == ['to@example.org']
    }

    void 'a plain text mail carries the prefixed subject, default sender and body'() {
        when:
        Boolean sent = service.sendMail('Hello', 'Plain text', 'to@example.org')

        then:
        sent
        MimeMessage message = onlyMessage()
        message.subject == '[OpenBoxes] Hello'
        addresses(message.from) == ['info@example.org']
        addresses(message.getRecipients(Message.RecipientType.TO)) == ['to@example.org']
        message.isMimeType('text/plain')
        GreenMailUtil.getBody(message).trim() == 'Plain text'
    }

    void 'an html mail goes to every recipient as UTF-8 html'() {
        when:
        Boolean sent = service.sendHtmlMail('Caf\u00e9', '<p>Ol\u00e1</p>', ['a@example.org', 'b@example.org'])

        then:
        sent
        List<MimeMessage> messages = receivedMessages(2)
        messages*.messageID.unique().size() == 1  // one message, delivered to both recipients
        MimeMessage message = messages[0]
        message.subject == '[OpenBoxes] Caf\u00e9'
        addresses(message.getRecipients(Message.RecipientType.TO)) == ['a@example.org', 'b@example.org']
        Part html = findPart(message, 'text/html')
        html.contentType.toUpperCase(Locale.ROOT).contains('UTF-8')
        html.content == '<p>Ol\u00e1</p>'
    }

    void 'an explicit port overrides the configured default port'() {
        given: 'a default port nothing listens on'
        int smtpPort = greenMail.smtp.port
        config.grails.mail.port = String.valueOf(freePort())

        when:
        Boolean sent = service.sendMail('Hello', 'Plain text', ['to@example.org'], smtpPort)

        then:
        sent
        onlyMessage().subject == '[OpenBoxes] Hello'
    }

    void 'sending reports failure instead of throwing when the server cannot be reached'() {
        given:
        config.grails.mail.port = String.valueOf(freePort())

        when:
        Boolean sent = service.sendHtmlMail('Hello', '<p>Hi</p>', 'to@example.org')

        then:
        noExceptionThrown()
        !sent
    }

    void 'building the mail reports failure instead of throwing when an address is invalid'() {
        when:
        Boolean sent = service.sendHtmlMail('Hello', '<p>Hi</p>', 'not an address')

        then:
        noExceptionThrown()
        !sent
        !greenMail.waitForIncomingEmail(500, 1)
    }

    void 'an attachment is sent with its name, type and bytes'() {
        given:
        byte[] bytes = [0, 1, 2, 13, 10, -1] as byte[]

        when:
        Boolean sent = service.sendHtmlMailWithAttachment('to@example.org', 'Report', '<p>See attached</p>',
                bytes, 'report.pdf', 'application/pdf')

        then:
        sent
        MimeMessage message = onlyMessage()
        Part attachment = findAttachment(message)
        attachment.fileName == 'report.pdf'
        attachment.isMimeType('application/pdf')
        attachment.inputStream.bytes == bytes
        findPart(message, 'text/html').content == '<p>See attached</p>'
    }

    void 'an incompletely declared attachment is skipped but the mail is still sent'() {
        when:
        Boolean sent = service.sendHtmlMailWithAttachment(['to@example.org'], 'Report', '<p>Body</p>', [
                new Attachment(name: 'no-bytes.csv', mimeType: 'text/csv', bytes: null),
                new Attachment(name: 'kept.txt', mimeType: 'text/plain', bytes: 'kept'.getBytes('UTF-8')),
        ])

        then:
        sent
        MimeMessage message = onlyMessage()
        findAttachment(message).fileName == 'kept.txt'
        attachmentNames(message) == ['kept.txt']
    }

    void 'mail can be sent from a user, to themselves'() {
        when:
        Boolean sent = service.sendHtmlMailWithAttachment(new User(email: 'me@example.org'), 'Export', '<p>Body</p>',
                'x'.getBytes('UTF-8'), 'x.txt', 'text/plain')

        then:
        sent
        MimeMessage message = onlyMessage()
        addresses(message.from) == ['me@example.org']
        addresses(message.getRecipients(Message.RecipientType.TO)) == ['me@example.org']
    }

    void 'mail sent on behalf of a user carries their name and address as sender'() {
        given:
        User sender = new User(firstName: 'Ann', lastName: 'Sender', email: 'ann@example.org')

        when:
        Boolean sent = service.sendHtmlMailWithAttachment(sender, ['to@example.org'], [], 'Export', '<p>Body</p>',
                'x'.getBytes('UTF-8'), 'x.txt', 'text/plain', greenMail.smtp.port)

        then:
        sent
        InternetAddress from = (InternetAddress) onlyMessage().from[0]
        from.address == 'ann@example.org'
        from.personal == 'Ann Sender'
    }

    private static Part findPart(Part part, String mimeType) {
        if (part.isMimeType(mimeType) && !Part.ATTACHMENT.equalsIgnoreCase(part.disposition)) {
            return part
        }
        if (part.content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) part.content
            for (int i = 0; i < multipart.count; i++) {
                Part found = findPart(multipart.getBodyPart(i), mimeType)
                if (found) {
                    return found
                }
            }
        }
        return null
    }

    private static List<Part> attachments(Part part) {
        if (Part.ATTACHMENT.equalsIgnoreCase(part.disposition)) {
            return [part]
        }
        if (part.content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) part.content
            return (0..<multipart.count).collectMany { attachments(multipart.getBodyPart(it)) }
        }
        return []
    }

    private static Part findAttachment(Part part) {
        List<Part> found = attachments(part)
        assert found.size() == 1
        return found[0]
    }

    private static List<String> attachmentNames(Part part) {
        return attachments(part)*.fileName
    }
}
