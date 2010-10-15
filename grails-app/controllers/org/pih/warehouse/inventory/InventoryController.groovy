package org.pih.warehouse.inventory;

import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.ProductType;


class InventoryController {
    //def scaffold = Inventory		
	def inventoryService;
	
	def create = {
		def inventoryInstance = new Inventory()
		inventoryInstance.properties = params
		return [inventoryInstance: inventoryInstance]
	}
	
	def browse = {
		def selectedProductType = ProductType.get(params.productTypeId);
		def warehouseInstance = Warehouse.get(session.warehouse.id);
		if (!warehouseInstance) {
			flash.message = "Error retrieving inventory for selected warehouse"
		}
		
		
		def productCriteria = Product.createCriteria();
		log.info "product filter " + params;
		def results = productCriteria.list {
			and{
				if(params.productTypeId){
					eq("productType.id", Long.parseLong(params.productTypeId))
				}
				  if(params.categoryId){
					  categories {
						  eq("id", Long.parseLong(params.categoryId))
					  }
				}
				if (params.nameContains) {
					like("name", "%" + params.nameContains + "%")
				}
				if (params.unverified) {
					eq("unverified", true)
				}
			}
		}
		
		// Quick hack to create a new inventory if one does not already exist
		// FIXME make sure this doesn't cause inventories to magically disappear
		if (!warehouseInstance?.inventory) {			
			def inventoryInstance = new Inventory();
			warehouseInstance.inventory = inventoryInstance;
			warehouseInstance.save(flush:true);						
			flash.message = "Create a new inventory for warehouse ${session.warehouse.name}."
		}
		
		render(view: "edit", model: [
			warehouseInstance: warehouseInstance,
			inventoryInstance: warehouseInstance?.inventory,
			productMap : inventoryService.getProductMap(warehouseInstance?.id),
			inventoryMap : inventoryService.getInventoryMap(warehouseInstance?.id),
			productInstanceList : Product.getAll(),
			productTypes : ProductType.getAll(),
			selectedProductType : selectedProductType])		
	}

	
	
	def save = {
		def inventoryInstance = new Inventory(params)		
		inventoryInstance.warehouse = session.warehouse;
		if (inventoryInstance.save(flush: true)) {
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'inventory.label', default: 'Inventory'), inventoryInstance.id])}"
			redirect(action: "browse", id: inventoryInstance.id)
		}
		else {
			
			
			render(view: "browse", model: [inventoryInstance: inventoryInstance])
		}
	}
	
	def show = {
		//def inventoryInstance = new Inventory();
		def inventoryInstance = Inventory.get(params.id)
		//def stockCardInstance = StockCard.get(params.id)
		if (!inventoryInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "list")
		}
		else {
			[inventoryInstance: inventoryInstance]
		}
	}
	
	def edit = {
		def inventoryInstance = Inventory.get(params.id)
		if (!inventoryInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [inventoryInstance: inventoryInstance]
		}
	}
	
	def update = {
		def inventoryInstance = Inventory.get(params.id)
		if (inventoryInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (inventoryInstance.version > version) {					
					inventoryInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'inventory.label', default: 'Inventory')] as Object[], 
						"Another user has updated this Inventory while you were editing")
					render(view: "edit", model: [inventoryInstance: inventoryInstance])
					return
				}
			}
			inventoryInstance.properties = params
			if (!inventoryInstance.hasErrors() && inventoryInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'inventory.label', default: 'Inventory'), inventoryInstance.id])}"
				redirect(action: "show", id: inventoryInstance.id)
			}
			else {
				render(view: "edit", model: [inventoryInstance: inventoryInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "list")
		}
	}
	
	def delete = {
		def inventoryInstance = Inventory.get(params.id)
		if (inventoryInstance) {
			try {
				inventoryInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'inventory.label', default: 'Inventory'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'inventory.label', default: 'Inventory'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "list")
		}
	}

}
