package org.pih.warehouse

class UserController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST", login: "GET", doLogin: "POST"]

    /**
     * Allows user to log into the system.
     */
    def login = {
		log.info "show login page";
		String instructions = "To log on as a manager, please use <strong>jmiranda</strong>:<strong>password</strong>";
		if (!flash.message)
		    flash.message = instructions;
	
		//"${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
    }

    /** 
     * Performs the authentication logic.
     */
    def doLogin = {
		log.info "doLogin"
		
    	def userInstance = User.findWhere(username:params['username'], password:params['password'])
    	//log.info "$params.warehouse.id"
    	//def warehouse = Warehouse.get(params);
    	//if (warehouse)
    	//	log.info "$warehouse.name"
    	//userInstance.warehouse = warehouse		
		session.user = userInstance
		
		
		if (userInstance) {
		    println "user exists $userInstance";
		    redirect(controller:'home',action:'dashboard')
		}
		else {
		    println "user does not exist";
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

    /**
     * Show user preferences.
     */
    def preferences = {
    	log.info "show preferences"
    }
    
    /**
     * Show index page - just a redirect to the list page.
     */
    def index = {    	
    	log.info "doLogin"
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
     * Show the edit form for a user
     */
    def edit = {
        def userInstance = User.get(params.id)
        if (!userInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [userInstance: userInstance]
        }
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
                    
                    userInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'user.label', default: 'User')] as Object[], "Another user has updated this User while you were editing")
                    render(view: "edit", model: [userInstance: userInstance])
                    return
                }
            }
            userInstance.properties = params
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
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            redirect(action: "list")
        }
    }
    
}
