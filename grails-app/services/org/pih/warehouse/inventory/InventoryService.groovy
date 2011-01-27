package org.pih.warehouse.inventory;

import java.util.Map;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.ListUtils;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.Warehouse;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Category;

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
		log.info "category " + category?.name + " " + productList?.size();
		
		
		return productList;
	}*/

	BrowseInventoryCommand browseInventory(BrowseInventoryCommand commandInstance, Map params) { 
		
		// Get all product types and set the default product type				
		commandInstance.rootCategory = productService.getRootCategory();
		commandInstance.inventoryInstance = commandInstance?.warehouseInstance?.inventory;
		commandInstance.categoryInstance = Category.get(params?.categoryId)
		commandInstance.categoryInstance = commandInstance?.categoryInstance ?: commandInstance?.rootCategory;
		commandInstance.productList = (commandInstance?.categoryFilters) ? getProductsByCategories(commandInstance?.categoryFilters, params) : [];		
		commandInstance.productMap = getProductMap(commandInstance?.warehouseInstance?.id);
		commandInstance.inventoryItemMap =  getInventoryItemMap(commandInstance?.warehouseInstance?.id);
		commandInstance.productList = commandInstance?.productList?.sort() { it.name };
		commandInstance.quantityMap = getQuantityMap(commandInstance?.inventoryInstance);
		return commandInstance;
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
		log.info "transaction entries " + transactionEntries;		
		transactionEntries.each { 
			def currentQuantity = (quantityMap.get(it.product))?:0;			
			currentQuantity += it.quantity
			quantityMap.put(it.product, currentQuantity)
		}
		return quantityMap;		
	}

	
	
	RecordInventoryCommand getRecordInventoryCommand(RecordInventoryCommand commandInstance, Map params) { 		
		log.info "Params " + params;
		
		if (!commandInstance?.product) { 
			commandInstance.errors.reject("error.product.invalid","Product does not exist");
		}
		else { 			
			commandInstance.recordInventoryRow = new RecordInventoryRowCommand();
			
			// This gets all inventory items for a product
			//def inventoryItemList = getInventoryItemsByProduct(commandInstance?.product)
			
			// What we want is to get all inventory items that have been involved in transactions at this warehouse
			def inventoryItemList = getInventoryItemsByProductAndInventory(commandInstance?.product, commandInstance?.inventory);
			
			inventoryItemList.each { 
				//def lot = InventoryLot.findByProductAndLotNumber(commandInstance?.product, it.lotNumber);				
				def transactionEntryList = getTransactionEntriesByInventoryItem(it);
				log.info "entries: " + transactionEntryList*.quantity;
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
			}
		}
		return commandInstance;
		
	}
		
	
	RecordInventoryCommand saveRecordInventoryCommand(RecordInventoryCommand cmd, Map params) { 
		log.info "Saving record inventory command params: " + params
		
		try { 
			// Validation was done during bind, but let's do this just in case
			if (cmd.validate()) { 
				def inventoryItems = getInventoryItemsByProductAndInventory(cmd.product, cmd.inventory)
				// Create a new transaction
				def transaction = new Transaction(cmd.properties)
				
				// TODO Change this to be a valid lookup
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
						TransactionEntry transactionEntry = new TransactionEntry();
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
	
	List getProductsByCategories(List categories, Map params) { 
		def products = []
		def matchCategories = []
		if (categories) { 
			categories.each { cat -> 
				if (cat) {
					matchCategories << cat;
					matchCategories.addAll( (cat?.children)?cat.children:[]);
				}
			}
		}
		log.info matchCategories
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
				eq("product.id", productInstance.id)
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
}
