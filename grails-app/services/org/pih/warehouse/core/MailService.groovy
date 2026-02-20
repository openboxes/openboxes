/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import grails.config.Config
import org.apache.commons.mail.Email
import org.apache.commons.mail.EmailAttachment
import org.apache.commons.mail.HtmlEmail
import org.apache.commons.mail.SimpleEmail
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.pih.warehouse.api.PartialReceiptItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.data.FileGenerationService
import org.pih.warehouse.shipping.Shipment
import org.springframework.http.MediaType

import javax.mail.util.ByteArrayDataSource

class MailService {

    FileGenerationService fileGenerationService
    Config config = Holders.getConfig()

    ApplicationTagLib getApplicationTagLib() {
        return Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
    }

    String getDefaultFrom() {
        return config.getProperty("grails.mail.from")
    }

    String getDefaultHost() {
        return config.getProperty("grails.mail.host")
    }

    Integer getDefaultPort() {
        return Integer.valueOf(config.grails.mail.port)
    }

    String getUsername() {
        return config.getProperty("grails.mail.username")
    }

    String getPassword() {
        return config.getProperty("grails.mail.password")
    }

    Boolean getDebug() {
        return config.getProperty("grails.mail.debug")
    }

    String getPrefix() {
        return config.getProperty("grails.mail.prefix")
    }

    Boolean getStartTlsEnabled() {
        return config.getProperty("grails.mail.props.mail.smtp.starttls.enable").toBoolean()
    }

    Boolean getIsMailEnabled() {
        return config.getProperty("grails.mail.enabled").toBoolean()
    }

    Boolean sendMail(String subject, String msg, String to) {
        return doSendMail(subject, msg, null, null, Collections.singleton(to), null, null, null, false, false)
    }

    Boolean sendMail(String subject, String msg, Collection to, Integer port) {
        return doSendMail(subject, msg, null, null, to, null, null, port, false, false)
    }

    Boolean sendHtmlMail(String subject, String htmlMessage, String[] to) {
        return doSendMail(subject, body, null, null, to.toList(), null, null, null, true, false)
    }

    Boolean sendHtmlMail(String subject, String htmlMessage, String to) {
        return doSendMail(subject, htmlMessage, null, null, Collections.singleton(to), null, null, null, true, false)
    }

    Boolean sendHtmlMail(String subject, String htmlMessage, String to, Integer port) {
        return doSendMail(subject, htmlMessage, null, null, Collections.singleton(to), null, null, port, true, false)
    }

    Boolean sendHtmlMail(String subject, String htmlMessage, String to, Integer port, Boolean override) {
        return doSendMail(subject, htmlMessage, null, null, Collections.singleton(to), null, null, port, true, override)
    }

    Boolean sendHtmlMail(String subject, String body, Collection to) {
        return doSendMail(subject, body, null, null, to, null, null, null, true, false)
    }

    Boolean sendHtmlMail(String subject, String body, Collection to, Integer port, Boolean override) {
        return doSendMail(subject, body, null, null, to, null, null, port, true, override)
    }

    Boolean sendHtmlMailWithAttachment(String to, String subject, String body, byte[] bytes, String name, String mimeType) {
        return doSendMail(subject, body, null, null, Collections.singleton(to), null, Collections.singleton(new Attachment(name: name, mimeType: mimeType, bytes: bytes)), null, true, false)
    }

    /**
     * This particular flavor sends an email to the user, from the user, and may not work with sendgrid.
     */
    Boolean sendHtmlMailWithAttachment(User userInstance, String subject, String body, byte[] bytes, String name, String mimeType) {
        return doSendMail(subject, body, userInstance?.email, null, Collections.singleton(userInstance?.email), null, Collections.singleton(new Attachment(name: name, mimeType: mimeType, bytes: bytes)), null, true, false)
    }

    Boolean sendHtmlMailWithAttachment(Collection toList, String subject, String body, List<Attachment> attachments) {
        return doSendMail(subject, body, null, null, toList, null, attachments, null, true, false)
    }

    Boolean sendHtmlMailWithAttachment(Collection toList, Collection ccList, String subject, String body, byte[] bytes, String name, String mimeType) {
        return doSendMail(subject, body, null, null, toList, ccList, Collections.singleton(new Attachment(name: name, mimeType: mimeType, bytes: bytes)), null, true, false)
    }

