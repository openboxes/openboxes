package org.pih.warehouse.inventory;

import java.text.SimpleDateFormat;

import org.pih.warehouse.shipping.ShipmentStatusCode;
import org.pih.warehouse.util.DateUtil;
import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.Location 
import org.pih.warehouse.core.User;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.Warehouse;

class InventoryController {
	
    def productService;	
	def inventoryService;
	
	def index = { 
		redirect(action: "browse");
	}
	
	
	def list = { 
		[ warehouses : Warehouse.getAll() ]
	}
	
	/**
	 * Allows a user to browse the inventory for a particular warehouse.  
	 */
	def browse = { InventoryCommand cmd ->
		
		// Get the current warehouse from either the request or the session
		cmd.warehouseInstance = Warehouse.get(params?.warehouse?.id) 
		if (!cmd.warehouseInstance) {
			cmd.warehouseInstance = Warehouse.get(session?.warehouse?.id);
		}
		
		// Get the primary category from either the request or the session or as the first listed by default
		List quickCategories = productService.getQuickCategories();
		cmd.categoryInstance = Category.get(params?.categoryId)
		if (!cmd.categoryInstance) {
			cmd.categoryInstance = Category.get(session?.inventoryCategoryId);
			if (!cmd.categoryInstance) {
				cmd.categoryInstance = quickCategories.get(0);
			}
		}
		session?.inventoryCategoryId = cmd.categoryInstance.id
		
		// Pre-populate the sub-category and search terms from the session
		cmd.subcategoryInstance = Category.get(session?.inventorySubcategoryId);
		cmd.searchTerms = session?.inventorySearchTerms;
		cmd.showHiddenProducts = session?.showHiddenProducts;
		
		// If a new search is being performed, override the session-based terms from the request
		if (request.getParameter("searchPerformed")) {
			cmd.subcategoryInstance = Category.get(params?.subcategoryId);
			session?.inventorySubcategoryId = cmd.subcategoryInstance?.id;
			cmd.searchTerms = params.searchTerms
			session?.inventorySearchTerms = cmd.searchTerms;
			cmd.showHiddenProducts = params?.showHiddenProducts == "on";
			session?.showHiddenProducts = cmd.showHiddenProducts;
			cmd.showOutOfStockProducts = params?.showOutOfStockProducts == "on";
			session?.showOutOfStockProducts = cmd.showOutOfStockProducts;
		}
		
		// Pass this to populate the matching inventory items
		inventoryService.browseInventory(cmd);

		[ commandInstance: cmd, quickCategories: quickCategories ]
	}
	
	
	/**
	 * 
	 */
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
	
	
	/**
	 * 
	 */
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
	
		
	/**
	 * 
	 */
	def create = {
		def warehouseInstance = Warehouse.get(params?.warehouse?.id)
		if (!warehouseInstance) { 
			warehouseInstance = Warehouse.get(session?.warehouse?.id);
		}
		return [warehouseInstance: warehouseInstance]
	}
	
	
	/**
	 * 
	 */
	def save = {		
		def warehouseInstance = Warehouse.get(params.warehouse?.id)
		if (!warehouseInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'warehouse.label', default: 'Warehouse'), params.id])}"
			redirect(action: "list")
		} else {  
			warehouseInstance.inventory = new Inventory(params);
			//inventoryInstance.warehouse = session.warehouse;
			if (warehouseInstance.save(flush: true)) {
				flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), warehouseInstance.inventory.id])}"
				redirect(action: "browse")
			}
			else {
				render(view: "create", model: [warehouseInstance: warehouseInstance])
			}
		}
	}
	
	/**
	 * 
	 */
	def show = {
		def inventoryInstance = Inventory.get(params.id)
		if (!inventoryInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
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
	
	
	def addTo = {
		log.info("Add to ... " + params)
		
		if (params.actionButton) { 
			if (params.actionButton.equals("addToShipment")) { 
				chain(controller: "shipment", action: "addToShipment", params: params)
			}
			else if (params.actionButton.equals("addToTransaction")) {
				chain(controller: "inventory", action: "createTransaction", params: params)
			}
			else { 
				flash.message = "${warehouse.message(code: 'action.not.found.message', args: [params.actionButton])}"
			}
		}
		redirect(action: "browse");
		
	}
	
	
	def addToInventory = {
		def inventoryInstance = Inventory.get( params.id )
		def productInstance = Product.get( params.product.id )

		if (!productInstance) { 
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params?.product?.id])}"
			redirect(action: "browse");
		}
		else { 
			def itemInstance = new InventoryItem(product: productInstance)
			if (!itemInstance.hasErrors() && itemInstance.save(flush: true)) {
				flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), inventoryInstance.id])}"
				redirect(action: "browse", id: inventoryInstance.id)
			}
			else {
				flash.message = "${warehouse.message(code: 'inventory.unableToCreateItem.message')}"
				//inventoryInstance.errors = itemInstance.errors;
				//render(view: "browse", model: [inventoryInstance: inventoryInstance])
			}			
		}
	}
	
	
	def edit = {
		def inventoryInstance = Inventory.get(params.id)
		if (!inventoryInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
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
					inventoryInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'inventory.label', default: 'Inventory')] as Object[], 
						"Another user has updated this Inventory while you were editing")
					render(view: "edit", model: [inventoryInstance: inventoryInstance])
					return
				}
			}
			inventoryInstance.properties = params
			if (!inventoryInstance.hasErrors() && inventoryInstance.save(flush: true)) {
				flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), inventoryInstance.id])}"
				redirect(action: "browse", id: inventoryInstance.id)
			}
			else {
				render(view: "edit", model: [inventoryInstance: inventoryInstance])
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "list")
		}
	}
	
	def delete = {
		def inventoryInstance = Inventory.get(params.id)
		if (inventoryInstance) {
			try {
				inventoryInstance.delete(flush: true)
				flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "list")
		}
	}
	
	def addItem = {
		def inventoryInstance = Inventory.get(params?.inventory?.id)
		def productInstance = Product.get(params?.product?.id);
		def itemInstance = inventoryService.findByProductAndLotNumber(productInstance, params.lotNumber)
		if (itemInstance) {
			flash.message = "${warehouse.message(code: 'default.alreadyExists.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory item'), inventoryInstance.id])}"
			redirect(action: "show", id: inventoryInstance.id)
		}
		else {
			itemInstance = new InventoryItem(params)
			if (itemInstance.hasErrors() || !itemInstance.save(flush:true)) {
				flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory item'), inventoryInstance.id])}"
				redirect(action: "show", id: inventoryInstance.id)				
			}
			else {
				itemInstance.errors.each { println it }
				//redirect(action: "show", id: inventoryInstance.id)
				flash.message = "${warehouse.message(code: 'default.notUpdated.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory item'), inventoryInstance.id])}"
				render(view: "show", model: [inventoryInstance: inventoryInstance, itemInstance : itemInstance])
			}
		}
	}
	
	def deleteItem = {
		def itemInstance = InventoryItem.get(params.id)
		if (itemInstance) {
			try {
				itemInstance.delete(flush: true)
				flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
				redirect(action: "show", id: params.inventory.id)
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
				redirect(action: "show", id: params.inventory.id)
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "show", id: params.inventory.id)
		}

				
	}
	
	def listTransactions = { 
		redirect(action: listAllTransactions)
	}
	
	def listDailyTransactions = { 
		def dateFormat = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
		def dateSelected = (params.date) ? dateFormat.parse(params.date) : new Date();
		
		def transactionsByDate = Transaction.list().groupBy { DateUtil.clearTime(it?.transactionDate) }?.entrySet()?.sort{ it.key }?.reverse()
		
		def transactions = Transaction.findAllByTransactionDate(dateSelected);
		
		[ transactions: transactions, transactionsByDate: transactionsByDate, dateSelected: dateSelected ]
	}
	
	def listExpiringStock = { 
		def today = new Date();
		
		def excludeExpired = params.excludeExpired as Boolean
		
		// Stock that has already expired
		def expiredStock = InventoryItem.findAllByExpirationDateLessThan(today, [sort: 'expirationDate', order: 'desc']);
		
		// Stock expiring within the next 365 days
		def expiringStock = InventoryItem.findAllByExpirationDateBetween(today+1, today+180, [sort: 'expirationDate', order: 'asc']);
		
		// Get the set of categories BEFORE we filter		
		def categories = [] as Set
		
		categories.addAll(expiredStock.collect { it.product.category })
		categories.addAll(expiringStock.collect { it.product.category })
		log.info "categories: " + categories

		categories = categories.findAll { it != null }
		
		// poor man's filter
		def categorySelected = (params.category) ? Category.get(params.category as int) : null;
		log.info "categorySelected: " + categorySelected
		if (categorySelected) { 
			expiredStock = expiredStock.findAll { item -> item?.product?.category == categorySelected } 
			expiringStock = expiringStock.findAll { item -> item?.product?.category == categorySelected }
		}
		
		// filter by threshhold
		def threshholdSelected = (params.threshhold) ? params.threshhold as int : 0;
		log.info "threshholdSelected: " + threshholdSelected
		if (threshholdSelected) { 
			expiredStock = expiredStock.findAll { item -> (item?.expirationDate - today) < threshholdSelected }
			expiringStock = expiringStock.findAll { item -> (item?.expirationDate - today) <= threshholdSelected }
		}
		
		def warehouse = Warehouse.get(session.warehouse.id)		
		def quantityMap = inventoryService.getQuantityForInventory(warehouse.inventory)
		
		
		
		[expiredStock : expiredStock, expiringStock: expiringStock, quantityMap: quantityMap, categories : categories, 
			categorySelected: categorySelected, threshholdSelected: threshholdSelected, excludeExpired: excludeExpired]
	}
	
	
	
	def listAllTransactions = {		
		
		// FIXME Using the dynamic finder Inventory.findByWarehouse() does not work for some reason
		def currentInventory = Inventory.list().find( {it.warehouse.id == session.warehouse.id} )
		
		// we are only showing transactions for the inventory associated with the current warehouse
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		params.sort = params?.sort ?: "transactionDate"
		params.order = params?.order ?: "desc"
		def transactions = Transaction.findAllByInventory(currentInventory, params);
		def transactionCount = Transaction.countByInventory(currentInventory);
		
		
		render(view: "listTransactions", model: [transactionInstanceList: transactions, transactionCount: transactionCount ])
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
				if (inventoryService.isLocalTransfer(transactionInstance)) {
					inventoryService.deleteLocalTransfer(transactionInstance)
				}
				else {
					transactionInstance.delete(flush: true)
				}
				flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'transaction.label', default: 'Transaction'), params.id])}"
				redirect(action: "listTransactions")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'transaction.label', default: 'Transaction'), params.id])}"
				redirect(action: "editTransaction", id: params.id)
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'transaction.label', default: 'Transaction'), params.id])}"
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
		
		// either save as a local transfer, or a generic transaction
		// (catch any exceptions so that we display "nice" error messages)
		Boolean saved = null
		if (!transactionInstance.hasErrors()) {
			try {
				if (inventoryService.isValidForLocalTransfer(transactionInstance)) {
					saved = inventoryService.saveLocalTransfer(transactionInstance) 
				}
				else {
					saved = transactionInstance.save(flush:true)
				}
			}
			catch (Exception e) {
				log.error("Unabled to save transaction ", e);
			}
		}
		
		if (saved) {	
			flash.message = "${warehouse.message(code: 'inventory.transactionSaved.message')}"
			redirect(action: "showTransaction", id: transactionInstance?.id);
		}
		else { 		
			flash.message = "${warehouse.message(code: 'inventory.unableToSaveTransaction.message')}"
			def model = [ 
				transactionInstance : transactionInstance,
				productInstanceMap: Product.list().groupBy { it.category },
				transactionTypeList: TransactionType.list(),
				locationInstanceList: Location.list(),
				warehouseInstance: Warehouse.get(session?.warehouse?.id)
			]
			render(view: "editTransaction", model: model);
		}	
	}

	
	/**
	 * Show the transaction.
	 */
	def showTransaction = {
		def transactionInstance = Transaction.get(params.id);
		if (!transactionInstance) {
			flash.message = "${warehouse.message(code: 'inventory.noTransactionWithId.message', args: [params.id])}"
			transactionInstance = new Transaction();
		}
		
		def model = [
			transactionInstance : transactionInstance,
			productInstanceMap: Product.list().groupBy { it.category },
			transactionTypeList: TransactionType.list(),
			locationInstanceList: Location.list(),
			warehouseInstance: Warehouse.get(session?.warehouse?.id)
		];
		
		render(view: "showTransaction", model: model);
	}
	
	/**
	* Show the transaction.
	*/
   def showTransactionDialog = {
	   def transactionInstance = Transaction.get(params.id);
	   if (!transactionInstance) {
		 	flash.message = "${warehouse.message(code: 'inventory.noTransactionWithId.message', args: [params.id])}"
		   transactionInstance = new Transaction();
	   }
	   
	   def model = [
		   transactionInstance : transactionInstance,
		   productInstanceMap: Product.list().groupBy { it.category },
		   transactionTypeList: TransactionType.list(),
		   locationInstanceList: Location.list(),
		   warehouseInstance: Warehouse.get(session?.warehouse?.id)
	   ];
	   
	   render(view: "showTransactionDialog", model: model);
	   
   }
   
   
   

   	
	def confirmTransaction = { 
		def transactionInstance = Transaction.get(params?.id)
		if (transactionInstance?.confirmed) { 
			transactionInstance?.confirmed = Boolean.FALSE;
			transactionInstance?.confirmedBy = null;
			transactionInstance?.dateConfirmed = null;
					flash.message = "${warehouse.message(code: 'inventory.transactionHasBeenUnconfirmed.message')}"
		}
		else { 
			transactionInstance?.confirmed = Boolean.TRUE;
			transactionInstance?.confirmedBy = User.get(session?.user?.id);
			transactionInstance?.dateConfirmed = new Date();
			flash.message = "${warehouse.message(code: 'inventory.transactionHasBeenConfirmed.message')}"
		}
		redirect(action: "listAllTransactions")
	}
	
	def createTransaction = { 
		
		def transactionInstance = new Transaction();
		def productList = [] 
		if (flash.productList) { 
			flash.productList.each { 
				productList << it;
			}
		}
		
		// From show stock card page
		if (params?.product?.id) { 
			// 
			def product = Product.get(params.product.id)
			if (product) { 
				productList << product.id;
			}
		}
		
		// From inventory browser
		if (params.productId) { 
			def productIds = params.list('productId')
			productList = productIds.collect { Long.valueOf(it); }			
		}
		
		if (productList) { 
			productList.each { 
				def product = Product.get(it);
				def warehouse = Warehouse.get(session.warehouse.id)
				def inventory = Inventory.get(warehouse.inventory.id);
				log.info ("inventory " + inventory)
				def inventoryItems = inventoryService.getInventoryItemsByProductAndInventory(product, inventory);
				log.info "inventory items " + inventoryItems
				inventoryItems.each { inventoryItem ->
					def transactionEntry = new TransactionEntry(product: product, inventoryItem: inventoryItem, quantity: 0);
					transactionInstance.addToTransactionEntries(transactionEntry);
				}
			}
		}
		
		def model = [
			transactionInstance : transactionInstance,
			productInstanceMap: Product.list().groupBy { it.category },
			transactionTypeList: TransactionType.list(),
			locationInstanceList: Location.list(),
			warehouseInstance: Warehouse.get(session?.warehouse?.id) 
		];
		
		render(view: "editTransaction", model: model);
	}
	
	
	def saveNewTransaction = { 
		log.info ("Save new transaction " + params);
		
		// Try to find an existing transaction
		def transactionInstance = Transaction.get(params.id);
		
		
		// If there are no transactions with that ID, we create a new one
		if (!transactionInstance) { 
			transactionInstance = new Transaction(params);
		}
		// Otherwise, we bind parameters to the existing transaction
		else {
			//bindData(transactionInstance, params, [exclude: ['transactionEntries']]);
			//List transactionEntries = 
			//	ListUtils.lazyList([], FactoryUtils.instantiateFactory(TransactionEntry.class))		
			//bindData(transactionEntries, params, "entries");			
			//log.info("bind transaction entries " + transactionEntries);	
			transactionInstance.properties = params;
		}
		
		// Iterate over all transaction entries to reconcile the inventory item associated with each
		log.info("Saving inventory items " + transactionInstance.transactionEntries*.inventoryItem);
		transactionInstance?.transactionEntries.each { 
		
			def product = Product.get(it?.inventoryItem?.product?.id)
			def inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(product, it?.inventoryItem?.lotNumber)
				
			if (inventoryItem) {
				it.inventoryItem = inventoryItem;
			}
		 
			if (!it.inventoryItem.hasErrors() && it.inventoryItem.save()) { 
				log.info("saved inventory item " + it.inventoryItem.id);
			}
			// FIXME this need to be reworked and tested
			else { 
				it.inventoryItem.errors.each { error ->
					log.info "error with inventory item " + it.inventoryItem + " error = " + error
				}
				transactionInstance.errors.rejectValue('inventoryItems', 'error', 'this is the default message')
			}
			
		}
		
		// either save as a local transfer, or a generic transaction
		// (catch any exceptions so that we display "nice" error messages)
		Boolean saved = null
		if (!transactionInstance.hasErrors()) {
			try {
				if (inventoryService.isValidForLocalTransfer(transactionInstance)) {
					saved = inventoryService.saveLocalTransfer(transactionInstance) 
				}
				else {
					saved = transactionInstance.save()
				}
			}
			catch (Exception e) {
				log.error("Unable to save transaction ", e)
			}
		}
				
		if (saved) {	
					flash.message = "${warehouse.message(code: 'inventory.transactionSaved.message')}"
			redirect(action: "showTransaction", id: transactionInstance?.id);
			return;
		}
		else { 		
			def model = [ 
				transactionInstance : transactionInstance,
				productInstanceMap: Product.list().groupBy { it.category },
				transactionTypeList: TransactionType.list(),
				locationInstanceList: Location.list(),
				warehouseInstance: Warehouse.get(session?.warehouse?.id)
			]
			render(view: "editTransaction", model: model);
		}		
	}
	
	
	def editTransaction = { 		
		log.info "edit transaction: " + params
		def transactionInstance = Transaction.get(params?.id)
		
		def model = [ 
			transactionInstance: transactionInstance?:new Transaction(),
			productInstanceMap: Product.list().groupBy { it?.category },
			transactionTypeList: TransactionType.list(),
			locationInstanceList: Location.list(),
			warehouseInstance: Warehouse.get(session?.warehouse?.id) ]

		render(view: "editTransaction", model: model)

	}
	
	
	/**
	* TODO These are the same methods used in the inventory browser.  Need to figure out a better
	* way to handle this (e.g. through a generic ajax call or taglib).
	*/
	def removeCategoryFilter = {
		def category = Category.get(params?.categoryId)
		if (category)
			session.inventoryCategoryFilters.remove(category?.id);
		redirect(action: browse);
	}
	
	def clearAllFilters = {
		session.inventoryCategoryFilters = [];
		session.inventorySearchTerms = [];
		redirect(action: browse);
	}
	def addCategoryFilter = {
		def category = Category.get(params?.categoryId);
		if (category && !session.inventoryCategoryFilters.contains(category?.id))
			session.inventoryCategoryFilters << category?.id;	
		redirect(action: browse);
	}
	def narrowCategoryFilter = {
		def category = Category.get(params?.categoryId);
		session.inventoryCategoryFilters = []
		if (category && !session.inventoryCategoryFilters.contains(category?.id))
			   session.inventoryCategoryFilters << category?.id;
		redirect(action: browse);
	}
	def removeSearchTerm = {
		if (params.searchTerm)
			session.inventorySearchTerms.remove(params.searchTerm);
		redirect(action: browse);
	}
	
	def showHiddenProducts = { 
		if(!session.showHiddenProducts) { 
			session.showHiddenProducts = true;
		}
		else { 
			session.showHiddenProducts = !session.showHiddenProducts
		}
		redirect(action:browse)
	}
}


