/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory;

import grails.validation.ValidationException
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.pih.warehouse.product.Category;

import java.text.SimpleDateFormat;

import org.pih.warehouse.importer.ImportDataCommand;
import org.pih.warehouse.importer.ImporterUtil;


import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.ProductAttribute;
import org.pih.warehouse.product.ProductGroup;
import org.pih.warehouse.shipping.Shipment;


import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.validation.Errors;

import org.pih.warehouse.reporting.Consumption;
import org.pih.warehouse.auth.AuthService

class InventoryService implements ApplicationContextAware {

    def sessionFactory
    def productService
	//def authService

    ApplicationContext applicationContext

    boolean transactional = true

    /**
     * @return shipment service
     */
    def getShipmentService() {
        return applicationContext.getBean("shipmentService")
    }

    /**
     * @return order service
     */
    def getOrderService() {
        return applicationContext.getBean("orderService")
    }


    /**
     * Saves the specified warehouse
     *
     * @param warehouse
     */
    void saveLocation(Location warehouse) {
        log.debug("saving warehouse " + warehouse)
        log.debug("location type " + warehouse.locationType)
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

        warehouse.inventory = new Inventory(['warehouse': warehouse])
        saveLocation(warehouse)

        return warehouse.inventory
    }

    /**
     * @return a Sorted Map from product primary category to List of products
     */
    Map getProductMap(Collection inventoryItems) {

        Map map = new TreeMap();
        if (inventoryItems) {
            inventoryItems.each {
                Category category = it.category ? it.category : new Category(name: "Unclassified")
                List list = map.get(category)
                if (list == null) {
                    list = new ArrayList();
                    map.put(category, list);
                }
                list.add(it);
            }
            for (entry in map) {
                entry.value.sort { item1, item2 ->
                    //item1?.product?.name <=> item2?.product?.name
                    item1?.description <=> item2?.description
                }
            }
        }
        return map
    }

    /**
     * Search inventory items by term or product Id
     *
     * @param searchTerm
     * @param productId
     * @return
     */
    List findInventoryItems(String searchTerm) {
        return InventoryItem.withCriteria {
            or {
                ilike("lotNumber", "%" + searchTerm + "%")
                product {
                    ilike("name", "%" + searchTerm + "%")
                }
            }
        }
    }

