package org.pih.warehouse.inventory;

import java.util.Map;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.ListUtils;
import org.grails.plugins.excelimport.ExcelImportUtils;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.Warehouse;
import org.pih.warehouse.product.Attribute;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.ProductAttribute;
import org.pih.warehouse.shipping.Container;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.core.Constants 
import org.pih.warehouse.core.LocationType;
import org.springframework.validation.Errors;

class InventoryService {
	
	def productService;
	boolean transactional = true
	
	List<Warehouse> getAllWarehouses() {
		return Warehouse.list()
	}
	
	/**
    * Returns the Warehouse specified by the passed id parameter;
    * if no parameter is specified, returns a new warehouse instance
    */
    Warehouse getWarehouse(Long warehouseId) {
	   	if (warehouseId) {
	   		Warehouse warehouse = Warehouse.get(warehouseId)
	   		if (!warehouse) {
	   			throw new Exception("No warehouse found with warehouseId ${warehouseId}")
	   		}
	   		else {
	   			return warehouse
	   		}
	   	}
	   	// otherwise, we need to create a new warehouse
	   	else {
	   		Warehouse warehouse = new Warehouse()
	   		warehouse.locationType = LocationType.findByName('Warehouse')
	   		
	   		return warehouse
	   	}
    }
    
    /**
     * Saves the specified warehouse
     */
	void saveWarehouse(Warehouse warehouse) {
		// make sure a warehouse has an inventory
		if (!warehouse.inventory) {
			addInventory(warehouse)
		}
		warehouse.save(flush:true)
	}
    
	
	List<Transaction> getAllTransactions(Warehouse warehouse) {
		return Transaction.withCriteria { eq("thisWarehouse", warehouse) }
	}
	
	/**
	 * Gets the inventory associated with this warehouse;
	 * if no inventory, create a new inventory
	 */
	Inventory getInventory(Warehouse warehouse) {
		Inventory inventory = Inventory.withCriteria { eq("warehouse", warehouse) }
		return  inventory ?: addInventory(warehouse)
	}
	
	/**
	 * Adds an inventory to the specified warehouse
	 */
	Inventory addInventory(Warehouse warehouse) {
		if (!warehouse) {
			throw new RuntimeException("No warehouse specified.")
		}
		if (warehouse.inventory) {
			throw new RuntimeException("An inventory is already associated with this warehouse.")
		}
		
		warehouse.inventory = new Inventory([ 'warehouse' : warehouse ])
		saveWarehouse(warehouse)
		
		return warehouse.inventory
	}
	
	/**
	 * Returns a map of products grouped by category.
	 * 
	 * TODO Make sure this is doing what it's intended to do.  The groupBy 
	 * expression looks a little weird to me.
	 * 
	 * @param id
	 * @return
	 */
	Map getProductMap(Long id) { 		
		return Product.getAll().groupBy { it.categories*.parents } 
	}
	
	
	List searchInventoryItems(String searchTerm, String productId) { 		
		searchTerm = "%" + searchTerm + "%";
		def items = InventoryItem.withCriteria {
			or {
				ilike("lotNumber", searchTerm)
				//ilike("description", searchTerm)
				product { 
					ilike("name", searchTerm)
				}
			}				
			maxResults(10)
			
		}
		return items;
	}
	
	Integer getQuantity(String lotNumber, Inventory inventory) { 		
		def transactionEntries = getTransactionEntriesByLotNumberAndInventory(lotNumber, inventory);
		return (transactionEntries) ? transactionEntries*.quantity.sum() : 0;
		
	}
	

	BrowseInventoryCommand browseInventory(BrowseInventoryCommand commandInstance, Map params) { 
		
		// Get all product types and set the default product type				
		commandInstance.rootCategory = productService.getRootCategory();
		commandInstance.inventoryInstance = commandInstance?.warehouseInstance?.inventory;
		commandInstance.categoryInstance = Category.get(params?.categoryId)
		commandInstance.categoryInstance = commandInstance?.categoryInstance ?: commandInstance?.rootCategory;
		//commandInstance.productList = (commandInstance?.categoryFilters) ? getProductsByCategories(commandInstance?.categoryFilters, params) : [];		
		
		commandInstance.productList = getProducts(commandInstance, params);
		
		// This list gets calculated AFTER the product list, because we need to use the product list as the basis. 
		commandInstance.attributeMap = getProductAttributes();
		commandInstance.productMap = getProductMap(commandInstance?.warehouseInstance?.id);
		commandInstance.inventoryItemMap =  getInventoryItemMap(commandInstance?.warehouseInstance?.id);
		commandInstance.productList = commandInstance?.productList?.sort() { it.name };
		commandInstance.quantityMap = getQuantityMap(commandInstance?.inventoryInstance);
		return commandInstance;
	}
	
	
	/**
	 * Get a map of product attribute-value pairs for the given products.
	 * If products is empty, then we return all attribute-value pairs.
	 */
	Map getProductAttributes() { 
		//def map = new HashMap<Attribute, List<String>>();
		def productAttributes = ProductAttribute.list()
		//productAttributes.each { 			
		//	log.info it.product + " " + it.attribute + " " + it.value;
		//	map.put(it.attribute, it.value);
		//}
		return productAttributes.groupBy { it.attribute } 
	}
	
	
	/** 
	 * Get a map of quantities (indexed by product) for a particular inventory.
	 * 
	 * TODO This might perform poorly as we add more and more transaction entries 
	 * into an inventory.
	 */
	Map getQuantityMap(def inventoryInstance) { 
		def quantityMap = [:]
		def transactionEntries = TransactionEntry.createCriteria().list { 
			transaction { 
				eq("inventory.id", inventoryInstance?.id)
			}	
		}
		log.debug "transaction entries " + transactionEntries;		
		transactionEntries.each { 
			def currentQuantity = (quantityMap.get(it.product))?:0;			
			currentQuantity += it.quantity
			quantityMap.put(it.product, currentQuantity)
		}
		return quantityMap;		
	}

	
	
