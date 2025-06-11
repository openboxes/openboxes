package org.pih.warehouse

import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService

class BasicAuthInterceptor {

    UserService userService

    BasicAuthInterceptor() {
        // TODO Need to make sure there are no requests to the API outside the scope of the /api path
        match(uri: '/api/**')
    }

    boolean before() {

        // User was already authenticated
        if (session.user) {
            log.info "user is authenticated " + session.userInstance
            return true
        }

        else {

            // If there's an auth header, we'll attempt to authenticate
            def authHeader = request.getHeader('Authorization')
            if (authHeader) {
                // Authenticate user credentials
                def (userIdentifier, password) = decodeBasicAuth(authHeader)
                Boolean authenticated = userService.authenticate(userIdentifier, password)

                // FIXME This is required because authentication does not return the user.
                //  It might make more sense to return the user as part of the auth process.
                def user = User.findByUsernameOrEmail(userIdentifier, userIdentifier)
                if (!user?.active || !authenticated) {
                    response.sendError(401)
                    return false
                }
                setUser(user)
            }
        }
        return true
    }

    def decodeBasicAuth(String authHeader) {
        // Strip "Basic " from auth header
        def encoded = authHeader[6..-1]
        def decoded = new String(Base64.decoder.decode(encoded))
        return decoded.split(":", 2)
    }

    private void setUser(User user) {
        session.user = user
        // Set saved location, but don't override the session warehouse if it's already set
        if (user?.warehouse && user?.rememberLastLocation && !session.warehouse) {
            session.warehouse = user.warehouse
        }

    }

}