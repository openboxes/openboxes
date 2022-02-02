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

import org.apache.commons.mail.Email
import org.apache.commons.mail.EmailAttachment
import org.apache.commons.mail.HtmlEmail
import org.apache.commons.mail.SimpleEmail

import javax.mail.util.ByteArrayDataSource

class MailService {

    boolean transactional = false
    def grailsApplication

    String getDefaultFrom() {
        return grailsApplication.config.grails.mail.from
    }

    String getDefaultHost() {
        return grailsApplication.config.grails.mail.host
    }

    Integer getDefaultPort() {
        return Integer.valueOf(grailsApplication.config.grails.mail.port)
    }

    String getUsername() {
        return grailsApplication.config.grails.mail.username
    }

    String getPassword() {
        return grailsApplication.config.grails.mail.password
    }

    Boolean getDebug() {
        return grailsApplication.config.grails.mail.debug
    }

    String getPrefix() {
        return grailsApplication.config.grails.mail.prefix
    }

    Boolean getStartTlsEnabled() {
        return grailsApplication.config.grails.mail.props["mail.smtp.starttls.enable"]
    }

    Boolean getIsMailEnabled() {
        return grailsApplication.config.grails.mail.enabled.toBoolean()
    }

    void sendMail(String subject, String msg, String to) {
        sendMailImpl(subject, msg, null, null, Collections.singleton(to), null, null, null, false, false)
    }

    void sendMail(String subject, String msg, Collection to, Integer port) {
        sendMailImpl(subject, msg, null, null, to, null, null, port, false, false)
    }

    void sendHtmlMail(String subject, String htmlMessage, String[] to) {
        sendMailImpl(subject, body, null, null, to.toList(), null, null, null, true, false)
    }

    void sendHtmlMail(String subject, String htmlMessage, String to) {
        sendMailImpl(subject, htmlMessage, null, null, Collections.singleton(to), null, null, null, true, false)
    }

    void sendHtmlMail(String subject, String htmlMessage, String to, Integer port) {
        sendMailImpl(subject, htmlMessage, null, null, Collections.singleton(to), null, null, port, true, false)
    }

    void sendHtmlMail(String subject, String htmlMessage, String to, Integer port, Boolean override) {
        sendMailImpl(subject, htmlMessage, null, null, Collections.singleton(to), null, null, port, true, override)
    }

    void sendHtmlMail(String subject, String body, Collection to) {
        sendMailImpl(subject, body, null, null, to, null, null, null, true, false)
    }

    void sendHtmlMail(String subject, String body, Collection to, Integer port, Boolean override) {
        sendMailImpl(subject, body, null, null, to, null, null, port, true, override)
    }

    def sendHtmlMailWithAttachment(String to, String subject, String body, byte[] bytes, String name, String mimeType) {
        sendMailImpl(subject, body, null, null, Collections.singleton(to), ccList, Collections.singleton(new Attachment(name: name, mimeType: mimeType, bytes: bytes)), null, true, false)
    }

    /**
     * This particular flavor sends an email to the user, from the user, and may not work with sendgrid.
     */
    def sendHtmlMailWithAttachment(User userInstance, String subject, String body, byte[] bytes, String name, String mimeType) {
        sendMailImpl(subject, body, userInstance?.email, null, Collections.singleton(userInstance?.email), null, Collections.singleton(new Attachment(name: name, mimeType: mimeType, bytes: bytes)), null, true, false)
    }

    def sendHtmlMailWithAttachment(Collection toList, String subject, String body, List<Attachment> attachments) {
        sendMailImpl(subject, body, null, null, toList, null, attachments, null, true, false)
    }

    def sendHtmlMailWithAttachment(Collection toList, Collection ccList, String subject, String body, byte[] bytes, String name, String mimeType) {
        sendMailImpl(subject, body, null, null, toList, ccList, Collections.singleton(new Attachment(name: name, mimeType: mimeType, bytes: bytes)), null, true, false)
    }

    def sendHtmlMailWithAttachment(User fromUser, Collection toList, Collection ccList, String subject, String body, byte[] bytes, String name, String mimeType) {
        sendMailImpl(subject, body, fromUser?.email, fromUser?.name, toList, ccList, Collections.singleton(new Attachment(name: name, mimeType: mimeType, bytes: bytes)), null, true, false)
    }

    def sendHtmlMailWithAttachment(User fromUser, Collection toList, Collection ccList, String subject, String body, byte[] bytes, String name, String mimeType, Integer port) {
        sendMailImpl(subject, body, fromUser?.email, fromUser?.name, toList, ccList, Collections.singleton(new Attachment(name: name, mimeType: mimeType, bytes: bytes)), port, true, false)
    }

    def sendHtmlMailWithAttachment(User fromUser, Collection toList, Collection ccList, String subject, String body, List<Attachment> attachments, Integer port) {
        sendMailImpl(subject, body, fromUser?.email, fromUser?.name, toList, ccList, attachments, port, true, false)
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
     */
    void sendMailImpl(
            String subject,
            String body,
            String from,
            String fromName,
            Collection<String> to,
            Collection<String> cc,
            Collection<Attachment> attachments,
            Integer port,
            boolean useHtml,
            boolean override) {

        def summary = "email with subject '${subject}' to ${to} from ${from ?: defaultFrom} via ${defaultHost}:${port ?: defaultPort}"

        if (!isMailEnabled && !override) {
            log.info "email disabled: not sending ${summary}"
            return
        }

        log.info "sending ${summmary}"

        try {
            Email email
            if (useHtml) {
                HtmlEmail htmlEmail = new HtmlEmail()
                htmlEmail.setHtmlMsg(body)
                attachments.each {
                    htmlEmail.attach(new ByteArrayDataSource(it.bytes, it.mimeType),
                            it.name, it.name, EmailAttachment.ATTACHMENT)
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

            email.send()
        } catch (Exception e) {
            log.error("Error sending ${summary}", e)
            throw e
        }
    }
}