	RecordInventoryCommand getRecordInventoryCommand(RecordInventoryCommand commandInstance, Map params) { 		
		log.debug "Params " + params;
		
		if (!commandInstance?.product) { 
			commandInstance.errors.reject("error.product.invalid","Product does not exist");
		}
		else { 			
			commandInstance.recordInventoryRow = new RecordInventoryRowCommand();
			
			// This gets all inventory items for a product
			//def inventoryItemList = getInventoryItemsByProduct(commandInstance?.product)
			
			// What we want is to get all inventory items that have been involved in transactions at this warehouse
			//def inventoryItemList = getInventoryItemsByProductAndInventory(commandInstance?.product, commandInstance?.inventory);
			
			def transactionEntryList = getTransactionEntriesByProductAndInventory(commandInstance?.product, commandInstance?.inventory);
			
			def transactionEntryMap = transactionEntryList.groupBy { it.inventoryItem } 
			
			transactionEntryMap.keySet().each { 
				def transactionEntries = transactionEntryMap.get(it);
				def quantity = (transactionEntries)?transactionEntries*.quantity.sum():0;
				
				def inventoryItemRow = new RecordInventoryRowCommand()
				inventoryItemRow.id = it.id;
				inventoryItemRow.lotNumber = it.lotNumber;
				inventoryItemRow.expirationDate = it.expirationDate;
				//inventoryItemRow.description = it.description;
				inventoryItemRow.oldQuantity = quantity;
				inventoryItemRow.newQuantity = quantity;
				commandInstance.recordInventoryRows.add(inventoryItemRow);
			}
		}
		return commandInstance;
		
	}
		
	
	RecordInventoryCommand saveRecordInventoryCommand(RecordInventoryCommand cmd, Map params) { 
		log.debug "Saving record inventory command params: " + params
		
		try { 
			// Validation was done during bind, but let's do this just in case
			if (cmd.validate()) { 
				def inventoryItems = getInventoryItemsByProductAndInventory(cmd.product, cmd.inventory)
				// Create a new transaction
				def transaction = new Transaction(cmd.properties)
				
				// FIXME Change this to be a valid lookup
				transaction.transactionType = TransactionType.get(7)
				
				// Process each row added to the record inventory page
				cmd.recordInventoryRows.each { row -> 					
					// 1. Find an existing inventory item for the given lot number and product and description
					// FIXME need to add description here
					def inventoryItem = 
						InventoryItem.findByLotNumberAndProduct(row.lotNumber, cmd.product)
					//def inventoryItem = 
					//	findInventoryItemByProductAndLotNumberAndDescription(cmd.product, row.lotNumber, row.description);
					
					
					// 2. If the inventory item doesn't exist, we create a new one
					if (!inventoryItem) { 
						inventoryItem = new InventoryItem();
						inventoryItem.properties = row.properties
						inventoryItem.product = cmd.product;
						if (!inventoryItem.hasErrors() && inventoryItem.save()) { 										
							
						}
						else {
							// TODO Old error message = "Property [${error.getField()}] of [${inventoryItem.class.name}] with value [${error.getRejectedValue()}] is invalid"
							inventoryItem.errors.allErrors.each { error->
								cmd.errors.reject("inventoryItem.invalid",
									[inventoryItem, error.getField(), error.getRejectedValue()] as Object[],
									"[${error.getField()} ${error.getRejectedValue()}] - ${error.defaultMessage} ");
								
							}
							// We need to fix these errors before we can move on
							return cmd;
						}
					}
					// 3. If the quantities are different, we create a new transaction entry	
					if (row.oldQuantity != row.newQuantity) {
						def transactionEntry = new TransactionEntry();
						transactionEntry.properties = row.properties;
						transactionEntry.quantity = row.newQuantity - row.oldQuantity;  // difference
						transactionEntry.product = cmd.product
						transactionEntry.inventoryItem = inventoryItem;
						transaction.addToTransactionEntries(transactionEntry);						
					}
				}		
				
				
				// 4. Make sure that the inventory item has been saved before we process the transactions
				if (!cmd.hasErrors()) { 
					// Check if there are any changes recorded ... no reason to  
					if (!transaction.transactionEntries) { 	
						// We could do this, but it keeps us from changing the lot number and description
						cmd.errors.reject("transaction.noChanges", "There are no quantity changes in the current transaction");
					}
					else { 
						if (!transaction.hasErrors() && transaction.save()) { 
							// We saved the transaction successfully
						}
						else { 
							transaction.errors.allErrors.each { error ->
								cmd.errors.reject("transaction.invalid",
									[transaction, error.getField(), error.getRejectedValue()] as Object[],
									"Property [${error.getField()}] of [${transaction.class.name}] with value [${error.getRejectedValue()}] is invalid");
							}
						}
					}				
				}
			}
		} catch (Exception e) { 
			log.error("Error saving an inventory record to the database ", e);
			throw e;
		}				
		return cmd;
	}
	
