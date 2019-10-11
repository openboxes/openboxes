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

import grails.plugin.springcache.annotations.CacheFlush
import grails.validation.ValidationException
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationRole
import org.pih.warehouse.core.MailService
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.User

import javax.imageio.ImageIO as IIO
import javax.swing.ImageIcon
import java.awt.Graphics2D
import java.awt.Image as AWTImage
import java.awt.image.BufferedImage

class UserController {

    static allowedMethods = [save: "POST", update: "POST", delete: "GET"]
    MailService mailService
    def userService
    def locationService

    /**
     * Show index page - just a redirect to the list page.
     */
    def index = {
        log.info "user controller index"
        redirect(action: "list", params: params)
    }

    def redirect = {
        redirect(controller: "user", action: "edit", id: params.id)
    }

    /**
     * Show list of users
     */
    def list = {

        println params
        def userInstanceList = []
        def userInstanceTotal = 0

        params.max = Math.min(params.max ? params.int('max') : 15, 100)

        def query = params.q ? "%" + params.q + "%" : ""

        userInstanceList = userService.findUsers(query, params)
        userInstanceTotal = userInstanceList.totalCount

        [userInstanceList: userInstanceList, userInstanceTotal: userInstanceTotal]
    }


    def impersonate = {
        def userInstance = User.get(params.id)
        if (session.impersonateUserId) {
            flash.message = "Already impersonstating user ${session.user.username}"
        } else {

            if (!userInstance?.active) {
                throw new IllegalStateException("Impersonating an inactive user is unconscionable and strictly prohibited")
            }

            session.impersonateUserId = userInstance?.id
            session.activeUserId = session?.user?.id
        }
        redirect(controller: "dashboard", action: "index")
    }

    def sendTestEmail = {
        def userInstance = User.get(params.id)
        if (!userInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
            redirect(action: "list")
        } else {

            try {
                String subject = "${warehouse.message(code: 'system.testEmailSubject.label')}"
                String body = g.render(template: "/email/userCanReceiveEmail", model: [userInstance: userInstance])

                mailService.sendHtmlMail(subject, body, userInstance?.email)
                flash.message = "Email successfully sent to " + userInstance?.email

            } catch (Exception e) {
                flash.message = "Error sending email " + e.message
            }
        }
        redirect(action: "edit", id: userInstance?.id)
    }


    /**
     * Create a user
     */
    def create = {
        log.info "create a new user based on request parameters"
        def userInstance = new User()
        userInstance.properties = params

        return [userInstance: userInstance]
    }

