/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.user

import com.nimbusds.jose.JWSObject
import grails.converters.JSON
import org.pih.warehouse.core.MailService
import org.pih.warehouse.core.User

class GoogleAuthController {

    MailService mailService
    def userService
    def authService
    def grailsApplication
    def recaptchaService
    def ravenClient
    def googleAuthService
    def identifierService

    def config = {
        render ([data:googleAuthService.config] as JSON)
    }


    def callback = {
        log.info "callback: " + params


        if (params.code) {
            def token = googleAuthService.getToken(params.code)

            // FIXME Better error handling
            if (token.error) {
                log.error "Unexpected error while retrieving token: " + token
                throw new IllegalArgumentException("Unexpected error while retrieving token: " + token.error)
            }

            if (!token.id_token) {
                throw new IllegalArgumentException("Expected token to contain id_token")
            }

            JWSObject jwsObject = JWSObject.parse(token.id_token)
            User user = googleAuthService.findOrCreateUser(jwsObject)
            log.info "USER " + user?.id
            session.user = user
            session.oauthProvider = "google"
            redirect(controller: "auth", action: "login")
        }


        render "callback successful"
    }

    def tokenCallback = {
        log.info "token callback " + params

    }

    def login = {
        redirect(url: googleAuthService.authenticationUrl)
    }

    def logout = {
        if (googleAuthService.logoutUrl) {
            redirect(url: googleAuthService.logoutUrl)
        }
        else {
            redirect(controller: "auth", action: "login")
        }
    }


}
