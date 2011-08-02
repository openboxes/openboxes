package org.pih.warehouse.inventory

import org.pih.warehouse.product.Product;

class InventoryLevelController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [inventoryLevelInstanceList: InventoryLevel.list(params), inventoryLevelInstanceTotal: InventoryLevel.count()]
    }

    def create = {
        def inventoryLevelInstance = new InventoryLevel()
        inventoryLevelInstance.properties = params
        return [inventoryLevelInstance: inventoryLevelInstance]
    }

    def save = {
        def inventoryLevelInstance = new InventoryLevel(params)
        if (inventoryLevelInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), inventoryLevelInstance.id])}"
            redirect(action: "list", id: inventoryLevelInstance.id)
        }
        else {
            render(view: "create", model: [inventoryLevelInstance: inventoryLevelInstance])
        }
    }

    def show = {
        def inventoryLevelInstance = InventoryLevel.get(params.id)
        if (!inventoryLevelInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), params.id])}"
            redirect(action: "list")
        }
        else {
            [inventoryLevelInstance: inventoryLevelInstance]
        }
    }

    def edit = {
		def productInstance = Product.get(params.id)
		def inventoryLevelInstance = InventoryLevel.findByProduct(productInstance)
        //def inventoryLevelInstance = InventoryLevel.get(params.id)
        if (!inventoryLevelInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), params.id])}"
            //redirect(action: "list")
			redirect(action: "create")
        }
        else {
            return [inventoryLevelInstance: inventoryLevelInstance]
        }
    }

    def update = {
        def inventoryLevelInstance = InventoryLevel.get(params.id)
        if (inventoryLevelInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (inventoryLevelInstance.version > version) {
                    
                    inventoryLevelInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel')] as Object[], "Another user has updated this InventoryLevel while you were editing")
                    render(view: "edit", model: [inventoryLevelInstance: inventoryLevelInstance])
                    return
                }
            }
            inventoryLevelInstance.properties = params
            if (!inventoryLevelInstance.hasErrors() && inventoryLevelInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), inventoryLevelInstance.id])}"
                redirect(action: "list", id: inventoryLevelInstance.id)
            }
            else {
                render(view: "edit", model: [inventoryLevelInstance: inventoryLevelInstance])
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def inventoryLevelInstance = InventoryLevel.get(params.id)
        if (inventoryLevelInstance) {
            try {
                inventoryLevelInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), params.id])}"
            redirect(action: "list")
        }
    }
}
