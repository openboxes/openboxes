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

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwt
import io.jsonwebtoken.impl.DefaultJwtParser
import org.apache.http.client.methods.HttpGet
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.User

class OpenIdConnectService {

    def apiClientService
    def grailsApplication
    def identifierService
    def userDataService

    boolean transactional = true

    def getConfig() {
        return apiClientService.get(grailsApplication.config.openboxes.auth.keycloak.openIdConfigurationUrl)
    }

    boolean isEnabled() {
        return grailsApplication.config.openboxes.auth.keycloak.enabled?:false
    }

    boolean isAlwaysRedirect() {
        return grailsApplication.config.openboxes.auth.keycloak.alwaysRedirect?:false
    }

    def getEndSessionEndpointUrl() {
        String endSessionEndpointUrl = config.end_session_endpoint
        HttpGet request =  apiClientService.buildGet(endSessionEndpointUrl)
        return request.URI.toString()
    }


    def getTokenEndpointUrl() {
        String tokenEndpointUrl = config.token_endpoint
        HttpGet request =  apiClientService.buildGet(tokenEndpointUrl)
        return request.URI.toString()
    }

    def getAuthenticationUrl() {
        String authEndpointUrl = config.authorization_endpoint
        String clientId = grailsApplication.config.openboxes.auth.keycloak.clientId
        String clientSecret = grailsApplication.config.openboxes.auth.keycloak.clientSecret
        String scope = grailsApplication.config.openboxes.auth.keycloak.scope
        String responseType = grailsApplication.config.openboxes.auth.keycloak.responseType
        String csrfToken = identifierService.generateIdentifier(10)
        String defaultRedirectUri = linkGenerator.createLink(controller: "auth", action: "callback", absolute: true)
        log.info "default redirect URI ${defaultRedirectUri}"
        String redirectUri = grailsApplication.config.openboxes.auth.keycloak.redirectUri?:defaultRedirectUri
        log.info "redirect URI ${redirectUri}"
        String nonce = identifierService.generateIdentifier(5)
        Map data = [
                client_id : clientId,
                response_type: responseType,
                scope: scope,
                redirect_uri: redirectUri,
                state: csrfToken,
                nonce: nonce
        ]
        log.info "authEndpointUrl ${authEndpointUrl}"
        HttpGet request =  apiClientService.buildGet(authEndpointUrl, data)
        return request.URI.toString()
    }

    JSONObject fetchToken(String code) {
        String tokenEndpointUrl = config.token_endpoint
        String clientId = grailsApplication.config.openboxes.auth.keycloak.clientId
        String clientSecret = grailsApplication.config.openboxes.auth.keycloak.clientSecret
        String defaultRedirectUri = linkGenerator.createLink(controller: "auth", action: "callback", absolute: true)
        String redirectUri = grailsApplication.config.auth.openboxes.keycloak.redirectUri?:defaultRedirectUri
        def requestData = [
                code: code,
                client_id: clientId,
                client_secret: clientSecret,
                redirect_uri: redirectUri,
                grant_type: "authorization_code"
        ]

        def requestHeaders = [
                "Content-Type": "application/x-www-form-urlencoded"
        ]

        return apiClientService.post(tokenEndpointUrl, requestData, requestHeaders)
    }

    def getLinkGenerator() {
        return ApplicationHolder.application.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
    }

    Map parseToken(String idToken) {
        Jwt jwt = new DefaultJwtParser().parse(idToken)
        Claims claims = (Claims) jwt.getBody()

        claims.each {
            log.info "claims " + it.key + " = " + it.value
        }

        Map data = [:]
        return claims.each {
            data.put(it.key, it.value)
        }
        return data
    }

    User findOrCreateUser(Map userData) {
        log.info "userData " + userData
        String email = userData.email
        log.info "Find user by username or email: ${email}"
        if (!email) {
            throw new IllegalArgumentException("No email claim returned by identity provider")
        }
        User user = User.findByEmail(email)
        if (!user) {
            user = new User()
            user.email = userData.email
            user.username = userData.preferred_username
            user.firstName = userData.given_name
            user.lastName = userData.family_name
            user.password = identifierService.generateIdentifier(20)
            user.passwordConfirm = user.password
            user.save(flush:true, failOnError: true)
        }

        if (userData?.realm_access?.roles) {
            log.info "Found user roles " + userData.realm_access.roles
            userData?.realm_access?.roles.each { String roleName ->
                Role role = Role.findByName(roleName)
                log.info "found role by name ${roleName}"
                if (role) {
                    log.info "Added role ${role} to user ${user}"
                    user.addToRoles(role)
                }
            }
            user.save(flush:true, failOnError: true)
        }
        return user;
    }

}