	/*
	 * 
	 * Get products based on the 
	 */
	Set getProducts(BrowseInventoryCommand command, Map params) { 
		def products = new HashSet();
		
		if (command?.searchTerms && command?.categoryFilters) { 
			log.info "search " + command?.searchTerms;
			//products += getProductsBySearchTerms(command?.searchTerms);
			products = getProductsByAll(command?.searchTerms, command?.categoryFilters);
			
		}
		else if (command?.searchTerms) { 
			products = getProductsBySearchTerms(command?.searchTerms)
		}
		else if (command?.categoryFilters) { 
			products = getProductsByCategories(command?.categoryFilters, params);
		}
		else { 
			products = Product.list();
		}
		log.info "products " + products.unique();
		return products;		
	}


	/** 
	 * 	
	 * @param searchTerms
	 * @param categories
	 * @return
	 */
	List getProductsByAll(String searchTerms, List categories) { 
		// Get products that match the search terms by name and category
		def matchCategories = getExplodedCategories(categories);
		def products = Product.createCriteria().list() {
			if (searchTerms) {
				and {
					ilike("name", "%" + searchTerms + "%")
					'in'("category", matchCategories)
					
				}
			}
		}
	}
		
	/**
	 * Get products by search terms only, matching against product name OR category name.
	 * @param searchTerms
	 * @return
	 */
	List getProductsBySearchTerms(String searchTerms) { 
		log.info "get producst by search terms " + searchTerms;
		
		// Get products that match the search terms by name and category
		def products = Product.createCriteria().list() { 
			if (searchTerms) {
				or {
					ilike("name", "%" + searchTerms + "%")					
					category { 
						ilike("name", "%" + searchTerms + "%")
					}
				}
			}
		}
		/*
		if (!products) { 
			// Get products that match a category (e.g. Equipment matches all products 
			// under Equipment and its subcategories.
			def categories = Category.withCriteria { 
				ilike("name", searchTerms + "%");
			}		
			def matchedCategories = getExplodedCategories(categories);
			if (matchedCategories) { 
				products += Product.createCriteria().list() {
					'in'("category", matchedCategories)
				}		
			}
		
			// Get products that match inventory item by lot number or name.
			def inventoryItems = InventoryItem.withCriteria {
				or {
					ilike("lotNumber", searchTerms + "%")
					//ilike("description", searchTerms + "%")
					product {
						ilike("name", searchTerms + "%")
					}
				}
			}
			products += inventoryItems*.product;
		}
		*/
		log.info products
		
		return products;
	}
	
	/**
	 * Returns a list of categories and their children, given an initial set of 
	 * categories.  This function should probably be recursive so that we traverse 
	 * the entire category tree. 
	 * 
	 * @param categories
	 * @return
	 */
	List getExplodedCategories(List categories) { 		
		def matchCategories = []
		if (categories) {
			categories.each { cat ->
				if (cat) {
					matchCategories << cat;
					matchCategories.addAll( (cat?.children)?cat.children:[]);
				}
			}
		}
		return matchCategories;
	}
	

	/**
	 * Returns a list of products by category.  
	 * 	
	 * @param categories
	 * @param params
	 * @return
	 */
	List getProductsByCategories(List categories, Map params) { 
		def products = []
		
		def matchCategories = getExplodedCategories(categories);
		
		log.debug matchCategories
		if (matchCategories) { 
			products = Product.createCriteria().list(max:params.max, offset: params.offset ?: 0) {
				'in'("category", matchCategories)
			}
		}
		return products;	 
	}
	
	List getProductsByCategory(Category category, Map params) { 
		def products = [];
		if (category) { 
			def categories = (category?.children)?category.children:[];
			categories << category;
			if (categories) {
				products = Product.createCriteria().list(max:params.max, offset: params.offset ?: 0) {
					'in'("category", categories)
				}
			}
		}
		return products;
	}
	
	/**
	 * @param productId
	 * @return a list of inventory items 
	 */
	List getInventoryItemsByProduct(Product productInstance) { 
		if (!productInstance) 
			throw new Exception("ProductNotFoundException")
		return InventoryItem.findAllByProduct(productInstance)					
		
	}

	
	/**
	 * 
	 * @param product
	 * @param lotNumber
	 * @return
	 */
	InventoryItem findInventoryItemByProductAndLotNumber(Product product, String lotNumber) {
		def inventoryItems = InventoryItem.createCriteria().list() {
			and {
				eq("product.id", product?.id)
				if (lotNumber)
					eq("lotNumber", lotNumber)
				else 
					isNull("lotNumber")
			}
		}
		log.info ("Returned inventory items " + inventoryItems);
		// If the list is non-empty, return the first item
		if (inventoryItems) { 
			return inventoryItems.get(0);
		}
		return null;
	}
	 
	
	/**
	 * Get all inventory items for a given product within the given inventory.
	 * @param productInstance
	 * @param inventoryInstance
	 * @return a list of inventory items.
	 */
	Set getInventoryItemsByProductAndInventory(Product productInstance, Inventory inventoryInstance) {	   
		def inventoryItems = [] as Set
		def transactionEntries = getTransactionEntriesByProductAndInventory(productInstance, inventoryInstance);
		transactionEntries.each { 
			inventoryItems << it.inventoryItem;
		}
		inventoryItems.sort { it.lotNumber } 
		return inventoryItems;
	}

	
	/**
	 * Get a single inventory level instance for the given product and inventory.
	 * 
	 * @param productInstance
	 * @param inventoryInstance
	 * @return
	 */
	InventoryLevel getInventoryLevelByProductAndInventory(Product productInstance, Inventory inventoryInstance) { 		
		def inventoryLevel = InventoryLevel.findByProductAndInventory(productInstance, inventoryInstance)
				
		return (inventoryLevel)?:new InventoryLevel();
		
	}
	
	
	/**
	 * Get all transaction entries for a particular inventory item.
	 * @param itemInstance
	 * @return
	 */
	List getTransactionEntriesByInventoryItem(InventoryItem itemInstance) { 
		return TransactionEntry.findAllByInventoryItem(itemInstance);
	}
	
