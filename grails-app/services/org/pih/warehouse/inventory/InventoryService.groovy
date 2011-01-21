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
		commandInstance.categoryInstance = Category.get(params?.categoryId)
		commandInstance.categoryInstance = commandInstance?.categoryInstance ?: commandInstance?.rootCategory;
		commandInstance.productList = (commandInstance?.categoryFilters) ? getProductsByCategories(commandInstance?.categoryFilters, params) : [];		
		commandInstance.productMap = getProductMap(commandInstance?.warehouseInstance?.id);
		commandInstance.inventoryItemMap =  getInventoryItemMap(commandInstance?.warehouseInstance?.id);
		commandInstance.productList = commandInstance?.productList?.sort() { it.name };
		
		return commandInstance;
	}
	

	
	
	RecordInventoryCommand getRecordInventoryCommand(RecordInventoryCommand commandInstance, Map params) { 		
		log.info "Params " + params;
		
		def productInstance = commandInstance.product;
		
		if (!productInstance) { 
			commandInstance.errors.reject("error.product.invalid","Product does not exist");
		}
		else { 		
			commandInstance.recordInventoryRow = new RecordInventoryRowCommand();
			
			def inventoryItemList = getInventoryItemsByProduct(productInstance)
			inventoryItemList.each { 
				//def lot = InventoryLot.findByProductAndLotNumber(productInstance, it.lotNumber);				
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
				
				cmd.recordInventoryRows.each { row -> 
					
					// Update existing inventory item
					def inventoryItem = new InventoryItem(); 
					if (row.id) { 
						inventoryItem = InventoryItem.get(row.id);
					}
					inventoryItem.properties = row.properties
					if (!inventoryItem.hasErrors() && inventoryItem.save()) { 					
						// Create a new transaction entry	
						if (row.oldQuantity != row.newQuantity) {
							TransactionEntry transactionEntry = new TransactionEntry();
							transactionEntry.properties = row.properties;
							transactionEntry.quantity = row.newQuantity - row.oldQuantity;  // difference
							transactionEntry.product = cmd.product
							transactionEntry.inventoryItem = inventoryItem;
							transaction.addToTransactionEntries(transactionEntry);						
						}
					}
					else { 
						inventoryItem.errors.allErrors.each { error->
							cmd.errors.reject("inventoryItem.invalid",
								[inventoryItem, error.getField(), error.getRejectedValue()] as Object[],
								"Property [${error.getField()}] of [${inventoryItem.class.name}] with value [${error.getRejectedValue()}] is invalid");
						}
					}
				}		
				
				// Check if there are any changes recorded ... no reason to  
				if (transaction.transactionEntries) { 	
					if (!transaction.hasErrors() && transaction.save()) { 
						
					}
					else { 
						transaction.errors.allErrors.each { error ->
							cmd.errors.reject("transaction.invalid",
								[transaction, error.getField(), error.getRejectedValue()] as Object[],
								"Property [${error.getField()}] of [${transaction.class.name}] with value [${error.getRejectedValue()}] is invalid");
						}
					}
				}				
				else {
					// We could do this, but it keeps us from changing the lot number and description  
					//cmd.errors.reject("transaction.noChanges", "There are no quantity changes in the current transaction");
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
