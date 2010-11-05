package org.pih.warehouse.user;

import org.pih.warehouse.core.User;
import org.pih.warehouse.core.Role;
import org.pih.warehouse.core.RoleType;

class UserController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
    def mailService;
	
    /**
     * Show index page - just a redirect to the list page.
     */
    def index = {    	
    	log.info "user controller index"
        redirect(action: "list", params: params)
    }

    /**
     * Show list of users
     */
    def list = {
    	log.info "show a list of users"
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [userInstanceList: User.list(params), userInstanceTotal: User.count()]
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
        if (userInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])}"
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
    	log.info "show user"
        def userInstance = User.get(params.id)
        if (!userInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
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
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [userInstance: userInstance]
        }
    }
	
	
	def toggleActivation = { 
		def userInstance = User.get(params.id)
		if (!userInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
		}
		else {			
			userInstance.active = !userInstance.active;
			if (!userInstance.hasErrors() && userInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])}"
				
				// FIXME Refactor (place code in service layer)
				// Send notification emails to all administrators
				def recipients = [ ];
				def roleAdmin = Role.findByRoleType(RoleType.ROLE_ADMIN)
				if (roleAdmin) {
					def criteria = User.createCriteria()
					recipients = criteria.list {
						roles { 
							eq("id", roleAdmin.id)
						}
					}
				}
				recipients << userInstance;
				if (recipients) {
					recipients.each {
						println "Sending email to " + it.email;
						def subject = "User account has been " + (userInstance?.active ? "activated" : "de-activated");
						def message = "User account " + userInstance?.username + " has been " + (userInstance?.active ? "activated" : "de-activated");
						mailService.sendMail(subject, message, it.email);
					}
				}
			}
		}
		redirect(action: "show", id: userInstance.id)
	}

    /**
     * Update a user 
     */
    def update = {
    	log.info "update user"
        def userInstance = User.get(params.id)
        if (userInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (userInstance.version > version) {
                    userInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'user.label', default: 'User')] as Object[], "Another user has updated this User while you were editing")
                    render(view: "edit", model: [userInstance: userInstance])
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
			}
			else { 
				userInstance.properties = params	
				userInstance.passwordConfirm = userInstance.password 			
			}
			
			
            if (!userInstance.hasErrors() && userInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])}"
                redirect(action: "show", id: userInstance.id)
            }
            else {
                render(view: "edit", model: [userInstance: userInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            redirect(action: "list")
        }
    }
    
    /**
     * Delete a user
     */
    def delete = {    	
        def userInstance = User.get(params.id)
        if (userInstance) {			
			if (userInstance?.id == session?.user?.id) { 
				flash.message = "${message(code: 'default.cannot.delete.self.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
				redirect(action: "show", id: params.id)
			}
			else { 			
	            try {
	                userInstance.delete(flush: true)
	                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
	                redirect(action: "list")
	            }
	            catch (org.springframework.dao.DataIntegrityViolationException e) {
	                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
	                redirect(action: "show", id: params.id)
	            }
			}
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
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
			"${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
		}
	} 


	def uploadPhoto = { 
		
		def userInstance = User.get(params.id);		
		if (userInstance) { 
			def photo = request.getFile("photo");
			if (!photo?.empty && photo.size < 1024*1000) { // not empty AND less than 1MB
				userInstance.photo = photo.bytes;			
		        if (!userInstance.hasErrors() && userInstance.save(flush: true)) {
		            flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])}"
		        }
		        else {
					// there were errors, the photo was not saved
		        }
			}
            redirect(action: "show", id: userInstance.id)
		} 
		else { 
			"${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
		}
	}

    
}
