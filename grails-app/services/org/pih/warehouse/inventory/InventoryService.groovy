package org.pih.warehouse.inventory;

import java.util.Map;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.grails.plugins.excelimport.ExcelImportUtils;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.Warehouse;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.ProductAttribute;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.core.Constants 
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType;
import org.springframework.validation.Errors;

class InventoryService {
	
	def sessionFactory
	def productService
	boolean transactional = true
	
	/**
	 * 
	 * @return
	 */
	List<Warehouse> getAllWarehouses() {
		return Warehouse.list()
	}
    
	/**
	 * Returns the Warehouse specified by the passed id parameter; 
	 * if no parameter is specified, returns a new warehouse instance
	 * 
	 * @param warehouseId
	 * @return
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
	
	/**
    * Returns the Location specified by the passed id parameter;
    * if no parameter is specified, returns a new location instance
    */
    Location getLocation(Long locationId) {
	   	if (locationId) {
	   		Location location = Location.get(locationId)
	   		if (!location) {
	   			throw new Exception("No location found with locationId ${locationId}")
	   		}
	   		else {
	   			return location
	   		}
	   	}
	   	// otherwise, we need to create a new, empty location
	   	else {
	   		Location location = new Location()
	   		return location
	   	}
    }
	
    /**
     * Saves the specified location
     */
	void saveLocation(Location location) {
		location.save(flush:true)
	}
    
	/**
	 * Gets all transactions associated with a warehouse
	 */
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
	 * @return a Sorted Map from product primary category to List of products
	 */
	Map getProductMap(Collection products) {
		Map m = new TreeMap();
		if (products) {
			products.each {
				Category c = it.category ? it.category : new Category(name: "Unclassified")
				List l = m.get(c)
				if (l == null) {
					l = new ArrayList();
					m.put(c, l);
				}
				l.add(it);
			}
		}
		return m
	}
	
	/*
	Map getInventoryMap(Collection inventoryProducts) { 
		// Sort inventory items by quantity, then by product name
		Map inventoryProductMap = new TreeMap();
		if (inventoryProducts) {
			inventoryProducts.each {
				Category category = it.category ? it.category : new Category(name: "Unclassified")
				List list = inventoryProductMap.get(category)
				if (list == null) {
					list = new ArrayList();
					inventoryProductMap.put(category, list);
				}
				list.add(it);
			}
		}
		return inventoryProductMap;
	}*/
			
	
	/**
	 * Search inventory items by term or product Id
	 * 	
	 * @param searchTerm
	 * @param productId
	 * @return
	 */
	List searchInventoryItems(String searchTerm, String productId) { 		
		searchTerm = "%" + searchTerm + "%";
		def items = InventoryItem.withCriteria {
			or {
				ilike("lotNumber", searchTerm)
				product { 
					ilike("name", searchTerm)
				}
			}
		}
		return items;
	}
	
	
	/**
	 * 
	 * @param commandInstance
	 * @return
	 */
	InventoryCommand browseInventory(InventoryCommand commandInstance) { 
		
		// add an inventory to this warehouse if it doesn't exist
		if (!commandInstance?.warehouseInstance?.inventory) {
			addInventory(commandInstance.warehouseInstance)
		}
		
		// Get all product types and set the default product type				
		commandInstance.rootCategory = productService.getRootCategory();
		commandInstance.inventoryInstance = commandInstance?.warehouseInstance?.inventory;
		
		// Get the selected category or use the root category
		commandInstance.categoryInstance = commandInstance?.categoryInstance ?: commandInstance?.rootCategory;
		
		// Get current inventory for the given products
		commandInstance.inventoryItems = getCurrentInventory(commandInstance);								
		
		return commandInstance;
	}

