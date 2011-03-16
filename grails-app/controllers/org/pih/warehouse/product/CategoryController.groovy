package org.pih.warehouse.product

class CategoryController {

	def productService;
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }
	
	
	def tree = { 
		log.info params 
		def categoryInstance = Category.get(params.id)
		
		[ rootCategory : productService.getRootCategory(), categoryInstance : categoryInstance ]
	}
	
	
	def move = { 
		log.info params
		def parent = Category.get(params.newParent);
		def child = Category.get(params.child);
		log.info parent;
		log.info child;
		child.parentCategory = parent;
		if (!child.hasErrors() && child.save(flush:true)) { 
			flash.message = "Success"
		}
		redirect(action: "tree");
		
	}
	
		
	/*
	def editCategory = { 
		log.info params 
		def categoryInstance = Category.get(params.id)
		if (!categoryInstance) { 
			flash.message = "Unable to locate category with ID ${params.id}" 
		}
		render(view: "editCategory", model: [rootCategory: productService.getRootCategory(), categoryInstance: categoryInstance ])		
	}
	
	def createCategory = {
		render(view: "createCategory", model: [rootCategory: productService.getRootCategory(), categoryInstance: new Category() ])
	}*/

	def saveCategory = { 		
		log.info params;
		
		def categoryInstance = Category.get(params.id)		
		if (!categoryInstance)
			categoryInstance = new Category(params)
		else
			categoryInstance.properties = params;
		
		if (!categoryInstance.hasErrors() && categoryInstance.save()) {
			flash.message = "Saved category ${categoryInstance?.name} successfully";
		}
		redirect(action: "tree", params: params);
	}
	
	def deleteCategory = {
		log.info params
		def categoryInstance = Category.get(params.id)

		if (categoryInstance) { 
			try { 
				categoryInstance.delete(flush:true);
			} catch (Exception e) { 
				//categoryInstance.errors.reject(e.getMessage())
				throw e;
			}
		}

		/*
		if (categoryInstance.hasErrors()) { 
			render(view: "tree", model: [rootCategory: productService.getRootCategory(), categoryInstance: categoryInstance ])
		}
		*/	
		redirect(action: "tree");		
	}

		

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [categoryInstanceList: Category.list(params), categoryInstanceTotal: Category.count()]
    }

    def create = {
        def categoryInstance = new Category()
        categoryInstance.properties = params
		
        return [categoryInstance: categoryInstance, rootCategory: productService.getRootCategory()]
    }

    def save = {
        def categoryInstance = new Category(params)
        if (categoryInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'category.label', default: 'Category'), categoryInstance.id])}"
            redirect(action: "tree", id: categoryInstance.id)
        }
        else {
            render(view: "create", model: [categoryInstance: categoryInstance])
        }
    }

    def show = {
        def categoryInstance = Category.get(params.id)
        if (!categoryInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'category.label', default: 'Category'), params.id])}"
            redirect(action: "tree")
        }
        else {
            [categoryInstance: categoryInstance]
        }
    }

    def edit = {
        def categoryInstance = Category.get(params.id)

        if (!categoryInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'category.label', default: 'Category'), params.id])}"
            redirect(action: "tree")
        }
        else {
            return [categoryInstance: categoryInstance]
        }
    }

    def update = {
        def categoryInstance = Category.get(params.id)
        if (categoryInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (categoryInstance.version > version) {
                    
                    categoryInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'category.label', default: 'Category')] as Object[], "Another user has updated this Category while you were editing")
                    render(view: "edit", model: [categoryInstance: categoryInstance])
                    return
                }
            }
            categoryInstance.properties = params
            if (!categoryInstance.hasErrors() && categoryInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'category.label', default: 'Category'), categoryInstance.id])}"
                redirect(action: "tree", id: categoryInstance.id)
            }
            else {
                render(view: "edit", model: [categoryInstance: categoryInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'category.label', default: 'Category'), params.id])}"
            redirect(action: "tree")
        }
    }

    def delete = {
        def categoryInstance = Category.get(params.id)
        if (categoryInstance) {
            try {
                categoryInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'category.label', default: 'Category'), params.id])}"
                redirect(action: "tree")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'category.label', default: 'Category'), params.id])}"
                redirect(action: "tree", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'category.label', default: 'Category'), params.id])}"
            redirect(action: "tree")
        }
    }
}
