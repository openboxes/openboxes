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
import org.pih.warehouse.shipping.Shipment;
import org.springframework.validation.Errors;

class InventoryService {
	
	def productService;
	boolean transactional = true
	
	List<Warehouse> getAllWarehouses() {
		return Warehouse.list()
	}
	
	List<Transaction> getAllTransactions(Warehouse warehouse) {
		return Transaction.withCriteria { eq("thisWarehouse", warehouse) }
	}
	
	Inventory getInventory(Warehouse warehouse) {
		return Inventory.withCriteria { eq("warehouse", warehouse) }
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
		searchTerm += "%";
		def items = InventoryItem.withCriteria {
			or {
				ilike("lotNumber", searchTerm)
				ilike("description", searchTerm)
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
	
	/*
	List getProducts(Long id, Category category) { 
		def productList = [] 
		if (category) { 
			productList = Product.createCriteria().list {
				categories { 
					eq ("id", category?.id)
				}
			}
		}
		log.debug "category " + category?.name + " " + productList?.size();
		
		
		return productList;
	}*/

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
				inventoryItemRow.description = it.description;
				inventoryItemRow.oldQuantity = quantity;
				inventoryItemRow.newQuantity = quantity;
				commandInstance.recordInventoryRows.add(inventoryItemRow);
			}
			
			/*
			inventoryItemList.each { 
				//def lot = InventoryLot.findByProductAndLotNumber(commandInstance?.product, it.lotNumber);				
				//def transactionEntryList = getTransactionEntriesByInventoryItem(it);
				log.debug "entries: " + transactionEntryList*.quantity;
				def quantity = (transactionEntryList)?transactionEntryList*.quantity.sum():0;
				
				
				def row = new RecordInventoryRowCommand()
				row.id = it.id;
				row.lotNumber = it.lotNumber;
				row.expirationDate = it.expirationDate;
				row.description = it.description;
				row.oldQuantity = quantity;
				row.newQuantity = quantity;
				
				//if (!commandInstance.recordInventoryRows)
				//	commandInstance.recordInventoryRows = ListUtils.lazyList([], FactoryUtils.constantFactory(new RecordInventoryRowCommand()))
				
				commandInstance.recordInventoryRows.add(row);
			}*/
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
					// 1. Find an existing inventory item for the given lot number and product
					def inventoryItem = InventoryItem.findByLotNumberAndProduct(row.lotNumber, cmd.product)
					
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
	
	Set getProducts(BrowseInventoryCommand command, Map params) { 
		def products = new HashSet();
		
			
		if (command?.searchTerms) { 
			log.info "search " + command?.searchTerms;
			products += getProductsBySearchTerms(command?.searchTerms);
		}
		else { 
			if (command?.categoryFilters)
			products = getProductsByCategories(command?.categoryFilters, params);
		}
		log.info "products " + products.unique();
		return products;		
	}

		
	List getProductsBySearchTerms(String searchTerms) { 
		
		// Get products that match the search terms by name and category
		def products = Product.createCriteria().list() { 
			if (searchTerms) {
				or {
					ilike("name", searchTerms + "%")					
					category { 
						ilike("name", searchTerms + "%")
					}
				}
			}
		}
		
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
		// Get producst that match inventory item by description, lot number, or name.
		def inventoryItems = InventoryItem.withCriteria {
			or {
				ilike("lotNumber", searchTerms + "%")
				ilike("description", searchTerms + "%")
				product {
					ilike("name", searchTerms + "%")
				}
			}
		}

		products += inventoryItems*.product;
		
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
			
	
	Warehouse getWarehouse(Long id) { 
		return Warehouse.get(id);
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
		
		try { 
			// Create a new transaction for outgoing items
			Transaction debitTransaction = new Transaction();
			debitTransaction.transactionType = TransactionType.get(1); 	// transfer
			debitTransaction.source = shipmentInstance?.origin
			debitTransaction.destination = shipmentInstance?.destination;
			debitTransaction.inventory = shipmentInstance?.origin?.inventory
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
			shipmentInstance.errors.reject("shipment.invalid", e.message);
		}
	}	
	
	public List prepareInventory(String filename) { 

		Map CONFIG_CELL_MAP = [
			sheet:'Sheet1', cellMap: [ 'D3':'title', 'D6':'numSold', ]
		]
	
		Map CONFIG_COLUMN_MAP = [
			sheet:'Sheet1', startRow: 2, columnMap: [ 'A':'category','B':'serenicCode','C':'product', 'D':'make',
				'E':'dosage', 'F':'unitOfMeasure', 'G':'model', 'H':'lotNumber', 'I':'expirationDate', 'J':'quantity', 'K':'comments' ]
		]
		
		Map CONFIG_PROPERTY_MAP = [
			category:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			serenicCode:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			product:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			make:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			dosage:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			unitOfMeasure:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			model:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			lotNumber:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			expirationDate:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			quantity:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			comments:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		]
	
		
		def importer = new InventoryExcelImporter(filename, CONFIG_COLUMN_MAP, CONFIG_CELL_MAP, CONFIG_PROPERTY_MAP);
		def inventoryMapList = importer.getInventoryItems();
		
		return inventoryMapList
	}
	
	public void importInventory(Warehouse warehouse, List inventoryMapList, Errors errors) { 
		
		def errorMessages = [];
		
		try { 
			
			def transactionInstance = new Transaction(transactionDate: new Date(),
				transactionType: TransactionType.findByName("Inventory"),
				inventory: warehouse.inventory,
				destination: warehouse)
			def dateFormat = new SimpleDateFormat("yyyy-MM");
			
			inventoryMapList.each { Map importParams ->
				//log.info "Inventory item " + importParams
				def quantity = importParams?.quantity?.intValue();
				def description =
					importParams?.make + " " + importParams?.product + ", " + importParams?.model + ", " + importParams?.dosage + " " + importParams.unitOfMeasure;
				
				def serenicCode = String.valueOf(importParams?.serenicCode?.intValue());
				if (importParams?.serenicCode?.class != String.class) {
					//errorMessages << "Column 'Serenic Code' with value '${serenicCode}' should be formatted as a text value";
					//errors.reject("Column 'Serenic Code' with value '${serenicCode}' should be formatted as a text value");
				}
				def lotNumber = String.valueOf(importParams.lotNumber);
				if (importParams?.lotNumber?.class != String.class) {
					//errorMessages << "Column 'Serial Number / Lot Number' with value '${lotNumber}' should be formatted as a text value";
					//errors.reject("Column 'Serial Number / Lot Number' with value '${lotNumber}' should be formatted as a text value");
				}
				def expirationDate = null;
				if (importParams.expirationDate) {
					try { 
						expirationDate = dateFormat.parse(new String(importParams?.expirationDate));
					} catch (ParseException e) { 
						errors.reject("Could not parse date " + importParams?.expirationDate + " " + e.getMessage());
					}
				}
			
				// Create category if not exists
				Category category = Category.findByName(importParams.category);
				if (!category) {
					category = new Category(name: importParams.category);
					if (!category.save()) { 
						//throw new RuntimeException("Error saving category " + category?.name);
						errors.reject("Error saving category " + category?.name)
					}
					log.info "Created new category " + category.name;
				}
			
				// Create product if not exists
				Product product = Product.findByName(importParams.product);
				if (!product) {
					product = new Product(name: importParams.product, category: category);
					if (!product.save()) { 
						errors.reject("Error saving product " + product?.name)
						//throw new RuntimeException("Error saving product " + product?.name)
					}
					log.info "Created new product " + product.name;
				}
											
				// Create inventory item if not exists
				InventoryItem inventoryItem = InventoryItem.findByProductAndLotNumber(product, importParams.lotNumber);
				if (!inventoryItem) {
					inventoryItem = new InventoryItem()
					inventoryItem.product = product
					inventoryItem.lotNumber = lotNumber;
					inventoryItem.description = description;
					inventoryItem.expirationDate = expirationDate;
					log.info "Creating new inventoryItem for product " + product?.name + " with description " + inventoryItem?.description + " and lot number " + inventoryItem?.lotNumber;
					if (inventoryItem.hasErrors() || !inventoryItem.save()) {
						inventoryItem.errors.allErrors.each {
							errors.addError(it);
							
							//errors.rejectValue("fieldname", "message.error.code", [message(code: 'eventType.label', default: 'EventType')] as Object[], "Another user has updated this EventType while you were editing")
						}
					}
				}
				// If there's a quantity, we create a transaction entry for it.  Otherwise
				// it's just added to the inventory items 
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
	
		
}
