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
				def lot = InventoryLot.findByProductAndLotNumber(productInstance, it.lotNumber);				
				def row = new RecordInventoryRowCommand()
				row.id = it.id;
				row.lotNumber = it.lotNumber;
				row.expirationDate = lot?.expirationDate;
				row.description = it.description;
				row.oldQuantity = it.quantity;
				row.newQuantity = it.quantity;
				
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
				def inventoryItems = getInventoryItemsByProduct(cmd.product)
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
			throw new Exception("errors.product.ProductNotFoundException")
		def results = InventoryItem.findAllByProduct(productInstance)					
		return results;
	}
	
	List getTransactionEntriesByProduct(Product productInstance) { 		
		def results = TransactionEntry.findAllByProduct(productInstance)		
		return results;
	}
	
	
	Map getInventoryMap(Long id) { 
		return Warehouse.get(id)?.inventory?.inventoryItems?.groupBy { it.product } 
	}

	Map getInventoryLevelMap(Long id) { 
		return Warehouse.get(id)?.inventory?.inventoryLevels?.groupBy { it.product } 
	}
		
	
	Warehouse getWarehouse(Long id) { 
		return Warehouse.get(id);
	}
}
