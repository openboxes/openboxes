package org.pih.warehouse.user

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
	
		
	def switchWarehouse = { 
		log.info "switch warehouse"
	}
	
    /** 
     * Performs the authentication logic.
     */
    def doLogin = {
		log.error "doLogin"		
    	def userInstance = User.findWhere(username:params['username'], password:params['password'])
    					
		if (userInstance) {
        	log.error "warehouse id = ${params.warehouse.id}"	
        	def warehouse = null;
        	if (params.warehouse.id!='null') { 
            	log.error "looking up warehouse by id = ${params.warehouse.id}";
            	warehouse = Warehouse.get(params.warehouse.id);        		
        	}
    		log.error "warehouse = ${warehouse}"
        	if (warehouse) {     	    	
	    		log.error "saving warehouse ${warehouse?.name} to ${userInstance?.username}"
	    		// Save the current warehouse in the session
	    		session.warehouse = warehouse;
	    		    		
	    		// Save the user's preferred warehouse (if it's not set already)
	    		log.error "user.warehouse = ${userInstance?.warehouse?.name}"
	    		if (userInstance.warehouse) { 
	    			log.error "user does not have a preferred warehouse; setting preferred warehouse to ${warehouse?.name}"
		    		userInstance.warehouse = warehouse;
		    		userInstance.save(flush:true);
	    		}
	    		
	    		// Successfully logged in and select a warehouse
	    		log.error "user exists $userInstance";
	    		log.error "user chose a valid warehouse $warehouse";
	    		session.user = userInstance;    		
	    	    redirect(controller:'home',action:'index')
	    	}	
        	else { 
        		log.error "ask user to choose a warehouse"
        		//flash.message = "Please choose a valid warehouse.";

        		userInstance = new User(username:params['username'], password:params['password'])
    		    userInstance.errors.rejectValue("version", "default.authentication.failure",
    			    	[message(code: 'user.label', default: 'User')] as Object[], "Unable to authenticate user with no warehouse.")

        		render(view: "login", model: [userInstance: userInstance])
        	}    		    		
		}
		else {
			
		    log.error "user does not exist or password is incorrect";
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
    

}
