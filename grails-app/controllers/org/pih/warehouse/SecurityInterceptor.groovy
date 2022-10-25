package org.pih.warehouse
/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

class SecurityInterceptor {

    static ArrayList controllersWithAuthUserNotRequired = ['test', 'errors']
    static ArrayList actionsWithAuthUserNotRequired = ['status', 'test', 'login', 'logout', 'handleLogin', 'signup', 'handleSignup', 'json', 'updateAuthUserLocale', 'viewLogo', 'changeLocation', 'menu']

    static ArrayList controllersWithLocationNotRequired = ['categoryApi', 'productApi', 'genericApi', 'api']
    static ArrayList actionsWithLocationNotRequired = ['status', 'test', 'login', 'logout', 'handleLogin', 'signup', 'handleSignup', 'json', 'updateAuthUserLocale', 'viewLogo', 'chooseLocation', 'menu']

    def authService

    public SecurityInterceptor() {
        matchAll().except(uri: '/static/**').except(controller: "errors").except(uri: "/info").except(uri: "/health")
    }

    void afterView() {
        // Clear out current user after rendering the view
        authService.currentUser = null
        authService.currentLocation = null
    }
    boolean before() {

        // set user/warehouse, if present, or clear them if not
        authService.currentUser = session.user ?: null
        authService.currentLocation = session.warehouse ?: null

        // This allows requests for the health monitoring endpoint to pass through without a user
        if (controllerName.equals("api") && actionName.equals("status")) {
            return true
        }

        // This allows the megamenu to be g:include'd in the page (allowing for dynamic content to be added)
        if (controllerName.equals("dashboard") && actionName.equals("megamenu")) {
            return true
        }

        // This allows the menu to be g:include'd on mobile page (allowing for dynamic content to be added)
        if (controllerName.equals("mobile") && actionName.equals("menu")) {
            return true
        }

        // Not sure when this happens
        if (params.controller == null) {
            redirect(controller: 'auth', action: 'login')
            return true
        }
        // When a request does not require authentication, we return true
        // FIXME In order to start working on sync use cases, we need to authenticate
        else if (controllersWithAuthUserNotRequired.contains(controllerName)) {
            return true
        }
        // When there's no authenticated user in the session and a request requires authentication
        // we redirect to the auth login page.  targetUri is the URI the user was trying to get to.
        else if (!session.user && !(actionsWithAuthUserNotRequired.contains(actionName))) {
            def targetUri = ""
            // We only want to handle GETs because POSTs would be much more difficult
            if (request.method == "GET") {
                targetUri = (request.forwardURI - request.contextPath)
                if (request.queryString)
                    targetUri += "?" + request.queryString
            }

            // Prevent user from being redirected to invalid pages after re-authenticating
            if (!targetUri.contains("/dashboard/status") && !targetUri.contains("logout")) {
                log.info "Request requires authentication, saving targetUri = " + targetUri
                if (targetUri != "/") {
                    flash.message = "Your session has timed out."
                }
                session.targetUri = targetUri
            } else {
                log.info "Not saving targetUri " + targetUri
            }

            if (RequestUtil.isAjax(request)) {
                redirect(controller: "errors", action: "handleUnauthorized")
                return false
            }

            redirect(controller: 'auth', action: 'login')
            return false
        }

        // When a user has been authenticated, we want to check if they have an active account
        if (session?.user && !session?.user?.active) {
            session.user = null

            if (RequestUtil.isAjax(request)) {
                redirect(controller: "errors", action: "handleUnauthorized")
                return false
            }

            redirect(controller: 'auth', action: 'login')
            return false
        }

        // When a user has not selected a warehouse and they are requesting an action that requires one,
        // we redirect to the choose warehouse page.
        if (!session.warehouse && !(actionsWithLocationNotRequired.contains(actionName) ||
            controllersWithLocationNotRequired.contains(controllerName) || controllerName.endsWith("Api"))) {

            session.warehouseStillNotSelected = true
            log.info "Request ${controllerName}:${actionName} requires location, redirecting to chooseLocation ..."
            redirect(controller: 'dashboard', action: 'chooseLocation')
            return false
        }

        return true
    }
}
