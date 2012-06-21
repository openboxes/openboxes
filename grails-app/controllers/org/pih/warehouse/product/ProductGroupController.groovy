package org.pih.warehouse.product

class ProductGroupController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def productService 
	
    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [productGroupInstanceList: ProductGroup.list(params), productGroupInstanceTotal: ProductGroup.count()]
    }

    def create = {
        def productGroupInstance = new ProductGroup()
        productGroupInstance.properties = params
        return [productGroupInstance: productGroupInstance]
    }

    def save = {
        def productGroupInstance = new ProductGroup(params)
        if (productGroupInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), productGroupInstance.id])}"
            redirect(action: "edit", id: productGroupInstance.id)
        }
        else {
            render(view: "create", model: [productGroupInstance: productGroupInstance])
        }
    }

    def show = {
        def productGroupInstance = ProductGroup.get(params.id)
        if (!productGroupInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
            redirect(action: "list")
        }
        else {
            [productGroupInstance: productGroupInstance]
        }
    }

    def edit = {
		log.info "Edit product group: " + params
		
        def productGroupInstance = ProductGroup.get(params.id)
        if (!productGroupInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
            redirect(action: "list")
        }
        else {
			productGroupInstance.properties = params
			log.info "category: " + productGroupInstance?.category?.name
            return [productGroupInstance: productGroupInstance]
        }
    }

    def update = {
		
		log.info "Update product group " + params 
		
		
        def productGroupInstance = ProductGroup.get(params.id)
        if (productGroupInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (productGroupInstance.version > version) {
                    
                    productGroupInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'productGroup.label', default: 'ProductGroup')] as Object[], "Another user has updated this ProductGroup while you were editing")
                    render(view: "edit", model: [productGroupInstance: productGroupInstance])
                    return
                }
            }
			productGroupInstance.properties = params
			
			log.info(params['product.id'])
			
			
			productGroupInstance.products = productService.getProducts(params['product.id'])
			log.info "Products after: " + productGroupInstance.products
			
			
            
            if (!productGroupInstance.hasErrors() && productGroupInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), productGroupInstance.id])}"
                redirect(action: "list", id: productGroupInstance.id)
            }
            else {
                render(view: "edit", model: [productGroupInstance: productGroupInstance])
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def productGroupInstance = ProductGroup.get(params.id)
        if (productGroupInstance) {
            try {
                productGroupInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
            redirect(action: "list")
        }
    }
}
