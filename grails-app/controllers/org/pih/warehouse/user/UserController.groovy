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
import org.pih.warehouse.core.*

import java.awt.Image as AWTImage
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.imageio.ImageIO as IIO
import java.awt.Graphics2D



class UserController {

    static allowedMethods = [save: "POST", update: "POST", delete: "GET"]
    MailService mailService;
	def userService
	
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
		def userInstanceTotal = 0;
		
		params.max = Math.min(params.max ? params.int('max') : 15, 100)
		
        def query = params.q ? "%" + params.q + "%" : ""

        userInstanceList = userService.findUsers(query, params)
        userInstanceTotal = userInstanceList.totalCount
        //userInstanceList = User.findAllByUsernameLikeOrEmailLike(term, term, params)
        //userInstanceTotal = User.countByUsernameLikeOrEmailLike(term, term, params);

		[userInstanceList: userInstanceList, userInstanceTotal: userInstanceTotal]
	}
	
	def sendTestEmail = {
		def userInstance = User.get(params.id)
		if (!userInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
			redirect(action: "list")
		}
		else {
			
			try { 
				String subject = "${warehouse.message(code:'system.testEmailSubject.label')}"
				String body = g.render(template:"/email/userCanReceiveEmail", model:[userInstance:userInstance])
					
				mailService.sendHtmlMail(subject, body, userInstance?.email)
				flash.message = "Email successfully sent to " + userInstance?.email
				
			} catch (Exception e) { 
				flash.message = "Error sending email " + e.message
			}
		}
		redirect(action: "show", id: userInstance?.id)
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
		
		userInstance.password = params?.password?.encodeAsPassword();
		userInstance.passwordConfirm = params?.passwordConfirm?.encodeAsPassword();

        if (userInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'user.label'), userInstance.id])}"
            redirect(action: "show", id: userInstance.id)
        }
        else {
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
        }
        else {
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
		}
		else {
			[userInstance: userInstance]
		}
	}
	
	def cropPhoto = { 
		log.info "change photo for given user"
		def userInstance = User.get(params.id)
		if (!userInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
			redirect(action: "list")
		}
		else {
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
    	log.info "edit user"
        def userInstance = User.get(params.id)
        if (!userInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
            redirect(action: "list")
        }
        else {
            def locations = Location.AllDepotWardAndPharmacy()
            return [userInstance: userInstance, locations: locations]
        }
    }
	
	
	def toggleActivation = { 
		def userInstance = User.get(params.id)
		if (!userInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
		}
		else {			
			userInstance.active = !userInstance.active;
			if (!userInstance.hasErrors() && userInstance.save(flush: true)) {
				flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'user.label'), userInstance.id])}"				
				sendUserStatusChanged(userInstance)
			}
			else {
				def locations = Location.AllDepotWardAndPharmacy()
				render(view: "edit", model: [userInstance: userInstance, locations: locations])
				return;
			}
		}
		redirect(action: "show", id: userInstance.id)
	}

    /**
     * Update a user 
     */
    def update = {
        log.info(params)
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

            // Password in the db is different from the one specified
            // so the user must have changed the password.  We need
            // to compare the password with confirm password before
            // setting the new password in the database
            if (userInstance.password != params.password) {
                userInstance.properties = params
                userInstance.password = params?.password?.encodeAsPassword();
                userInstance.passwordConfirm = params?.passwordConfirm?.encodeAsPassword();
            } else {
                userInstance.properties = params
                // Needed to bypass the password == passwordConfirm validation
                userInstance.passwordConfirm = userInstance.password
            }

            // If a non-admin user edits their profile they will not have access to
            // the roles or location roles, so we need to prevent the updateRoles
            // method from being called.
            if (params.locationRolePairs) {
                updateRoles(userInstance, params.locationRolePairs)
            }

			if (params.timezone) {
				session.timezone = TimeZone.getTimeZone(params.timezone)

			}

            if (!userInstance.hasErrors() && userInstance.save(flush: true)) {
                // if this is the current user, update reference to that user in the session
                if (session.user.id == userInstance?.id) {
                    session.user = User.get(userInstance?.id)
                }

                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'user.label'), userInstance.id])}"
                redirect(action: "show", id: userInstance.id)
            } else {
                def locations = Location.AllDepotWardAndPharmacy()
                render(view: "edit", model: [userInstance: userInstance, locations: locations])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
            redirect(action: "list")
        }
    }

   private void updateRoles(user, locationRolePairs){
     def newAndUpdatedRoles = locationRolePairs.keySet().collect{ locationId ->
       if(locationRolePairs[locationId]){
         def location = Location.get(locationId)
         def role = Role.get(locationRolePairs[locationId])
         def existingRole = user.locationRoles.find{it.location == location}
         if(existingRole){
            existingRole.role = role
         }else{
           def newLocationRole = new LocationRole(user: user, location:location, role: role)
           user.addToLocationRoles(newLocationRole)
         }
       }
     }
     def rolesToRemove = user.locationRoles.findAll{ oldRole ->
        !locationRolePairs[oldRole.location.id]
     }
     rolesToRemove.each{ 
       user.removeFromLocationRoles(it)       
     }
   }
   
   
   def disableTranslationMode = {
	   log.info ("params " + params)
	   
	   session.useDebugLocale = false
	   redirect(controller: "dashboard", action: "index")	   
   }

   def enableTranslationMode = {
	   log.info ("params " + params)
	   session.useDebugLocale = true
	   redirect(controller: "dashboard", action: "index")
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
			//def locale = new Locale(params.locale)
			//if (session.user) { 
			//	session.user.locale = locale;
			//	session.locale = null				
			//}
			//else { 
			//	session.locale = locale;
			//}
			session.useDebugLocale = true
		}
		else { 
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
			redirect(uri: params.targetUri);
			return;
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
				redirect(action: "show", id: params.id)
			}
			else { 			
	            try {
	                userInstance.delete(flush: true)
	                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'user.label'), params.id])}"
	                redirect(action: "list")
	            }
	            catch (org.springframework.dao.DataIntegrityViolationException e) {
	                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'user.label'), params.id])}"
	                redirect(action: "show", id: params.id)
	            }
			}
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
            redirect(action: "list")
        }
    }

	/**
	 * View user's profile photo 
	 */
	def viewPhoto = { 
		def userInstance = User.get(params.id);		
		if (userInstance) { 
			byte[] image = userInstance.photo 
			response.outputStream << image
		} 
		else { 
			"${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
		}
	} 

	
	def viewThumb = { 
		def width = params.width ?: 128
		def height = params.height ?: 128
		
		def userInstance = User.get(params.id);
		if (userInstance) {
			byte[] bytes = userInstance.photo
			try { 
				resize(bytes, response.outputStream, width, height)
			} catch (Exception e) { 
				//"${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
				response.outputStream << bytes
			}
		}
		else { 
			"${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
		}
	}

	def uploadPhoto = { 
		
		def userInstance = User.get(params.id);		
		if (userInstance) { 
			def photo = request.getFile("photo");
			
			// List of OK mime-types
			def okcontents = [
				'image/png',
				'image/jpeg',
				'image/gif'
			]
			
			if (! okcontents.contains(photo.getContentType())) {
				log.info "Photo is not correct type"
				flash.message = "Photo must be one of: ${okcontents}"
				render(view: "changePhoto", model: [userInstance: userInstance])
				return;
			}
			
			if (!photo?.empty && photo.size < 1024*1000) { // not empty AND less than 1MB
				userInstance.photo = photo.bytes;			
		        if (!userInstance.hasErrors() && userInstance.save(flush: true)) {
		            flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'user.label'), userInstance.id])}"
					sendUserPhotoChanged(userInstance)
		        }
		        else {
		            flash.message = "${warehouse.message(code: 'default.not.updated.message', args: [warehouse.message(code: 'user.label'), userInstance.id])}"
					render(view: "uploadPhoto", model: [userInstance: userInstance])
					return
		        }
			}
			else { 
	            flash.message = "${warehouse.message(code: 'user.photoTooLarge.message', args: [warehouse.message(code: 'user.label'), userInstance.id])}"
				
			}
            redirect(action: "show", id: userInstance.id)
		} 
		else { 
			"${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label'), params.id])}"
		}
	}

	
	/**
	 * 
	 * @param userInstance
	 * @return
	 */
	def sendUserStatusChanged(User userInstance) {		
		try { 
			// Send notification emails to all administrators
			def users = userService.findUsersByRoleType(RoleType.ROLE_ADMIN)
			users << userInstance;
			
			def recipients = users.collect { it.email }
            def activatedOrDeactivated = "${userInstance.active ? warehouse.message(code:'user.activated.label') : warehouse.message(code:'user.disabled.label')}"
            def subject = "${warehouse.message(code: 'email.userAccountActivated.message', args: [userInstance.username,activatedOrDeactivated])}"
			//def subject = "${warehouse.message(code:'email.userAccountChanged.message',args:[userInstance?.email,activatedOrDeactivated])}";
			def body = "${g.render(template:'/email/userAccountActivated',model:[userInstance:userInstance])}"
			mailService.sendHtmlMail(subject, body.toString(), recipients);
			flash.message = "${warehouse.message(code:'email.sent.message',args:[userInstance.email])}"
		} 
		catch (Exception e) { 
			flash.message = "${warehouse.message(code:'email.notSent.message',args:[userInstance.email])}: ${e.message}"
		}
		
	}
	
	
	/**
	 * 
	 * @param userInstance
	 * @return
	 */
	def sendUserPhotoChanged(User userInstance) {
		try {
			def subject = "${warehouse.message(code:'email.userPhotoChanged.message',args:[userInstance?.email])}";
			def body = "${g.render(template:'/email/userPhotoChanged',model:[userInstance:userInstance])}"
			mailService.sendHtmlMailWithAttachment(userInstance, subject, body.toString(), userInstance.photo, "photo.png", "image/png");
			flash.message = "${warehouse.message(code:'email.sent.message',args:[userInstance.email])}"
		} 
		catch (Exception e) { 
			flash.message = "${warehouse.message(code:'email.notSent.message',args:[userInstance.email])}: ${e.message}"
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
				subject	"${warehouse.message(code:'email.userConfirmed.message',args:[userInstance.username])}"
				html "${g.render(template:"/email/userConfirmed", model:[userInstance:userInstance])}"
			}
			flash.message = "${warehouse.message(code:'email.sent.message',args:[userInstance.email])}: ${e.message}"
			//flash.message = “Confirmation email sent to ${userInstance.emailAddress}”
		} catch(Exception e) {
			log.error "Problem sending email $e.message", e
			flash.message = "${warehouse.message(code:'email.notSent.message',args:[userInstance.email])}: ${e.message}"
		}
	}
		
	//static scale = {
	//	BufferedImage thumbnail = Scalr.resize(image, 150);
	//}
	
	static resize = { bytes, out, maxW, maxH ->
		AWTImage ai = new ImageIcon(bytes).image
		int width = ai.getWidth( null )
		int height = ai.getHeight( null )
	
		println ("Resize ${width} x ${height} image to ${maxW} x ${maxH}")
		
		def limits = 0..2000
		assert limits.contains( width ) && limits.contains( height ) : 'Picture is either too small or too big!'
	
		float aspectRatio = width / height 
		float requiredAspectRatio = maxW / maxH
	
		int dstW = 0
		int dstH = 0
		if (requiredAspectRatio < aspectRatio) {
			dstW = maxW 
			dstH = Math.round( maxW / aspectRatio)
		} else {
			dstH = maxH 
			dstW = Math.round(maxH * aspectRatio)
		}
	
		BufferedImage bi = new BufferedImage(dstW, dstH,   BufferedImage.TYPE_INT_RGB)
		Graphics2D g2d = bi.createGraphics() 
		//g2d.setComposite(AlphaComposite.Src);
		g2d.drawImage(ai, 0, 0, dstW, dstH, null, null)
		g2d.dispose();
	
		IIO.write( bi, 'JPEG', out )
	}
    
}
