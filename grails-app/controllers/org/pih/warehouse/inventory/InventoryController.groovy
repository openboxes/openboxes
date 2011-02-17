package org.pih.warehouse.inventory;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.ListUtils;
import org.pih.warehouse.core.EventStatus;
import org.pih.warehouse.core.User;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.ShipmentService;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.Warehouse;


class InventoryController {
    //def scaffold = Inventory		
	def inventoryService;
	def shipmentService;
	
	def index = { 
		redirect(action: "browse");
	}
	
	
	def list = { 
		[ warehouses : Warehouse.getAll() ]
	}
	
	
	/** 
	 * TODO These are the same methods used in the product browser.  Need to figure out a better
	 * way to handle this (e.g. through a generic ajax call or taglib).
	 */
	def removeCategoryFilter = { 	
		def category = Category.get(params?.categoryId)		
		if (category)
			session.inventoryCategoryFilters.remove(category?.id);
		redirect(action: browse);		
	}
	def clearCategoryFilters = { 
		session.inventoryCategoryFilters.clear();
		session.inventoryCategoryFilters = null;
		redirect(action: browse);		
	}
	def addCategoryFilter = { 
		def category = Category.get(params?.categoryId);
		if (category && !session.inventoryCategoryFilters.contains(category?.id)) 
			session.inventoryCategoryFilters << category?.id;
		redirect(action: browse);		
	}

	/**
	 * Allows a user to browse the inventory for a particular warehouse.  
	 * 
	 * 	
	 */
	def browse = { BrowseInventoryCommand cmd ->
		// Get the warehouse from the request parameter
		cmd.warehouseInstance = Warehouse.get(params?.warehouse?.id) 
		
		// If it doesn't exist or if the parameter is null, get 
		// warehouse from the session
		if (!cmd.warehouseInstance) { 
			cmd.warehouseInstance = Warehouse.get(session?.warehouse?.id);
		}
		
							
		if (!cmd?.warehouseInstance?.inventory) { 
			redirect(action: "create")
		}
		
		cmd.shipmentList = shipmentService.getReceivingByDestinationAndStatus(cmd.warehouseInstance, EventStatus.SHIPPED);
		
		// Hydrate the category filters from the session
		// Allow us to get any attribute of a category without get a lazy init exception
		cmd.categoryFilters = []
		if (session.inventoryCategoryFilters) {
			session.inventoryCategoryFilters.each {
				cmd.categoryFilters << Category.get(it);
			}
		}
		inventoryService.browseInventory(cmd, params);
		
		[ commandInstance: cmd ]
	}
	
	def searchStock = {
		log.info params.query
		def products = []
		def inventoryItemMap = [:]
		
		
		def inventoryItems = InventoryItem.createCriteria().list() { 
			ilike("lotNumber", params.query + "%");
		}
		if (inventoryItems) { 
			inventoryItemMap = inventoryItems.groupBy { it.product } 
			inventoryItems.each { 
				products << it.product;
			}
			log.info "products: " + products
			if (inventoryItems?.size() == 1) { 
				params.put("inventoryItem.id", inventoryItems?.get(0)?.id)
				redirect(action: "enterStock", params: params);
			}
		}
		else { 		
			products = Product.createCriteria().list() {
				ilike("name", params.query + "%")
			}
			
			log.info products
			if (products) { 
				def items = InventoryItem.createCriteria().list() { 
					'in'("product", products)
				}
				log.info items;
				if (items) { 
					inventoryItemMap = items.groupBy { it.product } 
				}
			}
			if (products?.size() == 1) {
				params.put("product.id", products?.get(0)?.id);
				redirect(action: "enterStock", params: params);
			}
	
		}
		
		[productInstanceList : products, inventoryItemMap : inventoryItemMap]
	}
	
