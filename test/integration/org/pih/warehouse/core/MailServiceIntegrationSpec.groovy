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

import com.dumbster.smtp.SimpleSmtpServer
import grails.test.spock.IntegrationSpec

class MailServiceIntegrationSpec extends IntegrationSpec {

    def mailService

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {

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
    }
}
