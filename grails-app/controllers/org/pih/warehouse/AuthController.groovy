package org.pih.warehouse

class AuthController {

    static allowedMethods = [login: "GET", doLogin: "POST", logout: "GET"];
    
    /**
     * Show index page - just a redirect to the list page.
     */
	def index = {    	
		log.info "auth controller index";
		redirect(action: "login", params:params)
	}
                             
                             
    /**
     * Allows user to log into the system.
     */
    def login = {
		log.debug "debug logger enabled"
		log.error "error logger enabled"
		log.info "show login page";
		String instructions = "To log on as a manager, please use <strong>jmiranda</strong>:<strong>password</strong>.";
		if (!flash.message)
		    flash.message = instructions;
		else 
			flash.message += instructions;
		
		//"${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
	}
	
		
	def switchWarehouse = { 
		log.info "switch warehouse"
	}
	
    /** 
     * Performs the authentication logic.
     */
    def doLogin = {
		log.info "doLogin"
		
    	def userInstance = User.findWhere(username:params['username'], password:params['password'])
    	log.info "WAREHOUSE $params.warehouse.id"
    	def warehouse = Warehouse.get(params.warehouse.id);

    	if (warehouse != null) { 
    		// Save the current warehouse in the session
    		session.warehouse = warehouse;
    		    		
    		// Save the preferred warehouse (first time only)
    		if (userInstance.warehouse == null) { 
	    		userInstance.warehouse = warehouse;
	    		userInstance.save(flush:true);
    		}
    	}		
    			
		
		session.user = userInstance
		if (userInstance) {
		    log.info "user exists $userInstance";
		    redirect(controller:'home',action:'index')
		}
		else {
		    log.info "user does not exist";
		    flash.message = "Unable to authenticate user with the provided credentials."
	
		    //userInstance = new User();
		    //userInstance.errors.rejectValue("version", "default.authentication.failure",
		    //	[message(code: 'user.label', default: 'User')] as Object[], "Unable to authenticate user with the provided credentials.")
	
		    redirect(controller:'user',action:'login')
		}
    }
    
    /**
     * Allows user to log out of the system
     */
    def logout = { 
    	log.info "logout"
    	session.user = null
    	flash.message = "User was successfully logged out."
    	redirect(action:'login')
    }    
    

}
