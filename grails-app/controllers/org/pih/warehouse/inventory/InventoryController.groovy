
package org.pih.warehouse.inventory;

import groovy.sql.Sql;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.pih.warehouse.shipping.ShipmentStatusCode;
import org.pih.warehouse.util.DateUtil;
import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.Location 
import org.pih.warehouse.core.User;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.Warehouse;

import org.pih.warehouse.reporting.Consumption;

class InventoryController {
	
	def dataSource
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
		
		// if we have arrived via a quick link tab, reset any subcategories or search terms in the session
		if (params?.resetSearch) {
			session?.inventorySubcategoryId = null
			session?.inventorySearchTerms = null
		}
		
		// Pre-populate the sub-category and search terms from the session
		cmd.subcategoryInstance = Category.get(session?.inventorySubcategoryId)
		cmd.searchTerms = session?.inventorySearchTerms
		cmd.showHiddenProducts = session?.showHiddenProducts
		cmd.showOutOfStockProducts = session?.showOutOfStockProducts ?: true
		
		// If a new search is being performed, override the session-based terms from the request
		if (request.getParameter("searchPerformed")) {
			cmd.subcategoryInstance = Category.get(params?.subcategoryId)
			session?.inventorySubcategoryId = cmd.subcategoryInstance?.id
			cmd.searchTerms = params.searchTerms
			session?.inventorySearchTerms = cmd.searchTerms
			cmd.showHiddenProducts = params?.showHiddenProducts == "on"
			session?.showHiddenProducts = cmd.showHiddenProducts
			cmd.showOutOfStockProducts = params?.showOutOfStockProducts == "on"
			session?.showOutOfStockProducts = cmd.showOutOfStockProducts
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
		def expiringStock = InventoryItem.findAllByExpirationDateBetween(today+1, today+365, [sort: 'expirationDate', order: 'asc']);
		//def expiringStock = InventoryItem.list([sort: 'expirationDate', order: 'asc']);
		
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
			expiringStock = expiringStock.findAll { item -> (item?.expirationDate && (item?.expirationDate - today) <= threshholdSelected) }
		}
		
		def warehouse = Warehouse.get(session.warehouse.id)		
		def quantityMap = inventoryService.getQuantityForInventory(warehouse.inventory)
		
		
		
		[expiredStock : expiredStock, expiringStock: expiringStock, quantityMap: quantityMap, categories : categories, 
			categorySelected: categorySelected, threshholdSelected: threshholdSelected, excludeExpired: excludeExpired]
	}
	
	def listLowStock = {
		def warehouse = Warehouse.get(session.warehouse.id)
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

	
	def showConsumption2 = { 
		
		//Sql sql = new Sql(dataSource)
		//sql.executeInsert("select ")
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
		def warehouse = Warehouse.get(session.warehouse.id)
		
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
	
	def addTo = {
		log.info("addTo params " + params)

		// Process productId parameters from inventory browser
		if (params.productId) {
			log.info("Using params.productId");
			def productIds = params.list('productId')
			log.info("productIds " + productIds)
			def productList = productIds.collect { Long.valueOf(it); }
			flash.productList = productList;
		}

		// Then redirect to the appropriate action 
		if (params.actionButton) {
			if (params.actionButton.equals("addToShipment")) {
				redirect(controller: "shipment", action: "addToShipment", params: params)
				return;
			}
			else if (params.actionButton.equals("addToTransaction")) {
				redirect(controller: "inventory", action: "createTransaction", params: params)
				return;
			}
			/*
			else if (params.actionButton.equals("showConsumption")) { 
				redirect(controller: "inventory", action: "createTransaction", params: params)
				return;
			}*/
			else {
				flash.message = "${warehouse.message(code: 'action.not.found.message', args: [params.actionButton])}"
			}
		}
		redirect(action: "browse");
		
	}
	
	
	def createTransaction = { 
		log.info("createTransaction params " + params)
		def transactionInstance = new Transaction();
		def productList = [] 
		
		// From addTo action
		if (flash.productList) { 
			log.info("Using flash.productList");
			flash.productList.each { 
				productList << it;
			}
		}
		
		// From show stock card page
		if (params?.product?.id) { 
			log.info("Using params.product.id");
			def product = Product.get(params.product.id)
			if (product) { 
				productList << product.id;
			}
		}
		
		if (productList) { 
			productList.each { 
				log.info("find product " + it)
				def product = Product.get(it);
				def warehouse = Warehouse.get(session.warehouse.id)
				def inventory = Inventory.get(warehouse.inventory.id);
				log.info ("inventory=" + inventory + ", product=" + product)
				//def inventoryItems = inventoryService.getInventoryItemsByProductAndInventory(product, inventory);
				def inventoryItems = inventoryService.getInventoryItemsByProduct(product);
				log.info "inventory items " + inventoryItems
				inventoryItems.each { inventoryItem ->
					def transactionEntry = new TransactionEntry(product: product, inventoryItem: inventoryItem);
					transactionInstance.addToTransactionEntries(transactionEntry);
				}
			}
		}
		
		def warehouseInstance = Warehouse.get(session?.warehouse?.id);
		
		def model = [
			transactionInstance : transactionInstance,
			productInstanceMap: Product.list().groupBy { it.category },
			transactionTypeList: TransactionType.list(),
			locationInstanceList: Location.list(),
			quantityMap: inventoryService.getQuantityForInventory(warehouseInstance?.inventory),
			warehouseInstance: warehouseInstance
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
		
		// Get all transaction entries that have a positive quantity
		transactionInstance?.transactionEntries = 
			transactionInstance?.transactionEntries?.findAll { it.quantity && it.quantity != 0 } 
		
			
		// Iterate over the transaction entries to make sure it has a valid inventory item 
		transactionInstance?.transactionEntries.each { 
			log.info "Saving transaction entry " + it + " " + it.quantity
			// FIXME This is a bit of a hack.  We needed to be able to bind the product and lot number so 
			// we bind that to an unsaved inventory item in the transaction entry.  Then we need to look it up
			// just in case there's already an inventory item with that product and lot number 
			def product = Product.get(it?.inventoryItem?.product?.id)
			def inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(product, it?.inventoryItem?.lotNumber)
				
			if (inventoryItem) {
				it.inventoryItem = inventoryItem;
			}
		 
			// Make sure the user has not set a negative quantity 
			// (the transaction type handles whether the quantity is a DEBIT/CREDIT transaction)
			if (it?.quantity && it.quantity < 0) { 
				log.info ("Making quantity positive " + it.quantity)
				it.quantity = -(it.quantity);
			}
						
			// We need to save the inventory item in case it's new or has been modified by the user
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
			def warehouseInstance = Warehouse.get(session?.warehouse?.id)
			def model = [ 
				transactionInstance : transactionInstance,
				productInstanceMap: Product.list().groupBy { it.category },
				transactionTypeList: TransactionType.list(),
				locationInstanceList: Location.list(),
				quantityMap: inventoryService.getQuantityForInventory(warehouseInstance?.inventory),
				warehouseInstance: warehouseInstance
			]
			render(view: "editTransaction", model: model);
		}		
	}
	
	
	def editTransaction = { 		
		log.info "edit transaction: " + params
		def transactionInstance = Transaction.get(params?.id)
		def warehouseInstance = Warehouse.get(session?.warehouse?.id);
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


