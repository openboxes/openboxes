package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.json.JsonSlurper
import spock.lang.Specification

class ErrorsControllerSpec extends Specification implements ControllerUnitTest<ErrorsController>, DataTest {

    List<Map> messageCalls = []

    Closure doWithConfig() {{ config ->
        config.openboxes.mail.errors.enabled = true
        config.openboxes.mail.errors.recipients = ["errors@openboxes.com"]
    }}

    void setupSpec() {
        mockDomain(User)
    }

    void setup() {
        views['/email/_errorReport.gsp'] = 'error report'
        controller.userService = Stub(UserService) {
            findUsersByRoleType(_) >> []
        }
        Expando stubMessager = new Expando()
        stubMessager.message = { Map attrs ->
            messageCalls << attrs
            return attrs.code
        }
        controller.metaClass.warehouse = stubMessager
    }

    void "processError shows a success message when the bug report is sent"() {
        given:
        controller.mailService = Stub(MailService) {
            sendHtmlMailWithAttachment(*_) >> true
        }

        when:
        controller.processError()

        then:
        response.redirectedUrl.startsWith('/dashboard/index')
        redirectedFlash() == [message: 'email.errorReportSuccess.message']
        messageCall('email.errorReportSuccess.message') == [code: 'email.errorReportSuccess.message', args: [['errors@openboxes.com']], encodeAs: 'raw']
    }

    void "processError shows an error when the bug report is not sent"() {
        given:
        controller.mailService = Stub(MailService) {
            sendHtmlMailWithAttachment(*_) >> false
        }

        when:
        controller.processError()

        then:
        response.redirectedUrl.startsWith('/dashboard/index')
        redirectedFlash() == [error: 'email.notSent.message']
        messageCall('email.notSent.message') == [code: 'email.notSent.message', args: [['errors@openboxes.com']], encodeAs: 'raw']
    }

    void "processError shows an error when bug reporting is disabled"() {
        given:
        config.openboxes.mail.errors.enabled = false
        MailService mailService = Mock(MailService)
        controller.mailService = mailService

        when:
        controller.processError()

        then:
        0 * mailService._
        response.redirectedUrl.startsWith('/dashboard/index')
        redirectedFlash() == [error: 'email.errorReportDisabled.message']

        cleanup:
        config.openboxes.mail.errors.enabled = true
    }

    private Map messageCall(String code) {
        return messageCalls.find { it.code == code }
    }

    private Map redirectedFlash() {
        String param = new URI(response.redirectedUrl).rawQuery?.split('&')?.find { it.startsWith('flash=') }
        return param ? new JsonSlurper().parseText(URLDecoder.decode(param - 'flash=', 'UTF-8')) as Map : [:]
    }
}