	/**
	 * Get all transaction entries by product (this isn't very useful).  
	 * 
	 * @param productInstance
	 * @return
	 */
	List getTransactionEntriesByProduct(Product productInstance) { 		
		return TransactionEntry.findAllByProduct(productInstance)		
	}
	
	
	/**
	 * Get all transaction entries over all products/inventory items.
	 * 
	 * @param inventoryInstance
	 * @return
	 */
	List getTransactionEntriesByInventory(Inventory inventoryInstance) { 
		return TransactionEntry.createCriteria().list() {
			transaction {
				eq("inventory", inventoryInstance)
			}
		}
	}

	
	/**
	 * Get all transaction entries for a product within an inventory.  
	 * 
	 * @param productInstance
	 * @param inventoryInstance
	 * @return
	 */
	List getTransactionEntriesByProductAndInventory(Product productInstance, Inventory inventoryInstance) {
		return TransactionEntry.createCriteria().list() {
			and { 
				eq("product.id", productInstance?.id)
				transaction {
					eq("inventory", inventoryInstance)
				}
			}
		}
	}

	/**
	* Get all transaction entries for a lot number within an inventory.
	*
	* @param productInstance
	* @param inventoryInstance
	* @return
	*/
   List getTransactionEntriesByLotNumberAndInventory(String lotNumber, Inventory inventoryInstance) {
	   return TransactionEntry.createCriteria().list() {
		   and {
			   eq("lotNumber", lotNumber)
			   transaction {
				   eq("inventory", inventoryInstance)
			   }
		   }
	   }
   }
	
		
	List getInventoryItemList(Long id) { 
		def list = []
		def warehouseInstance = Warehouse.get(id);
		def inventoryInstance = warehouseInstance?.inventory;
		if (inventoryInstance) { 
			def transactionEntries = getTransactionEntriesByInventory(inventoryInstance)
			list = transactionEntries*.inventoryItem
		}				
		return list;
	}	
	
	Map getInventoryItemMap(Long id) { 
		return getInventoryItemList(id)?.groupBy { it.product } 
	}

	
	Map getInventoryLevelMap(Long id) { 
		//return Warehouse.get(id)?.inventory?.inventoryLevels?.groupBy { it.product } 
		return new HashMap();
	}

