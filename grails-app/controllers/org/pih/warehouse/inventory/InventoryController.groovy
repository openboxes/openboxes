package org.pih.warehouse.inventory;

import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.ProductType;
import org.pih.warehouse.inventory.Warehouse;


class InventoryController {
    //def scaffold = Inventory		
	def inventoryService;
		
	def list = { 
		[ warehouses : Warehouse.getAll() ]
	}
	
	def browse = {
		def warehouseInstance = Warehouse.get(params?.warehouse?.id) 
		if (!warehouseInstance) { 
			warehouseInstance = Warehouse.get(session?.warehouse?.id);
		}
							
		if (!warehouseInstance?.inventory) { 
			flash.message = "Warehouse does not have an inventory, please create one now."
			redirect(action: "create")
		}
		
		def productTypes = ProductType.getAll()
		def productType = ProductType.get(params?.productType?.id);
		if (!productType) { 
			productType = productTypes.head();
		}
		
		[
			warehouseInstance: warehouseInstance,
			inventoryInstance: warehouseInstance.inventory,
			productMap : inventoryService.getProductMap(warehouseInstance?.id),
			inventoryMap : inventoryService.getInventoryMap(warehouseInstance?.id),
			//productInstanceList : Product.getAll(),
			productType: productType,
			productTypes: productTypes
		]
	}
	

		
	def create = {
		def warehouseInstance = Warehouse.get(params?.warehouse?.id)
		if (!warehouseInstance) { 
			warehouseInstance = Warehouse.get(session?.warehouse?.id);
		}
		return [warehouseInstance: warehouseInstance]
	}
	
	def save = {		
		def warehouseInstance = Warehouse.get(params.warehouse?.id)
		if (!warehouseInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), params.id])}"
			redirect(action: "list")
		} else {  
			warehouseInstance.inventory = new Inventory(params);
			//inventoryInstance.warehouse = session.warehouse;
			if (warehouseInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.created.message', args: [message(code: 'inventory.label', default: 'Inventory'), warehouseInstance.inventory.id])}"
				redirect(action: "edit", id: warehouseInstance?.inventory?.id)
			}
			else {
				render(view: "create", model: [warehouseInstance: warehouseInstance])
			}
		}
	}
	
	def show = {
		def inventoryInstance = Inventory.get(params.id)
		if (!inventoryInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "list")
		}
		else {
			
			def inventoryMapping = inventoryInstance.inventoryItems.groupBy{ it?.product } 
			[	
				inventoryMapping: inventoryMapping,
				inventoryInstance: inventoryInstance,
				categories : Category.getAll(),
				productTypes : ProductType.getAll(), 
				productInstanceList : Product.getAll() ]

		}
	}
	
	def addToInventory = {
		def inventoryInstance = Inventory.get( params.id )
		def productInstance = Product.get( params.product.id )

		if (!productInstance) { 
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'product.label', default: 'Product'), params?.product?.id])}"
			redirect(action: "browse");
		}
		else { 
			def itemInstance = new InventoryItem(product: productInstance)
			if (!itemInstance.hasErrors() && itemInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'inventory.label', default: 'Inventory'), inventoryInstance.id])}"
				redirect(action: "show", id: inventoryInstance.id)
			}
			else {
				flash.message = "unable to create an inventory item"
				//inventoryInstance.errors = itemInstance.errors;
				//render(view: "browse", model: [inventoryInstance: inventoryInstance])
			}			
		}
	}
	
	
	def edit = {
		def inventoryInstance = Inventory.get(params.id)
		if (!inventoryInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "list")
		}
		else {
			def productInstanceMap = Product.getAll().groupBy { it.productType } 
			
			return [inventoryInstance: inventoryInstance, productInstanceMap: productInstanceMap]
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
	
	def addItem = {
		def inventoryInstance = Inventory.get(params?.inventory?.id)
		def productInstance = Product.get(params?.product?.id);
		def itemInstance = InventoryItem.findByProductAndLotNumber(productInstance, params.lotNumber)
		if (itemInstance) {
			flash.message = "${message(code: 'default.alreadyExists.message', args: [message(code: 'inventory.label', default: 'Inventory item'), inventoryInstance.id])}"
			redirect(action: "show", id: inventoryInstance.id)
		}
		else {
			itemInstance = new InventoryItem(params)
			if (itemInstance.hasErrors() || !itemInstance.save(flush:true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'inventory.label', default: 'Inventory item'), inventoryInstance.id])}"
				redirect(action: "show", id: inventoryInstance.id)				
			}
			else {
				itemInstance.errors.each { println it }
				//redirect(action: "show", id: inventoryInstance.id)
				flash.message = "${message(code: 'default.notUpdated.message', args: [message(code: 'inventory.label', default: 'Inventory item'), inventoryInstance.id])}"
				render(view: "show", model: [inventoryInstance: inventoryInstance, itemInstance : itemInstance])
			}
		}
	}
	
	def deleteItem = {
		def itemInstance = InventoryItem.get(params.id)
		if (itemInstance) {
			try {
				itemInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
				redirect(action: "show", id: params.inventory.id)
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
				redirect(action: "show", id: params.inventory.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "show", id: params.inventory.id)
		}

				
	}
	
	
	/*
	def edit = {
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
			flash.message = "Created a new inventory for warehouse ${session.warehouse.name}."
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
	*/
		

	

}
