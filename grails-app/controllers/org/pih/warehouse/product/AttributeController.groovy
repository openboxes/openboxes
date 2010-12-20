package org.pih.warehouse.product

class AttributeController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [attributeInstanceList: Attribute.list(params), attributeInstanceTotal: Attribute.count()]
    }

    def create = {
        def attributeInstance = new Attribute()
        attributeInstance.properties = params
        return [attributeInstance: attributeInstance]
    }

    def save = {
        def attributeInstance = new Attribute(params)
        if (attributeInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'attribute.label', default: 'Attribute'), attributeInstance.id])}"
            redirect(action: "list", id: attributeInstance.id)
        }
        else {
            render(view: "create", model: [attributeInstance: attributeInstance])
        }
    }

    def show = {
        def attributeInstance = Attribute.get(params.id)
        if (!attributeInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'attribute.label', default: 'Attribute'), params.id])}"
            redirect(action: "list")
        }
        else {
            [attributeInstance: attributeInstance]
        }
    }

    def edit = {
        def attributeInstance = Attribute.get(params.id)
        if (!attributeInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'attribute.label', default: 'Attribute'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [attributeInstance: attributeInstance]
        }
    }

    def update = {
        def attributeInstance = Attribute.get(params.id)
        if (attributeInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (attributeInstance.version > version) {                    
                    attributeInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'attribute.label', default: 'Attribute')] as Object[], "Another user has updated this Attribute while you were editing")
                    render(view: "edit", model: [attributeInstance: attributeInstance])
                    return
                }
            }
            attributeInstance.properties = params
						
            if (!attributeInstance.hasErrors() && attributeInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'attribute.label', default: 'Attribute'), attributeInstance.id])}"
                redirect(action: "edit", id: attributeInstance.id)
            }
            else {
                render(view: "edit", model: [attributeInstance: attributeInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'attribute.label', default: 'Attribute'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def attributeInstance = Attribute.get(params.id)
        if (attributeInstance) {
            try {
                attributeInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'attribute.label', default: 'Attribute'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'attribute.label', default: 'Attribute'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'attribute.label', default: 'Attribute'), params.id])}"
            redirect(action: "list")
        }
    }
	
	def addOption = { 
		def attributeInstance = Attribute.get(params.id)
		if (attributeInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (attributeInstance.version > version) {
					
					attributeInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'attribute.label', default: 'Attribute')] as Object[], "Another user has updated this Attribute while you were editing")
					render(view: "edit", model: [attributeInstance: attributeInstance])
					return
				}
			}
			attributeInstance.properties = params
			if (!attributeInstance.hasErrors() && attributeInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'attribute.label', default: 'Attribute'), attributeInstance.id])}"
				redirect(action: "edit", id: attributeInstance.id)
			}
			else {
				render(view: "edit", model: [attributeInstance: attributeInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'attribute.label', default: 'Attribute'), params.id])}"
			redirect(action: "list")
		}
	}
	
	
	def deleteOption = {
		def attributeInstance = Attribute.get(params.id)
		if (attributeInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (attributeInstance.version > version) {
					
					attributeInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'attribute.label', default: 'Attribute')] as Object[], "Another user has updated this Attribute while you were editing")
					render(view: "edit", model: [attributeInstance: attributeInstance])
					return
				}
			}
			//attributeInstance.properties = params
			def selectedOption = params.selectedOption;
			attributeInstance.removeFromOptions(selectedOption);
			
			if (!attributeInstance.hasErrors() && attributeInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'attribute.label', default: 'Attribute'), attributeInstance.id])}"
				redirect(action: "edit", id: attributeInstance.id)
			}
			else {
				render(view: "edit", model: [attributeInstance: attributeInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'attribute.label', default: 'Attribute'), params.id])}"
			redirect(action: "list")
		}
	}
	
}