    /**
     * Save a user
     */
    def save = {
        log.info "attempt to save the user; show form with validation errors on failure"
        def userInstance = new User(params)

        userInstance.password = params?.password?.encodeAsPassword()
        userInstance.passwordConfirm = params?.passwordConfirm?.encodeAsPassword()

        if (userInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'user.label'), userInstance.id])}"
            redirect(action: "edit", id: userInstance.id)
        } else {
            render(view: "create", model: [userInstance: userInstance])
        }
    }


    /**
     * Show a user
     */
    def show = {
        def userInstance = User.get(params.id)
        if (!userInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
            redirect(action: "list")
        } else {
            [userInstance: userInstance]
        }
    }

    /**
     * Allow user to change their avatar/photo.
     */
    def changePhoto = {
        log.info "change photo for given user"
        def userInstance = User.get(params.id)
        if (!userInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
            redirect(action: "list")
        } else {
            [userInstance: userInstance]
        }
    }

    def cropPhoto = {
        log.info "change photo for given user"
        def userInstance = User.get(params.id)
        if (!userInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
            redirect(action: "list")
        } else {
            [userInstance: userInstance]
        }
    }


    /**
     * Show user preferences.
     */
    def preferences = {
        log.info "show user preferences"
    }


    /**
     * Show the edit form for a user
     */
    def edit = {

        def userInstance = User.get(params.id)
        if (!userInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
            redirect(action: "list")
        } else {
            def locations = locationService.getLoginLocations(session.warehouse).sort()
            return [userInstance: userInstance, locations: locations]
        }
    }


    def toggleActivation = {
        def userInstance = User.get(params.id)
        if (!userInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
        } else {
            userInstance.active = !userInstance.active
            if (!userInstance.hasErrors() && userInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'user.label'), userInstance.id])}"
                sendUserStatusChanged(userInstance)
            } else {
                def locations = Location.AllDepotWardAndPharmacy()
                render(view: "edit", model: [userInstance: userInstance, locations: locations])
                return
            }
        }
        redirect(action: "edit", id: userInstance.id)
    }

    /**
     * Update a user
     */
    def update = {

        def userInstance = User.get(params.id)
        if (userInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (userInstance.version > version) {
                    userInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'user.label')] as Object[], "Another user has updated this User while you were editing")
                    def locations = Location.AllDepotWardAndPharmacy()
                    render(view: "edit", model: [userInstance: userInstance, locations: locations])
                    return
                }
            }

            try {
                userInstance = userService.updateUser(params.id, session.user.id, params)
                // Update session data if the user is editing their own profile
                if (session.user.id == userInstance?.id) {
                    session.user = User.get(userInstance?.id)
                    if (params.timezone) {
                        session.timezone = TimeZone.getTimeZone(params.timezone)
                    }
                }

                if (!flash.message)
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'user.label'), userInstance.id])}"

                redirect(action: "edit", id: userInstance.id)

            } catch (ValidationException e) {
                userInstance = User.read(params.id)
                userInstance.errors = e.errors
                def locations = Location.AllDepotWardAndPharmacy()
                render(view: "edit", model: [userInstance: userInstance, locations: locations])
            }

        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
            redirect(action: "list")
        }
    }


    def disableLocalizationMode = {
        session.useDebugLocale = false
        if (!grailsApplication.config.openboxes.locale.custom.enabled) {
            flash.message = "${g.message(code: 'localization.invalid.custom.message')}"
        }

        def referer = request.getHeader("Referer")
        if (referer) {
            redirect(url: referer)
        } else {
            redirect(controller: "dashboard", action: "index")
        }
    }

    def enableLocalizationMode = {
        session.useDebugLocale = true
        if (!grailsApplication.config.openboxes.locale.custom.enabled) {
            flash.message = "${g.message(code: 'localization.invalid.custom.message')}"
        }

        def referer = request.getHeader("Referer")
        if (referer) {
            redirect(url: referer)
        } else {
            redirect(controller: "dashboard", action: "index")
        }

    }

    /**
     * Updates the locale of the default user
     * Used by the locale selectors in the footer
     */
    @CacheFlush(["megamenuCache"])
    def updateAuthUserLocale = {

        log.info "update auth user locale " + params
        log.info params.locale == 'debug'
        if (params.locale == 'debug') {
            session.useDebugLocale = true
        } else {
            session.useDebugLocale = false
            // if no locale specified, do nothing
            if (!params.locale) {
                redirect(controller: "dashboard", action: "index")
            }

            // convert the passed locale parameter to an actual locale
            Locale locale = new Locale(params.locale)

            // if this isn't a valid locale, do nothing
            if (!locale) {
                redirect(controller: "dashboard", action: "index")
            }

            // fetch an instance of authenticated user
            def userInstance = User.get(session?.user?.id)
            if (userInstance) {
                userInstance.locale = locale
                userInstance.save(flush: true)

                session.locale = null
                // update the reference to the user in the session
                session.user = User.get(userInstance.id)
            }

            // when user is anonymous (not logged in)
            else {
                session.locale = locale
            }
        }

        log.info "Redirecting to " + params?.targetUri
        if (params?.targetUri) {
            redirect(uri: params.targetUri)
            return
        }

        // redirect to the dashboard
        redirect(controller: "dashboard", action: "index")
    }

    /**
     * Delete a user
     */
    def delete = {

        log.info(params)

        def userInstance = User.get(params.id)
        if (userInstance) {
            if (userInstance?.id == session?.user?.id) {
                flash.message = "${warehouse.message(code: 'default.cannot.delete.self.message', args: [warehouse.message(code: 'user.label'), params.id])}"
                redirect(action: "edit", id: params.id)
            } else {
                try {
                    userInstance.delete(flush: true)
                    flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'user.label'), params.id])}"
                    redirect(action: "list")
                }
                catch (org.springframework.dao.DataIntegrityViolationException e) {
                    flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'user.label'), params.id])}"
                    redirect(action: "edit", id: params.id)
                }
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
            redirect(action: "list")
        }
    }

    /**
     * View user's profile photo
     */
    def viewPhoto = {
        def userInstance = User.get(params.id)
        if (userInstance) {
            byte[] image = userInstance.photo
            response.outputStream << image
        } else {
            "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
        }
    }


    def viewThumb = {
        def width = params.width ?: 128
        def height = params.height ?: 128

        def userInstance = User.get(params.id)
        if (userInstance) {
            byte[] bytes = userInstance.photo
            try {
                resize(bytes, response.outputStream, width, height)
            } catch (Exception e) {
                response.outputStream << bytes
            }
        } else {
            "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
        }
    }

    def uploadPhoto = {

        def userInstance = User.get(params.id)
        if (userInstance) {
            def photo = request.getFile("photo")

            // List of OK mime-types
            def okcontents = [
                    'image/png',
                    'image/jpeg',
                    'image/gif'
            ]

            if (!okcontents.contains(photo.getContentType())) {
                log.info "Photo is not correct type"
                flash.message = "Photo must be one of: ${okcontents}"
                render(view: "changePhoto", model: [userInstance: userInstance])
                return
            }

            if (!photo?.empty && photo.size < 1024 * 1000) { // not empty AND less than 1MB
                userInstance.photo = photo.bytes
                if (!userInstance.hasErrors() && userInstance.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'user.label'), userInstance.id])}"
                    sendUserPhotoChanged(userInstance)
                } else {
                    flash.message = "${warehouse.message(code: 'default.not.updated.message', args: [warehouse.message(code: 'user.label'), userInstance.id])}"
                    render(view: "uploadPhoto", model: [userInstance: userInstance])
                    return
                }
            } else {
                flash.message = "${warehouse.message(code: 'user.photoTooLarge.message', args: [warehouse.message(code: 'user.label'), userInstance.id])}"

            }
            redirect(action: "edit", id: userInstance.id)
        } else {
            "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
        }
    }

    def createLocationRoles = {
        LocationRole locationRoleInstance = new LocationRole()
        locationRoleInstance.user = User.get(params.user.id)
        render(template: "/locationRole/form", model: [locationRoleInstance: locationRoleInstance])
    }

    def editLocationRole = {
        LocationRole locationRoleInstance = LocationRole.get(params.id)
        render(template: "/locationRole/form", model: [locationRoleInstance: locationRoleInstance])
    }

    def deleteLocationRole = {
        LocationRole locationRole = params.id ? LocationRole.get(params.id) : null
        User user = locationRole.user
        user.removeFromLocationRoles(locationRole)
        locationRole.delete()
        user.save(failOnError: true)
        redirect(action: "edit", id: user.id)
    }

    def saveLocationRole = {
        log.info("save location role " + params)
        User user = params.user.id ? User.get(params.user.id) : null
        Location location = params.location?.id ? Location.get(params.location.id) : null
        List<Role> roles = params.list("role.id").collect { roleId -> Role.get(roleId) }

        // Update existing role
        LocationRole locationRole = LocationRole.get(params.id)
        if (locationRole && roles.size() == 1) {
            locationRole.role = roles.first()
            locationRole.location = location
            user.addToLocationRoles(locationRole)
        }
        // Create new roles
        else {
            List<LocationRole> locationRoles = LocationRole.findAllByUserAndLocation(user, location)
            roles.each { role ->
                LocationRole foundLocationRole = locationRoles.find { it.role?.id == role?.id }
                if (!foundLocationRole) {
                    foundLocationRole = new LocationRole()
                    foundLocationRole.role = role
                    foundLocationRole.location = location
                    user.addToLocationRoles(foundLocationRole)
                }
            }
        }
        user.save(failOnError: true)

        redirect(action: "edit", id: params.user.id)
    }

    /**
     *
     * @param userInstance
     * @return
     */
    def sendUserStatusChanged(User userInstance) {
        try {
            // Send notification emails to all administrators
            def users = userService.findUsersByRoleType(RoleType.ROLE_USER_NOTIFICATION)

            // Include the user whose status has changed
            users << userInstance

            def recipients = users.collect { it.email }
            def activatedOrDeactivated = "${userInstance.active ? warehouse.message(code: 'user.activated.label') : warehouse.message(code: 'user.disabled.label')}"
            def subject = "${warehouse.message(code: 'email.userAccountActivated.message', args: [userInstance.username, activatedOrDeactivated])}"
            def body = "${g.render(template: '/email/userAccountActivated', model: [userInstance: userInstance])}"
            mailService.sendHtmlMail(subject, body.toString(), recipients)
            flash.message = "${warehouse.message(code: 'email.sent.message')}"
        }
        catch (Exception e) {
            flash.message = "${warehouse.message(code: 'email.notSent.message', args: [userInstance.email])}: ${e.message}"
        }

    }


    /**
     *
     * @param userInstance
     * @return
     */
    def sendUserPhotoChanged(User userInstance) {
        try {
            def subject = "${warehouse.message(code: 'email.userPhotoChanged.message', args: [userInstance?.email])}"
            def body = "${g.render(template: '/email/userPhotoChanged', model: [userInstance: userInstance])}"
            mailService.sendHtmlMailWithAttachment(userInstance, subject, body.toString(), userInstance.photo, "photo.png", "image/png")
            flash.message = "${warehouse.message(code: 'email.sent.message')}"
        }
        catch (Exception e) {
            flash.message = "${warehouse.message(code: 'email.notSent.message', args: [userInstance.email])}: ${e.message}"
        }
    }

    /**
     * Grails 'mail' way to send an email
     *
     * @param userInstance
     * @return
     */
    def sendUserConfirmed(User userInstance) {

        try {
            sendMail {
                to "${userInstance.email}"
                subject "${warehouse.message(code: 'email.userConfirmed.message', args: [userInstance.username])}"
                html "${g.render(template: "/email/userConfirmed", model: [userInstance: userInstance])}"
            }
            flash.message = "${warehouse.message(code: 'email.sent.message')}: ${e.message}"
            //flash.message = “Confirmation email sent to ${userInstance.emailAddress}”
        } catch (Exception e) {
            log.error "Problem sending email $e.message", e
            flash.message = "${warehouse.message(code: 'email.notSent.message', args: [userInstance.email])}: ${e.message}"
        }
    }

    // TODO Consider moving this to a utility or service
    static resize = { bytes, out, maxW, maxH ->
        AWTImage ai = new ImageIcon(bytes).image
        int width = ai.getWidth(null)
        int height = ai.getHeight(null)

        println("Resize ${width} x ${height} image to ${maxW} x ${maxH}")

        def limits = 0..2000
        assert limits.contains(width) && limits.contains(height): 'Picture is either too small or too big!'

        float aspectRatio = width / height
        float requiredAspectRatio = maxW / maxH

        int dstW = 0
        int dstH = 0
        if (requiredAspectRatio < aspectRatio) {
            dstW = maxW
            dstH = Math.round(maxW / aspectRatio)
        } else {
            dstH = maxH
            dstW = Math.round(maxH * aspectRatio)
        }

        BufferedImage bi = new BufferedImage(dstW, dstH, BufferedImage.TYPE_INT_RGB)
        Graphics2D g2d = bi.createGraphics()
        g2d.drawImage(ai, 0, 0, dstW, dstH, null, null)
        g2d.dispose()

        IIO.write(bi, 'JPEG', out)
    }
}
