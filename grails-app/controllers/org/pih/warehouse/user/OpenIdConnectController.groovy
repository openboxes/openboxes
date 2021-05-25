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
import org.pih.warehouse.core.User

class OpenIdConnectController {

    def grailsApplication
    def identifierService
    def openIdConnectService

    def authenticate = {
        session.state = identifierService.generateIdentifier(10)
        redirect url: openIdConnectService.getAuthenticationUrl(params.id, session.state)
    }

    def callback = {
        log.info "Params " + params

        if (params.state != "${session.state}") {
            flash.message = "The anti-forgery token returned by the identity provider is invalid."
            redirect(controller: "auth", action: "login")
            return
        }

        if (params.error) {
            flash.message = "${params.error_description}"
        } else if (!params.code) {
            flash.message = "Identity provider did not return proper response"
        }

        if (params.code) {
            def token = openIdConnectService.fetchToken(params.code, params.id)

            if (token.error) {
                log.error "Unexpected error while retrieving token: " + token
                throw new IllegalArgumentException("Unexpected error while retrieving token: " + token.error)
            }

            if (!token.id_token) {
                throw new IllegalArgumentException("Expected token to contain id_token")
            }

            try {
                JWSObject jwsObject = JWSObject.parse(token.id_token)
                User user = openIdConnectService.findOrCreateUser(jwsObject)
                session.user = user
                // In case we want to show a logout (end session URL)
                session.oauthProvider = params.id
            } catch (Exception e) {
                flash.message = e.message
            }
            redirect(controller: "auth", action: "login")
            return
        }

        redirect(controller: "auth", action: "login")
    }

    def logout = {
        def logoutUrl = openIdConnectService.getLogoutUrl(params.id)
        if (logoutUrl) {
            redirect(url: logoutUrl)
        }
        else {
            redirect(controller: "auth", action: "login")
        }
    }
}