	/**
	 * 
	 * @param commandInstance
	 * @return
	 */
	Map getCurrentInventory(def commandInstance) { 
		
		def inventoryItems = [];
		
		// Only used to count products on page
		commandInstance.products = getProducts(commandInstance);
		
		// Get quantity for each item in inventory 
		def quantityMap = getQuantityByProductMap(commandInstance?.inventoryInstance);
		commandInstance?.products.each { product -> 
			def quantityOnHand = quantityMap[product] ?: 0;
			inventoryItems << new InventoryItemCommand(category: product.category, product: product, quantityOnHand: quantityOnHand)
		}
		
		// Get inventory and sort by product name 
		def inventoryProductMap = getProductMap(inventoryItems);
		for (item in inventoryProductMap) {
			item.value.sort { item1, item2 ->
				item1?.product?.name <=> item2?.product?.name
			}
		}
		return inventoryProductMap;
	}
		
	
	/*
	*
	* Get products based on the
	*/
   Set getProducts(def commandInstance) {
	   // Get any category filters that match search terms
	   def products = getProductsByAll(
		   commandInstance?.searchTermFilters, 
		   commandInstance?.categoryFilters, 
		   commandInstance?.showHiddenProducts);
	   
	   
	   products = products?.sort() { it?.name };
	   return products;
   }


   /**
	* @param searchTerms
	* @param categories
	* @return
	*/
   List getProductsByAll(List productFilters, List categoryFilters, Boolean showHiddenProducts) {
	   // Get products that match the search terms by name and category
	   def categories = getCategoriesMatchingSearchTerms(productFilters)

	   // Categories
	   def matchCategories = getExplodedCategories(categoryFilters);
	   log.info "matchCategories " + matchCategories
	   
	   
	   // Base product list
	   def session = sessionFactory.getCurrentSession()
	  
	   // Get all products, including hidden ones 
	   def products = []
	   if (showHiddenProducts) { 
		   products = Product.list();
	   }
	   // Get all products that are managed
	   else { 
		   def query = session.createQuery("select product from InventoryLevel as inventoryLevel right outer join inventoryLevel.product as product where inventoryLevel.supported is null or inventoryLevel.supported = true")
		   products = query.list()
	   }
	   
	   log.info "base products " + products.size();
	   if (matchCategories && productFilters) {
		   def searchProducts = Product.createCriteria().list() {
			   and {
				   productFilters.each {
					   ilike("name", "%" + it + "%")
				   }
				   'in'("category", matchCategories)
			   }
		   }
		   products = products.intersect(searchProducts);
	   }
	   else {
		   def searchProducts = Product.createCriteria().list() {
			   or {
				   if (productFilters) {
					   productFilters.each {
						   String[] filterTerms = it.split("\\s+");
						   and {
							   filterTerms.each {
								   ilike("name", "%" + it + "%")
							   }
						   }
					   }
				   }
				   if (matchCategories) {
					   'in'("category", matchCategories)
				   }
				   if (categories) {
					   'in'("category", categories)
				   }
			   }
		   }
		   products = products.intersect(searchProducts);
	   }
	   
	   return products;
   }

	/**
	 * Get a map of product attribute-value pairs for the given products.
	 * If products is empty, then we return all attribute-value pairs.
	 */
	Map getProductAttributes() { 
		def productAttributes = ProductAttribute.list()
		return productAttributes.groupBy { it.attribute } 
	}
	
	
	
	/**
	 * Get quantity for the given product and lot number at the given warehouse.
	 * 
	 * @param warehouse
	 * @param product
	 * @param lotNumber
	 * @return
	 */
	Integer getQuantity(Warehouse warehouse, Product product, String lotNumber) {
		log.info ("Get quantity for product " + product?.name + " lotNumber " + lotNumber + " at location " + warehouse?.name)
		if (!warehouse) {
			throw new RuntimeException("Your warehouse has not been initialized");
		}
		else {
			warehouse = Warehouse.get(warehouse?.id)
		}
		def inventoryItem = findInventoryItemByProductAndLotNumber(product, lotNumber)
		if (!inventoryItem) {
			throw new RuntimeException("There's no inventory item for product " + product?.name + " lot number " + lotNumber)
		}
		
		return getQuantityForInventoryItem(inventoryItem, warehouse.inventory)
	}
	