	/**
	 * Adjusts the stock level by adding a new transaction entry with a 
	 * quantity change.
	 * 
	 * Passing in an instance of inventory item so that we can attach errors.	
	 * @param inventoryItem
	 * @param params
	 */
	boolean adjustStock(InventoryItem itemInstance, Map params) { 
		
		def inventoryInstance = Inventory.get(params?.inventory?.id)
		
		if (itemInstance && inventoryInstance) { 
			def transactionEntry = new TransactionEntry(params);
			def quantityChange = 0;
			try { 
				quantityChange = Integer.valueOf(params?.newQuantity) - Integer.valueOf(params?.oldQuantity);				
			} catch (Exception e) { 
				itemInstance.errors.reject("inventorItem.quantity.invalid")			
			}
						
			def transactionInstance = new Transaction(params);
			if (transactionEntry.hasErrors() || transactionInstance.hasErrors()) {
				itemInstance.errors = transactionEntry.errors
				itemInstance.errors = transactionInstance.errors
			}
			
			
						
			// TODO Move all of this logic into the service layer in order to take advantage of Hibernate/Spring transactions
			if (!itemInstance.hasErrors() && itemInstance.save()) {			
				// Need to create a transaction if we want the inventory item
				// to show up in the stock card
				transactionInstance.transactionDate = new Date();			
				// FIXME Not sure what transaction type this is -- should be ADJUSTMENT 
				transactionInstance.transactionType = TransactionType.get(7);
				transactionInstance.inventory = inventoryInstance;			
				
				// Add transaction entry to transaction
				transactionEntry.inventoryItem = itemInstance;
				transactionEntry.product = itemInstance.product;
				transactionEntry.lotNumber = itemInstance.lotNumber;
				// FIXME Not a safe way to do this - we should recalculate the old quantity 
				transactionEntry.quantity = quantityChange;
				transactionInstance.addToTransactionEntries(transactionEntry);			
				if (!transactionInstance.hasErrors() && transactionInstance.save()) {			
				
				}
				else { 
					transactionInstance?.errors.allErrors.each { 
						itemInstance.errors << it;
					}
				}

			}
			

		}
	}
	
	
	/**
	 * Create a transaction for the Send Shipment event.
	 * 
	 * @param shipmentInstance
	 */
	void createSendShipmentTransaction(Shipment shipmentInstance) { 
		log.info "create send shipment transaction" 
		
		if (!shipmentInstance.origin.isWarehouse()) {
			throw new RuntimeException ("Can't create send shipment transaction for origin that is not a warehouse")
		}
		
		try { 
			// Create a new transaction for outgoing items
			Transaction debitTransaction = new Transaction();
			debitTransaction.transactionType = TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID)
			debitTransaction.source = null
			debitTransaction.destination = shipmentInstance?.destination.isWarehouse() ? shipmentInstance?.destination : null
			debitTransaction.inventory = shipmentInstance?.origin?.inventory ?: addInventory(shipmentInstance.origin)
			debitTransaction.transactionDate = new Date();
		
			shipmentInstance.shipmentItems.each {
				def inventoryItem = InventoryItem.findByLotNumberAndProduct(it.lotNumber, it.product)
				
				// If the inventory item doesn't exist, we create a new one
				if (!inventoryItem) {
					inventoryItem = new InventoryItem();
					inventoryItem.lotNumber = it.lotNumber
					inventoryItem.product = it.product
					if (!inventoryItem.hasErrors() && inventoryItem.save()) {
						// at this point we've saved the inventory item successfully
					}
					else {
						//
						inventoryItem.errors.allErrors.each { error->
							def errorObj = [inventoryItem, error.getField(), error.getRejectedValue()] as Object[]
							shipmentInstance.errors.reject("inventoryItem.invalid",
								errorObj, "[${error.getField()} ${error.getRejectedValue()}] - ${error.defaultMessage} ");
						}
						return;
					}
				}
				
				// Create a new transaction entry
				def transactionEntry = new TransactionEntry();
				transactionEntry.quantity = 0 - it.quantity;
				transactionEntry.lotNumber = it.lotNumber
				transactionEntry.product = it.product;
				transactionEntry.inventoryItem = inventoryItem;
				debitTransaction.addToTransactionEntries(transactionEntry);
			}
			debitTransaction.save();
		} catch (Exception e) { 
			log.error("error occrred while creating transaction ", e);
			throw e
			//shipmentInstance.errors.reject("shipment.invalid", e.message);  // this doesn't seem to working properly
		}
	}	

	
	
	/**
	 * Reads a file for the given filename and generates an object that mirrors the 
	 * file.  Also preprocesses the object to make sure that the data is formatted
	 * correctly. 
	 * 
	 * @param filename
	 * @param errors
	 * @return
	 */
	public List prepareInventory(Warehouse warehouse, String filename, Errors errors) { 
		log.info "prepare inventory"
		Map CONFIG_CELL_MAP = [
			sheet:'Sheet1', cellMap: [ ]
		]
	
		Map CONFIG_COLUMN_MAP = [
			sheet:'Sheet1', startRow: 1, 
			columnMap: [ 
				'A':'category',
				'B':'productDescription', 
				'C':'unitOfMeasure', 
				'D':'manufacturer', 
				'E':'manufacturerCode',
				'F':'upc',
				'G':'ndc',
				'H':'lotNumber',
				'I':'expirationDate',
				'J':'quantity'
			]
		]
		
		Map CONFIG_PROPERTY_MAP = [
			parentCategory:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			category:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			productDescription: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			unitOfMeasure: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			manufacturer:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			manufacturerCode:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			upc:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			ndc:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			lotNumber:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			expirationDate:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			quantity:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null])
		]
	
		
		def importer = new InventoryExcelImporter(filename, CONFIG_COLUMN_MAP, CONFIG_CELL_MAP, CONFIG_PROPERTY_MAP);
		def inventoryMapList = importer.getInventoryItems();
		
		def dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		def transactionInstance = new Transaction(transactionDate: new Date(),
			transactionType: TransactionType.findByName("Inventory"),
			inventory: warehouse.inventory,
			destination: warehouse)
		
		// Iterate over each row
		inventoryMapList.each { Map importParams ->
			//log.info "Inventory item " + importParams
			
			def lotNumber = (importParams.lotNumber) ? String.valueOf(importParams.lotNumber) : null;
			if (importParams?.lotNumber instanceof Double) {
				errors.reject("Property 'Serial Number / Lot Number' with value '${lotNumber}' should be not formatted as a Double value");
			}
			else if (!importParams?.lotNumber instanceof String) {
				errors.reject("Property 'Serial Number / Lot Number' with value '${lotNumber}' should be formatted as a Text value");
			}
			
			def quantity = (importParams.quantity) ? importParams.quantity : 0;
			if (!importParams?.quantity instanceof Double) {
				errors.reject("Property [quantity] with value '${lotNumber}' should be as a Double value");
			}
			else if (importParams?.quantity instanceof String) {
				errors.reject("Property [quantity] with value '${lotNumber}' should not be formatted as a Text value");
			}

			def manufacturerCode = (importParams.manufacturerCode) ? String.valueOf(importParams.manufacturerCode) : null;
			if (!importParams?.manufacturerCode instanceof String) {
				errors.reject("Property 'Manufacturer Code' with value '${manufacturerCode}' should be formatted as a Text value");
			}
			else if (importParams?.manufacturerCode instanceof Double) {
				errors.reject("Property 'Manufacturer Code' with value '${manufacturerCode}' should not be formatted as a Double value");
			}

			def upc = (importParams.upc) ? String.valueOf(importParams.upc) : null;
			if (!importParams?.upc instanceof String) {
				errors.reject("Property 'UPC' with value '${upc}' should be formatted as a Text value");
			}
			else if (importParams?.upc instanceof Double) {
				errors.reject("Property 'UPC' with value '${upc}' should be not formatted as a Double value");
			}

			def ndc = (importParams.ndc) ? String.valueOf(importParams.ndc) : null;
			if (!importParams?.ndc instanceof String) {
				errors.reject("Property 'GTIN' with value '${ndc}' should be formatted as a Text value");
			}
			else if (importParams?.ndc instanceof Double) {
				errors.reject("Property 'GTIN' with value '${ndc}' should be not formatted as a Double value");
			}

			
			def expirationDate = null;
			if (importParams.expirationDate) {
				// If we're passed a date, we can just set the expiration
				if (importParams.expirationDate instanceof org.joda.time.LocalDate) {					
					expirationDate = importParams.expirationDate.toDateMidnight().toDate();
				}
				else {
					try {
						expirationDate = dateFormat.parse(new String(importParams?.expirationDate));
					} catch (ParseException e) {
						errors.reject("Could not parse date " + importParams?.expirationDate + " " + e.getMessage() + ".  Expected date format: yyyy-MM-dd");
					}
				}
			}
			//def parentCategory = (importParams?.parentCategory) ? Category.findByName(importParams.parentCategory) : null;
			//def category = Category.findByNameAndParentCategory(importParams.category, parentCategory);
			
			def category = Category.findByName(importParams.category);
			if (!category) {
				//category = new Category(name: importParams.category, parentCategory: parentCategory);
				category = new Category(name: importParams.category);
				if (!category.validate()) {
					category.errors.allErrors.each {
						errors.addError(it);
					}
				}
				log.info "Created new category " + category.name;
			}
			
			// Create product if not exists
			Product product = Product.findByName(importParams.productDescription);
			if (!product) {
				def manufacturer = importParams.manufacturer;
				def unitOfMeasure = importParams.unitOfMeasure;
	
				product = new Product(
					name:importParams.productDescription, 
					upc:upc, 
					ndc:ndc, 
					category:category,
					manufacturer:manufacturer, 
					manufacturerCode:manufacturerCode, 
					unitOfMeasure:unitOfMeasure);
				
				if (!product.validate()) {
					errors.reject("Error saving product " + product?.name)
					//throw new RuntimeException("Error saving product " + product?.name)
				}
				log.info "Created new product " + product.name;
			}
			
			/*
			def manufacturer = importParams?.manufacturer
			if (manufacturer) {
				// If the product does not have the Manufacturer attribute, we need to add it
				def attribute = Attribute.findByName("Manufacturer");
				// Create a new attribute if it doesn't already exist
				if (!attribute) {
					attribute = new Attribute(name: "Manufacturer", allowOther: true);
					if (!attribute.validate()) {
						attribute.errors.allErrors.each {
							errors.addError(it);
						}
					}
				}
							
				def hasManufacturer = product.attributes*.attribute.contains(attribute)
				if (manufacturer && !hasManufacturer) {
					//attribute.addToOptions(value);
					//attribute.save();
					def productAttribute = new ProductAttribute();
					productAttribute.attribute = attribute;
					productAttribute.value = manufacturer;
					product.addToAttributes(productAttribute);
					if (!product.validate()) {
						product.errors.allErrors.each {
							errors.addError(it);
						}
					}
				}
			}
			*/
			
										
			// Find the inventory item by product and lotNumber and description
			InventoryItem inventoryItem =
				findInventoryItemByProductAndLotNumber(product, lotNumber);
			
			log.info("Inventory item " + inventoryItem)
			// Create inventory item if not exists
			if (!inventoryItem) {
				inventoryItem = new InventoryItem()
				inventoryItem.product = product
				inventoryItem.lotNumber = lotNumber;
				inventoryItem.expirationDate = expirationDate;
				if (!inventoryItem.validate()) {
					inventoryItem.errors.allErrors.each {
						errors.addError(it);
					}
				}
			}
			
			// Create a transaction entry if there's a quantity specified
			if (importParams?.quantity) {
				TransactionEntry transactionEntry = new TransactionEntry();
				transactionEntry.quantity = quantity;
				transactionEntry.product = product;
				transactionEntry.lotNumber = importParams.lotNumber;
				transactionEntry.inventoryItem = inventoryItem;
				transactionInstance.addToTransactionEntries(transactionEntry);
				if (!transactionEntry.validate()) { 
					transactionEntry.errors.allErrors.each {
						errors.addError(it);
					}
				}
			}
		}
		
		if (transactionInstance.validate()) { 
			transactionInstance.errors.allErrors.each { 
				errors.addError(it);
			}
		}
		
		
		
		return inventoryMapList
	}
	
	
	
	/**
	 * Import data from given inventoryMapList into database.
	 * 
	 * @param warehouse
	 * @param inventoryMapList
	 * @param errors
	 */
	public void importInventory(Warehouse warehouse, List inventoryMapList, Errors errors) { 
		
		
		try { 
			def dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			
			def transactionInstance = new Transaction(transactionDate: new Date(),
				transactionType: TransactionType.findByName("Inventory"),
				inventory: warehouse.inventory,
				destination: warehouse)
			
			
			// Iterate over each row 
			inventoryMapList.each { Map importParams ->
				
				def lotNumber = (importParams.lotNumber) ? String.valueOf(importParams.lotNumber) : null;
				def quantity = (importParams.quantity) ? importParams.quantity : 0;

				def unitOfMeasure = importParams.unitOfMeasure;
				def manufacturer = (importParams.manufacturer) ? String.valueOf(importParams.manufacturer) : null;
				def manufacturerCode = (importParams.manufacturerCode) ? String.valueOf(importParams.manufacturerCode) : null;
				def upc = (importParams.upc) ? String.valueOf(importParams.upc) : null;
				def ndc = (importParams.ndc) ? String.valueOf(importParams.ndc) : null;
								
				def expirationDate = null;
				if (importParams.expirationDate) {
					// If we're passed a date, we can just set the expiration 
					if (importParams.expirationDate instanceof org.joda.time.LocalDate) {
						expirationDate = importParams.expirationDate.toDateMidnight().toDate();
					}
					else { 
						try { 
							expirationDate = dateFormat.parse(new String(importParams?.expirationDate));
						} catch (ParseException e) { 
							errors.reject("Could not parse date " + importParams?.expirationDate + " " + e.getMessage() + ".  Expected date format: yyyy-MM-dd");
						}
					}
				}

				//def parentCategory = (importParams?.parentCategory) ? Category.findByName(importParams.parentCategory) : null;			
				//def category = Category.findByNameAndParentCategory(importParams.category, parentCategory);
				def category = Category.findByName(importParams.category);
				if (!category) {
					//category = new Category(name: importParams.category, parentCategory: parentCategory);
					category = new Category(name: importParams.category);
					
					if (!category.save()) { 
						category.errors.allErrors.each {
							errors.addError(it);
						}
					}
					log.info "Created new category " + category.name;
				}
				
				
				// Create product if not exists
				Product product = Product.findByName(importParams.productDescription);
				if (!product) {
					product = new Product(name:importParams.productDescription,
						upc:upc,
						ndc:ndc,
						category:category,
						manufacturer:manufacturer,
						manufacturerCode:manufacturerCode,
						unitOfMeasure:unitOfMeasure);
	
					if (!product.save()) { 
						errors.reject("Error saving product " + product?.name)
						//throw new RuntimeException("Error saving product " + product?.name)
					}
					log.info "Created new product " + product.name;
				}
				
				/*
				def manufacturer = importParams?.manufacturer
				if (manufacturer) { 
					// If the product does not have the Manufacturer attribute, we need to add it 
					def attribute = Attribute.findByName("Manufacturer");					
					// Create a new attribute if it doesn't already exist
					if (!attribute) { 
						attribute = new Attribute(name: "Manufacturer", allowOther: true);
						if (!attribute.save()) {
							attribute.errors.allErrors.each {
								errors.addError(it);
							}
						}
					}
				
					def hasManufacturer = product.attributes*.attribute.contains(attribute)
					if (manufacturer && !hasManufacturer) { 
						//attribute.addToOptions(value);
						//attribute.save();					
						def productAttribute = new ProductAttribute();
						productAttribute.attribute = attribute;
						productAttribute.value = manufacturer;
						product.addToAttributes(productAttribute);
						if (!product.save()) { 
							product.errors.allErrors.each {
								errors.addError(it);
							}
						}
					}
				}	
				*/			
				
											
				// Find the inventory item by product and lotNumber and description
				InventoryItem inventoryItem = 
					findInventoryItemByProductAndLotNumber(product, lotNumber);
					
					
				
				log.info("Inventory item " + inventoryItem)
				// Create inventory item if not exists
				if (!inventoryItem) {
					inventoryItem = new InventoryItem()
					inventoryItem.product = product
					inventoryItem.lotNumber = lotNumber;
					inventoryItem.expirationDate = expirationDate;
					if (inventoryItem.hasErrors() || !inventoryItem.save()) {				
						log.info "Product " + product
						log.info "Inventory item " + importParams.lotNumber;
						inventoryItem.errors.allErrors.each {
							log.error "ERROR " + it;
							errors.addError(it);
						}
					}
				}
				
				// Create a transaction entry if there's a quantity specified 
				if (importParams?.quantity) {
					TransactionEntry transactionEntry = new TransactionEntry();
					transactionEntry.quantity = quantity;
					transactionEntry.lotNumber = lotNumber;
					transactionEntry.product = product;
					transactionEntry.inventoryItem = inventoryItem;
					transactionInstance.addToTransactionEntries(transactionEntry);
				}
			}
			
			
			if (!errors.hasErrors()) { 
				if (!transactionInstance.save()) {
					transactionInstance.errors.allErrors.each { 
						errors.addError(it);
					}
				}
			}
		} catch (Exception e) { 
			// Bad practice but need this for testing
			log.error("Error importing inventory", e);
			throw e;
		}
		
	}
	
	/**
	 * Finds the local transfer (if any) associated with the given transaction
	 */
	LocalTransfer getLocalTransfer(Transaction transaction) {
		LocalTransfer transfer = null
		transfer = LocalTransfer.findBySourceTransaction(transaction)
		if (transfer == null) { transfer = LocalTransfer.findByDestinationTransaction(transaction) }
		return transfer
	}
	
	/** 
	 * Returns true/false if the given transaction is associated with a local transfer
	 */
	Boolean isLocalTransfer(Transaction transaction) {
		getLocalTransfer(transaction) ? true : false
	}
	
	/**
	 * Returns true/false if the passed transaction is a valid candidate for a local transfer
	 */
	Boolean isValidForLocalTransfer(Transaction transaction) {
		// make sure that the transaction is of a valid type
		if (transaction.transactionType.id != Constants.TRANSFER_IN_TRANSACTION_TYPE_ID  &&
				transaction.transactionType.id != Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
			return false
		}
	
		// make sure we are operating only on locally managed warehouses
		if (transaction.source) {
			if (!(transaction.source instanceof Warehouse)) { return false }
			else if (!transaction.source.local){ return false }
		}
		if (transaction.destination) {
			if (!(transaction.destination instanceof Warehouse)) { return false }
			else if (!transaction.destination.local) { return false }
		}
		
		return true
	}
	
	/**
	 * Deletes a local transfer (and underlying transactions) associated with the passed transaction
	 */
	void deleteLocalTransfer(Transaction transaction) {
		LocalTransfer transfer = getLocalTransfer(transaction)
		if (transfer) { 
			transfer.delete(flush:true) 
		}
	}
	
	/**
	 * Creates or updates the local transfer associated with the given transaction
	 * Returns true if the save/update was successful
	 */
	Boolean saveLocalTransfer(Transaction baseTransaction) {
		// note than we are using exceptions here to take advantage of Grails built-in transactional capabilities on service methods
		// if there is an error, we want to throw an exception so the whole transaction is rolled back
		// (we can trap these exceptions if we want in the calling controller)
		
		if (!isValidForLocalTransfer(baseTransaction)) {
			throw new RuntimeException("Invalid transaction for creating a local transaction")
		}
		
		// first save the base transaction
		if (!baseTransaction.save(flush:true)) {
			throw new RuntimeException("Unable to save base transaction " + baseTransaction?.id)
		}

		// try to fetch any existing local transfer
		LocalTransfer transfer = getLocalTransfer(baseTransaction)
		
		// if there is no existing local transfer, we need to create a new one and set the source or destination transaction as appropriate
		if (!transfer) {
			transfer = new LocalTransfer()
			if (baseTransaction.transactionType.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID ) {
				transfer.sourceTransaction = baseTransaction
			}
			else {
				transfer.destinationTransaction = baseTransaction
			}
		}
			
		// create and save the new mirrored transaction
		Transaction mirroredTransaction = createMirroredTransaction(baseTransaction)
		if (!mirroredTransaction.save(flush:true)) {
			throw new RuntimeException("Unable to save mirrored transaction " + mirroredTransaction?.id)
		}
		
		// now assign this mirrored transaction to the local transfer
		Transaction oldTransaction
		if (baseTransaction.transactionType.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID ) {
			oldTransaction = transfer.destinationTransaction
			transfer.destinationTransaction = mirroredTransaction
		}
		else {
			oldTransaction = transfer.sourceTransaction
			transfer.sourceTransaction = mirroredTransaction
		}
		
		// save the local transfer
		if (!transfer.save(flush:true)) {
			throw new RuntimeException("Unable to save local transfer " + transfer?.id)
		}
	
		// delete the old transaction
		if (oldTransaction) { 
			oldTransaction.delete(flush:true)
		}
		
		return true
	}
	
	/**
	 * Private utility method to create a "mirror" of the given transaction
	 * (If given a Transfer Out transaction, creates the appropriate Transfer In transacion, and vice versa)
	 */
	private Transaction createMirroredTransaction(Transaction baseTransaction) {
		Transaction mirroredTransaction = new Transaction()
		
		if (baseTransaction.transactionType.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
			mirroredTransaction.transactionType = TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID)
			mirroredTransaction.source = baseTransaction.inventory.warehouse
			mirroredTransaction.destination = null
			mirroredTransaction.inventory = baseTransaction.destination.inventory ?: addInventory(baseTransaction.destination)
		}
		else if (baseTransaction.transactionType.id == Constants.TRANSFER_IN_TRANSACTION_TYPE_ID) {
			mirroredTransaction.transactionType = TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID)
			mirroredTransaction.source = null
			mirroredTransaction.destination = baseTransaction.inventory.warehouse
			mirroredTransaction.inventory = baseTransaction.source.inventory ?: addInventory(baseTransaction.source)
		}
		else {
			throw new RuntimeException("Invalid transaction type for mirrored transaction")
		}
				
		mirroredTransaction.transactionDate = baseTransaction.transactionDate	
			
		// create the transaction entries based on the base transaction
		baseTransaction.transactionEntries.each {		
			def inventoryItem = InventoryItem.findByLotNumberAndProduct(it.lotNumber, it.product)
			
			// If the inventory item doesn't exist, we create a new one
			if (!inventoryItem) {
				inventoryItem = new InventoryItem(lotNumber: it.lotNumber, product:it.product)
				if (inventoryItem.hasErrors() && !inventoryItem.save()) {
					throw new RuntimeException("Unable to create inventory item $inventoryItem while creating local transfer")
				}
			}
			
			// Create a new transaction entry (inverting the quantity)
			def transactionEntry = new TransactionEntry();
			transactionEntry.quantity = 0 - it.quantity;
			transactionEntry.lotNumber = it.lotNumber
			transactionEntry.product = it.product;
			transactionEntry.inventoryItem = inventoryItem;
			mirroredTransaction.addToTransactionEntries(transactionEntry);
		}	
		
		return mirroredTransaction
	}
}
