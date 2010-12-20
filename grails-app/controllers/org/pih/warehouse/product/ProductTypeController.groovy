package org.pih.warehouse.product

class ProductTypeController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [productTypeInstanceList: ProductType.list(params), productTypeInstanceTotal: ProductType.count()]
    }

    def create = {
        def productTypeInstance = new ProductType()
        productTypeInstance.properties = params
        return [productTypeInstance: productTypeInstance]
    }

    def save = {
        def productTypeInstance = new ProductType(params)
		
		
		productTypeInstance?.categories?.clear();
		println "size: " + productTypeInstance?.categories?.size()
		params.each {
			println ("category: " + it.key +  " starts with category_ " + it.key.startsWith("category_"))
			if (it.key.startsWith("category_")) {
				def category = Category.get((it.key - "category_") as Integer);
				log.info "adding " + category?.name
				productTypeInstance.addToCategories(category)
			}
		  }

        if (productTypeInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'productType.label', default: 'ProductType'), productTypeInstance.id])}"
            redirect(action: "list", id: productTypeInstance.id)
        }
        else {
            render(view: "create", model: [productTypeInstance: productTypeInstance])
        }
    }

    def show = {
        def productTypeInstance = ProductType.get(params.id)
        if (!productTypeInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'productType.label', default: 'ProductType'), params.id])}"
            redirect(action: "list")
        }
        else {
            [productTypeInstance: productTypeInstance]
        }
    }

    def edit = {
        def productTypeInstance = ProductType.get(params.id)
        if (!productTypeInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'productType.label', default: 'ProductType'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [productTypeInstance: productTypeInstance]
        }
    }

    def update = {
        def productTypeInstance = ProductType.get(params.id)
        if (productTypeInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (productTypeInstance.version > version) {
                    
                    productTypeInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'productType.label', default: 'ProductType')] as Object[], "Another user has updated this ProductType while you were editing")
                    render(view: "edit", model: [productTypeInstance: productTypeInstance])
                    return
                }
            }
            productTypeInstance.properties = params
			
			productInstance?.categories?.clear();
			params.each {
				println ("category: " + it.key +  " starts with category_ " + it.key.startsWith("category_"))
				
				if (it.key.startsWith("category_")) {
					def category = Category.get((it.key - "category_") as Integer);
					log.info "adding " + category?.name
					productInstance.addToCategories(category)
				}
			  }

			
            if (!productTypeInstance.hasErrors() && productTypeInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'productType.label', default: 'ProductType'), productTypeInstance.id])}"
                redirect(action: "list", id: productTypeInstance.id)
            }
            else {
                render(view: "edit", model: [productTypeInstance: productTypeInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'productType.label', default: 'ProductType'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def productTypeInstance = ProductType.get(params.id)
        if (productTypeInstance) {
            try {
                productTypeInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'productType.label', default: 'ProductType'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'productType.label', default: 'ProductType'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'productType.label', default: 'ProductType'), params.id])}"
            redirect(action: "list")
        }
    }
}
