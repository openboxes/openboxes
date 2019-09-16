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

import org.apache.commons.mail.ByteArrayDataSource
import org.apache.commons.mail.EmailAttachment
import org.apache.commons.mail.HtmlEmail
import org.apache.commons.mail.SimpleEmail
import org.codehaus.groovy.grails.commons.ConfigurationHolder

import javax.mail.internet.InternetAddress

class MailService {

    boolean transactional = false
    def userService
    def grailsApplication
    def config = ConfigurationHolder.config

    String getDefaultFrom() {
        return config.grails.mail.from
    }

    String getDefaultHost() {
        return config.grails.mail.host
    }

    Integer getDefaultPort() {
        Integer.valueOf(config.grails.mail.port)
    }

    String getUsername() {
        return config.grails.mail.username
    }

    String getPassword() {
        return config.grails.mail.password
    }

    Boolean getDebug() {
        return config.grails.mail.debug
    }

    String getPrefix() {
        return config.grails.mail.prefix
    }


    /**
     * @return
     */
    def isMailEnabled() {
        log.info "grailsApplication.config.grails.mail.enabled '" + grailsApplication.config.grails.mail.enabled + "'"
        Boolean isMailEnabled = grailsApplication.config?.grails?.mail?.enabled?.toBoolean()
        log.info(isMailEnabled ? "Mail is enabled" : "Mail is disabled")
        return isMailEnabled
    }

    /**
     * @param subject
     * @param msg
     * @param to
     * @return
     */
    def sendMail(String subject, String msg, String to) {
        sendMail(subject, msg, [to], null)
    }

    /**
     * @param subject
     * @param msg
     * @param to
     * @return
     */
    def sendMail(String subject, String msg, Collection to, Integer port) {
        if (isMailEnabled()) {
            log.info "Sending text email '" + subject + "' to " + to
            try {
                //SimpleEmail is the class which will do all the hard work for you
                SimpleEmail email = new SimpleEmail()
                email.setCharset("UTF-8")
                email.setHostName(defaultHost)

                // override port
                email.setSmtpPort(port ?: defaultPort)

                to.each {
                    email.addTo(it)
                }

                email.setFrom(defaultFrom)
                email.setSubject("${prefix} " + subject)
                email.setMsg(msg)

                if (debug) {
                    email.setDebug(debug)

                }
                // Authenticate
                if (username && password) {
                    email.setAuthentication(username, password)
                }
                email.send()
            } catch (Exception e) {
                log.error("Error sending plaintext email message with subject " + subject + " to " + to, e)
                throw e
            }
        }
    }


    /**
     * Send html email
     *
     * @param subject
     * @param htmlMessage
     * @param to
     * @return
     */
    def sendHtmlMail(String subject, String htmlMessage, String[] to) {
        log.debug "Sending email to array " + to
        sendHtmlMail(subject, htmlMessage, to, null)

    }


    /**
     *
     * @param subject
     * @param htmlMessage
     * @param to
     * @return
     */
    def sendHtmlMail(String subject, String htmlMessage, String to) {
        sendHtmlMail(subject, htmlMessage, [to], null, false)
    }

    def sendHtmlMail(String subject, String htmlMessage, String to, Integer port) {
        sendHtmlMail(subject, htmlMessage, [to], port, false)
    }

    def sendHtmlMail(String subject, String htmlMessage, String to, Integer port, Boolean override) {
        sendHtmlMail(subject, htmlMessage, [to], port, override)
    }


    def sendHtmlMail(String subject, String body, Collection to) {
        sendHtmlMail(subject, body, to, null, false)
    }

    /**
     * @param subject
     * @param body
     * @param to
     * @return
     */
    def sendHtmlMail(String subject, String body, Collection to, Integer port, Boolean override) {
        log.info "Sending email with subject ${subject} to ${to} from ${getDefaultFrom()} via ${defaultHost}:${port?:defaultPort}"
        if (isMailEnabled() || override) {
            log.info "Sending html email '" + subject + "' to " + to
            try {
                // Create the email message
                HtmlEmail email = new HtmlEmail()
                email.setCharset("UTF-8")
                email.setHostName(defaultHost)
                to.each {
                    email.addTo(it)
                }

                email.setFrom(defaultFrom)
                email.setSmtpPort(port ?: defaultPort)
                email.setSubject("${prefix} " + subject)
                email.setHtmlMsg(body)
                email.setTextMsg(subject)

                // Authenticate
                if (username && password) {
                    email.setAuthentication(username, password)
                }

                email.send()
            } catch (Exception e) {
                log.error("Error sending HTML email message with subject " + subject + " to " + to, e)
                throw e
            }
        }
    }


    /**
     *
     * @param to
     * @param subject
     * @param body
     * @param bytes
     * @param name
     * @param mimeType
     * @return
     */
    def sendHtmlMailWithAttachment(String to, String subject, String body, byte[] bytes, String name, String mimeType) {
        def toList = new ArrayList()
        toList.add(to)
        sendHtmlMailWithAttachment(null, toList, subject, body, bytes, name, mimeType, null)
    }

