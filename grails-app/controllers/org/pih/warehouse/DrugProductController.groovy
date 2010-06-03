package org.pih.warehouse

class DrugProductController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"];
    
    def index = {
        redirect(action: "list", params: params)
    }

    def browse = { 
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [productInstanceList: DrugProduct.list(params), productInstanceTotal: DrugProduct.count()]
    }
    
    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [productInstanceList: DrugProduct.list(params), productInstanceTotal: DrugProduct.count()]
    }

    def create = {
        def productInstance = new DrugProduct()
        productInstance.properties = params
        return [productInstance: productInstance]
    }

    def save = {
        def productInstance = new DrugProduct(params)
        if (productInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'drug.label', default: 'Product'), productInstance.id])}"
            redirect(action: "show", id: productInstance.id)
        }
        else {
            render(view: "create", model: [productInstance: productInstance])
        }
    }

    def show = {
        def productInstance = DrugProduct.get(params.id)
        if (!productInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'drug.label', default: 'Product'), params.id])}"
            redirect(action: "list")
        }
        else {
            [productInstance: productInstance]
        }
    }

    def edit = {
        def productInstance = DrugProduct.get(params.id)
        if (!productInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'drug.label', default: 'Product'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [productInstance: productInstance]
        }
    }

    def update = {
        def productInstance = DrugProduct.get(params.id)
        if (productInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (productInstance.version > version) {
                    
                    productInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'drug.label', default: 'Product')] as Object[], "Another user has updated this Product while you were editing")
                    render(view: "edit", model: [productInstance: productInstance])
                    return
                }
            }
            productInstance.properties = params
            if (!productInstance.hasErrors() && productInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'drug.label', default: 'Product'), productInstance.id])}"
                redirect(action: "show", id: productInstance.id)
            }
            else {
                render(view: "edit", model: [productInstance: productInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'drug.label', default: 'Product'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def productInstance = DrugProduct.get(params.id)
        if (productInstance) {
            try {
                productInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'drug.label', default: 'Product'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'drug.label', default: 'Product'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'drug.label', default: 'Product'), params.id])}"
            redirect(action: "list")
        }
    }
}
