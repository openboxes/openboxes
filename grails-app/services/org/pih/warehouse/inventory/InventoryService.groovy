package org.pih.warehouse.inventory;

import java.util.Map;

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
	
	List getProductsByCategories(List categories, Map params) { 
		def products = []
		def matchCategories = []
		if (categories) { 
			categories.each { c -> 
				if (c) {
					matchCategories << c;
					matchCategories.addAll( (c?.children)?c.children:[]);
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
