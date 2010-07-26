package org.pih.warehouse.product

class DrugProductController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [drugProductInstanceList: DrugProduct.list(params), drugProductInstanceTotal: DrugProduct.count()]
    }

    def create = {
        def drugProductInstance = new DrugProduct()
        drugProductInstance.properties = params
        return [drugProductInstance: drugProductInstance]
    }

    def save = {
        def drugProductInstance = new DrugProduct(params)
        if (drugProductInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'drugProduct.label', default: 'DrugProduct'), drugProductInstance.id])}"
            redirect(action: "show", id: drugProductInstance.id)
        }
        else {
            render(view: "create", model: [drugProductInstance: drugProductInstance])
        }
    }

    def show = {
        def drugProductInstance = DrugProduct.get(params.id)
        if (!drugProductInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'drugProduct.label', default: 'DrugProduct'), params.id])}"
            redirect(action: "list")
        }
        else {
            [drugProductInstance: drugProductInstance]
        }
    }

    def edit = {
        def drugProductInstance = DrugProduct.get(params.id)
        if (!drugProductInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'drugProduct.label', default: 'DrugProduct'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [drugProductInstance: drugProductInstance]
        }
    }

    def update = {
        def drugProductInstance = DrugProduct.get(params.id)
        if (drugProductInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (drugProductInstance.version > version) {
                    
                    drugProductInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'drugProduct.label', default: 'DrugProduct')] as Object[], "Another user has updated this DrugProduct while you were editing")
                    render(view: "edit", model: [drugProductInstance: drugProductInstance])
                    return
                }
            }
            drugProductInstance.properties = params
            if (!drugProductInstance.hasErrors() && drugProductInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'drugProduct.label', default: 'DrugProduct'), drugProductInstance.id])}"
                redirect(action: "show", id: drugProductInstance.id)
            }
            else {
                render(view: "edit", model: [drugProductInstance: drugProductInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'drugProduct.label', default: 'DrugProduct'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def drugProductInstance = DrugProduct.get(params.id)
        if (drugProductInstance) {
            try {
                drugProductInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'drugProduct.label', default: 'DrugProduct'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'drugProduct.label', default: 'DrugProduct'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'drugProduct.label', default: 'DrugProduct'), params.id])}"
            redirect(action: "list")
        }
    }
}
