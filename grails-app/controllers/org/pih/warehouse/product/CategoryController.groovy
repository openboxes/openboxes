package org.pih.warehouse.product

class CategoryController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }
	
	
	def tree = { 
		log.info params 
		def categoryInstanceList = Category.findAllByParentCategoryIsNull([sort: "name", order: "asc"]);
		
		log.info "categories: " + categoryInstanceList;
		
		def rootCategory = Category.findByName("ROOT");
		[rootCategory: rootCategory ]
	}
		
	def editCategory = { 
		log.info params 
		def categoryInstance = Category.get(params.id)
		if (!categoryInstance) { 
			flash.message = "Unable to locate category with ID ${params.id}" 
		}
		def rootCategory = Category.findByName("ROOT");		
		render(view: "tree", model: [rootCategory: rootCategory, categoryInstance: categoryInstance ])		
	}

	def saveCategory = { 		
		log.info params;
		
		def categoryInstance = Category.get(params.id)		
		if (!categoryInstance)
			categoryInstance = new Category(params)
		else
			categoryInstance.properties = params;
		
		if (!categoryInstance.hasErrors() && categoryInstance.save(flush:true)) {
			flash.message = "Saved category ${categoryInstance?.name} successfully";
		}
		redirect(action: tree);
	}
	
	def deleteCategory = {
		log.info params
		def categoryInstance = Category.get(params.id)

		if (categoryInstance) { 
			categoryInstance.delete();
		}
		
		redirect(action: tree);		
	}

		

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [categoryInstanceList: Category.list(params), categoryInstanceTotal: Category.count()]
    }

    def create = {
        def categoryInstance = new Category()
        categoryInstance.properties = params
		
		def rootCategory = new Category(name: "root");
		rootCategory.categories = Category.findAllByParentCategoryIsNull()

        return [categoryInstance: categoryInstance, rootCategory: rootCategory]
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
		
		def rootCategory = new Category(name: "root");
		rootCategory.categories = Category.findAllByParentCategoryIsNull()

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