    List findInventoryItemsByProducts(List<Product> products) {
        def inventoryItems = []
        if (products) {
            inventoryItems = InventoryItem.withCriteria {
                'in'("product", products)
                order("expirationDate", "asc")
            }
        }
        return inventoryItems;
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

        // Get the selected category or use the root category
        def rootCategory = productService?.getRootCategory();
        commandInstance.categoryInstance = commandInstance?.categoryInstance ?: productService.getRootCategory();

        // Get current inventory for the given products
        if (commandInstance?.categoryInstance != rootCategory || commandInstance?.searchPerformed || commandInstance.tag) {
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
    Map getCurrentInventory(InventoryCommand commandInstance) {

        def inventoryItemCommands = [];
        List categories = new ArrayList();
        if (commandInstance?.subcategoryInstance) {
            categories.add(commandInstance?.subcategoryInstance);
        }
        else {
            categories.add(commandInstance?.categoryInstance);
        }

        List searchTerms = (commandInstance?.searchTerms ? Arrays.asList(commandInstance?.searchTerms?.split(" ")) : null);
        log.info "searchTerms = " + searchTerms
        log.debug("get products: " + commandInstance?.warehouseInstance)
        log.info "command.tag  = " + commandInstance.tag
        def products = []
        if (commandInstance.tag) {
            products = getProductsByTags(commandInstance.tag)
            commandInstance.numResults = products.size()
        } else {
            // Get all products, including hidden ones
            def matchCategories = getExplodedCategories(categories)
            products = getProductsByTermsAndCategories(searchTerms, matchCategories, commandInstance?.maxResults, commandInstance?.offset)

            commandInstance.numResults = products.totalCount
            println  "command instance numResult: " + products.totalCount
            if (!commandInstance?.showHiddenProducts) {
                products.removeAll(getHiddenProducts(commandInstance?.warehouseInstance))
            }
            println "after getProductsByTermsAndCategories: " + new Date().time

            // now localize to only match products for the current locale
            // TODO: this would also have to handle the category filtering
            //  products = products.findAll { product ->
            //  def localizedProductName = getLocalizationService().getLocalizedString(product.name);  // TODO: obviously, this would have to use the actual locale
            // return productFilters.any {
            //   localizedProductName.contains(it)  // TODO: this would also have to be case insensitive
            // }
            // }
        }
        products = products?.sort() { map1, map2 -> map1.category <=> map2.category ?: map1.name <=> map2.name };

        // Get quantity for each item in inventory TODO: Should only be doing this for the selected products for speed
        def quantityOnHandMap = getQuantityByProductMap(commandInstance?.warehouseInstance?.inventory, products);
        def quantityOutgoingMap = getOutgoingQuantityByProduct(commandInstance?.warehouseInstance, products);
        def quantityIncomingMap = getIncomingQuantityByProduct(commandInstance?.warehouseInstance, products);

        println "after getProducts: " + new Date().time
        products.each { product ->
            def quantityOnHand = quantityOnHandMap[product] ?: 0
            def quantityToReceive = quantityIncomingMap[product] ?: 0
            def quantityToShip = quantityOutgoingMap[product] ?: 0
            if (commandInstance?.showOutOfStockProducts || (quantityOnHand + quantityToReceive + quantityToShip > 0)) {
                inventoryItemCommands << getInventoryItemCommand(product,
                        commandInstance?.warehouseInstance?.inventory,
                        quantityOnHand, quantityToReceive, quantityToShip,
                        commandInstance?.showOutOfStockProducts)
            }
        }
        println "before getProductGroups: " + new Date().time

        def productGroups = getProductGroups(commandInstance);
        productGroups.each { productGroup ->
            def inventoryItemCommand = getInventoryItemCommand(productGroup, commandInstance?.warehouseInstance.inventory, commandInstance.showOutOfStockProducts)
            inventoryItemCommand.inventoryItems = new ArrayList();
            productGroup.products.each { product ->
                inventoryItemCommand.quantityOnHand += quantityOnHandMap[product] ?: 0
                inventoryItemCommand.quantityToReceive += quantityIncomingMap[product] ?: 0
                inventoryItemCommand.quantityToShip += quantityOutgoingMap[product] ?: 0
                inventoryItemCommand.inventoryItems << getInventoryItemCommand(product, commandInstance?.warehouseInstance.inventory,
                        quantityOnHandMap[product] ?: 0,
                        quantityIncomingMap[product] ?: 0,
                        quantityOutgoingMap[product] ?: 0,
                        commandInstance.showOutOfStockProducts)

                // Remove all products in the product group from the main productd list
                inventoryItemCommands.removeAll { it.product == product }
            }
            inventoryItemCommands << inventoryItemCommand
        }


        commandInstance?.categoryToProductMap = getProductMap(inventoryItemCommands);
        return commandInstance?.categoryToProductMap
    }


    InventoryItemCommand getInventoryItemCommand(Product product, Inventory inventory, Integer quantityOnHand, Integer quantityToReceive, Integer quantityToShip, Boolean showOutOfStockProducts) {
        InventoryItemCommand inventoryItemCommand = new InventoryItemCommand();
        def inventoryLevel = InventoryLevel.findByProductAndInventory(product, inventory)
        inventoryItemCommand.description = product.name
        inventoryItemCommand.category = product.category
        inventoryItemCommand.product = product
        inventoryItemCommand.inventoryLevel = inventoryLevel
        inventoryItemCommand.quantityOnHand = quantityOnHand
        inventoryItemCommand.quantityToReceive = quantityToReceive
        inventoryItemCommand.quantityToShip = quantityToShip
        return inventoryItemCommand
    }

    InventoryItemCommand getInventoryItemCommand(ProductGroup productGroup, Inventory inventory, Boolean showOutOfStockProducts) {
        InventoryItemCommand inventoryItemCommand = new InventoryItemCommand();
        inventoryItemCommand.description = productGroup.description
        inventoryItemCommand.productGroup = productGroup
        inventoryItemCommand.category = productGroup.category
        //inventoryItemCommand.quantityOnHand = 1
        //inventoryItemCommand.quantityToReceive = 0
        //inventoryItemCommand.quantityToShip = 0

        return inventoryItemCommand
    }

    /**
     *
     * @param inventoryCommand
     * @return
     */
    Set<ProductGroup> getProductGroups(InventoryCommand inventoryCommand) {
        List categoryFilters = new ArrayList();
        if (inventoryCommand?.subcategoryInstance) {
            categoryFilters.add(inventoryCommand?.subcategoryInstance);
        }
        else {
            categoryFilters.add(inventoryCommand?.categoryInstance);
        }

        List searchTerms = (inventoryCommand?.searchTerms ? Arrays.asList(inventoryCommand?.searchTerms.split(" ")) : null);

        def productGroups = getProductGroups(inventoryCommand?.warehouseInstance, searchTerms, categoryFilters,
                inventoryCommand?.showHiddenProducts);

        productGroups = productGroups?.sort() { it?.description };
        return productGroups;
    }

    /**
     * Get all product groups for the given location, searchTerms, categories.
     * @param location
     * @param searchTerms
     * @param categoryFilters
     * @param showHiddenProducts
     * @return
     */
    Set<ProductGroup> getProductGroups(Location location, List searchTerms, List categoryFilters, Boolean showHiddenProducts) {
        def productGroups = ProductGroup.list()
        productGroups = productGroups.intersect(getProductGroups(searchTerms, categoryFilters))
        /*
          // Get products that match the search terms by name and category
          def categories = getCategoriesMatchingSearchTerms(searchTerms)

          // Categories
          def matchCategories = getExplodedCategories(categoryFilters);
          log.debug "matchCategories " + matchCategories


          // Get all products, including hidden ones

          if (!showHiddenProducts) {
              def statuses = []
              statuses << InventoryStatus.NOT_SUPPORTED
              statuses << InventoryStatus.SUPPORTED_NON_INVENTORY

              def removeProducts = getProductsByLocationAndStatuses(location, statuses)
              log.debug "remove " + removeProducts.size() + " hidden products"
              products.removeAll(removeProducts)

          }

          log.debug "base products " + products.size();
          if (matchCategories && searchTerms) {
              def searchProducts = ProductGroup.createCriteria().list() {
                  and {
                      or {
                          searchTerms.each {
                              ilike("description", "%" + it + "%")
                          }
                      }
                      'in'("category", matchCategories)
                  }
              }
              products = products.intersect(searchProducts);
          }
          else {
              def searchProducts = ProductGroup.createCriteria().list() {
                  or {
                      if (searchTerms) {
                          searchTerms.each {
                              String[] filterTerms = it.split("\\s+");
                              or {
                                  and {
                                      filterTerms.each {
                                          ilike("description", "%" + it + "%")
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
          */
        return productGroups;
    }


    List getProductGroups(List searchTerms, List categories) {
        def productGroups = ProductGroup.createCriteria().list() {
            or {
                if (searchTerms) {
                    and {
                        searchTerms.each { searchTerm ->
                            ilike("description", "%" + searchTerm + "%")
                        }
                    }
                }
                if (categories) {
                    'in'("category", categories)
                }
            }
        }
        return productGroups
    }

    /**
     * Get the outgoing quantity for all products at the given location.
     *
     * @param location
     * @return
     */
    Map getOutgoingQuantityByProduct(Location location, List<Product> products) {
        Map quantityByProduct = [:]
        Map quantityShippedByProduct = getShipmentService().getOutgoingQuantityByProduct(location, products);
        Map quantityOrderedByProduct = getOrderService().getOutgoingQuantityByProduct(location, products)
        //Map quantityRequestedByProduct = getRequisitionService().getOutgoingQuantityBgetInventoryItemsJsonByProductyProduct(location)
        quantityShippedByProduct.each { product, quantity ->
            def productQuantity = quantityByProduct[product];
            if (!productQuantity) productQuantity = 0;
            productQuantity += quantity ?: 0;
            quantityByProduct[product] = productQuantity;
        }
        quantityOrderedByProduct.each { product, quantity ->
            def productQuantity = quantityByProduct[product];
            if (!productQuantity) productQuantity = 0;
            productQuantity += quantity ?: 0;
            quantityByProduct[product] = productQuantity;
        }
//		quantityRequestedByProduct.each { product, quantity ->
//			def productQuantity = quantityByProduct[product];
//			if (!productQuantity) productQuantity = 0;
//			productQuantity += quantity?:0;
//			quantityByProduct[product] = productQuantity;
//		}
        return quantityByProduct;
    }

    /**
     * Get the incoming quantity for all products at the given location.
     * @param location
     * @return
     */
    Map getIncomingQuantityByProduct(Location location, List<Product> products) {
        Map quantityByProduct = [:]
        Map quantityShippedByProduct = getShipmentService().getIncomingQuantityByProduct(location, products);
        Map quantityOrderedByProduct = getOrderService().getIncomingQuantityByProduct(location, products)
        //Map quantityRequestedByProduct = getRequisitionService().getIncomingQuantityByProduct(location)
        quantityShippedByProduct.each { product, quantity ->
            def productQuantity = quantityByProduct[product];
            if (!productQuantity) productQuantity = 0;
            productQuantity += quantity ?: 0;
            quantityByProduct[product] = productQuantity;
        }
        quantityOrderedByProduct.each { product, quantity ->
            def productQuantity = quantityByProduct[product];
            if (!productQuantity) productQuantity = 0;
            productQuantity += quantity ?: 0;
            quantityByProduct[product] = productQuantity;
        }
//		quantityRequestedByProduct.each { product, quantity ->
//			def productQuantity = quantityByProduct[product];
//			if (!productQuantity) productQuantity = 0;
//			productQuantity += quantity?:0;
//			quantityByProduct[product] = productQuantity;
//		}
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
     * Get all expired inventory items for the given category and location.
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

        // FIXME poor man's filter
        if (category) {
            expiredStock = expiredStock.findAll { item -> item?.product?.category == category }
        }
        return expiredStock

    }

    /**
     * Get all inventory items that are expiring within the given threshold.
     *
     * @param category the category filter
     * @param threshhold the threshhold filter
     * @return a list of inventory items
     */
    List getExpiringStock(Category category, Location location, Integer threshhold) {
        def today = new Date();

        // Get all stock expiring ever (we'll filter later)
        def expiringStock = InventoryItem.findAllByExpirationDateGreaterThan(today + 1, [sort: 'expirationDate', order: 'asc']);


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
	 * Get all products matching the given terms and categories.
	 * 
	 * @param terms
	 * @param categories
	 * @return
	 */
	List<Product> getProductsByTermsAndCategories(terms, categories, maxResult, offset) {
		//def products = Product.executeQuery("select 
		//"select inventory_item.lot_number from product left join inventory_item on inventory_item.product_id = product.id where product.name like '%lactomer%'"
		
        def products = Product.createCriteria().list() {
			createAlias("inventoryItems", "inventoryItems", CriteriaSpecification.LEFT_JOIN)
			if(categories) {
                inList("category", categories)
            }
            if (terms) {
                or {
					and {
						terms.each {term ->
							ilike('inventoryItems.lotNumber', "%" + term + "%")
						}
					}
                    or {
                        and {
                            terms.each {term ->
                                ilike("name", "%" + term + "%")
                            }
                        }
                        and {
                            terms.each {term ->
                                ilike("description", "%" + term + "%")
                            }
                        }
                        and {
                            terms.each { term ->
                                ilike("manufacturer", "%" + term + "%")
                            }
                        }
                        and {
                            terms.each { term ->
                                ilike("manufacturerCode", "%" + term + "%")
                            }
                        }
                        and {
                            terms.each { term ->
                                ilike("upc", "%" + term + "%")
                            }
                        }
						and {
							terms.each { term ->
								ilike("ndc", "%" + term + "%")
							}
						}
						and {
							terms.each { term ->
								ilike("unitOfMeasure", "%" + term + "%")
							}
						}
                        and {
                            terms.each { term ->
                                ilike("productCode", "%" + term + "%")
                            }
                        }
                    }
                }
            }
        }.collect { it }.unique { it.id }
	
		println "products: " + products
		
		
        return Product.createCriteria().list(max: maxResult, offset: offset) {
            inList("id", (products.collect { it.id })?: [""])
            order("category")
        }
	}
	
	/**
	 * Get all products that have the given tags.
	 * 
	 * @param inputTags
	 * @return
	 */
	def getProductsByTags(List inputTags) {
		println "Get products by tags: " + inputTags
		def products = Product.withCriteria {
			tags {
				'in'('tag', inputTags)
			}
		}
		return products
	}
	
	/**
	 * Get all products that have the given tag.
	 * 
	 * @param tag
	 * @return
	 */
	def getProductsByTag(String tag) {
		def products = Product.withCriteria {
			tags {
				eq('tag', tag)
			}
		}
		return products
	}

    /**
     * Return a list of products that are marked as NON_SUPPORTED or NON_INVENTORIED
     * at the given location.
     *
     * @param location
     * @return
     */
    List getHiddenProducts(Location location) {
        return getProductsByLocationAndStatuses(location, [InventoryStatus.NOT_SUPPORTED, InventoryStatus.SUPPORTED_NON_INVENTORY])
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

    List getProductsByLocationAndStatuses(Location location, List statuses) {
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


    List getProductsByLocationAndStatus(Location location, InventoryStatus status) {
        log.debug("get products by status: " + location)
        def session = sessionFactory.getCurrentSession()
        def products = session.createQuery("select product from InventoryLevel as inventoryLevel \
	   		right outer join inventoryLevel.product as product \
	   		where inventoryLevel.status = :status \
	   		and inventoryLevel.inventory.id = :inventoryId")
                .setParameter("status", status)
                .setParameter("inventoryId", location?.inventory?.id)
                .list()
        return products
    }

    /**
     * Get quantity for the given product and lot number at the given warehouse.
     *
     * @param warehouse
     * @param product
     * @param lotNumber
     * @return
     */
    Integer getQuantity(Location location, Product product, String lotNumber) {

        log.debug("Get quantity for product " + product?.name + " lotNumber " + lotNumber + " at location " + location?.name)
        if (!location) {
            throw new RuntimeException("Your warehouse has not been initialized");
        }
        else {
            location = Location.get(location?.id)
        }
        def inventoryItem = findInventoryItemByProductAndLotNumber(product, lotNumber)
        if (!inventoryItem) {
            throw new RuntimeException("There's no inventory item for product " + product?.name + " lot number " + lotNumber)
        }

        return getQuantity(location.inventory, inventoryItem)
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
    Map<Product, Map<InventoryItem, Integer>> getQuantityByProductAndInventoryItemMap(List<TransactionEntry> entries) {
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
            if (!(reachedProductInventoryTransaction[item.product] && reachedProductInventoryTransaction[item.product] != transaction) &&
                    !(reachedInventoryTransaction[item.product] && reachedInventoryTransaction[item.product][item] && reachedInventoryTransaction[item.product][item] != transaction)) {

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
    Map<Product, Integer> getQuantityByProductMap(List<TransactionEntry> entries) {
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
    Map<Product, Integer> getQuantityByProductMap(Inventory inventory) {
        def transactionEntries = getTransactionEntriesByInventory(inventory);
        return getQuantityByProductMap(transactionEntries)
    }

    /**
     * Get a map of quantities (indexed by product) for a particular inventory.
     *
     * @param inventoryInstance
     * @return
     */
    Map<Product, Integer> getQuantityByProductMap(Inventory inventory, List<Product> products) {
        println "inventory:" + inventory
        println "products:" + products
        def transactionEntries = getTransactionEntriesByInventoryAndProduct(inventory, products);
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
    Map<String, Map<Product, Integer>> getProductsBelowMinimumAndReorderQuantities(Inventory inventoryInstance, Boolean includeUnsupported) {

        def inventoryLevels = getInventoryLevelsByInventory(inventoryInstance)

        Map<Product, Integer> reorderProductsQuantityMap = new HashMap<Product, Integer>()
        Map<Product, Integer> minimumProductsQuantityMap = new HashMap<Product, Integer>()

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
     * Gets the quantity of a specific inventory item at a specific inventory
     *
     * @param item
     * @param inventory
     * @return
     */
    Integer getQuantity(Inventory inventory, InventoryItem inventoryItem) {
		
		if (!inventory) {
			println AuthService.currentLocation
			def currentLocation = AuthService?.currentLocation?.get()
			println currentLocation
			if (!currentLocation?.inventory)
				throw new Exception("Inventory not found")
				
			inventory = currentLocation.inventory
		}
        def transactionEntries = getTransactionEntriesByInventoryAndInventoryItem(inventory, inventoryItem)
        def quantityMap = getQuantityByInventoryItemMap(transactionEntries)

        // inventoryItem -> org.pih.warehouse.inventory.InventoryItem_$$_javassist_10
        //log.debug "inventoryItem -> " + inventoryItem.class

        // FIXME was running into an issue where a proxy object was being used to represent the inventory item
        // so the map.get() method was returning null.  So we needed to fully load the inventory item using
        // the GORM get method.
        inventoryItem = InventoryItem.get(inventoryItem.id)
        Integer quantity = quantityMap[inventoryItem]
        return quantity ?: 0;
    }

    Integer getQuantityAvailableToPromise(Inventory inventory, InventoryItem inventoryItem) {
        def quantityOnHand = getQuantity(inventory, inventoryItem)
        /*
          def shipmentItems = ShipmentItem.findAllByInventoryItem(inventoryItem)
          shipmentItems.findAll { (it?.shipment?.origin == inventory?.warehouse) && it?.shipment?.isPending() }
          def quantityShipping = shipmentItems.sum { it.quantity }
          def picklistItems = PicklistItem.findAllByInventoryItem(inventoryItem)
          picklistItems.findAll { it?.picklist?.request?.origin == inventory?.warehouse }
          def quantityPicked = picklistItems.sum { it.quantity }
          */


        return quantityOnHand;
    }

    /**
     * Get a map of quantity values for all available inventory items in the given inventory.
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
        cmd.productInstance = Product.get(params?.product?.id ?: params.id);  // check product.id and id
        cmd.inventoryInstance = cmd.warehouseInstance?.inventory
        cmd.inventoryLevelInstance = getInventoryLevelByProductAndInventory(cmd.productInstance, cmd.inventoryInstance)

        // Get current stock of a particular product within an inventory
        // Using set to make sure we only return one object per inventory items
        Set inventoryItems = getInventoryItemsByProductAndInventory(cmd.productInstance, cmd.inventoryInstance);
        cmd.inventoryItemList = inventoryItems as List
        cmd.inventoryItemList?.sort { it.expirationDate }?.sort { it.lotNumber }

        // Get all lot numbers for a given product
        cmd.lotNumberList = getInventoryItemsByProduct(cmd?.productInstance) as List

        // Get transaction log for a particular product within an inventory
        cmd.transactionEntryList = getTransactionEntriesByInventoryAndProduct(cmd.inventoryInstance, [cmd.productInstance]);
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
    void populateRecordInventoryCommand(RecordInventoryCommand commandInstance, Map params) {
        log.debug "Params " + params;

        // set the default transaction date to today
        commandInstance.transactionDate = new Date()
        commandInstance.transactionDate.clearTime()

        if (!commandInstance?.productInstance) {
            commandInstance.errors.reject("error.product.invalid", "Product does not exist");
        }
        else {
            commandInstance.recordInventoryRow = new RecordInventoryRowCommand();

            // get all transaction entries for this product at this inventory
            def transactionEntryList = getTransactionEntriesByInventoryAndProduct(commandInstance?.inventoryInstance, [commandInstance?.productInstance])
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
        //return commandInstance
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
                def inventoryItems = getInventoryItemsByProductAndInventory(cmd.productInstance, cmd.inventoryInstance)
                // Create a new transaction
                def transaction = new Transaction(cmd.properties)
                transaction.inventory = cmd.inventoryInstance
                transaction.transactionType = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)

                // Process each row added to the record inventory page
                cmd.recordInventoryRows.each { row ->

                    if (row) {
                        // 1. Find an existing inventory item for the given lot number and product and description
                        def inventoryItem =
                            findInventoryItemByProductAndLotNumber(cmd.productInstance, row.lotNumber)

                        // 2. If the inventory item doesn't exist, we create a new one
                        if (!inventoryItem) {
                            inventoryItem = new InventoryItem();
                            inventoryItem.properties = row.properties
                            inventoryItem.product = cmd.productInstance;
                            if (!inventoryItem.hasErrors() && inventoryItem.save()) {

                            }
                            else {
                                // TODO Old error message = "Property [${error.getField()}] of [${inventoryItem.class.name}] with value [${error.getRejectedValue()}] is invalid"
                                inventoryItem.errors.allErrors.each { error ->
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
                    matchCategories.addAll((category?.children) ? getExplodedCategories(category?.children) : []);
                }
            }
        }
        return matchCategories;
    }

    /**
     * Return a list of categories that match the given search terms.
     *
     * @param searchTerms
     * @return
     */
    List getCategoriesMatchingSearchTerms(List searchTerms) {
        def categories = []
        if (searchTerms) {

            categories = Category.createCriteria().list() {
                or {
                    searchTerms.each { searchTerm ->
                        ilike("name", "%" + searchTerm + "%")
                    }
                }
            }
        }
        return getExplodedCategories(categories)
        //return categories;
    }

    /**
     * Get all products for the given category.
     *
     * @param category
     * @return
     */
    List getProductsByCategory(Category category) {
        def products = Product.createCriteria().list() {
            eq("category", category)
        }
        return products;
    }

    /**
     *
     * @param category
     * @return
     */
    List getProductsByNestedCategory(Category category) {
        def products = [];
        if (category) {
            def categories = (category?.children) ?: [];
            categories << category;
            if (categories) {
                log.debug("get products by nested category: " + category + " -> " + categories)

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
     * @return a map of inventory items by product
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
     * @return a map of inventory items indexed by product
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
        def productsWithEmptyInventoryItem = findInventoryItemWithEmptyLotNumber()?.collect { it.product };
        def productsWithoutEmptyInventoryItem = products - productsWithEmptyInventoryItem;
        return productsWithoutEmptyInventoryItem;
    }

    /**
     * Finds the inventory item for the given product and lot number.
     *
     * @param product the product of the desired inventory item
     * @param lotNumber the lot number of the desired inventory item
     * @return a single inventory item
     */
    InventoryItem findInventoryItemByProductAndLotNumber(Product product, String lotNumber) {
        log.debug("Find inventory item by product " + product?.id + " and lot number '" + lotNumber + "'")
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
        log.debug("Returned inventory items " + inventoryItems);
        // If the list is non-empty, return the first item
        if (inventoryItems) {
            return inventoryItems.get(0);
        }
        return null;
    }

    /**
     * Uses an inventory item that is bound at the controller.
     *
     * @param inventoryItem
     * @return
     */
    InventoryItem findOrCreateInventoryItem(InventoryItem inventoryItem) {
        return findOrCreateInventoryItem(inventoryItem.product, inventoryItem.lotNumber, inventoryItem.expirationDate)
    }

    /**
     * TODO Need to finish this method.
     *
     * @param product
     * @param lotNumber
     * @param expirationDate
     * @return
     */
    InventoryItem findOrCreateInventoryItem(Product product, String lotNumber, Date expirationDate) {
        def inventoryItem =
            findInventoryItemByProductAndLotNumber(product, lotNumber);

        // If the inventory item doesn't exist, we create a new one
        if (!inventoryItem) {
            inventoryItem = new InventoryItem();
            inventoryItem.lotNumber = lotNumber
            inventoryItem.expirationDate = expirationDate;
            inventoryItem.product = product
            inventoryItem.save()
        }


        return inventoryItem

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
        def transactionEntries = getTransactionEntriesByInventoryAndProduct(inventoryInstance, [productInstance]);
        transactionEntries.each {
            inventoryItems << it.inventoryItem;
        }
        inventoryItems = inventoryItems.sort { it.expirationDate }.sort { it.lotNumber }
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

        return (inventoryLevel) ?: new InventoryLevel();

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
     * Get all transaction entries over list of products/inventory items.
     *
     * @param inventoryInstance
     * @return
     */
    List getTransactionEntriesByInventoryAndProduct(Inventory inventory, List<Product> products) {
        def criteria = TransactionEntry.createCriteria();
        def transactionEntries = criteria.list {
            transaction {
                eq("inventory", inventory)
                order("transactionDate", "asc")
                order("dateCreated", "asc")
            }
            if (products) {
                inventoryItem {
                    inList("product", products)
                }
            }
        }
        return transactionEntries;
    }

    /**
     * Gets all transaction entries for a inventory item within an inventory
     *
     * @param inventoryItem
     * @param inventory
     */
    List getTransactionEntriesByInventoryAndInventoryItem(Inventory inventory, InventoryItem inventoryItem) {
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
            throw new RuntimeException("Can't create send shipment transaction for origin that is not a depot")
        }

        try {
            // Create a new transaction for outgoing items
            Transaction debitTransaction = new Transaction();
            debitTransaction.transactionType = TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID)
            debitTransaction.source = null
            //debitTransaction.destination = shipmentInstance?.destination.isWarehouse() ? shipmentInstance?.destination : null
            debitTransaction.destination = shipmentInstance?.destination
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
                        inventoryItem.errors.allErrors.each { error ->
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
                log.debug "debit transaction errors " + debitTransaction.errors
                throw new TransactionException(message: "An error occurred while saving ${debitTransaction?.transactionType?.transactionCode} transaction", transaction: debitTransaction);
            }

            // Associate the incoming transaction with the shipment
            shipmentInstance.addToOutgoingTransactions(debitTransaction)
            shipmentInstance.save();

        } catch (Exception e) {
            log.error("An error occrred while creating transaction ", e);
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
        if (transaction?.transactionType?.id != Constants.TRANSFER_IN_TRANSACTION_TYPE_ID &&
                transaction?.transactionType?.id != Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
            return false
        }

        // make sure we are operating only on locally managed warehouses
        if (transaction?.source) {
            if (!(transaction?.source instanceof Location)) {   //todo: should use source.isWarehouse()? hibernate always set source to a location
                return false
            }
            else if (!transaction?.source.local) {
                return false
            }
        }
        if (transaction?.destination) {
            if (!(transaction?.destination instanceof Location)) { //todo: should use destination.isWarehouse()? hibernate always set destination to a location
                return false
            }
            else if (!transaction?.destination.local) {
                return false
            }
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
            transfer.delete(flush: true)
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
        if (!baseTransaction.save(flush: true)) {
            throw new RuntimeException("Unable to save base transaction " + baseTransaction?.id)
        }

        // try to fetch any existing local transfer
        LocalTransfer transfer = getLocalTransfer(baseTransaction)

        // if there is no existing local transfer, we need to create a new one and set the source or destination transaction as appropriate
        if (!transfer) {
            transfer = new LocalTransfer()
            if (baseTransaction.transactionType.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
                transfer.sourceTransaction = baseTransaction
            }
            else {
                transfer.destinationTransaction = baseTransaction
            }
        }

        // create and save the new mirrored transaction
        Transaction mirroredTransaction = createMirroredTransaction(baseTransaction)
        if (!mirroredTransaction.save(flush: true)) {
            throw new RuntimeException("Unable to save mirrored transaction " + mirroredTransaction?.id)
        }

        // now assign this mirrored transaction to the local transfer
        Transaction oldTransaction
        if (baseTransaction.transactionType.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
            oldTransaction = transfer.destinationTransaction
            transfer.destinationTransaction = mirroredTransaction
        }
        else {
            oldTransaction = transfer.sourceTransaction
            transfer.sourceTransaction = mirroredTransaction
        }

        // save the local transfer
        if (!transfer.save(flush: true)) {
            throw new RuntimeException("Unable to save local transfer " + transfer?.id)
        }

        // delete the old transaction
        if (oldTransaction) {
            oldTransaction.delete(flush: true)
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
        log.debug("startDate = " + startDate + " endDate = " + endDate)
        def criteria = Consumption.createCriteria()
        def results = criteria.list {
            if (startDate && endDate) {
                between('transactionDate', startDate, endDate)
            }
        }

        return results
    }

    /**
     *
     * @return
     */
    def getConsumptions(Date startDate, Date endDate, String groupBy) {
        log.debug("startDate = " + startDate + " endDate = " + endDate)
        def criteria = Consumption.createCriteria()
        def results = criteria.list {
            if (startDate && endDate) {
                between('transactionDate', startDate, endDate)
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
     */
    def getQuantity(Product product, Location location, Date beforeDate) {
        def quantity = 0;
        def transactionEntries = getTransactionEntriesBeforeDate(product, location, beforeDate)
        quantity = adjustQuantity(quantity, transactionEntries)
        return quantity;
    }


    def getQuantity(InventoryItem inventoryItem, Location location, Date beforeDate) {
        def quantity = 0;
        def transactionEntries = getTransactionEntriesBeforeDate(inventoryItem, location, beforeDate)
        quantity = adjustQuantity(quantity, transactionEntries)
        return quantity
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
        return getQuantity(product, location, date ?: new Date());
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
        return getQuantity(product, location, date ?: new Date());
    }

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
    def getTransactionEntriesBeforeDate(InventoryItem inventoryItem, Location location, Date beforeDate) {
        def transactionEntries = []
        if (beforeDate) {
            def criteria = TransactionEntry.createCriteria();
            transactionEntries = criteria.list {
                and {
                    eq("inventoryItem", inventoryItem)
                    transaction {
                        // All transactions before given date
                        lt("transactionDate", beforeDate)
                        eq("inventory", location?.inventory)
                        order("transactionDate", "asc")
                        order("dateCreated", "asc")
                    }
                }
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
                inventoryItem { eq("product", product) }
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
     * @param location
     * @param category
     * @param startDate
     * @param endDate
     * @return
     */
    def getTransactionEntries(Location location, Category category, Date startDate, Date endDate) {
        def categories = []
        categories << category
        def matchCategories = getExplodedCategories(categories)
        return getTransactionEntries(location, matchCategories, startDate, endDate)

    }

    def getTransactionEntries(Location location, List categories, Date startDate, Date endDate) {
        def criteria = TransactionEntry.createCriteria();
        def transactionEntries = criteria.list {
            if (categories) {
                inventoryItem {
                    product {
                        'in'("category", categories)
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

    public void validateData(ImportDataCommand command) {
        processData(command);
    }


    public void processData(ImportDataCommand command) {
        Date today = new Date()
        today.clearTime()
        def transactionInstance = new Transaction(transactionDate: today,
                transactionType: TransactionType.findById(Constants.INVENTORY_TRANSACTION_TYPE_ID),
                inventory: command?.location?.inventory)

        command.transaction = transactionInstance

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
                        name: params.productDescription,
                        upc: params.upc,
                        ndc: params.ndc,
                        category: category,
                        manufacturer: params.manufacturer,
                        manufacturerCode: params.manufacturerCode,
                        unitOfMeasure: params.unitOfMeasure,
                        coldChain: Boolean.valueOf(params.coldChain));

                if (!product.validate()) {
                    product.errors.allErrors.each {
                        command.errors.addError(it);
                    }
                }
                else {
                    command.products << product
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
                else {
                    command.inventoryItems << inventoryItem
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

        def quantity = params.quantity ?: 0;
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

                println params

                def lotNumber = (params.lotNumber) ? String.valueOf(params.lotNumber) : null;
                def quantity = (params.quantity) ?: 0;

                def unitOfMeasure = params.unitOfMeasure;
                def manufacturer = (params.manufacturer) ? String.valueOf(params.manufacturer) : null;
                def manufacturerCode = (params.manufacturerCode) ? String.valueOf(params.manufacturerCode) : null;
                def upc = (params.upc) ? String.valueOf(params.upc) : null;
                def ndc = (params.ndc) ? String.valueOf(params.ndc) : null;

                def expirationDate = ImporterUtil.parseDate(params.expirationDate, command.errors);

                def category = ImporterUtil.findOrCreateCategory(params.category, command.errors)
                category.save();
                if (!category) {
                    throw new ValidationException("error finding/creating category")
                }
                log.debug "Creating product " + params.productDescription + " under category " + category
                // Create product if not exists
                Product product = Product.findByName(params.productDescription);
                if (!product) {
                    product = new Product(
                            name: params.productDescription,
                            upc: upc,
                            ndc: ndc,
                            category: category,
                            manufacturer: manufacturer,
                            manufacturerCode: manufacturerCode,
                            unitOfMeasure: unitOfMeasure,
                            coldChain: Boolean.valueOf(params.coldChain));

                    if (product.hasErrors() || !product.save()) {
                        command.errors.reject("Error saving product " + product?.name)

                    }
                    log.debug "Created new product " + product.name;
                }

                // Find the inventory item by product and lotNumber and description
                InventoryItem inventoryItem =
                    findInventoryItemByProductAndLotNumber(product, lotNumber);

                log.debug("Inventory item " + inventoryItem)
                // Create inventory item if not exists
                if (!inventoryItem) {
                    inventoryItem = new InventoryItem()
                    inventoryItem.product = product
                    inventoryItem.lotNumber = lotNumber;
                    inventoryItem.expirationDate = expirationDate;
                    if (inventoryItem.hasErrors() || !inventoryItem.save()) {
                        inventoryItem.errors.allErrors.each {
                            log.error "ERROR " + it;
                            command.errors.addError(it);
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

            // Only save the transaction if there are transaction entries and there are no errors
            if (transactionInstance?.transactionEntries && !command.hasErrors()) {
                if (!transactionInstance.save()) {
                    transactionInstance.errors.allErrors.each {
                        command.errors.addError(it);
                    }
                }
            }
        } catch (Exception e) {
            // Bad practice but need this for testing
            log.error("Error importing inventory", e);
            throw e;
        }

    }

    public Map<String, Integer> getQuantityForProducts(Inventory inventory, ArrayList<String> productIds) {
        def ids = productIds.collect{ "'${it}'"}.join(",")
        def sql = "select te from TransactionEntry as te where te.transaction.inventory.id='${inventory.id}' and te.inventoryItem.product.id in (${ids})"
        def transactionEntries = TransactionEntry.executeQuery(sql)
        def result =[:]
        transactionEntries.each{ println(it)}
        def map = getQuantityByProductMap(transactionEntries)
        map.keySet().each{ result[it.id] = map[it] }
        result
    }

    public Map<Product, List<InventoryItem>> getInventoryItemsWithQuantity(List<Product> products, Inventory inventory) {
        def transactionEntries = TransactionEntry.createCriteria().list() {
            transaction {
                eq("inventory", inventory)
            }
            inventoryItem {
                'in'("product", products)
            }
        }
        def map = getQuantityByProductAndInventoryItemMap(transactionEntries)
        def result = [:]
        map.keySet().each{ product ->
            def valueMap = map[product]

            def inventoryItems = valueMap.keySet().collect { item ->
                item.quantity = valueMap[item]
                item
            }
            inventoryItems.sort{ it.expirationDate}
            result[product]  = inventoryItems
        }
        result
    }

}
