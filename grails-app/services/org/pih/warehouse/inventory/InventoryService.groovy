package org.pih.warehouse.inventory;

import java.util.Map;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.pih.warehouse.importer.ImportDataCommand;
import org.pih.warehouse.importer.ImporterUtil;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.ProductAttribute;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.core.Constants 
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.validation.Errors;

import org.pih.warehouse.reporting.Consumption;

class InventoryService implements ApplicationContextAware {
	
	def sessionFactory
	def productService
	
	ApplicationContext applicationContext
	
	boolean transactional = true

	/**
	 * @return shipment service
	 */
	def getShipmentService() {
		return applicationContext.getBean("shipmentService")
	}

	/**
	 * @return request service
	 */
	def getRequestService() {
		return applicationContext.getBean("requestService")
	}

	/**
	 * @return order service
	 */
	def getOrderService() {
		return applicationContext.getBean("orderService")
	}

	/**
	 * @return	localization service
	 */
	def getLocalizationService() {
		return applicationContext.getBean("localizationService")
	}
	
	
	

	/**
	 * Gets all locations.
	 * 
	 * @return
	 */
	List<Location> getAllLocations() {
		return Location.list()
	}
    
    /**
     * Saves the specified warehouse
	 * 
	 * @param warehouse
	 */
	void saveLocation(Location warehouse) {
		log.debug ("saving warehouse " + warehouse)
		log.debug ("location type " + warehouse.locationType)
		// make sure a warehouse has an inventory
		if (!warehouse.inventory) {
			addInventory(warehouse)
		}
		log.debug warehouse.locationType
		
		warehouse.save(failOnError: true)
	}
	
	/**
	 * Returns the Location specified by the passed id parameter;
	 * if no parameter is specified, returns a new location instance
	 * 
	 * @param locationId
	 * @return
	 */
    Location getLocation(String locationId) {
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
	 * 
	 * @param location
	 */
	//void saveLocation(Location location) {
	//	location.save(flush:true)
	//}
    
	/**
	 * Gets all transactions associated with a warehouse
	 * 
	 * @param warehouse
	 * @return
	 */
	List<Transaction> getAllTransactions(Location warehouse) {
		return Transaction.withCriteria { eq("thisLocation", warehouse) }
	}

	/**
	 * Get all consumption transactions between the given dates.	
	 * 
	 * @return	a list of consumption transactions 
	 */
	List getConsumptionTransactions(Date startDate, Date endDate) { 
		def CONSUMPTION_TYPE = TransactionType.get(Constants.CONSUMPTION_TRANSACTION_TYPE_ID);
		return Transaction.findAllByTransactionTypeAndTransactionDateBetween(CONSUMPTION_TYPE, startDate, endDate)
	}
	
	
		
	/**
	 * Gets the inventory associated with this warehouse;
	 * if no inventory, create a new inventory
	 * 
	 * @param warehouse
	 * @return
	 */
	Inventory getInventory(Location warehouse) {
		Inventory inventory = Inventory.withCriteria { eq("warehouse", warehouse) }
		return  inventory ?: addInventory(warehouse)
	}
	
	/**
	 * Adds an inventory to the specified warehouse
	 * 
	 * @param warehouse
	 * @return
	 */
	Inventory addInventory(Location warehouse) {
		if (!warehouse) {
			throw new RuntimeException("No warehouse specified.")
		}
		if (warehouse.inventory) {
			throw new RuntimeException("An inventory is already associated with this warehouse.")
		}
		
		warehouse.inventory = new Inventory([ 'warehouse' : warehouse ])
		saveLocation(warehouse)
		
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
			for (entry in m) {
				entry.value.sort { item1, item2 ->
					item1?.product?.name <=> item2?.product?.name
				}
			}
		}
		return m
	}
	
	/**
	 * Search inventory items by term or product Id
	 * 	
	 * @param searchTerm
	 * @param productId
	 * @return
	 */
	List findInventoryItems(String searchTerm, String productId) { 		
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
		
		def rootCategory = productService?.getRootCategory();
		
		// Get the selected category or use the root category
		commandInstance.categoryInstance = commandInstance?.categoryInstance ?: productService.getRootCategory();
		
		// Get current inventory for the given products
		if (commandInstance?.categoryInstance != rootCategory || commandInstance?.searchPerformed) {  
			getCurrentInventory(commandInstance);								
		}
		else { 
			commandInstance?.categoryToProductMap = [:]
		}
		
		return commandInstance;
	}
	
	
	/**
	 * 
	 * @param commandInstance
	 * @return
	 */
	Map getCurrentInventory(def commandInstance) { 
		
		// Get quantity for each item in inventory TODO: Should only be doing this for the selected products for speed
		def quantityOnHandMap = getQuantityByProductMap(commandInstance?.warehouseInstance?.inventory);
		def quantityOutgoingMap = getOutgoingQuantityByProduct(commandInstance?.warehouseInstance);
		def quantityIncomingMap = getIncomingQuantityByProduct(commandInstance?.warehouseInstance);
		
		def products = [];
		
		getProducts(commandInstance).each { product -> 
			def quantityOnHand = quantityOnHandMap[product] ?: 0;
			def quantityToReceive = quantityIncomingMap[product] ?: 0;
			def quantityToShip = quantityOutgoingMap[product] ?: 0;
			
			def inventoryLevel = InventoryLevel.findByProductAndInventory(product, commandInstance?.warehouseInstance?.inventory)
			//log.debug "inventory level " + product?.name + ": " + inventoryLevel?.status
			
			if (commandInstance?.showOutOfStockProducts || (quantityOnHand + quantityToReceive + quantityToShip > 0)) {
				products << new ProductCommand(
					category: product.category, 
					product: product, 
					inventoryLevel: InventoryLevel.findByProductAndInventory(product, commandInstance?.warehouseInstance?.inventory),
					quantityOnHand: quantityOnHand, 
					quantityToReceive: quantityToReceive, 
					quantityToShip: quantityToShip 
				);
			}
		}
		
		commandInstance?.categoryToProductMap = getProductMap(products);

		return commandInstance?.categoryToProductMap
	}
		