	/**
	 * Converts a list of passed transaction entries into a quantity
	 * map indexed by product and then by inventory item
	 * 
	 * Note that the transaction entries should all be from the same inventory,
	 * or the quantity results would be somewhat nonsensical
	 * 
	 * TODO: add a parameter here to optionally take in a product, which means that we are only
	 * calculation for a single product, which means that we can stop after we hit a product inventory transaction?
	 */
	Map<Product,Map<InventoryItem,Integer>> getQuantityByProductAndInventoryItemMap(List<TransactionEntry> entries) {
		def quantityMap = [:]
		def reachedInventoryTransaction = [:]   // used to keep track of which items we've found an inventory transaction for
		def reachedProductInventoryTransaction = [:]  // used to keep track of which items we've found a product inventory transaction for
		
		// first make sure the transaction entries are sorted, with most recent first
		entries = entries.sort().reverse()
		
		entries.each {
			def item = it.inventoryItem
			def transaction = it.transaction
			
			// first see if this is an entry we can skip (because we've already reach a product inventory transaction
			// for this product, or a inventory transaction for this inventory item)
			if ( !(reachedProductInventoryTransaction[item.product] && reachedProductInventoryTransaction[item.product] != transaction) &&
			      !(reachedInventoryTransaction[item.product] && reachedInventoryTransaction[item.product][item] && reachedInventoryTransaction[item.product][item] != transaction) ) {
				
				// check to see if there's an entry in the map for this product and create if needed
				if (!quantityMap[item.product]) {
					quantityMap[item.product] = [:]
				}
				
				// check to see if there's an entry for this inventory item in the map and create if needed
				if (!quantityMap[item.product][item]) {
					quantityMap[item.product][item] = 0
				}
				
				// now update quantity as necessary
				def code = it.transaction.transactionType.transactionCode
				
				if (code == TransactionCode.CREDIT) { 
					quantityMap[item.product][item] += it.quantity 
				}
				if (code == TransactionCode.DEBIT) { 
					quantityMap[item.product][item] -= it.quantity  
				}
				if (code == TransactionCode.INVENTORY) {
					quantityMap[item.product][item] += it.quantity
					
					// mark that we are done with this inventory item (after this transaction)
					if (!reachedInventoryTransaction[item.product]) {
						reachedInventoryTransaction[item.product] = [:]
					}
					reachedInventoryTransaction[item.product][item] = transaction
				}
				if (code == TransactionCode.PRODUCT_INVENTORY) {
					quantityMap[item.product][item] += it.quantity
					
					// mark that we are done with this product (after this transaction)
					reachedProductInventoryTransaction[item.product] = transaction
				}
			}
		}
		
		return quantityMap
	}
	
	
	/**
	 * Converts a list of passed transaction entries into a quantity
	 * map indexed by product
	 * 
	 * Note that the transaction entries should all be from the same inventory,
	 * or the quantity results would be somewhat nonsensical
	 */
	Map<Product,Integer> getQuantityByProductMap(List<TransactionEntry> entries) {
		def quantityMap = [:]
		
		// first get the quantity and inventory item map
		def quantityMapByProductAndInventoryItem = getQuantityByProductAndInventoryItemMap(entries)
		
		// now collapse this down to be by product
		quantityMapByProductAndInventoryItem.keySet().each {
			def product = it
			quantityMap[product] = 0
			quantityMapByProductAndInventoryItem[product].values().each {
				quantityMap[product] += it
			}
		}                 	
		return quantityMap
	}
	
	/**
	 * Converts  list of passed transactions entries into a quantity 
	 * map indexed by inventory item
	 */
	Map<InventoryItem, Integer> getQuantityByInventoryItemMap(List<TransactionEntry> entries) {
		def quantityMap = [:]
		                   
		// first get the quantity and inventory item map
		def quantityByProductAndInventoryItemMap = getQuantityByProductAndInventoryItemMap(entries)
		
		// now collapse this down to be by product
		quantityByProductAndInventoryItemMap.keySet().each {
			def product = it
			quantityByProductAndInventoryItemMap[product].keySet().each {
				quantityMap[it] = quantityByProductAndInventoryItemMap[product][it]
			}
		}
		return quantityMap
	}
	
