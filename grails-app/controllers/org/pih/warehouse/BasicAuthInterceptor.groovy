package org.pih.warehouse

import grails.core.GrailsApplication
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService

class BasicAuthInterceptor {

    UserService userService
    GrailsApplication grailsApplication

    BasicAuthInterceptor() {
        // TODO Need to make sure there are no requests to the API outside the scope of the /api path
        //  for now these requests can be authenticated using the cookie auth.
        match(uri: '/api/**')
    }

    boolean before() {

        // Check if the basic auth interceptor is enabled
        Boolean enabled = grailsApplication.config.getProperty('openboxes.interceptor.basicAuth.enabled', Boolean, false)
        if (!enabled) return true

        // User was already authenticated
        if (session.user) {
            log.info "user is authenticated " + session.userInstance
            return true
        }

        // If there's an auth header, we'll attempt to authenticate
        def authHeader = request.getHeader('Authorization')
        if (authHeader) {
            // Authenticate user credentials
            def (username, password) = decodeBasicAuth(authHeader)
            Boolean authenticated = userService.authenticate(username, password)

            // FIXME This is required because authentication does not return the user.
            //  It might make more sense to return the user as part of the auth process.
            def user = User.findByUsernameOrEmail(username, username)
            if (!user?.active || !authenticated) {
                response.sendError(401)
                return false
            }
            // Initialize the user session
            initializeUserSession(user)
        }
        return true
    }

    def decodeBasicAuth(String authHeader) {
        // Strip "Basic " from auth header
        def encoded = authHeader[6..-1]
        def decoded = new String(Base64.decoder.decode(encoded))
        return decoded.split(":", 2)
    }

    private void initializeUserSession(User user) {
        session.user = user
        // Set saved location, but don't override the session warehouse if it's already set
        if (user?.warehouse && user?.rememberLastLocation && !session.warehouse) {
            session.warehouse = user.warehouse
        }

    }

}