package org.pih.warehouse

class InventoryController {
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"];
    
    def beforeInterceptor = [action:this.&auth,except:'login']

    def afterInterceptor = { model, modelAndView ->
       //println "Current view is ${modelAndView.viewName}"
       //if(model.someVar) modelAndView.viewName = "/mycontroller/someotherview"
       //println "View is now ${modelAndView.viewName}"
    }
    
    // defined as a regular method so its private
    def auth() {
	println "checking if user is authenticated $session.user";
	if(!session.user) {
	    println "user in not authenticated";
	    redirect(controller: "user", action: "login");
	    return false
	} else {
	    println "user is authenticated";
	}
    }

    def login = {
	
    }

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [transactions: Transaction.list(params), transactionsCount: Transaction.count()]
    }

    def create = {
        def transaction = new Transaction()
        transaction.properties = params
        return [transaction: transaction]
    }

    def save = {
        def transaction = new Transaction(params)
        if (transaction.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'transaction.label', default: 'Transaction'), transaction.id])}"
            redirect(action: "show", id: transaction.id)
        }
        else {
            render(view: "create", model: [transaction: transaction])
        }
    }

    def show = {
        def transaction = Transaction.get(params.id)
        if (!transaction) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'transaction.label', default: 'Transaction'), params.id])}"
            redirect(action: "list")
        }
        else {
            [transaction: transaction]
        }
    }

    def edit = {
        def transaction = Transaction.get(params.id)
        if (!transaction) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'transaction.label', default: 'Transaction'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [transaction: transaction]
        }
    }

    def update = {
        def transaction = Transaction.get(params.id)
        if (transaction) {
            if (params.version) {
                def version = params.version.toLong()
                if (transaction.version > version) {
                    
                    productInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'transaction.label', default: 'Transaction')] as Object[], "Another user has updated this Transaction while you were editing")
                    render(view: "edit", model: [transaction: transaction])
                    return
                }
            }
            transaction.properties = params
            if (!transaction.hasErrors() && transaction.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'transaction.label', default: 'Transaction'), transaction.id])}"
                redirect(action: "show", id: transaction.id)
            }
            else {
                render(view: "edit", model: [transaction: transaction])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'transaction.label', default: 'Transaction'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def transaction = Transaction.get(params.id)
        if (transaction) {
            try {
                transaction.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'transaction.label', default: 'Transaction'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'transaction.label', default: 'Transaction'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'transaction.label', default: 'Transaction'), params.id])}"
            redirect(action: "list")
        }
    }
}
