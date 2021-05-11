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

import com.nimbusds.jose.JWSObject
import org.pih.warehouse.core.User

class AzureAuthService {

    def apiClientService
    def grailsApplication
    def identifierService

    boolean transactional = true

    def getConfig() {
        return apiClientService.get("https://login.microsoftonline.com/openboxes.com/v2.0/.well-known/openid-configuration")
    }

    def getAuthenticationUrl() {
        String authEndpointUrl = config.authorization_endpoint
        String clientId = grailsApplication.config.azure.oauth2.clientId
        String clientSecret = grailsApplication.config.azure.oauth2.clientSecret
        String csrfToken = identifierService.generateIdentifier(10)

        // https://login.microsoftonline.com/{tenant}/oauth2/v2.0/authorize?
        //client_id=6731de76-14a6-49ae-97bc-6eba6914391e
        //&response_type=id_token
        //&redirect_uri=http%3A%2F%2Flocalhost%2Fmyapp%2F
        //&response_mode=form_post
        //&scope=openid
        //&state=12345
        //&nonce=678910

        Map data = [
                client_id : clientId,
                response_type: "code",
                scope: "openid profile email",
                redirect_uri: "https://openboxes.ngrok.io/openboxes/azureAuth/callback",
                state: csrfToken,
                nonce: identifierService.generateIdentifier(5)
        ]

        def request = apiClientService.buildGetRequest(authEndpointUrl, data)

        log.info "Redirecting to " + request.URI.toString()
        return request.URI.toString()
    }

    def getLogoutUrl() {
        return config.end_session_endpoint
    }


    def getToken(String code) {
        String tokenEndpointUrl = config.token_endpoint
        String clientId = grailsApplication.config.azure.oauth2.clientId
        String clientSecret = grailsApplication.config.azure.oauth2.clientSecret

        def requestData = [
                code: code,
                client_id: clientId,
                client_secret: clientSecret,
                redirect_uri: "https://openboxes.ngrok.io/openboxes/azureAuth/callback",
                grant_type: "authorization_code"
        ]
        log.info "tokenEndpointUrl " + tokenEndpointUrl

        return apiClientService.post(tokenEndpointUrl, requestData, false)
    }


    User findOrCreateUser(JWSObject jwsObject) {
        String email = jwsObject.payload.toJSONObject().email
        User user = User.findByUsernameOrEmail(email, email)
        if (!user) {
            user = new User()
            user.username = jwsObject.payload.toJSONObject().email
            user.email = jwsObject.payload.toJSONObject().email
            user.firstName = jwsObject.payload.toJSONObject().given_name
            user.lastName = jwsObject.payload.toJSONObject().family_name
            user.password = identifierService.generateIdentifier(20)
            user.passwordConfirm = user.password
            user.save(flush:true, failOnError: true)
        }
        return user;
    }



}
