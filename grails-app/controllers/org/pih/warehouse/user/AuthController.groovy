package org.pih.warehouse.user

import grails.util.GrailsUtil;

import org.pih.warehouse.core.RoleType;
import org.pih.warehouse.core.User;
import org.pih.warehouse.core.Role;

class AuthController {

	def mailService;
	
    static allowedMethods = [login: "GET", doLogin: "POST", logout: "GET"];
    
    /**
     * Show index page - just a redirect to the list page.
     */
	def index = {    	
		log.info "auth controller index";
		redirect(action: "login", params:params)
	}

	/**
	 * Checks whether there is an authenticated user in the session.
	 */
	def authorized = { 
		if (session.user == null) { 
        	flash.message = "${warehouse.message(code: 'auth.notAuthorized.message')}"
    		redirect(controller: 'auth', action: 'login');
    	}		
	}
                             
    /**
     * Allows user to log into the system.
     */
    def login = {			
		//"${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'user.label', default: 'User'), params.id])}"
	}
	
	
    /** 
     * Performs the authentication logic.
     */
	
	/*
    def handleAuthentication = {
		log.debug "doLogin"		
    		def userInstance = User.findWhere(username:params['email'], password:params['password'])
					
		// Successfully logged in
		if (userInstance) {		
			
			if (!userInstance?.active) { 
				flash.message = "Your account is currently inactive."
				redirect(controller: 'auth', action: 'login');
				return;
			}
			
			// Need to fetch the manager and roles
			def warehouse = userInstance?.warehouse?.name;
			def managerUsername = userInstance?.manager?.username;
			def roles = userInstance?.roles;

			session.user = userInstance;		
			session.warehouse = userInstance.warehouse
			
			
			if (params?.targetUri) { 
				redirect(uri: params.targetUri);
				return;
			}
			redirect(controller:'dashboard',action:'index')
    		
		}
		// Invalid username or password
		else {
			
		    log.info "user does $params.username not exist or password $params.password is incorrect";
		    //flash.message = "Unable to authenticate user with the provided credentials."
	
			userInstance = new User(username:params['username'], password:params['password'])

		    //userInstance = new User();
		    userInstance.errors.rejectValue("version", "default.authentication.failure",
		    	[warehouse.message(code: 'user.label', default: 'User')] as Object[], "Unable to authenticate user with the provided credentials.");
	
		    render(view: "login", model: [userInstance: userInstance])
		}
    }
	*/
	
	def handleLogin = {
		def userInstance = User.findByUsernameOrEmail(params.username, params.username)
		
		TimeZone userTimezone = TimeZone.getTimeZone("America/New_York")
		String browserTimezone = request.getParameter("browserTimezone")
		if (browserTimezone != null) {
			userTimezone = TimeZone.getTimeZone(browserTimezone)
		}
		session.timezone = userTimezone;
		
		if (userInstance) {
			
			if (!userInstance?.active) {
				flash.message = "${warehouse.message(code: 'auth.accountRequestUnderReview.message')}"	
				redirect(controller: 'auth', action: 'login');
				return;
			}

			// Compare encoded/hashed password as well as in cleartext (support existing cleartext passwords)			
			if (userInstance.password == params.password.encodeAsPassword() || userInstance.password == params.password) {			

				// Need to fetch the manager and roles in order to avoid 
				// Hibernate error ("could not initialize proxy - no Session")				
				def warehouse = userInstance?.warehouse?.name;
				def managerUsername = userInstance?.manager?.username;
				def roles = userInstance?.roles;
	
				session.user = userInstance;
				
				// For now, we'll just execute this code in dev environments
				if (GrailsUtil.environment == "development") { 
					// PIMS-782 Force the user to select a warehouse each time
					if (userInstance?.warehouse) { 
						session.warehouse = userInstance.warehouse
					}
					
					if (session?.targetUri) {
						redirect(uri: session.targetUri);
						//session.removeAttribute("targetUri")
						return;
					}
				}
				redirect(controller:'dashboard',action:'index')
			}
			else {
				flash.message = "${warehouse.message(code: 'auth.incorrectPassword.label', args: [params.username])}"		
				userInstance = new User(username:params['username'])				
				userInstance.errors.rejectValue("version", "default.authentication.failure",
					[warehouse.message(code: 'user.label', default: 'User')] as Object[], "${warehouse.message(code: 'auth.unableToAuthenticateUser.message')}");
				render(view: "login", model: [userInstance: userInstance])
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'auth.userNotFound.message', args: [params.username])}"	
			redirect(action:login)
		}
	}
	
	
	/**
	 * Allows user to log out of the system
	 */
	def logout = { 
		def username = session.user.username;    	
		session.user = null;
		session.locale = null;
		session.warehouse = null;
		flash.message = "${warehouse.message(code: 'auth.logoutSuccess.message', args: [username])}"	
		redirect(action:'login')
	}    

	
	/**
	 * Allow user to register a new account
	 */
	def signup = { }
	
	/**
	 * Handle account registration.
	 */
	def handleSignup = { 		
		if ("POST".equalsIgnoreCase(request.getMethod())) { 
			def userInstance = new User();
			userInstance.properties = params
			userInstance.password = params.password.encodeAsPassword();
			userInstance.passwordConfirm = params.passwordConfirm.encodeAsPassword();			
			userInstance.active = Boolean.FALSE;
			
			// Create account 
			if (!userInstance.hasErrors() && userInstance.save(flush: true)) {				
				session.user = userInstance;				
				
				def recipients = [ ];
				def roleAdmin = Role.findByRoleType(RoleType.ROLE_ADMIN)
				if (roleAdmin) {
					def criteria = User.createCriteria()
					recipients = criteria.list {
						roles {
							eq("id", roleAdmin.id)
						}
					}
					if (recipients) {
						recipients.each {
							def subject = "${warehouse.message(code: 'email.userAccountCreated.message', args: [userInstance.username])}"							
							def body = g.render(template:"/email/userAccountCreated", model:[userInstance:userInstance])
							mailService.sendHtmlMail(subject, body.toString(), it.email);
						}
					}
				}
				
				// Send confirmation email to user 
				if (userInstance.email) { 
					def subject = "${warehouse.message(code: 'email.userAccountConfirmed.message')}"
					def body = g.render(template:"/email/userAccountConfirmed", model:[userInstance:userInstance])
					mailService.sendHtmlMail(subject, body.toString(), userInstance.email);
				}

				
				flash.message = "${warehouse.message(code: 'default.create.message', args: [warehouse.message(code: 'user.label'), userInstance.id])}"
				redirect(action:login)
			}			
			else { 
				// Reset the password to what the user entered
				userInstance.password = params.password;
				userInstance.passwordConfirm = params.passwordConfirm;
				//flash.message = "${warehouse.message(code: 'default.error.message', args: [warehouse.message(code: 'user.label', default: 'User'), userInstance.id])}"
				render(view: "signup", model: [userInstance : userInstance]);
			}
		}		
	}

}
