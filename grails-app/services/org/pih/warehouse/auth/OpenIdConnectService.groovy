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
import grails.validation.ValidationException
import org.pih.warehouse.core.User

class OpenIdConnectService {

    def apiClientService
    def grailsApplication
    def identifierService

    boolean transactional = true

    def getConfig(String providerId) {
        return grailsApplication.config.openboxes.oauth2Providers."${providerId}"
    }

    def getOpenIdConfiguration(String providerId) {
        def config = getConfig(providerId)
        def openIdConfigurationUrl = config.openIdConfigurationUrl
        return openIdConfigurationUrl ? apiClientService.get(openIdConfigurationUrl) : config.openIdConfiguration
    }

    def getAuthenticationUrl(String providerId, String state) {

        def config = getConfig(providerId)
        def openIdConfig = getOpenIdConfiguration(providerId)

        String clientId = config.clientId
        String clientSecret = config.clientSecret
        String authEndpointUrl = openIdConfig.authorization_endpoint

        log.info "authEndpointUrl " + authEndpointUrl

        if (!authEndpointUrl) {
            throw new Exception("No auth endpoint URL")
        }

        Map requestData = [
                client_id : clientId,
                response_type: config.responseType,
                scope: config.scopes,
                redirect_uri: getRedirectUri(providerId),
                state: state,
                nonce: identifierService.generateIdentifier(9)
        ]
        // Add optional config
        if (config.domainHint) {
            requestData.put("hd", config.domainHint)
        }

        def request = apiClientService.buildGetRequest(authEndpointUrl, requestData)
        return request.URI.toString()
    }

    def getRedirectUri(String providerId) {
        def config = getConfig(providerId);

        // Use user-overriden the redirectUrl
        if (config.redirectUrl)
            return config.redirectUrl

        // Otherwise generate a default redirectUrl
        def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
        return g.createLink(controller: "openIdConnect", action: "callback", id: providerId, absolute: true)

    }

    def getLogoutUrl(String providerId) {
        return getOpenIdConfiguration(providerId)?.end_session_endpoint
    }

    def fetchToken(String code, String providerId) {
        def config = getConfig(providerId)
        def openIdConfig = getOpenIdConfiguration(providerId)
        String tokenEndpointUrl = openIdConfig.token_endpoint
        def requestData = [
                code: code,
                client_id: config.clientId,
                client_secret: config.clientSecret,
                redirect_uri: getRedirectUri(providerId),
                grant_type: config.grantType
        ]
        log.info "tokenEndpointUrl " + tokenEndpointUrl

        return apiClientService.post(tokenEndpointUrl, requestData, false)
    }

    User findOrCreateUser(JWSObject jwsObject) {
        log.info "jwsObject " + jwsObject.payload.toJSONObject()
        String email = jwsObject.payload.toJSONObject().email
        log.info "Find user by username or email: ${email}"
        if (!email) {
            throw new IllegalArgumentException("No email claim returned by identity provider")
        }
        User user = User.findByEmail(email)
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
