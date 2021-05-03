/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.auth

class OidcService {

    def apiClientService
    def grailsApplication
    def identifierService

    boolean transactional = true

    def getConfig() {
        return apiClientService.get("https://accounts.google.com/.well-known/openid-configuration")
    }

    def authenticate() {
        String authEndpointUrl = config.authorization_endpoint
        String clientId = grailsApplication.config.google.oauth2.clientId
        String clientSecret = grailsApplication.config.google.oauth2.clientSecret
        String csrfToken = identifierService.generateIdentifier(10)

        Map data = [
                client_id : clientId,
                response_type: "code",
                scope: "openid profile email",
                redirect_uri: "https://openboxes.ngrok.io/openboxes/auth/callback",
                state: csrfToken,
                nonce: identifierService.generateIdentifier(5)
        ]



        return apiClientService.get(authEndpointUrl, data)

    }

    def getToken(String code) {
        String tokenEndpointUrl = config.token_endpoint
        String clientId = grailsApplication.config.google.oauth2.clientId
        String clientSecret = grailsApplication.config.google.oauth2.clientSecret

        def requestData = [
                code: code,
                client_id: clientId,
                client_secret: clientSecret,
                redirect_uri: "https://openboxes.ngrok.io/openboxes/auth/callback",
                grant_type: "authorization_code"
        ]
        log.info "tokenEndpointUrl " + tokenEndpointUrl

        return apiClientService.post(tokenEndpointUrl, requestData, false)
    }




}