	/**
	 * Get the outgoing quantity for all products at the given location.
	 * 
	 * @param location
	 * @return
	 */
	Map getOutgoingQuantityByProduct(Location location) { 
		Map quantityByProduct = [:]
		Map quantityShippedByProduct = getShipmentService().getOutgoingQuantityByProduct(location);		
		Map quantityOrderedByProduct = getOrderService().getOutgoingQuantityByProduct(location)
		Map quantityRequestedByProduct = getRequestService().getOutgoingQuantityByProduct(location)
		quantityShippedByProduct.each { product, quantity ->
			def productQuantity = quantityByProduct[product];
			if (!productQuantity) productQuantity = 0;			
			productQuantity += quantity?:0;
			quantityByProduct[product] = productQuantity;
		}
		quantityOrderedByProduct.each { product, quantity ->
			def productQuantity = quantityByProduct[product];
			if (!productQuantity) productQuantity = 0;			
			productQuantity += quantity?:0;
			quantityByProduct[product] = productQuantity;
		}
		quantityRequestedByProduct.each { product, quantity ->
			def productQuantity = quantityByProduct[product];
			if (!productQuantity) productQuantity = 0;			
			productQuantity += quantity?:0;
			quantityByProduct[product] = productQuantity;
		}
		return quantityByProduct;
	}

	/**
	 * Get the incoming quantity for all products at the given location.	
	 * @param location
	 * @return
	 */
	Map getIncomingQuantityByProduct(Location location) { 
		Map quantityByProduct = [:]
		Map quantityShippedByProduct = getShipmentService().getIncomingQuantityByProduct(location);		
		Map quantityOrderedByProduct = getOrderService().getIncomingQuantityByProduct(location)
		Map quantityRequestedByProduct = getRequestService().getIncomingQuantityByProduct(location)
		quantityShippedByProduct.each { product, quantity ->
			def productQuantity = quantityByProduct[product];
			if (!productQuantity) productQuantity = 0;			
			productQuantity += quantity?:0;
			quantityByProduct[product] = productQuantity;
		}
		quantityOrderedByProduct.each { product, quantity ->
			def productQuantity = quantityByProduct[product];
			if (!productQuantity) productQuantity = 0;			
			productQuantity += quantity?:0;
			quantityByProduct[product] = productQuantity;
		}
		quantityRequestedByProduct.each { product, quantity ->
			def productQuantity = quantityByProduct[product];
			if (!productQuantity) productQuantity = 0;			
			productQuantity += quantity?:0;
			quantityByProduct[product] = productQuantity;
		}
		return quantityByProduct;
	}
	
	
	/**
	 * Get a map of quantity for each inventory item for the given inventory and product.
	 * 
	 * @param inventory
	 * @param product
	 * @return
	 */
	Map getQuantityByInventoryAndProduct(Inventory inventory, Product product) { 
		Map inventoryItemQuantity = [:]
		Set inventoryItems = getInventoryItemsByProductAndInventory(product, inventory);
		Map<InventoryItem, Integer> quantityMap = getQuantityForInventory(inventory)
		inventoryItems.each { 
			inventoryItemQuantity[it] = quantityMap[it]
		}
		return inventoryItemQuantity;
	}
		
	/**
	 * 
	 * @param category
	 * @param location
	 * @return
	 */
	List getExpiredStock(Category category, Location location) { 
		def today = new Date();
		
		// Stock that has already expired
		def expiredStock = InventoryItem.findAllByExpirationDateLessThan(today, [sort: 'expirationDate', order: 'desc']);

		log.debug expiredStock
		
		def quantityMap = getQuantityForInventory(location.inventory)		
		expiredStock = expiredStock.findAll { quantityMap[it] > 0 }
		
		// Get the set of categories BEFORE we filter
		//def categories = [] as Set		
		//categories.addAll(expiredStock.collect { it.product.category })
		//categories = categories.findAll { it != null }

		// poor man's filter
		if (category) {
			expiredStock = expiredStock.findAll { item -> item?.product?.category == category }
		}
		return expiredStock
		
	}
	
	/**
	 * Get all inventory items that are expiring within the given threshhold.
	 * 
	 * @param category	the category filter
	 * @param threshhold the threshhold filter
	 * @return a list of inventory items
	 */
	List getExpiringStock(Category category, Location location, Integer threshhold) { 
		def today = new Date();
		
		// Get all stock expiring ever (we'll filter later)
		def expiringStock = InventoryItem.findAllByExpirationDateGreaterThan(today+1, [sort: 'expirationDate', order: 'asc']);
		
		
		def quantityMap = getQuantityForInventory(location.inventory)
		expiringStock = expiringStock.findAll { quantityMap[it] > 0 }

		if (category) {
			expiringStock = expiringStock.findAll { item -> item?.product?.category == category }
		}
		
		if (threshhold) {
			expiringStock = expiringStock.findAll { item -> (item?.expirationDate && (item?.expirationDate - today) <= threshhold) }
		}
		
		return expiringStock
	}
	
	
	
	
	
	/**
	 * Get a set of products based on the filters in the given command object.
	 * 
	 * @param commandInstance
	 * @return
	 */
	Set getProducts(def commandInstance) {
		List categoryFilters = new ArrayList();
		if (commandInstance?.subcategoryInstance) {
			categoryFilters.add(commandInstance?.subcategoryInstance);
		}
		else {
			categoryFilters.add(commandInstance?.categoryInstance);
		}
		List searchTerms = (commandInstance?.searchTerms ? Arrays.asList(commandInstance?.searchTerms.split(" ")) : null);
		
		log.debug("get products: " + commandInstance?.warehouseInstance)
		def products = getProductsByAll(
			commandInstance?.warehouseInstance,
			searchTerms,
			categoryFilters,
			commandInstance?.showUnsupportedProducts, 
			commandInstance?.showNonInventoryProducts);


		products = products?.sort() { it?.name };
		return products;
	}