	def enterStock = { 
		def inventoryItem = InventoryItem.get(params?.inventoryItem?.id);		
		def productInstance = new Product();
		if (inventoryItem) { 
			productInstance = inventoryItem?.product;	
		} else {  
			productInstance = Product.get(params?.product?.id)
		}
		def warehouseInstance = Warehouse.get(params?.warehouse?.id)
		if (!warehouseInstance) {
			warehouseInstance = Warehouse.get(session?.warehouse?.id);
		}
		render(view: "enterStock", model: 
			[warehouseInstance: warehouseInstance, transactionInstance : new Transaction(), productInstance: productInstance, inventoryItem: inventoryItem])
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
				redirect(action: "browse")
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
				redirect(action: "browse", id: inventoryInstance.id)
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
				redirect(action: "browse", id: inventoryInstance.id)
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
	
	
	def listTransactions = { 
		redirect(action: listAllTransactions)
	}
	
	def listAllTransactions = {
		if (!params.sort) {
			params.sort = "dateCreated"
			params.order = "desc"
		}		
		def transactions = Transaction.list(params)
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
					lotNumber: it.lotNumber);
				
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

	
	/**
	 * Show the transaction.
	 */
	def showTransaction = {
		def transactionInstance = Transaction.get(params.id);
		if (!transactionInstance) {
			flash.message = "There was no transaction with ID " + params.id;
			transactionInstance = new Transaction();
		}
		
		def model = [
			transactionInstance : transactionInstance,
			productInstanceMap: Product.list().groupBy { it.category },
			transactionTypeList: TransactionType.list(),
			warehouseInstanceList: Warehouse.list(),
			warehouseInstance: Warehouse.get(session?.warehouse?.id)
		];
		
		render(view: "showTransaction", model: model);
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
	
	def createTransaction = { 
		
		def transactionInstance = new Transaction();
		def model = [
			transactionInstance : transactionInstance,
			productInstanceMap: Product.list().groupBy { it.category },
			transactionTypeList: TransactionType.list(),
			warehouseInstanceList: Warehouse.list(),
			warehouseInstance: Warehouse.get(session?.warehouse?.id) 
		];
		
		render(view: "createTransaction", model: model);
	}
	
	
	def saveNewTransaction = { 
		log.info ("Save new transaction " + params);
		def transactionInstance = Transaction.get(params.id);
		
		
		if (!transactionInstance) { 
			transactionInstance = new Transaction(params);
		}
		else {
			//bindData(transactionInstance, params, [exclude: ['transactionEntries']]);
			//List transactionEntries = 
			//	ListUtils.lazyList([], FactoryUtils.instantiateFactory(TransactionEntry.class))		
			//bindData(transactionEntries, params, "entries");			
			//log.info("bind transaction entries " + transactionEntries);	
			transactionInstance.properties = params;
		}
				
		transactionInstance?.transactionEntries.each { 
			log.info("Process transaction entry " + it.id);
			log.info("Find inventory item by lot number " + it.lotNumber);
			
				// Find inventory item by lot number
			def inventoryItem = InventoryItem.findByLotNumberAndProduct(it?.lotNumber, it?.product);
	
			// Create a new inventory item if one is not found 
			if (!inventoryItem) { 
				inventoryItem = new InventoryItem();
				inventoryItem.product = it.product;
				inventoryItem.lotNumber = it.lotNumber;
				//inventoryItem.description = it.product.name;
				inventoryItem.save();
				
				// FIXME Need to check for errors here
			}
			it.inventoryItem = inventoryItem;
		}
		log.info("Transaction entries " + transactionInstance?.transactionEntries)

		if (!transactionInstance.hasErrors() && transactionInstance.save(flush:true)) {
			flash.message = "Transaction saved successfully";
			redirect(action: "editTransaction", id: transactionInstance?.id);
		}
						
		def model = [ 
			transactionInstance : transactionInstance,
			productInstanceMap: Product.list().groupBy { it.category },
			transactionTypeList: TransactionType.list(),
			warehouseInstanceList: Warehouse.list(),
			warehouseInstance: Warehouse.get(session?.warehouse?.id)
		]
		
		
		render(view: "createTransaction", model: model);
		
		
	}
	
	
	def editTransaction = { 		
		log.info "edit transaction: " + params
		def transactionInstance = Transaction.get(params?.id)
		
		def model = [ 
			transactionInstance: transactionInstance?:new Transaction(),
			productInstanceMap: Product.list().groupBy { it?.category },
			transactionTypeList: TransactionType.list(),
			warehouseInstanceList: Warehouse.list(),
			warehouseInstance: Warehouse.get(session?.warehouse?.id) ]

		render(view: "createTransaction", model: model)

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