	/** 
	 * Get a map of quantities (indexed by product) for a particular inventory.
	 * 
	 * TODO This might perform poorly as we add more and more transaction entries 
	 * into an inventory.
	 */
	Map<Product,Integer>  getQuantityByProductMap(Inventory inventoryInstance) {                   
		def transactionEntries = TransactionEntry.createCriteria().list { 
			transaction { 
				eq("inventory.id", inventoryInstance?.id)
			}	
		}
		return getQuantityByProductMap(transactionEntries)		
	}
		
	/**
	 * Gets the quantity of a specific inventory item at a specific inventory
	 */
	Integer getQuantityForInventoryItem(InventoryItem item, Inventory inventory) {
		def transactionEntries = getTransactionEntriesByInventoryItemAndInventory(item, inventory)
		def quantity = getQuantityByInventoryItemMap(transactionEntries)[item]
		return quantity ? quantity : 0;
	}
	
	Integer getQuantityForProduct(Product product) { 
		return 0;
	}
	
	
	Map<InventoryItem, Integer> getQuantityForInventory(Inventory inventory) { 
		def transactionEntries = getTransactionEntriesByInventory(inventory);
		return getQuantityByInventoryItemMap(transactionEntries);
	}
	
	
	/**
	 * Fetches and populates a StockCard Command object
	 */
	StockCardCommand getStockCardCommand(StockCardCommand cmd, Map params) {
		log.debug "Params " + params
		
		// Get basic details required for the whole page
		cmd.productInstance = Product.get(params?.product?.id?:params.id);  // check product.id and id
		cmd.inventoryInstance = cmd.warehouseInstance?.inventory
		cmd.inventoryLevelInstance = getInventoryLevelByProductAndInventory(cmd.productInstance, cmd.inventoryInstance)
	
		// Get current stock of a particular product within an inventory
		// Using set to make sure we only return one object per inventory items
		Set inventoryItems = getInventoryItemsByProductAndInventory(cmd.productInstance, cmd.inventoryInstance);
		cmd.inventoryItemList = inventoryItems as List
		
		cmd.inventoryItemList.sort { it.lotNumber }
		
		// Get transaction log for a particular product within an inventory
		cmd.transactionEntryList = getTransactionEntriesByProductAndInventory(cmd.productInstance, cmd.inventoryInstance);
		cmd.transactionEntriesByInventoryItemMap = cmd.transactionEntryList.groupBy { it.inventoryItem }
		cmd.transactionEntriesByTransactionMap = cmd.transactionEntryList.groupBy { it.transaction }
		
		// create the quantity map for this product
		cmd.quantityByInventoryItemMap = getQuantityByInventoryItemMap(cmd.transactionEntryList)
		
		return cmd
	}
	
	
	
