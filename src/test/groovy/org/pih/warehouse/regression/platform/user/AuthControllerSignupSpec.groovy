package org.pih.warehouse.regression.platform.user

import org.pih.warehouse.user.*
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class AuthControllerSignupSpec extends Specification implements ControllerUnitTest<AuthController> {

    void 'an enabled signup without reCAPTCHA redirects to the login page'() {
        given:
        config.openboxes.signup.enabled = true
        config.openboxes.signup.recaptcha.v2.secretKey = ''

        when:
        controller.signup()

        then:
        response.redirectedUrl == '/auth/login'
        flash.message.startsWith('Apologies, but reCAPTCHA is not set up')
    }
}
