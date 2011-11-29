
package org.pih.warehouse.inventory;

import grails.validation.ValidationException;
import groovy.sql.Sql;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.pih.warehouse.shipping.ShipmentStatusCode;
import org.pih.warehouse.util.DateUtil;
import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.Location 
import org.pih.warehouse.core.User;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.core.Location;

import org.pih.warehouse.reporting.Consumption;

class InventoryController {
	
	def dataSource
    def productService;	
	def inventoryService;
	
	def index = { 
		redirect(action: "browse");
	}
	
	
	def list = { 
		[ warehouses : Location.getAll() ]
	}
	
	/**
	 * Allows a user to browse the inventory for a particular warehouse.  
	 */
	def browse = { InventoryCommand cmd ->
		log.info("Browse inventory " + params)
		// Get the current warehouse from either the request or the session
		cmd.warehouseInstance = Location.get(params?.warehouse?.id) 
		if (!cmd.warehouseInstance) {
			cmd.warehouseInstance = Location.get(session?.warehouse?.id);
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
		
		// if we have arrived via a quick link tab, reset any subcategories or search terms in the session
		if (params?.resetSearch) {
			session?.inventorySubcategoryId = null
			session?.inventorySearchTerms = null
		}
		
		// Pre-populate the sub-category and search terms from the session
		cmd.subcategoryInstance = Category.get(session?.inventorySubcategoryId)
		cmd.searchTerms = session?.inventorySearchTerms
		cmd.showUnsupportedProducts = session?.showUnsupportedProducts
		cmd.showNonInventoryProducts = session?.showNonInventoryProducts
		cmd.showOutOfStockProducts = session?.showOutOfStockProducts ?: true
		
		// If a new search is being performed, override the session-based terms from the request
		if (request.getParameter("searchPerformed")) {
			cmd.subcategoryInstance = Category.get(params?.subcategoryId)
			session?.inventorySubcategoryId = cmd.subcategoryInstance?.id
			cmd.searchTerms = params.searchTerms
			session?.inventorySearchTerms = cmd.searchTerms
			cmd.showUnsupportedProducts = params?.showUnsupportedProducts == "on"
			session?.showUnsupportedProducts = cmd.showUnsupportedProducts
			cmd.showOutOfStockProducts = params?.showOutOfStockProducts == "on"
			session?.showOutOfStockProducts = cmd.showOutOfStockProducts
			cmd.showNonInventoryProducts = params?.showNonInventoryProducts == "on"
			session?.showNonInventoryProducts = cmd.showNonInventoryProducts

		}
		
		// Pass this to populate the matching inventory items
		inventoryService.browseInventory(cmd);

		[ commandInstance: cmd, quickCategories: quickCategories ]
	}
	
		
	/**
	 * 
	 */
	def create = {
		def warehouseInstance = Location.get(params?.warehouse?.id)
		if (!warehouseInstance) { 
			warehouseInstance = Location.get(session?.warehouse?.id);
		}
		return [warehouseInstance: warehouseInstance]
	}
	
	
	/**
	 * 
	 */
	def save = {		
		def warehouseInstance = Location.get(params.warehouse?.id)
		if (!warehouseInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), params.id])}"
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
	
	def listExpiredStock = { 
		def warehouse = Location.get(session.warehouse.id)
		def categorySelected = (params.category) ? Category.get(params.category as int) : null;		
		def expiredStock = inventoryService.getExpiredStock(categorySelected, warehouse);
		def categories = expiredStock?.collect { it.product.category }?.unique()
		def quantityMap = inventoryService.getQuantityForInventory(warehouse.inventory)
		
		[inventoryItems:expiredStock, quantityMap:quantityMap, categories:categories, categorySelected:categorySelected]
	}
	
	
	def listExpiringStock = { 
		def threshhold = (params.threshhold) ? params.threshhold as int : 0;
		def category = (params.category) ? Category.get(params.category as int) : null;
		def location = Location.get(session.warehouse.id)		
		def expiringStock = inventoryService.getExpiringStock(category, location, threshhold)
		def categories = expiringStock?.collect { it?.product?.category }?.unique().sort { it.name } ;
		def quantityMap = inventoryService.getQuantityForInventory(location.inventory)
		
		[inventoryItems:expiringStock, quantityMap:quantityMap, categories:categories, 
			categorySelected:category, threshholdSelected:threshhold ]
	}
	
	def listLowStock = {
		def warehouse = Location.get(session.warehouse.id)
		def results = inventoryService.getProductsBelowMinimumAndReorderQuantities(warehouse.inventory, params.showUnsupportedProducts ? true : false)
		
		Map inventoryLevelByProduct = new HashMap();
		inventoryService.getInventoryLevelsByInventory(warehouse.inventory).each {
			inventoryLevelByProduct.put(it.product, it);
		}
		
		// Set of categories that we can filter by
		def categories = [] as Set
		categories.addAll(results['reorderProductsQuantityMap']?.keySet().collect { it.category })
		categories.addAll(results['minimumProductsQuantityMap']?.keySet().collect { it.category })
		categories = categories.findAll { it != null }
		
		// poor man's filter
		def categorySelected = (params.category) ? Category.get(params.category as int) : null;
		log.info "categorySelected: " + categorySelected
		if (categorySelected) {
			results['reorderProductsQuantityMap'] = results['reorderProductsQuantityMap'].findAll { it.key?.category == categorySelected }
			results['minimumProductsQuantityMap'] = results['minimumProductsQuantityMap'].findAll { it.key?.category == categorySelected }
		}
		
		[reorderProductsQuantityMap: results['reorderProductsQuantityMap'], minimumProductsQuantityMap: results['minimumProductsQuantityMap'], 
			categories: categories, categorySelected: categorySelected, showUnsupportedProducts: params.showUnsupportedProducts, inventoryLevelByProduct: inventoryLevelByProduct]
	}


	class ConsumptionCommand {
		String groupBy
		Date startDate
		Date endDate
		
		static constraints = {

		}
	}
	
	def showConsumption = { ConsumptionCommand command ->
		
		def consumptions = inventoryService.getConsumptionTransactionsBetween(command?.startDate, command?.endDate)
		def consumptionMap = consumptions.groupBy { it.product };
		
		//def products = Product.list()
		def products = consumptions*.product.unique();
		//products = products.findAll { consumptionMap[it] > 0 }
		def productMap = products.groupBy { it.category };
		def dateFormat = new SimpleDateFormat("ddMMyyyy")
		//def dateKeys = inventoryService.getConsumptionDateKeys()
		def startDate = command?.startDate?:(new Date()-7)
		def endDate = command?.endDate?:new Date()
		
		def calendar = Calendar.instance
		def dateKeys = (startDate..endDate).collect { date ->
			calendar.setTime(date);
			[
				date: date,
				day: calendar.get(Calendar.DAY_OF_MONTH),
				week: calendar.get(Calendar.WEEK_OF_YEAR),
				month: calendar.get(Calendar.MONTH),
				year: calendar.get(Calendar.YEAR),
				key: dateFormat.format(date)
			]
		}.sort { it.date }
		
		
		def groupBy = command?.groupBy;
		log.info("groupBy = " + groupBy)
		def daysBetween = (groupBy!="default") ? -1 : endDate - startDate
		if (daysBetween > 365 || groupBy.equals("yearly")) {
			groupBy = "yearly"
			dateFormat = new SimpleDateFormat("yyyy")
		}
		else if ((daysBetween > 61 && daysBetween < 365) || groupBy.equals("monthly")) {
			groupBy = "monthly"
			dateFormat = new SimpleDateFormat("MMM")
		}
		else if (daysBetween > 14 && daysBetween < 60 || groupBy.equals("weekly")) {
			groupBy = "weekly"
			dateFormat = new SimpleDateFormat("'Week' w")
		}
		else if (daysBetween > 0 && daysBetween <= 14 || groupBy.equals("daily")) {
			groupBy = "daily"
			dateFormat = new SimpleDateFormat("MMM dd")
		}
		dateKeys = dateKeys.collect { dateFormat.format(it.date) }.unique()
		
		
		log.info("consumption " + consumptionMap)
		def consumptionProductDateMap = [:]
		consumptions.each { 
			def dateKey = it.product.id + "_" + dateFormat.format(it.transactionDate)
			def quantity = consumptionProductDateMap[dateKey];
			if (!quantity) quantity = 0;
			quantity += it.quantity?:0
			consumptionProductDateMap[dateKey] = quantity;
			
			def totalKey = it.product.id + "_Total"
			def totalQuantity = consumptionProductDateMap[totalKey];
			if (!totalQuantity) totalQuantity = 0;
			totalQuantity += it.quantity?:0
			consumptionProductDateMap[totalKey] = totalQuantity;

		}
		
		
		
			
		/*
		def today = new Date();
		def warehouse = Location.get(session.warehouse.id)
		
		// Get all transactions from the past week 
		def transactions = inventoryService.getConsumptionTransactions(today-7, today);
		def transactionEntries = []
		transactions.each { transaction -> 
			transaction.transactionEntries.each { transactionEntry -> 
				transactionEntries << transactionEntry
			}
		}
		
		def consumptionMap = [:]
		log.info "Products " + products.size();
				
		def transactionEntryMap = transactionEntries.groupBy { it.inventoryItem.product }		
		transactionEntryMap.each { key, value ->
			def consumed = value.sum { it.quantity }			
			log.info("key="+key + ", value = " + value + ", total consumed=" + consumed);
			consumptionMap[key] = consumed;
		}
		*/

		


		
				
		[
			command: command,
			productMap : productMap,
			consumptionMap: consumptionMap,
			consumptionProductDateMap: consumptionProductDateMap,
			productKeys: products, 
			results: inventoryService.getConsumptions(command?.startDate, command?.endDate, command?.groupBy),
			dateKeys: dateKeys]
	}

	def refreshConsumptionData = { 
		def consumptionType = TransactionType.get(2)		
		def transactions = Transaction.findAllByTransactionType(consumptionType)

		// Delete all consumption rows		
		Consumption.executeUpdate("delete Consumption c")
		
		// Reset auto increment counter to 0
		// ALTER TABLE consumption AUTO_INCREMENT=0
		
		transactions.each { xact ->
			xact.transactionEntries.each { entry -> 
				def consumption = new Consumption(
					product: entry.inventoryItem.product, 
					inventoryItem: entry.inventoryItem, 
					quantity: entry.quantity,
					transactionDate: entry.transaction.transactionDate,
					location: entry.transaction.inventory.warehouse,
					month: entry.transaction.transactionDate.month,
					day: entry.transaction.transactionDate.day,
					year: entry.transaction.transactionDate.year+1900);
				
				if (!consumption.hasErrors() && consumption.save()) { 
					
				}
				else { 
					flash.message = "error saving Consumption " + consumption.errors
				}
			}
		}
		redirect(controller: "inventory", action: "showConsumption")
	}
	
	
	/**
	 * Used to create default inventory items.
	 * @return
	 */
	def createDefaultInventoryItems = { 
		def products = inventoryService.findProductsWithoutEmptyLotNumber();
		products.each { product -> 
			def inventoryItem = new InventoryItem()
			inventoryItem.product = product
			inventoryItem.lotNumber = null;
			inventoryItem.expirationDate = null;
			inventoryItem.save();
		}
		redirect(controller: "inventory", action: "showProducts")
	}
	
		
	def showProducts = { 
		def products = inventoryService.findProductsWithoutEmptyLotNumber()
		[ products : products ]
		
	}
	
	
	def listAllTransactions = {		
		
		// FIXME Using the dynamic finder Inventory.findByLocation() does not work for some reason
		def currentInventory = Inventory.list().find( {it.warehouse.id == session.warehouse.id} )
		
		// we are only showing transactions for the inventory associated with the current warehouse
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		params.sort = params?.sort ?: "dateCreated"
		params.order = params?.order ?: "desc"
		def transactions = []
		def transactionCount = 0;
		def transactionType = TransactionType.get(params?.transactionType?.id)
		if (transactionType) { 
			transactions = Transaction.findAllByInventoryAndTransactionType(currentInventory, transactionType, params);			
			transactionCount = Transaction.countByInventoryAndTransactionType(currentInventory, transactionType);
		}
		else { 
			transactions = Transaction.findAllByInventory(currentInventory, params);
			transactionCount = Transaction.countByInventory(currentInventory);
		}
		def transactionMap = Transaction.findAllByInventory(currentInventory).groupBy { it?.transactionType?.id } 
		log.info(transactionMap.keySet())
		render(view: "listTransactions", model: [transactionInstanceList: transactions, 
			transactionCount: transactionCount, transactionTypeSelected: transactionType, 
			transactionMap: transactionMap ])
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
	
	
	/*
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
				warehouseInstance: Location.get(session?.warehouse?.id)
			]
			render(view: "editTransaction", model: model);
		}	
	}
	*/
	
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
			warehouseInstance: Location.get(session?.warehouse?.id)
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
		   warehouseInstance: Location.get(session?.warehouse?.id)
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
		log.info("createTransaction params " + params)		
		def command = new TransactionCommand();
		def warehouseInstance = Location.get(session?.warehouse?.id);
		def transactionInstance = new Transaction(params);
		//transactionInstance?.transactionDate = new Date();
		//transactionInstance?.source = warehouseInstance
		
		if (!transactionInstance?.transactionType) { 
			flash.message = "Cannot create transaction for unknown transaction type";			
			redirect(controller: "inventory", action: "browse")
		}
		
		// Process productId parameters from inventory browser
		if (params.product?.id) {
			def productIds = params.list('product.id')
			def products = productIds.collect { Long.valueOf(it); }
			command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, products);
		}
		// If given a list of inventory items, we just return those inventory items
		else if (params?.inventoryItem?.id) { 
			def inventoryItemIds = params.list('inventoryItem.id')
			def inventoryItems = inventoryItemIds.collect { InventoryItem.get(Long.valueOf(it)); }
			command?.productInventoryItems = inventoryItems.groupBy { it.product } 
		}
		
		command.transactionInstance = transactionInstance
		command.warehouseInstance = warehouseInstance

		command.quantityMap = inventoryService.getQuantityForInventory(warehouseInstance?.inventory);
		command.transactionTypeList = TransactionType.list();
		command.locationList = Location.list();
		
		[command : command]
		
	}

	
	
	/**
	 * Save a transaction that sets the current inventory level for stock.
	 */
	def saveInventoryTransaction = { TransactionCommand command ->
		log.info ("Saving inventory adjustment " + params)

		def transaction = command?.transactionInstance;
		def warehouseInstance = Location.get(session?.warehouse?.id);
		def quantityMap = inventoryService.getQuantityForInventory(warehouseInstance?.inventory)
		
		// Item cannot have a negative quantity
		command.transactionEntries.each {
			if (it.quantity < 0) {
				transaction.errors.rejectValue("transactionEntries", "transactionEntry.quantity.invalid", [it?.inventoryItem?.lotNumber] as Object[], "")
			}
		}

		// Check to see if there are errors, if not save the transaction
		if (!transaction.hasErrors()) {
			try {
				// Add validated transaction entries to the transaction we want to persist
				command.transactionEntries.each {
					def transactionEntry = new TransactionEntry()
					transactionEntry.inventoryItem = it.inventoryItem
					transactionEntry.quantity = it.quantity
					transaction.addToTransactionEntries(transactionEntry)
				}
				
				// Validate the transaction object
				if (!transaction.hasErrors() && transaction.validate()) {
					transaction.save(failOnError: true)
					flash.message = "Successfully saved transaction " + transaction.transactionNumber()
					//redirect(controller: "inventory", action: "browse")
					redirect(controller: "inventory", action: "showTransaction", id: transaction?.id)
				}
			} catch (ValidationException e) {
				log.info ("caught validation exception " + e)
			}
		}

		// After the attempt to save the transaction, there might be errors on the transaction
		if (transaction.hasErrors()) {
			log.info ("has errors" + transaction.errors)
			
			// Get the list of products that the user selected from the inventory browser			
			if (params.product?.id) {
				def productIds = params.list('product.id')
				def products = productIds.collect { Long.valueOf(it); }
				command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, products);
			}
			// If given a list of inventory items, we just return those inventory items
			else if (params?.inventoryItem?.id) {
				def inventoryItemIds = params.list('inventoryItem.id')
				def inventoryItems = inventoryItemIds.collect { InventoryItem.get(Long.valueOf(it)); }
				command?.productInventoryItems = inventoryItems.groupBy { it.product }
			}
	
			
			// Populate the command object and render the form view.
			command.transactionInstance = transaction
			command.warehouseInstance = warehouseInstance
			command.quantityMap = quantityMap;
			command.transactionTypeList = TransactionType.list();
			command.locationList = Location.list();
			
			render(view: "createTransaction", model: [command: command]);
		}
	}

	/**
	 * Save a transaction that debits stock from the given inventory.
	 * 
	 * TRANSFER_OUT, CONSUMED, DAMAGED, EXPIRED
	 */
	def saveDebitTransaction = { TransactionCommand command ->
		log.info ("Saving debit transactions " + params)

		def transaction = command?.transactionInstance;
		def warehouseInstance = Location.get(session?.warehouse?.id);
		def quantityMap = inventoryService.getQuantityForInventory(warehouseInstance?.inventory)
		
		// Quantity cannot be greater than on hand quantity
		command.transactionEntries.each {
			def onHandQuantity = quantityMap[it.inventoryItem];
			if (it.quantity > onHandQuantity) {
				transaction.errors.rejectValue("transactionEntries", "transactionEntry.quantity.invalid", [it?.inventoryItem?.lotNumber] as Object[], "")
			}
		}

		// Check to see if there are errors, if not save the transaction
		if (!transaction.hasErrors()) {
			try {
				// Add validated transaction entries to the transaction we want to persist
				command.transactionEntries.each {
					if (it.quantity) { 
						def transactionEntry = new TransactionEntry()
						transactionEntry.inventoryItem = it.inventoryItem
						transactionEntry.quantity = it.quantity
						transaction.addToTransactionEntries(transactionEntry)
					}
				}
				
				// Validate the transaction object
				if (!transaction.hasErrors() && transaction.validate()) {
					transaction.save(failOnError: true)
					flash.message = "Successfully saved transaction " + transaction.transactionNumber()
					//redirect(controller: "inventory", action: "browse")
					redirect(controller: "inventory", action: "showTransaction", id: transaction?.id)
				}
			} catch (ValidationException e) {
				log.info ("caught validation exception " + e)
			}
		}

		// After the attempt to save the transaction, there might be errors on the transaction
		if (transaction.hasErrors()) {
			log.info ("has errors" + transaction.errors)
			
			// Get the list of products that the user selected from the inventory browser
			if (params.product?.id) {
				def productIds = params.list('product.id')
				def products = productIds.collect { Long.valueOf(it); }
				command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, products);
			}
			// If given a list of inventory items, we just return those inventory items
			else if (params?.inventoryItem?.id) {
				def inventoryItemIds = params.list('inventoryItem.id')
				def inventoryItems = inventoryItemIds.collect { InventoryItem.get(Long.valueOf(it)); }
				command?.productInventoryItems = inventoryItems.groupBy { it.product }
			}
	
			// Populate the command object and render the form view.
			command.transactionInstance = transaction
			command.warehouseInstance = warehouseInstance
			command.quantityMap = quantityMap;
			command.transactionTypeList = TransactionType.list();
			command.locationList = Location.list();
			
			render(view: "createTransaction", model: [command: command]);
		}
	}

	
	
	/**
	 * Save a transaction that debits stock from the given inventory.
	 * 
	 * TRANSFER_IN
	 */
	def saveCreditTransaction = { TransactionCommand command ->

		log.info("Saving credit transaction: " + params)
		def transactionInstance = command?.transactionInstance
		def warehouseInstance = Location.get(session?.warehouse?.id);
		def quantityMap = inventoryService.getQuantityForInventory(warehouseInstance?.inventory)

		
		// Quantity cannot be greater than on hand quantity
		command.transactionEntries.each {
			if (it.quantity < 0) {
				transaction.errors.rejectValue("transactionEntries", "transactionEntry.quantity.invalid", [it?.inventoryItem?.lotNumber] as Object[], "")
			}
		}

		// We need to process each transaction entry to make sure that it has a valid inventory item (or we will create one if not)
		command.transactionEntries.each { 
			log.info(it?.inventoryItem?.id + " " + it.product + " " + it.lotNumber + " " + it.expirationDate)
			if (!it.inventoryItem) { 
				// Find an existing inventory item for the given lot number and product and description
				log.info("Find inventory item " + it.product + " " + it.lotNumber)
				def inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(it.product, it.lotNumber)
				log.info("Found inventory item? " + inventoryItem)
				
				// If the inventory item doesn't exist, we create a new one
				if (!inventoryItem) {
					inventoryItem = new InventoryItem();
					inventoryItem.lotNumber = it.lotNumber
					inventoryItem.expirationDate = (it.lotNumber) ? it.expirationDate : null
					inventoryItem.product = it.product;
					log.info("Save inventory item " + inventoryItem)
					if (inventoryItem.hasErrors() || !inventoryItem.save()) {
						inventoryItem.errors.allErrors.each { error->
							command.errors.reject("inventoryItem.invalid",
								[inventoryItem, error.getField(), error.getRejectedValue()] as Object[],
								"[${error.getField()} ${error.getRejectedValue()}] - ${error.defaultMessage} ");
							
						}
					}
				}
				it.inventoryItem = inventoryItem
			}
		}	
		
		// Now that all transaction entries in the command have inventory items, 
		// we need to create a persistable transaction entry
		command.transactionEntries.each {
			def transactionEntry = new TransactionEntry(inventoryItem: it.inventoryItem, quantity: it.quantity)			
			transactionInstance.addToTransactionEntries(transactionEntry)
		}

		// Check to see if there are errors, if not save the transaction
		if (!transactionInstance.hasErrors()) {
			try {
				// Validate the transaction object
				if (!transactionInstance.hasErrors() && transactionInstance.validate()) {
					transactionInstance.save(failOnError: true)
					flash.message = "Successfully saved transaction " + transactionInstance.transactionNumber()
					//redirect(controller: "inventory", action: "browse")
					redirect(controller: "inventory", action: "showTransaction", id: transactionInstance?.id)
				}
			} catch (ValidationException e) {
				log.info ("caught validation exception " + e)
			}
		}

		// Should be true if a validation exception was thrown 
		if (transactionInstance.hasErrors()) {
			log.info ("has errors" + transactionInstance.errors)
		
			// Get the list of products that the user selected from the inventory browser
			if (params.product?.id) {
				def productIds = params.list('product.id')
				def products = productIds.collect { Long.valueOf(it); }
				command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, products);
			}
			// If given a list of inventory items, we just return those inventory items
			else if (params?.inventoryItem?.id) {
				def inventoryItemIds = params.list('inventoryItem.id')
				def inventoryItems = inventoryItemIds.collect { InventoryItem.get(Long.valueOf(it)); }
				command?.productInventoryItems = inventoryItems.groupBy { it.product }
			}
			
			// Populate the command object and render the form view.
			//command.transactionInstance = transaction
			command.warehouseInstance = warehouseInstance
			command.quantityMap = quantityMap;
			command.transactionTypeList = TransactionType.list();
			command.locationList = Location.list();
			
			render(view: "createTransaction", model: [command: command]);
		}
	}
	
	/**
	 * Save a transaction that debits stock from the given inventory.
	 * 
	 * Not used at the moment.  
	 */
	def saveOutgoingTransfer = { Transaction transaction, TransactionCommand command ->
		log.info ("Saving stock transfer " + params)

		def warehouseInstance = Location.get(session?.warehouse?.id);
		def quantityMap = inventoryService.getQuantityForInventory(warehouseInstance?.inventory)
		
		// Validate transaction entries
		log.info ("BEGINNING")
		def transactionEntriesToRemove = []
		transaction.transactionEntries.each {
			log.info("transaction entry " + it.inventoryItem + " " + it.quantity);
			def quantityOnHand = quantityMap[it.inventoryItem]
			if (quantityOnHand < it.quantity) {
				transaction.errors.rejectValue("transactionEntries", "transactionEntry.quantity.invalid", 
													[it?.inventoryItem?.lotNumber] as Object[], "")
			} 
			
			if (!it.quantity) { 
				log.info ("remove " + it?.inventoryItem?.id + " from transaction entries")
				transactionEntriesToRemove.add(it)
			}
		}

		// Remove any transaction entries that are invalid
		transactionEntriesToRemove.each {
			log.info("REMOVE " + it.inventoryItem + " " + it.quantity);
			transaction.transactionEntries.remove(it)
		}
		
		log.info ("REMAINING")
		transaction.transactionEntries.each {
			log.info("transaction entry " + it.inventoryItem + " " + it.quantity);
		}
		// Check to see if there are errors, if not save the transaction 		
		if (!transaction.hasErrors()) { 
			try { 
				// Validate the transaction object
				if (!transaction.hasErrors() && transaction.validate()) { 
					transaction.save(failOnError: true)				
					flash.message = "Successfully saved transaction " + transaction.transactionNumber()
					//redirect(controller: "inventory", action: "browse")
					redirect(controller: "inventory", action: "showTransaction", id: transaction?.id)					
				} 
			} catch (ValidationException e) { 
				log.info ("caught validation exception " + e)
			}
		}

		// After the attempt to save the transaction, there might be errors on the transaction		
		if (transaction.hasErrors()) { 
			log.info ("has errors" + transaction.errors)
			
			// Get the list of products that the user selected from the inventory browser
			if (params.product?.id) {
				def productIds = params.list('product.id')
				def products = productIds.collect { Long.valueOf(it); }
				command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, products);
			}
			// If given a list of inventory items, we just return those inventory items
			else if (params?.inventoryItem?.id) {
				def inventoryItemIds = params.list('inventoryItem.id')
				def inventoryItems = inventoryItemIds.collect { InventoryItem.get(Long.valueOf(it)); }
				command?.productInventoryItems = inventoryItems.groupBy { it.product }
			}
			
			// Populate the command object and render the form view.
			command.transactionInstance = transaction
			command.warehouseInstance = warehouseInstance
			command.quantityMap = quantityMap;
			command.transactionTypeList = TransactionType.list();
			command.locationList = Location.list();
			
			render(view: "createTransaction", model: [command: command]);
		}			
	}

	
	
	def editTransaction = { 		
		log.info "edit transaction: " + params
		def transactionInstance = Transaction.get(params?.id)
		def warehouseInstance = Location.get(session?.warehouse?.id);
		def model = [ 
			
			transactionInstance: transactionInstance?:new Transaction(),
			productInstanceMap: Product.list().groupBy { it?.category },
			transactionTypeList: TransactionType.list(),
			locationInstanceList: Location.list(),
			quantityMap: inventoryService.getQuantityForInventory(warehouseInstance?.inventory),
			warehouseInstance: warehouseInstance ]

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
	
}