    Boolean sendHtmlMailWithAttachment(User fromUser, Collection toList, Collection ccList, String subject, String body, byte[] bytes, String name, String mimeType) {
        return doSendMail(subject, body, fromUser?.email, fromUser?.name, toList, ccList, Collections.singleton(new Attachment(name: name, mimeType: mimeType, bytes: bytes)), null, true, false)
    }

    Boolean sendHtmlMailWithAttachment(User fromUser, Collection toList, Collection ccList, String subject, String body, byte[] bytes, String name, String mimeType, Integer port) {
        return doSendMail(subject, body, fromUser?.email, fromUser?.name, toList, ccList, Collections.singleton(new Attachment(name: name, mimeType: mimeType, bytes: bytes)), port, true, false)
    }

    Boolean sendHtmlMailWithAttachment(User fromUser, Collection toList, Collection ccList, String subject, String body, List<Attachment> attachments, Integer port) {
        return doSendMail(subject, body, fromUser?.email, fromUser?.name, toList, ccList, attachments, port, true, false)
    }

    void sendHtmlMailWithGoodsDeliveryNoteAttached(Shipment shipment, List<PartialReceiptItem> items, Person recipient, Person receivedBy) {
        String subject = applicationTagLib.message(code: "email.yourItemReceived.message", args: [shipment.destination.name, shipment.shipmentNumber])
        GString body = "${applicationTagLib.render(template: "/email/shipmentItemReceived", model: [shipmentInstance: shipment, receiptItems: items, recipient: recipient, receivedBy: receivedBy])}"

        String barcodeFileUri = fileGenerationService.generateBarcodeFileUri(shipment.shipmentNumber)

        String fileName = "GoodsReceiptNote-${shipment.shipmentNumber}.pdf"

        byte[] goodsDeliveryNotePdf = fileGenerationService.generatePdfFromTemplate("/email/goodsDeliveryNote", [
                shipment: shipment,
                currentLocation: AuthService.currentLocation,
                barcodeFileUri: barcodeFileUri
        ])

        sendHtmlMailWithAttachment(
                recipient.email,
                subject,
                body.toString(),
                goodsDeliveryNotePdf,
                fileName,
                MediaType.APPLICATION_PDF_VALUE
        )
    }

    /**
     * Send an email.
     *
     * @param subject
     * @param body
     * @param from
     * @param fromName
     * @param to
     * @param cc
     * @param attachments
     * @param port
     * @param useHtml
     * @param override
     * @return true if an email was sent, false otherwise.
     */
    Boolean doSendMail(
        String subject,
        String body,
        String from,
        String fromName,
        Collection<String> to,
        Collection<String> cc,
        Collection<Attachment> attachments,
        Integer port,
        Boolean useHtml,
        Boolean override) {

        Email email
        def summary = "email with subject '${subject}' to ${to} from ${from ?: defaultFrom} via ${defaultHost}:${port ?: defaultPort}"

        if (!isMailEnabled && !override) {
            log.info "email disabled: not sending ${summary}"
            return false
        }

        try {
            if (useHtml) {
                HtmlEmail htmlEmail = new HtmlEmail()
                htmlEmail.setHtmlMsg(body)
                attachments.each {
                    if (it.bytes && it.mimeType && it.name) {
                        htmlEmail.attach(new ByteArrayDataSource(it.bytes, it.mimeType),
                                it.name, it.name, EmailAttachment.ATTACHMENT)
                    } else {
                        log.warn "skipping incompletely-declared attachment"
                    }
                }
                email = htmlEmail
            } else {
                email = new SimpleEmail()
                email.setMsg(body)
            }

            email.setCharset("UTF-8")
            email.setFrom(from ?: defaultFrom, fromName)
            email.setHostName(defaultHost)
            email.setSmtpPort(port ?: defaultPort)
            email.setSubject("${prefix} ${subject}")
            to.each {
                email.addTo(it)
            }

            if (debug) {
                email.setDebug(debug)
            }
            if (username && password) {
                email.setAuthentication(username, password)
            }
            if (startTlsEnabled) {
                email.setStartTLSEnabled(startTlsEnabled)
            }
        } catch (Exception e) {
            log.error("could not create ${summary}", e)
            return false
        }

        try {
            log.info "sending ${summary}"
            email.send()
            return true
        } catch (Exception e) {
            log.error("could not send ${summary}", e)
            return false
        }
        return false
    }
}
