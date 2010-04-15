package org.pih.warehouse

class UserController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST", login: "GET", doLogin: "POST"]


    def login = {
	println "show login page";
	String instructions = "To log on as a manager, please use <strong>jmiranda</strong>:<strong>password</strong>";
	if (!flash.message)
	    flash.message = instructions;

	//"${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
    }

    def doLogin = {

	println "do login"
	def userInstance = User.findWhere(username:params['username'], password:params['password'])
	println "found user";
	session.user = userInstance
	if (userInstance) {
	    println "user exists $userInstance";
	    redirect(controller:'product',action:'list')
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

     /*
    def doLogin = {
        println "locating user $params";
	def userList = User.list(params)
	println "found users $userList";
        //def userInstance = User.get(params.username);
	if (!userList.isEmpty()) {
	    println "found user $userList";
            //flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            //redirect(action: "list")
	    redirect(action: "list");
        }

    }*/

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [userInstanceList: User.list(params), userInstanceTotal: User.count()]
    }

    def create = {
        def userInstance = new User()
        userInstance.properties = params
        return [userInstance: userInstance]
    }

    def save = {
        def userInstance = new User(params)
        if (userInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])}"
            redirect(action: "show", id: userInstance.id)
        }
        else {
            render(view: "create", model: [userInstance: userInstance])
        }
    }

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