   /**
	* @param searchTerms
	* @param categories
	* @return
	*/
   List getProductsByAll(Location location, List productFilters, List categoryFilters, Boolean showUnsupportedProducts, Boolean showNonInventoryProducts) {
	   // Get products that match the search terms by name and category
	   def categories = getCategoriesMatchingSearchTerms(productFilters)

	   // Categories
	   def matchCategories = getExplodedCategories(categoryFilters);
	   log.debug "matchCategories " + matchCategories
	   
	   
	   // Base product list
	   def session = sessionFactory.getCurrentSession()
	  
	   // Get all products, including hidden ones 
	   def products = Product.list()
	   //def unsupportedProducts = session.createQuery("select product from InventoryLevel as inventoryLevel right outer join inventoryLevel.product as product where (inventoryLevel.status is null or ((inventoryLevel.status = 'SUPPORTED' or inventoryLevel.status = 'NOT_SUPPORTED' or inventoryLevel.status = 'SUPPORTED_NON_INVENTORY') and inventoryLevel.inventory.id = :inventoryId").setParameter("inventoryId", location?.inventory?.id)
	   //def nonInventoryProducts = 
	   //def supportedProducts = 
	   // Start with products that do not have a status
	   //products.addAll(getSupportedProducts(location))
	   
	   log.debug("show unsupported products " + showUnsupportedProducts)
	   
	   if (!showUnsupportedProducts) { 
		   def statuses = []
		   statuses << InventoryStatus.NOT_SUPPORTED
		   def removeProducts = getProductsByStatuses(location, statuses)
		   log.debug "remove " + removeProducts.size() + " unsupported products"
		   products.removeAll(removeProducts)
		   
	   }
	   
	   log.debug("show non inventory products " + showNonInventoryProducts)
	   if (!showNonInventoryProducts) { 
		   def statuses = []
		   statuses << InventoryStatus.SUPPORTED_NON_INVENTORY
		   def removeProducts = getProductsByStatuses(location, statuses)
		   log.debug "remove " + removeProducts.size() + " non-inventories products"
		   products.removeAll(removeProducts)
	   }
	   
	   log.debug "base products " + products.size();
	   if (matchCategories && productFilters) {
		   def searchProducts = Product.createCriteria().list() {
			   and {
				   or {
					   and {
						   productFilters.each {
							   ilike("name", "%" + it + "%")
						   }
					   }
					   and {
						   productFilters.each {
							   ilike("manufacturer", "%" + it + "%")
						   }
					   }
					   and {
						   productFilters.each {
							   ilike("productCode", "%" + it + "%")
						   }
					   }
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
						   or {
							   and {
								   filterTerms.each {
									   ilike("name", "%" + it + "%")
								   }
							   }
							   and {
								   filterTerms.each {
									   ilike("manufacturer", "%" + it + "%")
								   }
							   }
							   and {
								   filterTerms.each {
									   ilike("productCode", "%" + it + "%")
								   }
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
	   
		// now localize to only match products for the current locale
		// TODO: this would also have to handle the category filtering
		//  products = products.findAll { product ->
		//  def localizedProductName = getLocalizationService().getLocalizedString(product.name);  // TODO: obviously, this would have to use the actual locale
		// return productFilters.any {
		//   localizedProductName.contains(it)  // TODO: this would also have to be case insensitive
		// }
		// }
	   
	   return products;
   }
   
   
   /**
    * 
    * @param location
    * @return
    */
   List getSupportedProducts(Location location) { 
	   def products = []
	   products.addAll(getProductsWithoutInventoryLevel(location))
	   log.debug "Add all without inventory level " + products.size()
	   products.addAll(getProductsByStatus(location, InventoryStatus.SUPPORTED))
	   log.debug "add all with status == supported " + products.size()
	   return products
   }
   
   
   List getProductsWithoutInventoryLevel(Location location) { 
	   def session = sessionFactory.getCurrentSession()
	   def query = session.createSQLQuery(
		   "select * from product left outer join\
		   (select * from inventory_level where inventory_level.inventory_id = :inventoryId) as i\
		   on product.id = i.product_id").addEntity(Product.class);
	   def products = query.setString("inventoryId", location.inventory.id).list();	   
	   
	   return products
   }
   
   List getProductsByStatuses(Location location, List statuses) { 
	   log.debug("get products by statuses: " + location)
	   def session = sessionFactory.getCurrentSession()
	   def products = session.createQuery("select product from InventoryLevel as inventoryLevel \
		   right outer join inventoryLevel.product as product \
           where inventoryLevel.status IN (:statuses) \
		   and inventoryLevel.inventory.id = :inventoryId")
		.setParameterList("statuses", statuses)
		.setParameter("inventoryId", location?.inventory?.id)
		.list()
   		return products
   }
   
   
   List getProductsByStatus(Location location, InventoryStatus status) { 
	   log.debug("get products by status: " + location)
	   def session = sessionFactory.getCurrentSession()
	   return session.createQuery("select product from InventoryLevel as inventoryLevel \
	   		right outer join inventoryLevel.product as product \
	   		where inventoryLevel.status = :status \
	   		and inventoryLevel.inventory.id = :inventoryId")
	   .setParameter("status", status)
	   .setParameter("inventoryId", location?.inventory?.id)
	   .list()
   }

	/**
	 * Get a map of product attribute-value pairs for the given products.
	 * If products is empty, then we return all attribute-value pairs.
	 * 
	 * @return
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
	Integer getQuantity(Location warehouse, Product product, String lotNumber) {
		log.debug ("Get quantity for product " + product?.name + " lotNumber " + lotNumber + " at location " + warehouse?.name)
		if (!warehouse) {
			throw new RuntimeException("Your warehouse has not been initialized");
		}
		else {
			warehouse = Location.get(warehouse?.id)
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
	 * 
	 * @param entries
	 * @return
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
	 * 
	 * @param entries
	 * @return
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
	 * 
	 * @param entries
	 * @return
	 */
	Map getQuantityByInventoryItemMap(List<TransactionEntry> entries) {
		def quantityMap = [:]
		                   
		// first get the quantity and inventory item map
		def quantityByProductAndInventoryItemMap = 
			getQuantityByProductAndInventoryItemMap(entries)
		
		// now collapse this down to be by product
		quantityByProductAndInventoryItemMap.keySet().each { product ->
			quantityByProductAndInventoryItemMap[product].keySet().each { inventoryItem ->
				if (!quantityMap[inventoryItem]) { 
					quantityMap[inventoryItem] = 0;
				}
				quantityMap[inventoryItem] += quantityByProductAndInventoryItemMap[product][inventoryItem]
			}
		}
		return quantityMap
	}
	
	/** 
	 * Get a map of quantities (indexed by product) for a particular inventory.
	 * 
	 * TODO This might perform poorly as we add more and more transaction entries 
	 * into an inventory.
	 * 
	 * @param inventoryInstance
	 * @return
	 */
	Map<Product,Integer>  getQuantityByProductMap(Inventory inventory) {                   
		def transactionEntries = getTransactionEntriesByInventory(inventory);
		return getQuantityByProductMap(transactionEntries)		
	}
	
		
	/**
	* Gets a product-to-quantity maps for all products in the selected inventory
	* whose quantity falls below a the minimum or reorder level
	* (Note that items that are below the minimum level are excluded from
	* the list of items below the reorder level)
	* 
	* Set the includeUnsupported boolean to include unsupported items when compiling the product-to-quantity maps 
	*/
   Map<String,Map<Product,Integer>> getProductsBelowMinimumAndReorderQuantities(Inventory inventoryInstance, Boolean includeUnsupported) {
	   
	   def inventoryLevels = getInventoryLevelsByInventory(inventoryInstance)
		   
	   Map<Product,Integer> reorderProductsQuantityMap = new HashMap<Product,Integer>()
	   Map<Product,Integer> minimumProductsQuantityMap = new HashMap<Product,Integer>()
	   
	   for (level in inventoryLevels) {
		   // getQuantityByInventory returns an Inventory Item to Quantity map, so we want to sum all the values in this map to get the total quantity
		   def quantity = getQuantityByInventoryAndProduct(inventoryInstance, level.product)?.values().sum { it } ?: 0
		   
		   // The product is supported or the user asks for unsupported products to be included  
		   if (level.status == InventoryStatus.SUPPORTED || includeUnsupported) {
			   if (quantity <= level.minQuantity) {
				   minimumProductsQuantityMap[level.product] = quantity
			   }
			   else if (quantity <= level.reorderQuantity) {
				   reorderProductsQuantityMap[level.product] = quantity
			   }
		   }
		   
	   }
	  
	   return [minimumProductsQuantityMap: minimumProductsQuantityMap, reorderProductsQuantityMap: reorderProductsQuantityMap]
   } 
   
  /**
	* Gets a product-to-quantity maps for all products in the selected inventory
	* whose quantity falls below a the minimum or reorder level
	* (Note that items that are below the minimum level are excluded from
	* the list of items below the reorder level)
	* 
	* Excludes unsupported items
	*/
   Map<String,Map<Product,Integer>> getProductsBelowMinimumAndReorderQuantities(Inventory inventoryInstance) {
		return getProductsBelowMinimumAndReorderQuantities(inventoryInstance, false)   
   }

  
	/**
	 * Gets the quantity of a specific inventory item at a specific inventory
	 * 
	 * @param item
	 * @param inventory
	 * @return
	 */
	Integer getQuantityForInventoryItem(InventoryItem item, Inventory inventory) {
		def transactionEntries = getTransactionEntriesByInventoryItemAndInventory(item, inventory)
		def quantity = getQuantityByInventoryItemMap(transactionEntries)[item]
		
		log.debug("quantity -> " + quantity)
		return quantity ? quantity : 0;
	}
	
	/**
	 * Get quantity for a given product.
	 * 
	 * @param product
	 * @return
	 */
	Integer getQuantityForProduct(Product product) { 
		throw new UnsupportedOperationException();
	}
	

	/**
	 * Get quantity for all available inventory items in the given inventory.
	 * 
	 * @param inventory
	 * @return
	 */
	Map<InventoryItem, Integer> getQuantityForInventory(Inventory inventory) { 
		def transactionEntries = getTransactionEntriesByInventory(inventory);
		return getQuantityByInventoryItemMap(transactionEntries);
	}

		
	/**
	 * Fetches and populates a StockCard Command object
	 * 
	 * @param cmd
	 * @param params
	 * @return
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
		cmd.inventoryItemList.sort { it.expirationDate }
		
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
	 * 
	 * @param commandInstance
	 * @param params
	 * @return
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
	 * 
	 * @param cmd
	 * @param params
	 * @return
	 */
	RecordInventoryCommand saveRecordInventoryCommand(RecordInventoryCommand cmd, Map params) { 
		log.debug "Saving record inventory command params: " + params
		
		try { 
			// Validation was done during bind, but let's do this just in case
			if (cmd.validate()) { 
				def inventoryItems = getInventoryItemsByProductAndInventory(cmd.product, cmd.inventory)
				// Create a new transaction
				def transaction = new Transaction(cmd.properties)
				
				transaction.transactionType = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)
				
				// Process each row added to the record inventory page
				cmd.recordInventoryRows.each { row -> 					
					
					if (row) { 
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
		log.debug "get products by search terms " + productFilters;
		
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
		log.debug products
		
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
	
	/**
	 * 
	 * @param category
	 * @return
	 */
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
	 * @param productIds
	 * @return 	a map of inventory items by product
	 */
	Map getInventoryItemsByProducts(Location warehouse, List<Integer> productIds) { 
		def inventoryItemMap = [:]
		if (productIds) {
			//def inventory = Inventory.get(warehouse?.inventory?.id);
			productIds.each {
				def product = Product.get(it);
				//def inventoryItems = getInventoryItemsByProductAndInventory(product, inventory);
				inventoryItemMap[product] = getInventoryItemsByProduct(product)
			}
		}
		return inventoryItemMap;
	}
	
	
	/**
	 * @return	a map of inventory items indexed by product
	 */
	Map getInventoryItems() { 
		def inventoryItemMap = [:]		
		Product.list().each { inventoryItemMap.put(it, []) }		
		InventoryItem.list().each {
			inventoryItemMap[it.product] << it
		}
		return inventoryItemMap;
	}

	
	List<InventoryItem> findInventoryItemWithEmptyLotNumber() { 
		return InventoryItem.createCriteria().list() {
			or {
				isNull("lotNumber")
				eq("lotNumber", "")
			}
		}
	}
	
	List<Product> findProductsWithoutEmptyLotNumber() { 
		def products = Product.list();
		def productsWithEmptyInventoryItem = findInventoryItemWithEmptyLotNumber()?.collect { it.product } ;
		def productsWithoutEmptyInventoryItem = products - productsWithEmptyInventoryItem;
		return productsWithoutEmptyInventoryItem;
	}
	
	
	/**
	 * 
	 * @param product
	 * @param lotNumber
	 * @return
	 */
	InventoryItem findInventoryItemByProductAndLotNumber(Product product, String lotNumber) {
		log.debug ("Find inventory item by product " + product?.id + " and lot number '" + lotNumber + "'" )
		def inventoryItems = InventoryItem.createCriteria().list() {
			and {
				eq("product.id", product?.id)
				if (lotNumber) { 
					log.debug "lot number is not null"
					eq("lotNumber", lotNumber)
				}
				else {  
					or { 
						log.debug "lot number is null"
						isNull("lotNumber")
						eq("lotNumber", "")
					}
				}
			}
		}
		log.debug ("Returned inventory items " + inventoryItems);
		// If the list is non-empty, return the first item
		if (inventoryItems) { 
			return inventoryItems.get(0);
		}
		return null;
	}
	 
	
	/**
	 * Get all inventory items for a given product within the given inventory.
	 * 
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
	 * Get all the inventory levels associated with the given inventory
	 */
	List<InventoryLevel> getInventoryLevelsByInventory(Inventory inventoryInstance) {
		return InventoryLevel.findAllByInventory(inventoryInstance)
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
	 * 
	 * @param productList
	 * @param inventoryInstance
	 * @return
	 */
	Map<Product, InventoryLevel> getInventoryLevels(List<Product> productList, Location location) { 
		def inventoryLevelList = getInventoryLevelsByInventory(location.inventory);
		return inventoryLevelList.groupBy { it.product } 
	}
	
	
	/**
	 * Get all transaction entries over all products/inventory items.
	 * 
	 * @param inventoryInstance
	 * @return
	 */
	List getTransactionEntriesByInventory(Inventory inventory) { 
		def criteria = TransactionEntry.createCriteria();
		def transactionEntries = criteria.list {
			transaction {
				eq("inventory", inventory)
				order("transactionDate", "asc")
				order("dateCreated", "asc")
			}
		}
		return transactionEntries;
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
    * 
    * @param inventoryItem
    * @param inventory
    */
   List getTransactionEntriesByInventoryItemAndInventory(InventoryItem inventoryItem, Inventory inventory) {
	   return TransactionEntry.createCriteria().list() {
		   and {
			   eq("inventoryItem", inventoryItem)
			    transaction {
				   eq("inventory", inventory)
			   	}
		   }
	   }
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
		log.debug "create send shipment transaction" 
		
		if (!shipmentInstance.origin.isWarehouse()) {
			throw new RuntimeException ("Can't create send shipment transaction for origin that is not a warehouse")
		}
		
		try { 
			// Create a new transaction for outgoing items
			Transaction debitTransaction = new Transaction();
			debitTransaction.transactionType = TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID)
			debitTransaction.source = null
			debitTransaction.destination = shipmentInstance?.destination.isWarehouse() ? shipmentInstance?.destination : null
			//debitTransaction.destination = shipmentInstance?.destination
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
				
				// Create a new transaction entry for each shipment item
				def transactionEntry = new TransactionEntry();
				transactionEntry.quantity = it.quantity;
				transactionEntry.inventoryItem = inventoryItem;
				debitTransaction.addToTransactionEntries(transactionEntry);
			}
		
			if (!debitTransaction.save()) {
				log.error debitTransaction.errors
				throw new RuntimeException("Failed to save 'Send Shipment' transaction", debitTransaction);
			}
		} catch (Exception e) { 
			log.error("error occrred while creating transaction ", e);
			throw e
			//shipmentInstance.errors.reject("shipment.invalid", e.message);  // this doesn't seem to working properly
		}
	}	
	

	
	/**
	 * Finds the local transfer (if any) associated with the given transaction
	 * 
	 * @param transaction
	 * @return
	 */
	LocalTransfer getLocalTransfer(Transaction transaction) {
		LocalTransfer transfer = null
		transfer = LocalTransfer.findBySourceTransaction(transaction)
		if (transfer == null) { transfer = LocalTransfer.findByDestinationTransaction(transaction) }
		return transfer
	}
	
	/** 
	 * Returns true/false if the given transaction is associated with a local transfer
	 * 
	 * @param transaction
	 * @return
	 */
	Boolean isLocalTransfer(Transaction transaction) {
		getLocalTransfer(transaction) ? true : false
	}
	
	/**
	 * Returns true/false if the passed transaction is a valid candidate for a local transfer
	 * 
	 * @param transaction
	 * @return
	 */
	Boolean isValidForLocalTransfer(Transaction transaction) {
		// make sure that the transaction is of a valid type
		if (transaction?.transactionType?.id != Constants.TRANSFER_IN_TRANSACTION_TYPE_ID  &&
				transaction?.transactionType?.id != Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
			return false
		}
	
		// make sure we are operating only on locally managed warehouses
		if (transaction?.source) {
			if (!(transaction?.source instanceof Location)) { return false }
			else if (!transaction?.source.local){ return false }
		}
		if (transaction?.destination) {
			if (!(transaction?.destination instanceof Location)) { return false }
			else if (!transaction?.destination.local) { return false }
		}
		
		return true
	}
	
	/**
	 * Deletes a local transfer (and underlying transactions) associated with the passed transaction
	 * 
	 * @param transaction
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
	 * 
	 * @param baseTransaction
	 * @return
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
	 * 
	 * @param baseTransaction
	 * @return
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
			/*
			def inventoryItem = 
				findInventoryItemByProductAndLotNumber(it.product, it.lotNumber)
			
			// If the inventory item doesn't exist, we create a new one
			if (!inventoryItem) {
				inventoryItem = new InventoryItem(lotNumber: it.lotNumber, product:it.product)
				if (inventoryItem.hasErrors() && !inventoryItem.save()) {
					throw new RuntimeException("Unable to create inventory item $inventoryItem while creating local transfer")
				}
			}*/
			
			
			// Create a new transaction entry
			def transactionEntry = new TransactionEntry();
			transactionEntry.quantity = it.quantity;
			transactionEntry.inventoryItem = it.inventoryItem;
			mirroredTransaction.addToTransactionEntries(transactionEntry);
		}	
		
		return mirroredTransaction
	}

	/**
	*
	* @return
	*/
   def getConsumptionTransactionsBetween(Date startDate, Date endDate) {
	   log.debug ("startDate = " + startDate + " endDate = " + endDate)
	   def criteria = Consumption.createCriteria()
	   def results = criteria.list {
		   if (startDate && endDate) {
			   between('transactionDate',startDate, endDate)
		   }
	   }
   
	  return results
   }

	/**
	 * 
	 * @return
	 */
	def getConsumptions(Date startDate, Date endDate, String groupBy) { 
		log.debug ("startDate = " + startDate + " endDate = " + endDate)
		def criteria = Consumption.createCriteria()
		def results = criteria.list {
			if (startDate && endDate) { 
				between('transactionDate',startDate, endDate)
			}
			projections {
				sum('quantity')
				groupProperty('product')
				groupProperty('transactionDate')
			}
		}
	
	   return results
	}
	
		
	/**
	 * 	
	 * @return
	 */
	def getConsumptionDateKeys() {
		def monthsYears = Consumption.executeQuery("""select distinct 
				day(transactionDate), 
				week(transactionDate), 
				month(transactionDate), 
				year(transactionDate)
			from Consumption order by year(transactionDate) desc, month(transactionDate) desc""")
		return monthsYears?.collect() { [day: it[0], week: it[1], month: it[2], year: it[3] ] }
	}
	
	/**
	*
	* @param product
	* @param date
	* @return
	*/
	def getProductQuantity(Product product, Location location, Date date) {
		throw new UnsupportedOperationException(); 
	}

	/**
	 * 
	 */
	def getQuantity(Product product, Location location, Date beforeDate) { 
		def quantity = 0;
		def transactionEntries = getTransactionEntriesBeforeDate(product, location, beforeDate)
		quantity = adjustQuantity(quantity, transactionEntries)
		return quantity;		
	}
	

	/**
	 * Get the initial quantity of a product for the given location and date.  
	 * If the date is null, then we assume that the answer is 0.
	 * 	
	 * @param product
	 * @param location
	 * @param date
	 * @return
	 */
	def getInitialQuantity(Product product, Location location, Date date) { 
		def quantity = 0;
		if (date) { 
			quantity = getQuantity(product, location, date);
		}
		return quantity;
	}
	
	
	/**
	 * Get the current quantity (as of the given date) or today's date if the 
	 * given date is null.
	 * 
	 * @param product
	 * @param location
	 * @param date
	 * @return
	 */
	def getCurrentQuantity(Product product, Location location, Date date) { 
		def quantity = 0;
		if (date) { 
			quantity = getQuantity(product, location, date);
		}
		else { 
			quantity = getQuantity(product, location, new Date());
		}
		return quantity;
	}
	
	
	
	/**
	 * Get the quantity of a particular product at the given location, 
	 * on the given date.
	 * 
	 * @param product
	 * @param date
	 * @return
	def getQuantity(Product product, Location location, Date date) { 
		def quantity = 0;
		def inventoryItems = InventoryItem.findAllByProduct(product)
		inventoryItems.each { inventoryItem ->
			quantity += getQuantity(inventoryItem, location, date);
		}
		return quantity;
	} 
	 */
	
	
	
	/**
	 * Get quantity for the given inventory item at the given location, on the 
	 * given date.  
	 * 
	 * @param inventoryItem
	 * @param date
	 * @return
	def getQuantity(InventoryItem inventoryItem, Location location, Date date) { 
		def quantity = 0;
		
		// Get the date of this inventory item's last 'inventory' transaction 
		def lastInventoryTransactionEntry = getPreviousInventoryTransactionEntry(inventoryItem, location, date);
		
		// If there's no last inventory date for the item, we look for the last inventory date for the product
		if (!lastInventoryTransactionEntry) {
			lastInventoryTransactionEntry = getPreviousInventoryTransactionEntry(inventoryItem?.product, location, date);
		}
		
		// Date of last inventory might be null if there's never been an inventory
		def lastInventoryDate = lastInventoryTransactionEntry?.transaction?.transactionDate;
		quantity = lastInventoryTransactionEntry?.quantity ?: 0

		log.debug ("Starting quantity = " + quantity)
				
		// Get all transactions for an inventory item from the last inventory 
		// date until the given date 
		def transactionEntries = getTransactionEntries(inventoryItem, location, lastInventoryDate, date)
		adjustQuantity(quantity, transactionEntries)

		log.debug ("Ending quantity = " + quantity)				
		return quantity;
	}
	*/

	
	/**
	 * 	
	 * @param initialQuantity
	 * @param transactionEntries
	 * @return
	 */
	def adjustQuantity(Integer initialQuantity, List<Transaction> transactionEntries) { 
		def quantity = initialQuantity;
		transactionEntries.each { transactionEntry ->
			quantity = adjustQuantity(quantity, transactionEntry)
		}
		return quantity;
	}
	
	/**
	 * 
	 * @param initialQuantity
	 * @param transactionEntry
	 * @return
	 */
	def adjustQuantity(Integer initialQuantity, TransactionEntry transactionEntry) {		
		def quantity = initialQuantity;
		
		def code = transactionEntry?.transaction?.transactionType?.transactionCode;		
		if (code == TransactionCode.INVENTORY || code == TransactionCode.PRODUCT_INVENTORY) {
			quantity = transactionEntry.quantity;
		} 
		else if (code == TransactionCode.DEBIT) {
			quantity -= transactionEntry.quantity;
		} 
		else if (code == TransactionCode.CREDIT) {
			quantity += transactionEntry.quantity;
		}
		return quantity;		
	}
	
	
	/**
	 * 
	 * @param product
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	def getTransactionEntries(Product product, Location location, Date startDate, Date endDate) {
		def criteria = TransactionEntry.createCriteria();
		def transactionEntries = criteria.list {
			inventoryItem {
				eq("product", product)
			}
			transaction { 				
				// All transactions between start date and end date
				if (startDate && endDate) {
					between("transactionDate", startDate, endDate)
				} 
				// All transactions after start date
				else if (startDate) { 
					ge("transactionDate", startDate)
				}				
				// All transactions before end date
				else if (endDate) { 
					le("transactionDate", endDate)
				}
				eq("inventory", location?.inventory)
				order("transactionDate", "asc")
				order("dateCreated", "asc")
			}
		}
		return transactionEntries;
	}
	
	/**
	*
	* @param product
	* @param startDate
	* @param endDate
	* @return
	*/
   def getTransactionEntriesBeforeDate(Product product, Location location, Date beforeDate) {
	   def criteria = TransactionEntry.createCriteria();
	   def transactionEntries = []
	   
	   if (beforeDate) { 
		   transactionEntries = criteria.list {
			   inventoryItem {
				   eq("product", product)
			   }
			   transaction {			   
				   // All transactions before given date
				   lt("transactionDate", beforeDate)
				   eq("inventory", location?.inventory)
				   order("transactionDate", "asc")
				   order("dateCreated", "asc")
   
			   }
		   }
	   }
	   return transactionEntries;
   }

		
	
	
		
	/**
	 * 
	 * @param inventoryItem
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	def getTransactionEntries(InventoryItem inventoryItem, Location location, Date startDate, Date endDate) { 
		def criteria = TransactionEntry.createCriteria();
		def transactionEntries = criteria.list {
			eq("inventoryItem", inventoryItem)
			transaction {
				if (startDate && endDate) {
					between("transactionDate", startDate, endDate)
				} 
				else if (startDate) { 
					ge("transactionDate", startDate)
				}
				else if (endDate) { 
					le("transactionDate", endDate)
				}
				eq("inventory", location?.inventory)
				order("transactionDate", "asc")
				order("dateCreated", "asc")
			}
		}
		return transactionEntries;
	}
	
	/**
	 * 
	 * @param location
	 * @param category
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	def getTransactionEntries(Location location, Category category, Date startDate, Date endDate) {
		def criteria = TransactionEntry.createCriteria();
		def categories = []
		categories << category
		def matchCategories = getExplodedCategories(categories)
		def transactionEntries = criteria.list {
			if (category) {
				inventoryItem { 
					product { 
						'in'("category", matchCategories)
					}
				}
			}
			transaction {
				if (startDate && endDate) {
					between("transactionDate", startDate, endDate)
				}
				else if (startDate) {
					ge("transactionDate", startDate)
				}
				else if (endDate) {
					le("transactionDate", endDate)
				}
				eq("inventory", location?.inventory)
				order("transactionDate", "asc")
				order("dateCreated", "asc")
			}
		}
		return transactionEntries;
		 
	
	}
	
	/**
	 * 
	 * @param product
	 * @param location
	 * @param date
	 * @return
	 */
	def getPreviousInventoryTransactionEntry(Product product, Location location, Date date) {
		def transactionTypes = []
		transactionTypes << TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)
		def criteria = TransactionEntry.createCriteria()
		def transactionEntry = criteria.get { 
			inventoryItem {
				eq("product", product)
			}
			transaction {
				'in'("transactionType", transactionTypes)		
				eq("inventory", location?.inventory)
				order('transactionDate', 'desc')				
			}
			maxResults(1)
		}
		return transactionEntry;
	}
	
	/**
	 * 
	 * @param inventoryItem
	 * @param date
	 * @return
	 */
	def getPreviousInventoryTransactionEntry(InventoryItem inventoryItem, Location location, Date date) { 
		def transactionTypes = []
		transactionTypes << TransactionType.get(Constants.INVENTORY_TRANSACTION_TYPE_ID)		
		def criteria = TransactionEntry.createCriteria()		
		def transactionEntry = criteria.get {
			eq("inventoryItem", inventoryItem)
			transaction { 
				'in'("transactionType", transactionTypes)		
				eq("inventory", location?.inventory)				
				order('transactionDate', 'desc')
			}
			maxResults(1)
		}
		return transactionEntry
	}
	public void validateData(ImportDataCommand command) {
		Date today = new Date()
		today.clearTime()
		def transactionInstance = new Transaction(transactionDate: today,
				transactionType: TransactionType.findById(Constants.INVENTORY_TRANSACTION_TYPE_ID),
				inventory: command?.location?.inventory)

		// Iterate over each row and validate values
		command?.data?.each { Map params ->
			//log.debug "Inventory item " + importParams

			validateInventoryData(params, command.errors)

			def expirationDate = ImporterUtil.parseDate(params.expirationDate, command.errors)
			def category = ImporterUtil.findOrCreateCategory(params.category, command.errors);

			// Create product if not exists
			Product product = Product.findByName(params.productDescription);
			if (!product) {
				product = new Product(
						name:params.productDescription,
						upc:params.upc,
						ndc:params.ndc,
						category:category,
						manufacturer:params.manufacturer,
						manufacturerCode:params.manufacturerCode,
						unitOfMeasure:params.unitOfMeasure);

				if (!product.validate()) {
					command.errors.reject("Error saving product " + product?.name)
					//throw new RuntimeException("Error saving product " + product?.name)
				}
				log.debug "Created new product " + product.name;
			}

			// Find the inventory item by product and lotNumber and description
			InventoryItem inventoryItem =
				findInventoryItemByProductAndLotNumber(product, params.lotNumber);

			log.debug("Inventory item " + inventoryItem)
			// Create inventory item if not exists
			if (!inventoryItem) {
				inventoryItem = new InventoryItem()
				inventoryItem.product = product
				inventoryItem.lotNumber = params.lotNumber;
				inventoryItem.expirationDate = expirationDate;
				if (!inventoryItem.validate()) {
					inventoryItem.errors.allErrors.each {
						command.errors.addError(it);
					}
				}
			}

			// Create a transaction entry if there's a quantity specified
			if (params?.quantity) {
				TransactionEntry transactionEntry = new TransactionEntry();
				transactionEntry.quantity = params.quantity;
				transactionEntry.inventoryItem = inventoryItem;
				transactionInstance.addToTransactionEntries(transactionEntry);
				if (!transactionEntry.validate()) {
					transactionEntry.errors.allErrors.each {
						command.errors.addError(it);
					}
				}
			}
		}

		if (transactionInstance.validate()) {
			transactionInstance.errors.allErrors.each {
				command.errors.addError(it);
			}
		}
	}
	
	/**
	*
	* @param importParams
	* @param errors
	*/
   private void validateInventoryData(Map params, Errors errors) {
	   def lotNumber = (params.lotNumber) ? String.valueOf(params.lotNumber) : null;
	   if (params?.lotNumber instanceof Double) {
		   errors.reject("Property 'Serial Number / Lot Number' with value '${lotNumber}' should be not formatted as a Double value");
	   }
	   else if (!params?.lotNumber instanceof String) {
		   errors.reject("Property 'Serial Number / Lot Number' with value '${lotNumber}' should be formatted as a Text value");
	   }

	   def quantity = params.quantity?: 0;
	   if (!params?.quantity instanceof Double) {
		   errors.reject("Property [quantity] with value '${quantity} for '${lotNumber}' should be formatted as a Double value");
	   }
	   else if (params?.quantity instanceof String) {
		   errors.reject("Property [quantity] with value '${quantity} for '${lotNumber}' should not be formatted as a Text value");
	   }

	   def manufacturerCode = (params.manufacturerCode) ? String.valueOf(params.manufacturerCode) : null;
	   if (!params?.manufacturerCode instanceof String) {
		   errors.reject("Property 'Manufacturer Code' with value '${manufacturerCode}' should be formatted as a Text value");
	   }
	   else if (params?.manufacturerCode instanceof Double) {
		   errors.reject("Property 'Manufacturer Code' with value '${manufacturerCode}' should not be formatted as a Double value");
	   }

	   def upc = (params.upc) ? String.valueOf(params.upc) : null;
	   if (!params?.upc instanceof String) {
		   errors.reject("Property 'UPC' with value '${upc}' should be formatted as a Text value");
	   }
	   else if (params?.upc instanceof Double) {
		   errors.reject("Property 'UPC' with value '${upc}' should be not formatted as a Double value");
	   }

	   def ndc = (params.ndc) ? String.valueOf(params.ndc) : null;
	   if (!params?.ndc instanceof String) {
		   errors.reject("Property 'GTIN' with value '${ndc}' should be formatted as a Text value");
	   }
	   else if (params?.ndc instanceof Double) {
		   errors.reject("Property 'GTIN' with value '${ndc}' should be not formatted as a Double value");
	   }
   }

   /**
	* Import data from given inventoryMapList into database.
	*
	* @param location
	* @param inventoryMapList
	* @param errors
	*/
   public void importData(ImportDataCommand command) {


	   try {
		   def dateFormat = new SimpleDateFormat("yyyy-MM-dd");


		   Date today = new Date().clearTime()

		   def transactionInstance = new Transaction(transactionDate: today,
				   transactionType: TransactionType.findById(Constants.INVENTORY_TRANSACTION_TYPE_ID),
				   inventory: command?.location.inventory)

		   // Iterate over each row
		   command?.data?.each { Map params ->

			   def lotNumber = (params.lotNumber) ? String.valueOf(params.lotNumber) : null;
			   def quantity = (params.quantity)?:0;

			   def unitOfMeasure = params.unitOfMeasure;
			   def manufacturer = (params.manufacturer) ? String.valueOf(params.manufacturer) : null;
			   def manufacturerCode = (params.manufacturerCode) ? String.valueOf(params.manufacturerCode) : null;
			   def upc = (params.upc) ? String.valueOf(params.upc) : null;
			   def ndc = (params.ndc) ? String.valueOf(params.ndc) : null;

			   def expirationDate = parseDate(params.expirationDate);

			   def category = findOrCreateCategory(params.category)

			   // Create product if not exists
			   Product product = Product.findByName(params.productDescription);
			   if (!product) {
				   product = new Product(
					   name:params.productDescription,
					   upc:upc,
					   ndc:ndc,
					   category:category,
					   manufacturer:manufacturer,
					   manufacturerCode:manufacturerCode,
					   unitOfMeasure:unitOfMeasure);

				   if (!product.save()) {
					   errors.reject("Error saving product " + product?.name)
				   }
				   log.debug "Created new product " + product.name;
			   }


			   // Find the inventory item by product and lotNumber and description
			   InventoryItem inventoryItem =
					   inventoryService.findInventoryItemByProductAndLotNumber(product, lotNumber);

			   log.debug("Inventory item " + inventoryItem)
			   // Create inventory item if not exists
			   if (!inventoryItem) {
				   inventoryItem = new InventoryItem()
				   inventoryItem.product = product
				   inventoryItem.lotNumber = lotNumber;
				   inventoryItem.expirationDate = expirationDate;
				   if (inventoryItem.hasErrors() || !inventoryItem.save()) {
					   log.debug "Product " + product
					   log.debug "Inventory item " + params.lotNumber;
					   inventoryItem.errors.allErrors.each {
						   log.error "ERROR " + it;
						   errors.addError(it);
					   }
				   }
			   }

			   // Create a transaction entry if there's a quantity specified
			   if (params?.quantity) {
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
	
	
	
}
