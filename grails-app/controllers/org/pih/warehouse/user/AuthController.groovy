package org.pih.warehouse.user

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
        	flash.message = 'You are not authorized to access this page.  '
    		redirect(controller: 'auth', action: 'login');
    	}		
	}
                             
    /**
     * Allows user to log into the system.
     */
    def login = {			
		//"${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
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
		    	[message(code: 'user.label', default: 'User')] as Object[], "Unable to authenticate user with the provided credentials.");
	
		    render(view: "login", model: [userInstance: userInstance])
		}
    }
	*/
	
	def handleLogin = {
		def userInstance = User.findByUsernameOrEmail(params.username, params.username)
		if (userInstance) {
			
			if (!userInstance?.active) {
				flash.message = "Your account request has been received and is under review by the system administrator. Please contact the system administrator if you have any questions or concerns."
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
				
				// PIMS-782 Force the user to select a warehouse each time
				//session.warehouse = userInstance.warehouse
				
				//if (params?.targetUri) {
				//	redirect(uri: params.targetUri);
				//	return;
				//}
				redirect(controller:'dashboard',action:'index')
					
					
			}
			else {
				flash.message = "Incorrect password for user <b>${params.username}</b>"				
				userInstance = new User(username:params['username'])				
				userInstance.errors.rejectValue("version", "default.authentication.failure",
					[message(code: 'user.label', default: 'User')] as Object[], "Unable to authenticate user with the provided credentials.");
				render(view: "login", model: [userInstance: userInstance])
			}
		}
		else {
			flash.message = "User not found for username or email <b>${params.username}</b>"
			redirect(action:login)
		}
	}
	
	
	/**
	 * Allows user to log out of the system
	 */
	def logout = { 
		def username = session.user.username;    	
		session.user = null;
		session.warehouse = null;
		flash.message = "User ${username} was successfully logged out."
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
							def subject = "A new user account has been created";
							def message = "Please sign-in to activate the account " + userInstance?.username + "."
							mailService.sendMail(subject, message, it.email);
						}
					}
				}
				
				// Send confirmation email to user 
				if (userInstance.email) { 
					def subject = "Your user account has been created";
					def message = "Please wait for an administrator to activate your account." 
					mailService.sendMail(subject, message, userInstance.email);
				}

				
				flash.message = "${message(code: 'default.create.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])}"
				redirect(controller:'dashboard', action:'index')
			}			
			else { 
				// Reset the password to what the user entered
				userInstance.password = params.password;
				userInstance.passwordConfirm = params.passwordConfirm;
				//flash.message = "${message(code: 'default.error.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])}"
				render(view: "signup", model: [userInstance : userInstance]);
			}
		}		
	}

}
