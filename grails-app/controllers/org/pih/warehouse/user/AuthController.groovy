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


import org.apache.commons.mail.EmailException
import org.pih.warehouse.core.MailService
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User

class AuthController {

    MailService mailService
    def userService
    def authService
    def grailsApplication

    static allowedMethods = [login: "GET", doLogin: "POST", logout: "GET"]

    /**
     * Show index page - just a redirect to the list page.
     */
    def index = {
        log.info "auth controller index"
        redirect(action: "login", params: params)
    }

    /**
     * Checks whether there is an authenticated user in the session.
     */
    def authorized = {
        if (session.user == null) {
            flash.message = "${warehouse.message(code: 'auth.notAuthorized.message')}"
            redirect(controller: 'auth', action: 'login')
        }
    }

    /**
     * Allows user to log into the system.
     */
    def login = {
        if (session.user) {
            flash.message = "You have already logged in."
            redirect(controller: "dashboard", action: "index")
        }

    }


    /**
     * Performs the authentication logic.
     */
    def handleLogin = {
        def userInstance = User.findByUsernameOrEmail(params.username, params.username)
        if (userInstance) {

            // FIXME Handle setting timezone based on configuration
            TimeZone userTimezone = TimeZone.getTimeZone("America/New_York")
            // Check for user's preferred timezone
            if (userInstance.timezone) {
                userTimezone = TimeZone.getTimeZone(userInstance.timezone)
            }
            // If there's no user preference timezone, use the browser timezone (login page sets parameter)
            else {
                String browserTimezone = request.getParameter("browserTimezone")
                if (browserTimezone != null) {
                    userTimezone = TimeZone.getTimeZone(browserTimezone)
                }
            }
            session.timezone = userTimezone

            // Check if user is active -- redirect back to login page
            if (!userInstance?.active) {
                flash.message = "${warehouse.message(code: 'auth.accountRequestUnderReview.message')}"
                redirect(controller: 'auth', action: 'login')
                return
            }

            // Passwords match
            // Compare encoded/hashed password as well as in cleartext (support existing cleartext passwords)
            //if (userInstance.password == params.password.encodeAsPassword() || userInstance.password == params.password) {
            if (userService.authenticate(params.username, params.password)) {
                // Need to fetch the manager and roles in order to avoid
                // Hibernate error ("could not initialize proxy - no Session")
                // def warehouse = userInstance?.warehouse?.name;
                // def managerUsername = userInstance?.manager?.username;
                // def roles = userInstance?.roles;

                session.user = userInstance
                session.userName = userInstance?.username

                // PIMS-782 Force the user to select a warehouse each time
                if (userInstance?.warehouse && userInstance?.rememberLastLocation) {
                    session.warehouse = userInstance.warehouse
                }

                if (session?.targetUri) {
                    redirect(uri: session.targetUri)
                    return
                }

                redirect(controller: 'dashboard', action: 'index')
            } else {
                // Invalid password
                flash.message = "${warehouse.message(code: 'auth.incorrectPassword.label', args: [params.username])}"
                userInstance = new User(username: params['username'])
                userInstance.errors.rejectValue("version", "default.authentication.failure",
                        [warehouse.message(code: 'user.label', default: 'User')] as Object[], "${warehouse.message(code: 'auth.unableToAuthenticateUser.message')}")
                render(view: "login", model: [userInstance: userInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'auth.userNotFound.message', args: [params.username])}"
            redirect(action: 'login')
        }
    }


    /**
     * Allows user to log out of the system
     */
    def logout = {
        if (session.impersonateUserId) {
            session.user = User.get(session.activeUserId)
            session.impersonateUserId = null
            session.activeUserId = null
            redirect(controller: "dashboard", action: "index")
        } else {
            flash.message = "${warehouse.message(code: 'auth.logoutSuccess.message')}"
            session.invalidate()
            redirect(action: 'login')
        }
    }


    /**
     * Allow user to register a new account
     */
    def signup = {

        Boolean enabled = grailsApplication.config.openboxes.signup.enabled
        if (!enabled) {
            flash.message = "Apologies, but the signup feature is disabled on your system. "  +
                    "Please contact a system administrator for access."
            redirect(controller: "auth", action: "login")
        }

    }

    /**
     * Handle account registration.
     */
    def handleSignup = {

        def userInstance = new User()
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            userInstance.properties = params

            if (params.password) {
                userInstance.password = params.password.encodeAsPassword()
                userInstance.passwordConfirm = params.passwordConfirm.encodeAsPassword()
            }
            userInstance.active = Boolean.FALSE

            // Set the email as username for backwards compatibility since we're no longer including username on signup
            userInstance.username = params.email

            // Create account
            if (!userInstance.hasErrors() && userInstance.save(flush: true)) {
                session.user = userInstance


                // Attempt to add default roles to user instance
                try {
                    def defaultRoles = grailsApplication.config.openboxes.signup.defaultRoles
                    if (!defaultRoles.isEmpty()) {
                        println "Default roles: " + defaultRoles
                        def roleTypes = defaultRoles.split(",")
                        roleTypes.each { roleType ->
                            def role = Role.findByRoleType(roleType)
                            userInstance.addToRoles(role)
                        }

                        if (userInstance.roles) {
                            userInstance.active = Boolean.TRUE
                        }
                        userInstance.save()
                    }
                } catch (Exception e) {
                    log.error("Unable to assign default roles: " + e.message, e)
                }

                // Send email to administrators
                try {
                    def recipients = userService.findUsersByRoleType(RoleType.ROLE_USER_NOTIFICATION)

                    // Send email to user notification recipients
                    if (recipients) {
                        def to = recipients?.collect { it.email }?.unique()
                        def subject = "${warehouse.message(code: 'email.userAccountCreated.message', args: [userInstance.username])}"
                        def body = g.render(template: "/email/userAccountCreated", model: [userInstance: userInstance])
                        mailService.sendHtmlMail(subject, body.toString(), to)
                    }

                    // Send confirmation email to user
                    if (userInstance.email) {
                        def subject = "${warehouse.message(code: 'email.userAccountConfirmed.message', args: [userInstance.email])}"
                        def body = g.render(template: "/email/userAccountConfirmed", model: [userInstance: userInstance])
                        mailService.sendHtmlMail(subject, body.toString(), userInstance.email)
                    }
                } catch (EmailException e) {
                    log.error("Unable to send emails: " + e.message, e)
                }

            } else {
                log.info("There will be errors: " + userInstance.errors)
                // Reset the password to what the user entered
                userInstance.password = params.password
                userInstance.passwordConfirm = params.passwordConfirm
                render(view: "signup", model: [userInstance: userInstance])
                return
            }
        }

        // FIXME For some reason, flash.message does not get displayed on redirect
        //flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'user.label'), userInstance.email])}"
        redirect(action: "login")
    }


    def renderAccountCreatedEmail = {
        def userInstance = User.get(params.id)
        render(template: "/email/userAccountCreated", model: [userInstance: userInstance])
    }

    def renderAccountConfirmedEmail = {
        def userInstance = User.get(params.id)
        render(template: "/email/userAccountConfirmed", model: [userInstance: userInstance])
    }
}