    /**
     *
     * @param userInstance
     * @param subject
     * @param body
     * @param bytes
     * @param name
     * @param mimeType
     * @return
     */
    def sendHtmlMailWithAttachment(User userInstance, String subject, String body, byte[] bytes, String name, String mimeType) {
        sendHtmlMailWithAttachment(userInstance, userInstance?.email, subject, body, bytes, name, mimeType, null)
    }

    /**
     *
     * @param toList
     * @param subject
     * @param body
     * @param attachments
     * @return
     */
    def sendHtmlMailWithAttachment(Collection toList, String subject, String body, List<Attachment> attachments) {
        sendHtmlMailWithAttachment(null, toList, [], subject, body, attachments, null)
    }

    /**
     *
     * @param toList
     * @param ccList
     * @param subject
     * @param body
     * @param bytes
     * @param name
     * @param mimeType
     * @return
     */
    def sendHtmlMailWithAttachment(Collection toList, Collection ccList, String subject, String body, byte[] bytes, String name, String mimeType) {
        sendHtmlMailWithAttachment(null, toList, ccList, subject, body, bytes, name, mimeType, null)
    }

    /**
     *
     * @param toList
     * @param ccList
     * @param subject
     * @param body
     * @param bytes
     * @param name
     * @param mimeType
     * @return
     */
    def sendHtmlMailWithAttachment(User fromUser, Collection toList, Collection ccList, String subject, String body, byte[] bytes, String name, String mimeType) {
        sendHtmlMailWithAttachment(fromUser, toList, ccList, subject, body, bytes, name, mimeType, null)
    }

    /**
     *
     * @param fromUser
     * @param toList
     * @param ccList
     * @param subject
     * @param body
     * @param bytes
     * @param name
     * @param mimeType
     * @params port* @return
     */
    def sendHtmlMailWithAttachment(User fromUser, Collection toList, Collection ccList, String subject, String body, byte[] bytes, String name, String mimeType, Integer port) {
        List<Attachment> attachments = []
        Attachment attachment = new Attachment(name: name, mimeType: mimeType, bytes: bytes)
        attachments.add(attachment)
        sendHtmlMailWithAttachment(fromUser, toList, ccList, subject, body, attachments, port)
    }

    /**
     *
     * @param toList
     * @param ccList
     * @param subject
     * @param body
     * @param attachments
     * @param port
     * @return
     */
    def sendHtmlMailWithAttachment(User fromUser, Collection toList, Collection ccList, String subject, String body, List<Attachment> attachments, Integer port) {
        log.info("Sending email with attachment " + toList)
        if (isMailEnabled()) {
            try {
                // Create the email message
                HtmlEmail email = new HtmlEmail()
                email.setCharset("UTF-8")
                email.setHostName(defaultHost)

                // Override smtp port
                email.setSmtpPort(port ?: defaultPort)

                email.setFrom(defaultFrom)
                toList.each { to -> email.addTo(to) }
                if (ccList) {
                    ccList.each { cc -> email.addCc(cc) }
                }

                email.setSubject("${prefix} " + subject)
                email.setHtmlMsg(body)

                // Override from user
                if (fromUser) {
                    email.setFrom(fromUser.email, fromUser.name)
                }

                // Authenticate
                if (username && password) {
                    email.setAuthentication(username, password)
                }

                // add the attachment
                attachments.each {
                    email.attach(new ByteArrayDataSource(it.bytes, it.mimeType),
                            it.name, it.name, EmailAttachment.ATTACHMENT)
                }

                // send the email
                email.send()
            } catch (Exception e) {
                log.error "Problem sending email $e.message", e
            }
        }
    }

    /**
     *
     * @param toList
     * @param ccList
     * @param subject
     * @param body
     * @param bytes
     * @param name
     * @param mimeType
     * @return
     */
    def sendHtmlMailWithAttachment(message) {
        log.info("Sending email with attachment " + message.to)

        if (isMailEnabled()) {
            try {
                // Create the email message
                HtmlEmail email = new HtmlEmail()
                email.setCharset("UTF-8")
                email.setHostName(message.host ?: defaultHost)
                email.setSmtpPort(message.port ?: defaultPort)

                // Set from, to, cc, subject, and body
                email.setFrom(message.from ?: defaultFrom)
                email.setSubject("${prefix} ${message.subject}")
                email.setHtmlMsg(message.body)
                email.setTo(message.to.collect { new InternetAddress(it) })
                if (message.cc) email.setCc(message.cc.collect { new InternetAddress(it) })
                if (message.bcc) email.setBcc(message.bcc.collect { new InternetAddress(it) })

                // Authenticate
                if (username && password) {
                    email.setAuthentication(username, password)
                }

                // add the attachment
                email.attach(new ByteArrayDataSource(message.attachment, message.mimeType),
                        message.attachmentName, message.attachmentName, EmailAttachment.ATTACHMENT)

                // send the email
                email.send()
            } catch (Exception e) {
                log.error "Problem sending email $e.message", e
            }
        }
    }
}
