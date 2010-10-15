package org.pih.warehouse.user

import org.pih.warehouse.core.User;

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
    def doLogin = {
		log.debug "doLogin"		
    		def userInstance = User.findWhere(username:params['email'], password:params['password'])
			
		//if (!userInstance) = 
		//	userInstance = User.findWhere(email:params['email'], password:params['password']);
		
		// Successfully logged in
		if (userInstance) {			
			// Need to fetch the manager and roles
			def warehouse = userInstance?.warehouse?.name;
			def managerUsername = userInstance?.manager?.username;
			def roles = userInstance?.roles;

			session.user = userInstance;		
			session.warehouse = userInstance.warehouse
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
	def signup = { 

		if ("POST".equalsIgnoreCase(request.getMethod())) { 

			def userInstance = new User();
			userInstance.properties = params
			userInstance.username = params.email;
			// Create account 
			if (!userInstance.hasErrors() && userInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.create.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])}"
				redirect(controller:'dashboard', action:'index')
			}
			// Render errors
			else { 
				//flash.message = "${message(code: 'default.error.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])}"
				render(view: "signup", model: [userInstance : userInstance]);
			}
		}		
	}

}
