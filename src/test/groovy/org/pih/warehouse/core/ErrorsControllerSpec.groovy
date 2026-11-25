package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.json.JsonSlurper
import spock.lang.Specification
import testutil.MessageLocalizerStub

class ErrorsControllerSpec extends Specification implements ControllerUnitTest<ErrorsController>, DataTest {

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
        controller.messageLocalizer = MessageLocalizerStub.MESSAGE_LOCALIZER_STUB
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

    private Map redirectedFlash() {
        String param = new URI(response.redirectedUrl).rawQuery?.split('&')?.find { it.startsWith('flash=') }
        return param ? new JsonSlurper().parseText(URLDecoder.decode(param - 'flash=', 'UTF-8')) as Map : [:]
    }
}
