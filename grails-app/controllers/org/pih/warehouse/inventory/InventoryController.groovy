package org.pih.warehouse.inventory;

import org.pih.warehouse.core.User;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.ProductType;
import org.pih.warehouse.inventory.Warehouse;


class InventoryController {
    //def scaffold = Inventory		
	def inventoryService;
		
	def index = { 
		redirect(action: "browse");
	}
	
	
	def list = { 
		[ warehouses : Warehouse.getAll() ]
	}
	
	def browse = {
		
		// Get the warehouse from the request parameter
		def warehouseInstance = Warehouse.get(params?.warehouse?.id) 
		
		// If it doesn't exist or if the parameter is null, get 
		// warehouse from the session
		if (!warehouseInstance) { 
			warehouseInstance = Warehouse.get(session?.warehouse?.id);
		}
							
		if (!warehouseInstance?.inventory) { 
			redirect(action: "create")
		}
		
		// Get all product types and set the default product type 
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
			inventoryLevelMap : inventoryService.getInventoryLevelMap(warehouseInstance?.id),
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
				redirect(action: "show", id: warehouseInstance?.inventory?.id)
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
	
	
	def createTransaction = { 
		redirect(action: "editTransaction")
		//render(view: "editTransaction", model: [ productInstanceMap: productMap, 
		//	transactionTypeList: transactionTypes, warehouseInstanceList: warehouses, transactionInstance: transaction]);
	}

	def listTransactions = { 
		redirect(action: listAllTransactions)
	}
	
	def listAllTransactions = {
		def transactions = Transaction.list()
		render(view: "listTransactions", model: [transactionInstanceList: transactions])
	}

		
	def listPendingTransactions = { 
		def transactions = Transaction.findAllByConfirmedOrConfirmedIsNull(Boolean.FALSE)
		render(view: "listTransactions", model: [transactionInstanceList: transactions])
	}
	
	def listConfirmedTransactions = { 		
		def transactions = Transaction.findAllByConfirmed(Boolean.TRUE)
		render(view: "listTransactions", model: [transactionInstanceList: transactions])
	}
	
	
	def deleteTransaction = { 
		def transactionInstance = Transaction.get(params.id);
		
		if (transactionInstance) {
			try {
				transactionInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'transaction.label', default: 'Transaction'), params.id])}"
				redirect(action: "listTransactions")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'transaction.label', default: 'Transaction'), params.id])}"
				redirect(action: "editTransaction", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'transaction.label', default: 'Transaction'), params.id])}"
			redirect(action: "listTransactions")
		}
	}
	
	def saveTransaction = {	
		log.info "save transaction: " + params
		def transactionInstance = Transaction.get(params.id);
		def inventoryInstance = Inventory.get(params.inventory.id);
		
		
		if (!transactionInstance) {
			transactionInstance = new Transaction();
		} 
		
		transactionInstance.properties = params
		transactionInstance.transactionEntries.each { 
			def inventoryItem = InventoryItem.findByProductAndLotNumber(it.product, it.lotNumber);
			if (!inventoryItem) { 
				inventoryItem = new InventoryItem(
					active: Boolean.TRUE, 
					product: it.product, 
					lotNumber: it.lotNumber, 
					inventoryItemType: InventoryItemType.NON_SERIALIZED,
					inventory: inventoryInstance);
				
				if (!inventoryItem.hasErrors() && inventoryItem.save()) { 
					println "saved inventory item"
				}
				else { 
					transactionInstance.errors = inventoryItem.errors;
					flash.message = "Unable to save inventory item";
					render(view: "editTransaction", model: [
						warehouseInstance: Warehouse.get(session?.warehouse?.id),
						transactionInstance: transactionInstance,
						productInstanceMap: Product.list().groupBy { it?.productType },
						transactionTypeList: TransactionType.list(),
						warehouseInstanceList: Warehouse.list()])
				}				
			}
			
			def inventoryLot = InventoryLot.findByProductAndLotNumber(it.product, it.lotNumber);
			if (!inventoryLot) {
				inventoryLot = new InventoryLot(params);
				inventoryLot.product = it.product
				inventoryLot.lotNumber = it.lotNumber
				inventoryLot.initialQuantity = 0;
				if (!inventoryLot.hasErrors() && inventoryLot.save()) {
					println "saved inventory lot"
				}
				else { 
					transactionInstance.errors = inventoryLot.errors;
					flash.message = "Unable to save inventory lot";
					render(view: "editTransaction", model: [
						warehouseInstance: Warehouse.get(session?.warehouse?.id),
						transactionInstance: transactionInstance,
						productInstanceMap: Product.list().groupBy { it?.productType },
						transactionTypeList: TransactionType.list(),
						warehouseInstanceList: Warehouse.list()])
				}				
			}
			it.inventoryItem = inventoryItem;
		}
		
		if (!transactionInstance.hasErrors() && transactionInstance.save(flush: true)) {
			flash.message = "${message(code: 'default.saved.message', args: [message(code: 'transaction.label', default: 'Transaction'), transactionInstance.id])}"
			redirect(action: "showTransaction", id: transactionInstance?.id)
		}
		else {			
			render(view: "editTransaction", model: [
						warehouseInstance: Warehouse.get(session?.warehouse?.id),
						transactionInstance: transactionInstance,
						productInstanceMap: Product.list().groupBy { it?.productType },
						transactionTypeList: TransactionType.list(),
						warehouseInstanceList: Warehouse.list()])
		}		
	}

	def showTransaction = {
		def transactionInstnace = Transaction.get(params?.id)
		
		[transactionInstance: transactionInstnace]
	}

		
	def confirmTransaction = { 
		def transactionInstance = Transaction.get(params?.id)
		if (transactionInstance?.confirmed) { 
			transactionInstance?.confirmed = Boolean.FALSE;
			transactionInstance?.confirmedBy = null;
			transactionInstance?.dateConfirmed = null;
			flash.message = "Transaction has been unconfirmed"
		}
		else { 
			transactionInstance?.confirmed = Boolean.TRUE;
			transactionInstance?.confirmedBy = User.get(session?.user?.id);
			transactionInstance?.dateConfirmed = new Date();
			flash.message = "Transaction has been confirmed"
		}
		redirect(action: "listAllTransactions")
	}
	
	
	def editTransaction = { 		
		log.info "edit transaction: " + params
		def transactionInstance = Transaction.get(params?.id)
		
		def model = [ 
			transactionInstance: transactionInstance?:new Transaction(),
			productInstanceMap: Product.list().groupBy { it?.productType },
			transactionTypeList: TransactionType.list(),
			warehouseInstanceList: Warehouse.list(),
			warehouseInstance: Warehouse.get(session?.warehouse?.id) ]

		render(view: "editTransaction", model: model)

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