	/**
	 * Fetches and populates a RecordInventory Command object
	 */
	RecordInventoryCommand getRecordInventoryCommand(RecordInventoryCommand commandInstance, Map params) { 		
		log.debug "Params " + params;
		
		// set the default transaction date to today
		commandInstance.transactionDate = new Date()
		commandInstance.transactionDate.clearTime()
		
		if (!commandInstance?.product) { 
			commandInstance.errors.reject("error.product.invalid","Product does not exist");
		}
		else { 			
			commandInstance.recordInventoryRow = new RecordInventoryRowCommand();
			
			// get all transaction entries for this product at this inventory
			def transactionEntryList = getTransactionEntriesByProductAndInventory(commandInstance?.product, commandInstance?.inventory)
			// create a map of inventory item quantities from this
			def quantityByInventoryItemMap = getQuantityByInventoryItemMap(transactionEntryList)
			                                                                                       		
			quantityByInventoryItemMap.keySet().each { 
				def quantity = quantityByInventoryItemMap[it]
				
				def inventoryItemRow = new RecordInventoryRowCommand()
				inventoryItemRow.id = it.id
				inventoryItemRow.lotNumber = it.lotNumber
				inventoryItemRow.expirationDate = it.expirationDate
				//inventoryItemRow.description = it.description;
				inventoryItemRow.oldQuantity = quantity
				inventoryItemRow.newQuantity = quantity
				commandInstance.recordInventoryRows.add(inventoryItemRow)
			}
		}
		return commandInstance
	}
		
	
	/**
	 * Processes a RecordInventory Command object and perform updates
	 */
	RecordInventoryCommand saveRecordInventoryCommand(RecordInventoryCommand cmd, Map params) { 
		log.info "Saving record inventory command params: " + params
		
		try { 
			// Validation was done during bind, but let's do this just in case
			if (cmd.validate()) { 
				def inventoryItems = getInventoryItemsByProductAndInventory(cmd.product, cmd.inventory)
				// Create a new transaction
				def transaction = new Transaction(cmd.properties)
				
				transaction.transactionType = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)
				
				// Process each row added to the record inventory page
				cmd.recordInventoryRows.each { row -> 					
					// 1. Find an existing inventory item for the given lot number and product and description
					def inventoryItem = 
						findInventoryItemByProductAndLotNumber(cmd.product, row.lotNumber)
					
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
					// 3. Create a new transaction entry (even if quantity didn't change)	
					def transactionEntry = new TransactionEntry();
					transactionEntry.properties = row.properties;
					transactionEntry.quantity = row.newQuantity
					transactionEntry.inventoryItem = inventoryItem;
					transaction.addToTransactionEntries(transactionEntry);						
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
	
		
	/**
	 * Get products by search terms only, matching against product name OR category name.
	 * @param searchTerms
	 * @return
	 */
	List getProductsBySearchTerms(List productFilters) { 
		log.info "get products by search terms " + productFilters;
		
		// Get products that match the search terms by name and category
		def products = Product.createCriteria().list() { 
			if (productFilters) {
				or { 
					productFilters.each { 
						ilike("name", "%" + it + "%")				
					}	
					productFilters.each {
						category {
							ilike("name", "%" + it + "%")
						}
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
			categories.each { category ->
				if (category) {
					matchCategories << category;
					matchCategories.addAll( (category?.children)?getExplodedCategories(category?.children):[]);
				}
			}
		}
		return matchCategories;
	}
	

	/**
	 * 
	 * @param searchTerms
	 * @return
	 */
	List getCategoriesMatchingSearchTerms(List searchTerms) { 
		def categories = []
		if (searchTerms) { 
			categories = Category.createCriteria().list() { 
				searchTerms.each { 
					ilike("name", it)
				}
			}
		}
		return getExplodedCategories(categories);		
	}
	
	/**
	 * Returns a list of products by category.  
	 * 	
	 * @param categories
	 * @param params
	 * @return
	 */
	List getProductsByCategories(List categories) { 
		def products = []
		
		def matchCategories = getExplodedCategories(categories);
		
		log.debug matchCategories
		if (matchCategories) { 
			products = Product.createCriteria().list() {
				'in'("category", matchCategories)
			}
		}
		return products;	 
	}
	
	List getProductsByCategory(Category category) { 
		def products = [];
		if (category) { 
			def categories = (category?.children)?category.children:[];
			categories << category;
			if (categories) {
				products = Product.createCriteria().list() {
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
		log.info ("Find inventory item by product " + product?.id + " and lot number '" + lotNumber + "'" )
		def inventoryItems = InventoryItem.createCriteria().list() {
			and {
				eq("product.id", product?.id)
				if (lotNumber) { 
					log.info "lot number is not null"
					eq("lotNumber", lotNumber)
				}
				else {  
					or { 
						log.info "lot number is null"
						isNull("lotNumber")
						eq("lotNumber", "")
					}
				}
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
				inventoryItem {
					eq("product.id", productInstance?.id)
				}
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
	
   /**
    * Gets all transaction entries for a inventory item within an inventory
    */
   List getTransactionEntriesByInventoryItemAndInventory(InventoryItem item, Inventory inventoryInstance) {
	   return TransactionEntry.createCriteria().list() {
		   and {
			   eq("inventoryItem", item)
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
			def quantity = 0;
			try { 
				quantity = Integer.valueOf(params?.newQuantity);				
			} catch (Exception e) { 
				itemInstance.errors.reject("inventoryItem.quantity.invalid")			
			}
			
			def transactionInstance = new Transaction(params);
			if (transactionEntry.hasErrors() || transactionInstance.hasErrors()) {
				itemInstance.errors = transactionEntry.errors
				itemInstance.errors = transactionInstance.errors
			}
			
			if (!itemInstance.hasErrors() && itemInstance.save()) {			
				// Need to create a transaction if we want the inventory item
				// to show up in the stock card
				transactionInstance.transactionDate = new Date();	
				transactionInstance.transactionDate.clearTime();
				transactionInstance.transactionType = TransactionType.get(Constants.INVENTORY_TRANSACTION_TYPE_ID);
				transactionInstance.inventory = inventoryInstance;			
				
				// Add transaction entry to transaction
				transactionEntry.inventoryItem = itemInstance;
				transactionEntry.quantity = quantity
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
			debitTransaction.transactionDate = shipmentInstance.getActualShippingDate()
		
			shipmentInstance.shipmentItems.each {
				def inventoryItem = 
					findInventoryItemByProductAndLotNumber(it.product, it.lotNumber)
				
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
				transactionEntry.quantity = it.quantity;
				transactionEntry.inventoryItem = inventoryItem;
				debitTransaction.addToTransactionEntries(transactionEntry);
			}
		
			if (!debitTransaction.save()) {
				throw new RuntimeException("Failed to save 'Send Shipment' transaction");
			}
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
		
		Date today = new Date()
		today.clearTime()
		def transactionInstance = new Transaction(transactionDate: today,
			transactionType: TransactionType.findByName("Inventory"),
			inventory: warehouse.inventory)
		
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
				errors.reject("Property [quantity] with value '${quantity} for '${lotNumber}' should be formatted as a Double value");
			}
			else if (importParams?.quantity instanceof String) {
				errors.reject("Property [quantity] with value '${quantity} for '${lotNumber}' should not be formatted as a Text value");
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
			
			Date today = new Date()
			today.clearTime()
			def transactionInstance = new Transaction(transactionDate: today,
				transactionType: TransactionType.findByName("Inventory"),
				inventory: warehouse.inventory)
			
			
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
		if (transaction?.transactionType?.id != Constants.TRANSFER_IN_TRANSACTION_TYPE_ID  &&
				transaction?.transactionType?.id != Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
			return false
		}
	
		// make sure we are operating only on locally managed warehouses
		if (transaction?.source) {
			if (!(transaction?.source instanceof Warehouse)) { return false }
			else if (!transaction?.source.local){ return false }
		}
		if (transaction?.destination) {
			if (!(transaction?.destination instanceof Warehouse)) { return false }
			else if (!transaction?.destination.local) { return false }
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
			def inventoryItem = 
				findInventoryItemByProductAndLotNumber(it.product, it.lotNumber)
			
			// If the inventory item doesn't exist, we create a new one
			if (!inventoryItem) {
				inventoryItem = new InventoryItem(lotNumber: it.lotNumber, product:it.product)
				if (inventoryItem.hasErrors() && !inventoryItem.save()) {
					throw new RuntimeException("Unable to create inventory item $inventoryItem while creating local transfer")
				}
			}
			
			// Create a new transaction entry
			def transactionEntry = new TransactionEntry();
			transactionEntry.quantity = it.quantity;
			transactionEntry.inventoryItem = inventoryItem;
			mirroredTransaction.addToTransactionEntries(transactionEntry);
		}	
		
		return mirroredTransaction
	}
}
