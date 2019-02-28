/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import grails.orm.PagedResultList
import grails.validation.ValidationException
import groovy.sql.Sql
import groovyx.gpars.GParsPool
import org.apache.commons.lang.StringUtils
import org.grails.plugins.csv.CSVWriter
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.LocalDate
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.importer.ImporterUtil
import org.pih.warehouse.importer.InventoryExcelImporter
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductException
import org.pih.warehouse.product.ProductGroup
import org.pih.warehouse.reporting.Consumption
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.validation.Errors

import java.sql.BatchUpdateException
import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat

class InventoryService implements ApplicationContextAware {

	def dataSource
    def sessionFactory
    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
    def startTime = System.currentTimeMillis()
    def lastBatchStarted = startTime
    def persistenceInterceptor

    def dataService
	def productService
	def identifierService
    def messageService
	def locationService
	//def authService

	ApplicationContext applicationContext

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
	void saveLocation(Location location) {

		// make sure a warehouse has an inventory
		if (!location.inventory) {
			addInventory(location)
		}

        // Should not allow user to disable a bin location that has items in it
        if (!location.active && location.parentLocation) {
			List binLocationEntries = getQuantityByBinLocation(location.parentLocation, location)
            if (!binLocationEntries.isEmpty()) {
                location.errors.reject("location.cannotDisableBinLocationWithStock.message")
                throw new ValidationException("cannot save location", location.errors)
            }
        }

        location.save()
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

        def results = InventoryItem.withCriteria {
            createAlias('product', 'p', CriteriaSpecification.LEFT_JOIN)
            or {
                ilike("lotNumber", "%" + searchTerm + "%")
                ilike("p.name", "%" + searchTerm + "%")
                ilike("p.productCode", "%" + searchTerm + "%")
            }
        }
        return results
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

		getCurrentInventory(commandInstance);

		return commandInstance;
	}

	/**
	 *
	 * @param commandInstance
	 * @return
	 */
	Map getCurrentInventory(InventoryCommand commandInstance) {

        long initialStartTime = System.currentTimeMillis()
		long startTime = System.currentTimeMillis()
		log.debug "getCurrentInventory()"
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
		log.info "command.tag  = " + commandInstance.tags
		log.info "command.catalog  = " + commandInstance.catalogs

		def products = []

        // User wants to view all products that match the given tag
		if (commandInstance.tags) {
			commandInstance.numResults = countProductsByTags(commandInstance.tags)
			products = getProductsByTags(commandInstance.tags, commandInstance?.maxResults as int, commandInstance?.offset as int)
		}

		// User wants to view all products that match the given catalog
		else if (commandInstance.catalogs) {
			commandInstance.numResults = countProductsByCatalogs(commandInstance.catalogs)
			products = getProductsByCatalogs(commandInstance.catalogs, commandInstance?.maxResults as int, commandInstance?.offset as int)
		}

        // User wants to view all products in the given shipment
        else if (commandInstance.shipment) {
            commandInstance.numResults = countProductsByShipment(commandInstance.shipment)
            products = getProductsByShipment(commandInstance.shipment, commandInstance?.maxResults as int, commandInstance?.offset as int)
        }
        else {
			// Get all products, including hidden ones
			def matchCategories = getExplodedCategories(categories)
            log.info " * Get all categories: " + (System.currentTimeMillis() - startTime) + " ms"
			startTime = System.currentTimeMillis()

			products = getProductsByTermsAndCategories(searchTerms, matchCategories, commandInstance?.showHiddenProducts, commandInstance?.warehouseInstance.inventory, commandInstance?.maxResults, commandInstance?.offset)
            log.info " * Get products by terms and categories: " + (System.currentTimeMillis() - startTime) + " ms"
			startTime = System.currentTimeMillis()

			commandInstance.numResults = products.totalCount

			if (!commandInstance?.showHiddenProducts) {
				products.removeAll(getHiddenProducts(commandInstance?.warehouseInstance))
			}
            log.info " * After removing all hidden products: " + (System.currentTimeMillis() - startTime) + " ms"
			startTime = System.currentTimeMillis()

		}
		products = products?.sort() { map1, map2 -> map1.category <=> map2.category ?: map1.name <=> map2.name };
        log.info "Sort products " + (System.currentTimeMillis() - startTime) + " ms"

		def inventoryLevelMap = InventoryLevel.findAllByInventory(commandInstance?.warehouseInstance?.inventory)?.groupBy { it.product }
        log.debug "Get inventory level map: " + (System.currentTimeMillis() - startTime) + " ms"
		startTime = System.currentTimeMillis()

        log.info "Products: " + products

		products.each { product ->
			def innerStartTime = System.currentTimeMillis()
			def inventoryLevel = (inventoryLevelMap[product])?inventoryLevelMap[product][0]:null
			if (inventoryLevel && inventoryLevel instanceof ArrayList) {
				throw new Exception("Cannot have multiple inventory levels for a single product [" + product.productCode + ":" + product.name + "]: " + inventoryLevel)
			}
            inventoryItemCommands << getInventoryItemCommand(product,
                    commandInstance?.warehouseInstance?.inventory,
                    inventoryLevel, 0, 0, 0, commandInstance?.showOutOfStockProducts)
            log.info " * process product : " + (System.currentTimeMillis() - innerStartTime) + " ms"
		}
        log.info " * process on hand quantity: " + (System.currentTimeMillis() - startTime) + " ms"
		startTime = System.currentTimeMillis()


		commandInstance?.categoryToProductMap = getProductMap(inventoryItemCommands);

        log.info " * Get category to product map: " + (System.currentTimeMillis() - startTime) + " ms"

        log.info "Total time - Get current inventory: " + (System.currentTimeMillis() - initialStartTime) + " ms"
		return commandInstance?.categoryToProductMap
	}


	InventoryItemCommand getInventoryItemCommand(Product product, Inventory inventory, InventoryLevel inventoryLevel, Integer quantityOnHand, Integer quantityToReceive, Integer quantityToShip, Boolean showOutOfStockProducts) {
		InventoryItemCommand inventoryItemCommand = new InventoryItemCommand();

		//def startTime = System.currentTimeMillis()
		//def inventoryLevel = InventoryLevel.findByProductAndInventory(product, inventory)
		//log.debug " * find inventory level by product and inventory: " + (System.currentTimeMillis() - startTime) + " ms"
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
		inventoryItemCommand.description = productGroup.name
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

		productGroups = productGroups?.sort() { it?.name };
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
		Map quantityShippedByProduct = shipmentService.getOutgoingQuantityByProduct(location, products);
		Map quantityOrderedByProduct = orderService.getOutgoingQuantityByProduct(location, products)
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

		//log.debug "getQuantityByInventoryAndProduct: " + quantityMap

		inventoryItems.each {
			def quantity = quantityMap[it]
			if (quantity) {
				inventoryItemQuantity[it] = quantity
			}
		}
		return inventoryItemQuantity;
	}


    def getExpirationSummary(location) {
        def expirationSummary = [:]
        def expirationAlerts = getExpirationAlerts(location)
        expirationAlerts.groupBy { it?.inventoryItem?.expires }.each { key, value ->
            expirationSummary[key] = value.size()

        }
        return expirationSummary
    }

    def getExpirationAlerts(location) {
        def startTime = System.currentTimeMillis()

        def expirationAlerts = []
        def today = new Date()
        def quantityMap = getQuantityOnHandByInventoryItem(location)

        quantityMap.each { key, value ->
            if (value > 0) {
                def daysToExpiry = key.expirationDate ? (key.expirationDate - today) : null
                expirationAlerts << [ id:  key.id, lotNumber: key.lotNumber, quantity: value,
                    expirationDate: key.expirationDate, daysToExpiry: daysToExpiry,
                    product: key.product.toJson(), inventoryItem: key.toJson()
                ]
            }
        }

        log.info "Expiration alerts: " + (System.currentTimeMillis() - startTime) + " ms"

        return expirationAlerts

    }


	def getInventoryItemSnapshot(Location location, Integer daysToExpiry) {
        def results = InventoryItemSnapshot.executeQuery("""
            SELECT a.inventoryItem, a.quantityOnHand, DATEDIFF(a.inventoryItem.expirationDate, current_date) as daysToExpiry
            FROM InventoryItemSnapshot a
            WHERE a.date = (select max(b.date) from InventoryItemSnapshot b)
            AND a.location = :location
            AND a.quantityOnHand > 0
            AND DATEDIFF(a.inventoryItem.expirationDate, current_date) <= :daysToExpiry
            ORDER BY a.inventoryItem.expirationDate ASC
            """, [location:location, daysToExpiry:daysToExpiry])
        return results
	}


	/**
	 * Get all expired inventory items for the given category and location.
	 *
	 * @param category
	 * @param location
	 * @return
	 */
	List getExpiredStock(Category category, Location location) {

		long startTime = System.currentTimeMillis()

		// Stock that has already expired
		def expiredStock = InventoryItem.findAllByExpirationDateLessThan(new Date(), [sort: 'expirationDate', order: 'desc']);

		log.debug expiredStock

		Map<InventoryItem, Integer> quantityMap = getQuantityByLocation(location)
		expiredStock = expiredStock.findAll { quantityMap[it] > 0 }

		// FIXME poor man's filter
		if (category) {
			expiredStock = expiredStock.findAll { item -> item?.product?.category == category }
		}

        log.debug "Get expired stock: " + (System.currentTimeMillis() - startTime) + " ms"
		return expiredStock

	}

	/**
	 * Get all inventory items that are expiring within the given threshold.
	 *
	 * @param category the category filter
	 * @param threshold the threshold filter
	 * @return a list of inventory items
	 */
	List getExpiringStock(Category category, Location location, Integer threshold) {
		long startTime = System.currentTimeMillis()

		def today = new Date();

		// Get all stock expiring ever (we'll filter later)
		def expiringStock = InventoryItem.findAllByExpirationDateGreaterThan(today + 1, [sort: 'expirationDate', order: 'asc']);
		def quantityMap = getQuantityByLocation(location)
		expiringStock = expiringStock.findAll { quantityMap[it] > 0 }
		if (category) {
			expiringStock = expiringStock.findAll { item -> item?.product?.category == category }
		}

		if (threshold) {
			expiringStock = expiringStock.findAll { item -> (item?.expirationDate && (item?.expirationDate - today) <= threshold) }
		}
        log.debug "Get expiring stock: " + (System.currentTimeMillis() - startTime) + " ms"
		return expiringStock
	}


    def getReconditionedStock(Location location) {
        long startTime = System.currentTimeMillis()
        def quantityMap = getCurrentInventory(location)
        def reconditionedStock = quantityMap.findAll { it.key.reconditioned }
        log.debug "Get reconditioned stock: " + (System.currentTimeMillis() - startTime) + " ms"
        return reconditionedStock
    }


    def getTotalStock(Location location) {
        long startTime = System.currentTimeMillis()
        def quantityMap = getCurrentInventory(location)
        log.debug "Get total stock: " + (System.currentTimeMillis() - startTime) + " ms"
        return quantityMap
    }

    def getInStock(Location location) {
        long startTime = System.currentTimeMillis()
        def quantityMap = getCurrentInventory(location)
        def inStock = quantityMap.findAll { it.value > 0 }
        log.debug "Get in stock: " + (System.currentTimeMillis() - startTime) + " ms"
        return inStock
    }


    def getTotalStockValue(Location location) {
        def hitCount = 0;
        def missCount = 0;
        def totalCount = 0;
        def totalStockValue = 0.0
		def stockValueByProduct = [:]
        if (location.inventory) {
            def quantityMap = getCurrentInventory(location)
            quantityMap.each { product, quantity ->
                if (product.pricePerUnit) {
                    def stockValueForProduct = product.pricePerUnit * quantity
					if (stockValueForProduct > 0) {
						stockValueByProduct[product] = stockValueForProduct
						totalStockValue += stockValueForProduct
					}
                    hitCount++
                }
                else {
                    missCount++
                }
            }
            totalCount = quantityMap?.keySet()?.size()
        }
        return [totalStockValue:totalStockValue, hitCount: hitCount, missCount: missCount, totalCount:totalCount, stockValueByProduct:stockValueByProduct]

    }


    def getDashboardAlerts(Location location) {
		log.info "Dashboard alerts for ${location}"

        long startTime = System.currentTimeMillis()
        def quantityMap = getCurrentInventory(location)
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }
        log.info inventoryLevelMap.keySet().size()

        def totalStock = quantityMap
        def reconditionedStock = quantityMap.findAll { it.key.reconditioned }
        def onHandQuantityZero = quantityMap.findAll { it.value <= 0 }
        def inStock = quantityMap.findAll { it.value > 0 }

        //def lowStock = quantityMap.findAll { it.value <= it?.key?.getInventoryLevel(location?.id)?.minQuantity }
        def outOfStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0
        }

        def lowStock = quantityMap.findAll { product,quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def minQuantity = inventoryLevelMap[product]?.first()?.minQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && minQuantity && quantity > 0 && quantity <= minQuantity
        }

        def reorderStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def reorderQuantity = inventoryLevelMap[product]?.first()?.reorderQuantity
            def minQuantity = inventoryLevelMap[product]?.first()?.minQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && reorderQuantity && minQuantity > 0 && quantity <= reorderQuantity
        }

        def healthyStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def reorderQuantity = inventoryLevelMap[product]?.first()?.reorderQuantity
            def maxQuantity = inventoryLevelMap[product]?.first()?.maxQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity > reorderQuantity && quantity <= maxQuantity
        }


        def overStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def maxQuantity = inventoryLevelMap[product]?.first()?.maxQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && maxQuantity && quantity > maxQuantity
        }

        def outOfStockClassA = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0 && inventoryLevel?.abcClass == "A"
        }

        def outOfStockClassB = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0 && inventoryLevel?.abcClass == "B"
        }

        def outOfStockClassC = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0 && inventoryLevel?.abcClass == "C"
        }

        def outOfStockClassNone = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0 && inventoryLevel?.abcClass == null
        }



        //println lowStock.keySet().size()
        log.debug "Get low stock: " + (System.currentTimeMillis() - startTime) + " ms"
        //return lowStock

        [   lowStock: lowStock.keySet().size(),
                lowStockCost: getTotalCost(lowStock),
            reorderStock: reorderStock.keySet().size(),
                reorderStockCost: getTotalCost(reorderStock),
            overStock: overStock.keySet().size(),
                overStockCost: getTotalCost(overStock),
            totalStock: totalStock.keySet().size(),
                totalStockCost: getTotalCost(totalStock),
            reconditionedStock: reconditionedStock.keySet().size(),
                reconditionedStockCost: getTotalCost(reconditionedStock),
            healthyStock: healthyStock.keySet().size(),
                healthyStockCost: getTotalCost(healthyStock),
            outOfStock:outOfStock.keySet().size(),
                outOfStockCost: getTotalCost(outOfStock),
            outOfStockClassA:outOfStockClassA.keySet().size(),
                outOfStockCostClassA: getTotalCost(outOfStockClassA),
            outOfStockClassB:outOfStockClassB.keySet().size(),
                outOfStockCostClassB: getTotalCost(outOfStockClassB),
            outOfStockClassC:outOfStockClassC.keySet().size(),
                outOfStockCostClassC: getTotalCost(outOfStockClassC),
            outOfStockClassNone:outOfStockClassNone.keySet().size(),
                outOfStockCostClassNone: getTotalCost(outOfStockClassNone),

            onHandQuantityZero:onHandQuantityZero.keySet().size(),
                onHandQuantityZeroCost: getTotalCost(onHandQuantityZero),
            inStock:inStock.keySet().size(),
                inStockCost: getTotalCost(inStock),
        ]
    }


    def getTotalCost(quantityMap) {
        def totalCost = 0;
        quantityMap.each { k,v ->
            totalCost += k.pricePerUnit?:0 * v?:0
        }
        return totalCost;
    }

    def getInventoryStatus(Location location) {
        def quantityMap = getCurrentInventory(location)
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }
        def inventoryStatusMap = [:]
        quantityMap.each { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            inventoryStatusMap[product] = inventoryLevel?.statusMessage(quantity)?:"${inventoryLevel?.id}"

        }
        return inventoryStatusMap
    }

    /**
     * Get inventory status, inventory level and quantity on hand for all products.
     *
     * @param location
     * @return
     */
    def getInventoryStatusAndLevel(Location location) {
        def quantityMap = getCurrentInventory(location)
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }
        def inventoryStatusMap = [:]
        quantityMap.each { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def status = inventoryLevel?.statusMessage(quantity)?:"NONE"
            inventoryStatusMap[product] = [inventoryLevel:inventoryLevel,status:status,onHandQuantity:quantity]
        }
        return inventoryStatusMap
    }

    /**
     * Get inventory status for a single inventory level
     *
     * @param inventoryLevel
     * @param quantity
     * @return
     */
    /*
    def getInventoryStatus(inventoryLevel, quantity) {
        def status = ""
        if (inventoryLevel?.status >= InventoryStatus.SUPPORTED  || !inventoryLevel?.status) {
            if (quantity <= 0) {
                status = "STOCK_OUT"
            }
            else if (inventoryLevel?.minQuantity && quantity <= inventoryLevel?.minQuantity) {
                status = "LOW_STOCK"
            }
            else if (inventoryLevel?.reorderQuantity && quantity <= inventoryLevel?.reorderQuantity ) {
                status = "REORDER"
            }
            else if (inventoryLevel?.maxQuantity && quantity > inventoryLevel?.maxQuantity && inventoryLevel?.maxQuantity > 0) {
                status = "OVERSTOCK"
            }
            else {
                status = "IN_STOCK"
            }
        }
        else if (inventoryLevel?.status == InventoryStatus.NOT_SUPPORTED) {
            status = "NOT_SUPPORTED"
        }
        else if (inventoryLevel?.status == InventoryStatus.SUPPORTED_NON_INVENTORY) {
            status = "SUPPORTED_NON_INVENTORY"
        }
        else {
            status = "UNAVAILABLE"
        }
        return status
    }*/


    def getQuantityOnHandZero(Location location) {
        long startTime = System.currentTimeMillis()
        def quantityMap = getCurrentInventory(location)
        //def stockOut = quantityMap.findAll { it.value <= 0 }

        //def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }
        def stockOut = quantityMap.findAll { product, quantity ->
            //def inventoryLevel = inventoryLevelMap[product]?.first()
            quantity <= 0
        }

        log.info "Get quantity on hand zero: " + (System.currentTimeMillis() - startTime) + " ms"
        return stockOut

    }

    def getOutOfStock(Location location, String abcClass) {
        long startTime = System.currentTimeMillis()
        def quantityMap = getCurrentInventory(location)
        //def stockOut = quantityMap.findAll { it.value <= 0 }

        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }
        def stockOut = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            if (abcClass)
                inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0 && (abcClass == inventoryLevel.abcClass)
            else
                inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0
        }

        log.info "Get stock out: " + (System.currentTimeMillis() - startTime) + " ms"
        return stockOut
    }


    def getLowStock(Location location) {
		long startTime = System.currentTimeMillis()
		def quantityMap = getCurrentInventory(location)
        log.info ("getQuantityByProductMap: " + (System.currentTimeMillis() - startTime) + " ms")
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }
        log.info ("getInventoryLevelMap: " + (System.currentTimeMillis() - startTime) + " ms")
        log.info inventoryLevelMap.keySet().size()
		//def lowStock = quantityMap.findAll { it.value <= it?.key?.getInventoryLevel(location?.id)?.minQuantity }
        def lowStock = quantityMap.findAll { product,quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def minQuantity = inventoryLevelMap[product]?.first()?.minQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && minQuantity && quantity <= minQuantity
        }
        log.info "Get low stock: " + (System.currentTimeMillis() - startTime) + " ms"
		return lowStock
	}

	def getReorderStock(Location location) {
		long startTime = System.currentTimeMillis()
		def quantityMap = getCurrentInventory(location)
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }
		def reorderStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def reorderQuantity = inventoryLevelMap[product]?.first()?.reorderQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && reorderQuantity && quantity <= reorderQuantity
        }
        log.info "Get reorder stock: " + (System.currentTimeMillis() - startTime) + " ms"
		return reorderStock
	}

    def getOverStock(Location location) {
        long startTime = System.currentTimeMillis()
        def quantityMap = getCurrentInventory(location)
        //def overStock = quantityMap.findAll { it.value > it?.key?.getInventoryLevel(location?.id)?.maxQuantity }
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }
        def overStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def maxQuantity = inventoryLevelMap[product]?.first()?.maxQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && maxQuantity && quantity > maxQuantity
        }
        log.info "Get over stock: " + (System.currentTimeMillis() - startTime) + " ms"
        return overStock
    }

    def getHealthyStock(Location location) {
        long startTime = System.currentTimeMillis()
        def quantityMap = getCurrentInventory(location)
        //def overStock = quantityMap.findAll { it.value > it?.key?.getInventoryLevel(location?.id)?.maxQuantity }
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }


        def healthyStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def reorderQuantity = inventoryLevelMap[product]?.first()?.reorderQuantity
            def maxQuantity = inventoryLevelMap[product]?.first()?.maxQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity > reorderQuantity && quantity <= maxQuantity
        }
        log.info "Get healthy stock: " + (System.currentTimeMillis() - startTime) + " ms"
        return healthyStock
    }



    /**
     * Get all products matching the given terms and categories.
     *
     * @param terms
     * @param categories
     * @return
     */
    List<Product> searchProducts(String[] terms, List<Category> categories) {
        def startTime = System.currentTimeMillis()
        def products = Product.createCriteria().listDistinct {
            createAlias('productSuppliers', 'ps', CriteriaSpecification.LEFT_JOIN)
            createAlias('inventoryItems', 'ii', CriteriaSpecification.LEFT_JOIN)

            eq("active", true)
            if(categories) {
                inList("category", categories)
            }
            if (terms) {
                and {
                    terms.each { term ->
                        or {
                            ilike("name", "%" + term + "%")
                            ilike("description", "%" + term + "%")
                            ilike("brandName", "%" +term + "%")
                            ilike("manufacturer", "%" +term + "%")
                            ilike("manufacturerCode", "%" +term + "%")
                            ilike("manufacturerName", "%" + term + "%")
                            ilike("vendor", "%" + term + "%")
                            ilike("vendorCode", "%" + term + "%")
                            ilike("vendorName", "%" + term + "%")
                            ilike("upc", "%" + term + "%")
                            ilike("ndc", "%" + term + "%")
                            ilike("unitOfMeasure", "%" + term + "%")
                            ilike("productCode", "%" + term + "%")
                            ilike("ps.name", "%" + term + "%")
                            ilike("ps.code", "%" + term + "%")
                            ilike("ps.productCode", "%" + term + "%")
                            ilike("ps.manufacturerCode", "%" + term + "%")
                            ilike("ps.manufacturerName", "%" + term + "%")
                            ilike("ps.supplierCode", "%" + term + "%")
                            ilike("ps.supplierName", "%" + term + "%")
                            ilike("ii.lotNumber", "%" + term + "%")
                        }
                    }
                }
            }
            order("name", "asc")
        }
        log.info "Query for products: " + (System.currentTimeMillis() - startTime) + " ms"

        return products;
    }


	/**
	 * Get all products matching the given terms and categories.
	 *
	 * @param terms
	 * @param categories
	 * @return
	 */
	List<Product> getProductsByTermsAndCategories(terms, categories, showHidden, currentInventory, maxResults, offset) {
		def startTime = System.currentTimeMillis()
        def products = Product.createCriteria().list(max: maxResults, offset: offset) {
            createAlias('productSuppliers', 'ps', CriteriaSpecification.LEFT_JOIN)
            createAlias('inventoryItems', 'ii', CriteriaSpecification.LEFT_JOIN)

			eq("active", true)
            if(categories) {
                inList("category", categories)
            }
            if (terms) {
                and {
                    terms.each { term ->
                        or {
                            ilike("name", "%" + term + "%")
                            ilike("description", "%" + term + "%")
                            ilike("brandName", "%" +term + "%")
                            ilike("manufacturer", "%" +term + "%")
                            ilike("manufacturerCode", "%" +term + "%")
                            ilike("manufacturerName", "%" + term + "%")
                            ilike("vendor", "%" + term + "%")
                            ilike("vendorCode", "%" + term + "%")
                            ilike("vendorName", "%" + term + "%")
                            ilike("upc", "%" + term + "%")
                            ilike("ndc", "%" + term + "%")
                            ilike("unitOfMeasure", "%" + term + "%")
                            ilike("productCode", "%" + term + "%")
                            ilike("ps.name", "%" + term + "%")
							ilike("ps.code", "%" + term + "%")
							ilike("ps.productCode", "%" + term + "%")
                            ilike("ps.manufacturerCode", "%" + term + "%")
                            ilike("ps.manufacturerName", "%" + term + "%")
                            ilike("ps.supplierCode", "%" + term + "%")
                            ilike("ps.supplierName", "%" + term + "%")
                            ilike("ii.lotNumber", "%" + term + "%")
                        }
                    }
                }
            }
            order("name", "asc")
        }
        log.info "Query for products: " + (System.currentTimeMillis() - startTime) + " ms"

        def totalCount = products.totalCount

        products = products.unique()

		if (terms) {
			products = products.sort() {
				a, b ->
					(terms.any { a.productCode.contains(it) } ? a.productCode : null) <=> (terms.any { b.productCode.contains(it) } ? b.productCode : null) ?:
					(terms.any { a.name.contains(it) } ? a.name : null) <=> (terms.any { b.name.contains(it) } ? b.name : null)
			}
			products = products.reverse()
		}

		return new PagedResultList(products, totalCount);
	}


    def getProductsByTagId(List<String> tagIds) {
        def products = Product.withCriteria {
            tags {
                'in'('id', tagIds)
            }
        }
        return products.unique()
    }

    /**
     * Get all products with the given tags.
     *
     * @param inputTags
     * @return
     */
    def getProductsByTags(List<String> inputTags) {
        return getProductsByTags(inputTags, 10, 0)
    }

	/**
	 * Get all products that have the given tags.
	 *
	 * @param inputTags
	 * @return
	 */
	def getProductsByTags(List<String> inputTags, int max, int offset) {
        log.info "Get products by tags=${inputTags} max=${max} offset=${offset}"
		def products = Product.withCriteria {
			tags {
                'in'('id', inputTags)
            }
			if (max > 0) maxResults(max)
			firstResult(offset)
		}
        log.info "Products: " + products

		return products
	}

	def countProductsByTags(List inputTags) {
        log.debug "Get products by tags: " + inputTags
		def results = Product.withCriteria {
			projections { count('id') }
			tags { 'in'('id', inputTags) }
		}
		//log.debug "Results " + results[0]
		return results[0]
	}

	/**
	 * Get all products in the given catalog
	 *
	 * @param inputCatalogs
	 * @return
	 */
	def getProductsByCatalogs(List<String> inputCatalogs, int max, int offset) {
		log.info "Get products by catalogs=${inputCatalogs} max=${max} offset=${offset}"
		def products = []
		for (inputCatalog in inputCatalogs) {
			def productInstance = ProductCatalog.get(inputCatalog)
			def productsInCatalog = productInstance.productCatalogItems.product
			products += productsInCatalog
		}
		return products
	}

	def countProductsByCatalogs(List inputCatalogs) {
		log.debug "Get products by catalogs: " + inputCatalogs
		def result = 0
		for (inputCatalog in inputCatalogs) {
			def productInstance = ProductCatalog.get(inputCatalog)
			result += productInstance.productCatalogItems.size()
		}
		return result
	}


	/**
	 * Get all products that have the given tag.
	 *
	 * @param tag
	 * @return
	 */
	def getProductsByTag(String tag) {
		def products = Product.withCriteria {
			tags { eq('tag', tag) }
		}
		return products
	}

    def countProductsByShipment(shipment) {
        return getProductsByShipment(shipment, 0, 0)?.size()?:0
    }

    def getProductsByShipment(shipment, max, offset) {
        def products = []
        if (shipment) {
            shipment.shipmentItems.each { shipmentItem ->
                products << shipmentItem.inventoryItem.product
            }
        }
        return products;

    }

	/**
	 * Return a list of products that are marked as NON_SUPPORTED or NON_INVENTORIED
	 * at the given location.
	 *
	 * @param location
	 * @return
	 */
	List getHiddenProducts(Location location) {
		return getProductsByLocationAndStatuses(location, [
			InventoryStatus.NOT_SUPPORTED,
			InventoryStatus.SUPPORTED_NON_INVENTORY
		])
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

		log.info("Get quantity for product " + product?.name + " lotNumber " + lotNumber + " at location " + location?.name)
		if (!location) {
			throw new RuntimeException("Must specify location in order to calculate quantity on hand");
		}

		def inventoryItem = findInventoryItemByProductAndLotNumber(product, lotNumber)
		if (!inventoryItem) {
			throw new RuntimeException("There's no inventory item for product " + product?.name + " lot number " + lotNumber)
		}

		return getQuantity(location.inventory, inventoryItem)
	}

    Map<Product, Map<InventoryItem, Integer>> getQuantityByProductAndInventoryItemMap(List<TransactionEntry> entries) {
        return getQuantityByProductAndInventoryItemMap(entries, false)
    }

	/**
	 * Converts a list of passed transaction entries into a quantity
	 * map indexed by product and then by inventory item
	 *
	 * Note that the transaction entries should all be from the same inventory,
	 * or the quantity results would be somewhat nonsensical
     *
     * Also note that this method is calculating backwards to get the current quantity on hand gor the given product
     * and its inventory items.
	 *
	 * TODO: add a parameter here to optionally take in a product, which means that we are only
	 * calculation for a single product, which means that we can stop after we hit a product inventory transaction?
	 *
	 * @param entries
	 * @return
	 */
	Map getQuantityByProductAndInventoryItemMap(List<TransactionEntry> entries, Boolean useBinLocation) {
		def startTime = System.currentTimeMillis()
		def quantityMap = [:]

        def reachedInventoryTransaction = [:]   // used to keep track of which items we've found an inventory transaction for
        def reachedProductInventoryTransaction = [:]  // used to keep track of which items we've found a product inventory transaction for

        if (entries) {

            // first make sure the transaction entries are sorted, with most recent first
            entries = entries.sort().reverse()


            // Iterate over all transaction entries until we hit an end condition
            entries.each { transactionEntry ->

                // There are cases where the transaction entry might be null, so we need to check for this edge case
                if (transactionEntry) {

                    def inventoryItem = transactionEntry.inventoryItem
                    def product = inventoryItem.product
                    def transaction = transactionEntry.transaction
                    def binLocation = transactionEntry.binLocation

                    // first see if this is an entry we can skip (because we've already reached a product inventory transaction
                    // for this product, or a inventory transaction for this inventory item)
                    if (!(reachedProductInventoryTransaction[product] && reachedProductInventoryTransaction[product] != transaction) &&
                            !(reachedInventoryTransaction[product] && reachedInventoryTransaction[product][inventoryItem]
                                    && reachedInventoryTransaction[product][inventoryItem] != transaction)) {

                        // check to see if there's an entry in the map for this product and create if needed
                        // check to see if there's an entry for this inventory item in the map and create if needed
                        if (useBinLocation) {
                            if (!quantityMap[product]) {
                                quantityMap[product] = [:]
                            }
                            if (!quantityMap[product][inventoryItem]) {
                                quantityMap[product][inventoryItem] = [:]
                            }
                            if (!quantityMap[product][inventoryItem][binLocation]) {
                                quantityMap[product][inventoryItem][binLocation] = 0
                            }
                        }
                        else {
                            if (!quantityMap[product]) {
                                quantityMap[product] = [:]
                            }
                            if (!quantityMap[product][inventoryItem]) {
                                quantityMap[product][inventoryItem] = 0
                            }
                        }

                        // now update quantity as necessary
                        def transactionCode = transactionEntry.transaction.transactionType.transactionCode
                        if (transactionCode == TransactionCode.CREDIT) {
                            if (useBinLocation) {
                                quantityMap[product][inventoryItem][binLocation] += transactionEntry.quantity
                            }
                            else {
                                quantityMap[product][inventoryItem] += transactionEntry.quantity
                            }
                        }
                        else if (transactionCode == TransactionCode.DEBIT) {
                            if (useBinLocation) {
                                quantityMap[product][inventoryItem][binLocation] -= transactionEntry.quantity
                            }
                            else {
                                quantityMap[product][inventoryItem] -= transactionEntry.quantity
                            }
                        }
                        else if (transactionCode == TransactionCode.INVENTORY) {
                            if (useBinLocation) {
                                quantityMap[product][inventoryItem][binLocation] += transactionEntry.quantity
                            }
                            else {
                                quantityMap[product][inventoryItem] += transactionEntry.quantity
                            }
                            // mark that we are done with this inventory item (after this transaction)
                            if (!reachedInventoryTransaction[product]) {
                                reachedInventoryTransaction[product] = [:]
                            }
                            reachedInventoryTransaction[product][inventoryItem] = transaction
                        }
                        else if (transactionCode == TransactionCode.PRODUCT_INVENTORY) {
                            if (useBinLocation) {
                                quantityMap[product][inventoryItem][binLocation] += transactionEntry.quantity
                            }
                            else {
                                quantityMap[product][inventoryItem] += transactionEntry.quantity
                            }
                            // mark that we are done with this product (after this transaction)
                            reachedProductInventoryTransaction[product] = transaction
                        }
                    }
                }
            }
        }
        log.debug "  * Get quantity by product and inventory item map: " + (System.currentTimeMillis() - startTime) + " ms"

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
		def startTime = System.currentTimeMillis()
		def quantityMap = [:]

		// first get the quantity and inventory item map
		def quantityMapByProductAndInventoryItem = getQuantityByProductAndInventoryItemMap(entries)

		// now collapse this down to be by product
		quantityMapByProductAndInventoryItem.keySet().each {
			def product = it
			quantityMap[product] = 0
			quantityMapByProductAndInventoryItem[product].values().each { quantityMap[product] += it }
		}

        log.debug " * Get quantity by product map: " + (System.currentTimeMillis() - startTime) + " ms"

		return quantityMap
	}


    Map getQuantityByInventoryItemMap(Location location, List<Product> products) {
        def transactionEntries = getTransactionEntriesByInventoryAndProduct(location.inventory, products)
        return getQuantityByInventoryItemMap(transactionEntries)
    }

	/**
	 * Converts list of passed transactions entries into a quantity
	 * map indexed by inventory item
	 *
	 * @param entries
	 * @return
	 */
	Map getQuantityByInventoryItemMap(List<TransactionEntry> entries) {
		def startTime = System.currentTimeMillis()
		def quantityMap = [:]

		// first get the quantity and inventory item map
		def quantityByProductAndInventoryItemMap =
				getQuantityByProductAndInventoryItemMap(entries)

        //log.info "quantityByProductAndInventoryItemMap: " + quantityByProductAndInventoryItemMap

		// now collapse this down to be by product
		quantityByProductAndInventoryItemMap.keySet().each { product ->
			quantityByProductAndInventoryItemMap[product].keySet().each { inventoryItem ->
				if (!quantityMap[inventoryItem]) {
					quantityMap[inventoryItem] = 0;
				}
				quantityMap[inventoryItem] += quantityByProductAndInventoryItemMap[product][inventoryItem]
			}
		}
        log.debug " * getQuantityByInventoryItemMap(): " + (System.currentTimeMillis() - startTime) + " ms"
        //log.info "quantityMap: " + quantityMap
		return quantityMap
	}

	def getBinLocations(Shipment shipmentInstance) {
		Map binLocationMap = [:]
		// Only show stock for inventory items added to shipment
		List inventoryItems = shipmentInstance?.shipmentItems*.inventoryItem.unique()
		inventoryItems.each { inventoryItem ->
			binLocationMap[inventoryItem] = getItemQuantityByBinLocation(shipmentInstance?.origin,inventoryItem)
		}
		return binLocationMap
	}


    List getQuantityByBinLocation(Location location) {
        def startTime = System.currentTimeMillis()
        List binLocations
        def products = getProductsWithTransactions(location)
        GParsPool.withPool(8) {
            log.info "Processing ${products.size()} products"
            binLocations = products.collectParallel { product ->
                persistenceInterceptor.init()
                List localBinLocations = []
                try {
                    def localTransactionEntries = getTransactionEntriesByInventoryAndProduct(location.inventory, [product])
                    localBinLocations = getQuantityByBinLocation(localTransactionEntries)
                    persistenceInterceptor.flush()
                } catch (Exception e)  {
                    log.info ("Error processing product ${product.productCode}: " + e.message)
                } finally {
                    persistenceInterceptor.destroy()
                }
                return localBinLocations
            }
        }
        binLocations = binLocations.flatten()
        log.info ("Calculate quantity: " + (System.currentTimeMillis()-startTime) + " ms")
        return binLocations
    }

	/**
	 * Should be used with caution (i.e. not in a loop) since it requires an expensive call to
	 * calculate all quantity within a parent location.
	 *
	 * @param location
	 * @param internalLocation
	 * @return
	 */
    List getQuantityByBinLocation(Location location, Location internalLocation) {
		List binLocationEntries = getQuantityByBinLocation(location.parentLocation)
		return binLocationEntries.findAll { it.binLocation == internalLocation }
    }

    List getProductQuantityByBinLocation(Location location, Product product) {
        List transactionEntries = getTransactionEntriesByInventoryAndProduct(location?.inventory, [product])
        List binLocations = getQuantityByBinLocation(transactionEntries)
        return binLocations
    }

    List getProductQuantityByBinLocation(Location location, List<Product> products) {
        List transactionEntries = getTransactionEntriesByInventoryAndProduct(location?.inventory, products)
        List binLocations = getQuantityByBinLocation(transactionEntries)
        return binLocations
    }

    List getItemQuantityByBinLocation(Location location, List<InventoryItem> inventoryItems) {
        List transactionEntries = getTransactionEntriesByInventoryAndInventoryItems(location?.inventory, inventoryItems)
        List binLocations = getQuantityByBinLocation(transactionEntries)
        return binLocations
    }

    List getItemQuantityByBinLocation(Location location, InventoryItem inventoryItem) {
        List transactionEntries = getTransactionEntriesByInventoryAndInventoryItem(location?.inventory, inventoryItem)
        List binLocations = getQuantityByBinLocation(transactionEntries)
        return binLocations
    }


    /**
     * Converts list of passed transactions entries into a list of bin locations.
     *
     * @param entries
     * @return
     */
    List getQuantityByBinLocation(List<TransactionEntry> entries, boolean includeOutOfStock) {

        def binLocations = []

        def status = { quantity -> quantity > 0 ? "inStock" : "outOfStock" }

        // first get the quantity and inventory item map
        Map quantityBinLocationMap = getQuantityByProductAndInventoryItemMap(entries, true)
        quantityBinLocationMap.keySet().each { Product product ->
            quantityBinLocationMap[product].keySet().each { inventoryItem ->
                quantityBinLocationMap[product][inventoryItem].keySet().each { binLocation ->
                    def quantity = quantityBinLocationMap[product][inventoryItem][binLocation]
                    def value = "Bin: " + binLocation?.name + ", Lot: " + (inventoryItem?.lotNumber ?: "") + ", Qty: " + quantity

                    // Exclude bin locations with quantity 0 (include negative quantity for data quality purposes)
                    if (quantity != 0 || includeOutOfStock) {
                        binLocations << [
                                id            : binLocation?.id,
                                status        : status(quantity),
                                value         : value,
                                category      : product.category,
                                genericProduct: product.genericProduct,
                                product       : product,
                                inventoryItem : inventoryItem,
                                binLocation   : binLocation,
                                quantity      : quantity
                        ]
                    }
                }
            }
        }

        // Sort by expiration date, then bin location
        binLocations = binLocations.sort { a,b ->
            a?.inventoryItem?.expirationDate <=> b?.inventoryItem?.expirationDate ?: a?.binLocation?.name <=> b.binLocation?.name
        }

        return binLocations
    }


    /**
     * Get quantity by bin location given a list transaction entries.
     *
     * @param transaction entries used to calculate bin quantities
     * @return all bin locations including out of stock items
     */
    List getQuantityByBinLocation(List<TransactionEntry> entries) {
        return getQuantityByBinLocation(entries, false)
    }



    def getQuantityByProductGroup(Location location) {
        def quantityMap = getQuantityByProductMap(location.inventory)

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
    Map<Product, Integer> getQuantityByProductMap(String locationId) {
        def location = Location.get(locationId)
        return getQuantityByProductMap(location.inventory)
    }

    Map<Product, Integer> getQuantityByProductMap(Location location) {
        return getQuantityByProductMap(location.inventory)
    }


    //@Cacheable("quantityOnHandCache")
    Map<Product, Integer> getQuantityByProductMap(Inventory inventory) {
        def startTime = System.currentTimeMillis()
		def transactionEntries = getTransactionEntriesByInventory(inventory);
		def quantityMap = getQuantityByProductMap(transactionEntries)

        log.info " * Get quantity by product map: " + (System.currentTimeMillis() - startTime) + " ms"

		return quantityMap
	}

    /**
     * FIXME Remove once I've replaced all references with method below.
     *
     * @param location
     * @return
     */
    Map<Product, Integer> getCurrentInventory(Location location) {
        return getQuantityOnHandByProduct(location)
    }

    /**
     * Get the most recent date in the inventory snapshot table.
     *
     * @return
     */
    Date getMostRecentInventorySnapshotDate() {
        return InventorySnapshot.executeQuery('select max(date) from InventorySnapshot')[0]
    }

    /**
     * Get the most recent date in the inventory snapshot table.
     *
     * @return
     */
    Date getLastUpdatedInventorySnapshotDate() {
        return InventorySnapshot.executeQuery('select max(lastUpdated) from InventorySnapshot')[0]
    }


    /**
     * Get the quantity on hand by product for the given location.
     *
     * @param location
     * @return
     */
    Map<Product, Integer> getQuantityOnHandByProduct(Location location) {
        Date date = getMostRecentInventorySnapshotDate()

        return getQuantityOnHandByProduct(location, date)
    }

    /**
     * Get quantity on hand by product for the given location and date.
	 *
     * @param location
     * @param date
     * @return
     */
    Map<Product, Integer> getQuantityOnHandByProduct(Location location, Date date) {
		def quantityMap = [:]
		if (date && location) {
			log.info "getQuantityOnHandByProduct " + location + " " + date
			def startTime = System.currentTimeMillis()
			def results = InventorySnapshot.executeQuery("""
						select i.date, product, category.name, i.quantityOnHand
						from InventorySnapshot i, Product product, Category category
						where i.location = :location
						and i.date = :date
						and i.product = product
						and i.product.category = category
						""", [location: location, date: date])

			log.info "Results: " + results.size()
			log.info "Query response time: " + (System.currentTimeMillis() - startTime)
			startTime = System.currentTimeMillis()


			results.each {
				quantityMap[it[1]] = it[3]
			}
			log.debug "Post-processing response time: " + (System.currentTimeMillis() - startTime)
		}

        return quantityMap
    }

	/**
	 * Get quantity on hand by product for the given locations.
	 *
	 * @param location
	 * @return
	 */
	Map<Product, Map<Location, Integer>> getQuantityOnHandByProductAndLocation(Location[] locations) {
		def quantityMap = [:]
		if (locations) {
			Date date = getMostRecentInventorySnapshotDate()
			log.info "getQuantityOnHandByProductAndLocation " + locations + " " + date
			def startTime = System.currentTimeMillis()
			def results = InventorySnapshot.executeQuery("""
						select i.date, product, i.location, category.name, i.quantityOnHand
						from InventorySnapshot i, Product product, Category category
						where i.location in (:locations)
						and i.date = :date
						and i.product = product
						and i.product.category = category
						""", [locations: locations, date: date])

			log.info "Results: " + results.size()
			log.info "Query response time: " + (System.currentTimeMillis() - startTime)
			startTime = System.currentTimeMillis()


			results.each {
				if (!quantityMap[it[1]]) {
					quantityMap[it[1]] = [:]
				}
				quantityMap[it[1]][it[2]?.id] = it[4]
			}
			log.debug "Post-processing response time: " + (System.currentTimeMillis() - startTime)
		}

		return quantityMap
	}

    /**
     * Get the most recent date from the inventory item snapshot table.
     *
     * @return
     */
    Date getMostRecentInventoryItemSnapshotDate() {
        return InventoryItemSnapshot.executeQuery('select max(date) from InventoryItemSnapshot')[0]
    }

	/**
     * Get quantity on hand by inventory item for the given location and date.
	 *
	 * @param location
	 * @return
     */
    Map<InventoryItem, Integer> getQuantityOnHandByInventoryItem(Location location) {
        def startTime = System.currentTimeMillis()
		def quantityMap = [:]
        Date date = getMostRecentInventoryItemSnapshotDate()
		if (location && date) {
			def results = InventoryItemSnapshot.executeQuery("""
						select iis.date, ii, product.category.name, iis.quantityOnHand
						from InventoryItemSnapshot iis, Product product, Category category, InventoryItem ii
						where iis.location = :location
						and iis.date = :date
						and iis.product = product
						and iis.inventoryItem = ii
						and iis.product.category = category
						""", [location: location, date: date])

			log.info "Results: " + results.size()
			log.info "Query response time: " + (System.currentTimeMillis() - startTime)
			startTime = System.currentTimeMillis()

			results.each {
				quantityMap[it[1]] = it[3]
			}
			log.info "Post-processing response time: " + (System.currentTimeMillis() - startTime)
		}
        return quantityMap
    }

    /**
     * Get a list of products that have an assocation with the given an inventory.
     *
     * @param inventory
     * @return
     */
    List<Product> getProductsByInventory(Inventory inventory) {
        InventoryLevel.executeQuery("select il.product from InventoryLevel as il where il.inventory = :inventory", [inventory:inventory])
    }

    /**
     * Get a map of quantities (indexed by product) for the given location
     * @param location
     * @param products
     * @return
     */
    Map<Product, Integer> getQuantityByProductMap(Location location, List<Product> products) {
        return getQuantityByProductMap(location.inventory, products)
    }

	/**
	 * Get a map of quantities (indexed by product) for a particular inventory.
	 *
	 * @param inventoryInstance
	 * @return
	 */
	Map<Product, Integer> getQuantityByProductMap(Inventory inventory, List<Product> products) {
		long startTime = System.currentTimeMillis()
        log.debug "get quantity by product map "

        log.debug "inventory: " + inventory
        log.debug "products: " + products.size()
		def transactionEntries = getTransactionEntriesByInventoryAndProduct(inventory, products);
		def quantityMap = getQuantityByProductMap(transactionEntries)
        log.info "quantityMap: " + quantityMap.keySet()

        // FIXME Hacky way to make sure all products that have been passed in have an entry in the quantity map
        // FIXME Requires a proper implementation for hashCode/equals methods which was disabled due to a different bug
        products.each {
            def product = Product.get(it.id)
            log.debug "contains key for product ${product.productCode} ${product.name}: " + quantityMap.containsKey(product)
            if (!quantityMap.containsKey(product)) {
                quantityMap[product] = 0
            }
        }

        log.debug "getQuantityByProductMap(): " + (System.currentTimeMillis() - startTime) + " ms"

		return quantityMap
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
		long startTime = System.currentTimeMillis()
		def inventoryLevels = getInventoryLevelsByInventory(inventoryInstance)

		Map<Product, Integer> reorderProductsQuantityMap = new HashMap<Product, Integer>()
		Map<Product, Integer> minimumProductsQuantityMap = new HashMap<Product, Integer>()

		for (level in inventoryLevels) {

			def quantityMap = getQuantityByInventoryAndProduct(inventoryInstance, level.product)

			// getQuantityByInventory returns an Inventory Item to Quantity map, so we want to sum all the values in this map to get the total quantity
			def quantity = quantityMap?.values().sum { it } ?: 0

			// The product is supported or the user asks for unsupported products to be included
			if (level.status == InventoryStatus.SUPPORTED || includeUnsupported) {
				if (quantity <= level.minQuantity) {
					minimumProductsQuantityMap[level.product] = quantity
				}
				else if (quantity <= 0) {
					minimumProductsQuantityMap[level.product] = quantity
				}
				else if (quantity <= level.reorderQuantity) {
					reorderProductsQuantityMap[level.product] = quantity
				}
			}
		}
        log.info "getProductsBelowMinimumAndReorderQuantities(): " + (System.currentTimeMillis() - startTime) + " ms"
		return [minimumProductsQuantityMap: minimumProductsQuantityMap, reorderProductsQuantityMap: reorderProductsQuantityMap]
	}
	/**
	 * @return current location from thread local
	 */
	Location getCurrentLocation() {
		def currentLocation = AuthService?.currentLocation?.get()
		if (!currentLocation?.inventory)
			throw new Exception("Inventory not found")
		return currentLocation
	}

	/**
	 * @param location
	 * @param product
	 * @return	get quantity by location and product
	 */
	Integer getQuantityAvailableToPromise(Location location, Product product) {
		def quantityMap = getQuantityForProducts(location.inventory, [product.id])
        log.debug "quantity map " + quantityMap;
		def quantityOnHand = getQuantityOnHand(location, product)?:0
		def quantityOutgoing = getQuantityToShip(location, product)?:0
		def quantityAvailableToPromise = quantityOnHand - quantityOutgoing

		return quantityAvailableToPromise?:0
	}

	/**
	 * @param location
	 * @param product
	 * @return	get quantity by location and product
	 */
	Integer getQuantityOnHand(Location location, Product product) {
		log.info "quantity on hand for location " + location + " product " + product
		def quantityMap = getQuantityForProducts(location.inventory, [product.id])
        log.debug "quantity map " + quantityMap;
		def quantity = quantityMap[product.id]

		return quantity?:0
	}

	Integer getQuantityToReceive(Location location, Product product) {
		Map quantityMap = getIncomingQuantityByProduct(location, [product]);
		def quantity = quantityMap[product]
		return quantity?:0
	}

	Integer getQuantityToShip(Location location, Product product) {
		Map quantityMap = getOutgoingQuantityByProduct(location, [product]);
		def quantity = quantityMap[product]
		return quantity?:0
	}


	/**
	 * @param inventoryItem
	 * @return current quantity of the given inventory item.
	 */
	Integer getQuantityFromBinLocation(Location location, Location binLocation, InventoryItem inventoryItem) {
		def startTime = System.currentTimeMillis()
		def quantity = getQuantity(location.inventory, binLocation, inventoryItem)
        log.info "getQuantity(): " + (System.currentTimeMillis() - startTime) + " ms"
		return quantity
	}


    Integer getQuantity(Inventory inventory, InventoryItem inventoryItem) {
        return getQuantity(inventory, null, inventoryItem)
    }

	/**
	 * Gets the quantity of a specific inventory item at a specific inventory
	 *
	 * @param item
	 * @param inventory
	 * @return
	 */
	Integer getQuantity(Inventory inventory, Location binLocation, InventoryItem inventoryItem) {

		if (!inventory) {
            throw new RuntimeException("Inventory does not exist")
		}

		def transactionEntries = getTransactionEntriesByInventoryAndInventoryItem(inventory, inventoryItem)
        if (binLocation) {
            List binLocations = getQuantityByBinLocation(transactionEntries)
            log.info "Bin locations: " + binLocations
            def entry = binLocations.find { it.inventoryItem == inventoryItem && it.binLocation == binLocation }
            return entry?.quantity?:0
        }
        else {
            def quantityMap = getQuantityByInventoryItemMap(transactionEntries)

            // FIXME was running into an issue where a proxy object was being used to represent the inventory item
            // so the map.get() method was returning null.  So we needed to fully load the inventory item using
            // the GORM get method.
            // inventoryItem -> org.pih.warehouse.inventory.InventoryItem_$$_javassist_10
            //log.debug "inventoryItem -> " + inventoryItem.class
            inventoryItem = InventoryItem.get(inventoryItem.id)
            Integer quantity = quantityMap[inventoryItem]
            return quantity ?: 0;
        }
	}


	Integer getQuantityAvailableToPromise(InventoryItem inventoryItem) {
		def currentLocation = getCurrentLocation()
		return getQuantityAvailableToPromise(currentLocation.inventory, inventoryItem)
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
     * Get the most recent quantity on hand from inventory item snapshot table. If there are no
     * records in the inventory item snapshot table then we calculate the QoH from transactions.
     *
     * @param location
     * @return
     */
    Map<InventoryItem, Integer> getQuantityByLocation(Location location) {
        Map<InventoryItem, Integer> quantityMap = getMostRecentInventoryItemSnapshot(location)
        if (!quantityMap) {
            quantityMap = getQuantityForInventory(location?.inventory)
        }
        return quantityMap;
    }

	/**
	 * Calculate quantity on hand values for all available inventory items at the given inventory.
	 *
     * FIXME Use sparingly - this is very expensive because it calculates the QoH over an entire inventory.
     *
	 * @param inventory
	 * @return
	 */
	Map<InventoryItem, Integer> getQuantityForInventory(Inventory inventory) {
		def transactionEntries = getTransactionEntriesByInventory(inventory);
		return getQuantityByInventoryItemMap(transactionEntries);
	}

    /**
     * Calculate quantity on hand values for all available inventory items in the given inventory.
     *
     * @param inventory
     * @param products
     * @return
     */
    Map<InventoryItem, Integer> getQuantityForInventory(Inventory inventory, List<Product> products) {
        def transactionEntries = getTransactionEntriesByInventoryAndProduct(inventory, products)
        return getQuantityByInventoryItemMap(transactionEntries);
    }


    Map<InventoryItem, Integer> getMostRecentInventoryItemSnapshot(Location location) {
        Map quantityMap = [:]
        def results = InventoryItemSnapshot.executeQuery("""
            SELECT a.inventoryItem, a.quantityOnHand, DATEDIFF(a.inventoryItem.expirationDate, current_date) as daysToExpiry
            FROM InventoryItemSnapshot a
            WHERE a.date = (select max(b.date) from InventoryItemSnapshot b)
            AND a.location = :location
            AND a.quantityOnHand > 0
            ORDER BY a.inventoryItem.expirationDate ASC
            """, [location:location])


        results.each {
            quantityMap[it[0]] = it[1]
        }

        return quantityMap
    }

	/**
	 * Fetches and populates a StockCard Command object
	 *
	 * @param cmd
	 * @param params
	 * @return
	 */
    //@Cacheable("stockCardCommandCache")
	StockCardCommand getStockCardCommand(StockCardCommand cmd, Map params) {
		// Get basic details required for the whole page
		cmd.product = Product.get(params?.product?.id ?: params.id);  // check product.id and id
        if (!cmd.product) {
            throw new ProductException("Product with identifier '${params?.product?.id?:params.id}' could not be found")
        }

		cmd.inventory = cmd.warehouse?.inventory
		cmd.inventoryLevel = getInventoryLevelByProductAndInventory(cmd.product, cmd.inventory)

		// Get current stock of a particular product within an inventory
		// Using set to make sure we only return one object per inventory items
		Set inventoryItems = getInventoryItemsByProductAndInventory(cmd.product, cmd.inventory);
		cmd.inventoryItemList = inventoryItems as List
		cmd.inventoryItemList?.sort { it.expirationDate }?.sort { it.lotNumber }

        cmd.totalQuantity = getQuantityOnHand(cmd.warehouse, cmd.product)

        //cmd.pendingShipmentList = shipmentService.getPendingShipments(cmd.warehouse);

		// Get all lot numbers for a given product
		//cmd.lotNumberList = getInventoryItemsByProduct(cmd?.product) as List

		// Get transaction log for a particular product within an inventory
		cmd.transactionEntryList = getTransactionEntriesByInventoryAndProduct(cmd.inventory, [cmd.product]);
		cmd.transactionEntriesByInventoryItemMap = cmd.transactionEntryList.groupBy { it.inventoryItem }
		cmd.transactionEntriesByTransactionMap = cmd.transactionEntryList.groupBy { it.transaction }

		// Used in the show lot numbers tab
		cmd.quantityByInventoryItemMap = getQuantityByInventoryItemMap(cmd.transactionEntryList)

        // Used in the current stock tab
        cmd.quantityByBinLocation = getQuantityByBinLocation(cmd.transactionEntryList)

		return cmd
	}


    def getStockHistory(Inventory inventory, Product product) {
        return getTransactionEntriesByInventoryAndProduct(inventory, [product]);
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
		//commandInstance.transactionDate.clearTime()

		if (!commandInstance?.product) {
			commandInstance.errors.reject("error.product.invalid", "Product does not exist");
		}
		else {
			commandInstance.recordInventoryRow = new RecordInventoryRowCommand();

			// get all transaction entries for this product at this inventory
			def transactionEntryList = getTransactionEntriesByInventoryAndProduct(commandInstance?.inventory, [commandInstance?.product])

            // Get list of bin locations
            List binLocationEntries = getQuantityByBinLocation(transactionEntryList)

            binLocationEntries.each {
                    def inventoryItemRow = new RecordInventoryRowCommand()
                    inventoryItemRow.id = it.id
                    inventoryItemRow.inventoryItem = it.inventoryItem
                    inventoryItemRow.binLocation = it.binLocation
                    inventoryItemRow.lotNumber = it?.inventoryItem?.lotNumber
                    inventoryItemRow.expirationDate = it?.inventoryItem?.expirationDate
                    inventoryItemRow.oldQuantity = it.quantity
                    inventoryItemRow.newQuantity = it.quantity
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
			//if (cmd.validate()) {
            def inventoryItems = getInventoryItemsByProductAndInventory(cmd.product, cmd.inventory)

            // Create a new transaction
            def transaction = new Transaction(cmd.properties)
            transaction.inventory = cmd.inventory
            transaction.comment = cmd.comment
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
                            inventoryItem.errors.allErrors.each { error ->
                                cmd.errors.reject("inventoryItem.invalid",
                                        [
                                            inventoryItem,
                                            error.getField(),
                                            error.getRejectedValue()] as Object[],
                                        "[${error.getField()} ${error.getRejectedValue()}] - ${error.defaultMessage} ");

                            }
                            // We need to fix these errors before we can move on
                            return cmd;
                        }
                    }
                    // 3. Create a new transaction entry (even if quantity didn't change)
                    def transactionEntry = new TransactionEntry()
                    transactionEntry.properties = row.properties
                    transactionEntry.quantity = row.newQuantity
                    transactionEntry.product = inventoryItem?.product
                    transactionEntry.inventoryItem = inventoryItem
                    transactionEntry.binLocation = row.binLocation
					transactionEntry.comments = row.comment
                    transaction.addToTransactionEntries(transactionEntry)
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
                                    [
                                        transaction,
                                        error.getField(),
                                        error.getRejectedValue()] as Object[],
                                    "Property [${error.getField()}] of [${transaction.class.name}] with value [${error.getRejectedValue()}] is invalid");
                        }
                    }
                }
            }
			//}
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
            eq("active", true)
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
                    eq("active", true)
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
		log.info("Find inventory item by product " + product?.id + " and lot number '" + lotNumber + "'")
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
			inventoryItem.save(flush:true)
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
		transactionEntries.each { inventoryItems << it.inventoryItem; }
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
        def startTime = System.currentTimeMillis()
		def criteria = TransactionEntry.createCriteria();
		def transactionEntries = criteria.list {
			transaction {
				eq("inventory", inventory)
				order("transactionDate", "asc")
				order("dateCreated", "asc")
			}
		}
        log.debug "getTransactionEntriesByInventory(): " + (System.currentTimeMillis() - startTime)

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
				inventoryItem { inList("product", products) }
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
    List getTransactionEntriesByInventoryAndBinLocation(Inventory inventory, Location binLocation) {
        def criteria = TransactionEntry.createCriteria();
        def transactionEntries = criteria.list {
			if (binLocation) {
				eq("binLocation", binLocation)
			}
            transaction {
                eq("inventory", inventory)
                order("transactionDate", "asc")
                order("dateCreated", "asc")
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
	List getTransactionEntriesByInventoryAndInventoryItem(Inventory inventory, InventoryItem item) {
		return TransactionEntry.createCriteria().list() {
            eq("inventoryItem", item)
            inventoryItem {
                eq("product", item?.product)
            }
            transaction {
                eq("inventory", inventory)
            }
		}
	}

    /**
     * Gets all transaction entries for a inventory item within an inventory
     *
     * @param inventoryItem
     * @param inventory
     */
    List getTransactionEntriesByInventoryAndInventoryItems(Inventory inventory, List<InventoryItem> inventoryItems) {
        return TransactionEntry.createCriteria().list() {
            'in'("inventoryItem", inventoryItems)
            transaction {
                eq("inventory", inventory)
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
	boolean adjustStock(AdjustStockCommand command) {

        def quantity = command.quantity
        def location = command.location
        def inventory = command.location.inventory
        def inventoryItem = command.inventoryItem
        def binLocation = command.binLocation
        def quantityAvailable = getQuantityFromBinLocation(location, binLocation, inventoryItem)

        log.info "Check quantity: ${quantity} vs ${quantityAvailable}: ${quantityAvailable==quantity}"
        if (quantityAvailable == quantity) {
            command.errors.rejectValue("quantity","adjustStock.invalid.quantity.message")
        }

        log.info "command " + command.validate()
        log.info "command has errors: " + command.hasErrors()
        log.info "command errors: " + command.errors

        if (command.validate() && !command.hasErrors()) {
            def transaction = new Transaction();
            // Need to create a transaction if we want the inventory item to show up in the stock card
            transaction.transactionDate = new Date();
            transaction.transactionType = TransactionType.get(Constants.INVENTORY_TRANSACTION_TYPE_ID);
            transaction.inventory = inventory;
            transaction.comment = command.comment

            // Add transaction entry to transaction
            def transactionEntry = new TransactionEntry();
            transactionEntry.quantity = quantity
            transactionEntry.inventoryItem = inventoryItem;
            transactionEntry.binLocation = binLocation

            transaction.addToTransactionEntries(transactionEntry);

            if (!transaction.save()) {
                log.info("Errors saving transaction: " + transaction.errors)
                command.errors.addAllErrors(transaction.errors)
            }
        }
        return command
	}


	/**
	 * Adjusts the stock level by adding a new transaction entry with a
	 * quantity change.
	 *
	 * Passing in an instance of inventory item so that we can attach errors.
	 * @param inventoryItem
	 * @param params
	 */
	def transferStock(TransferStockCommand command) {

        Integer quantity = command.quantity
        Location location = command.location
        Location binLocation = command.binLocation
        Inventory inventory = command.location.inventory
        InventoryItem inventoryItem = command.inventoryItem
        Location otherLocation = command.otherLocation
        Location otherBinLocation = command.otherBinLocation
        Boolean transferOut = command.transferOut

        def transaction = new Transaction();
		if (inventoryItem && inventory) {
			def transactionEntry = new TransactionEntry();
			transactionEntry.quantity = quantity;
			transactionEntry.inventoryItem = inventoryItem

            Integer quantityOnHand = getQuantityFromBinLocation(location, binLocation, inventoryItem);

            if (!otherLocation?.inventory) {
                //throw new RuntimeException("Destination does not have an inventory")
                transaction.errors.reject("Destination does not have an inventory")
            }

            if (location == otherLocation && binLocation == otherBinLocation) {
                //throw new RuntimeException("Destination must be different from source")
                transaction.errors.reject("Cannot transfer to the same location")
            }

            if (quantityOnHand < quantity) {
                //inventoryItem.errors.reject("inventoryItem.quantity.invalid")
                //throw new RuntimeException("Cannot exceed quantity on hand")
                transaction.errors.reject("Quantity cannot exceed quantity on hand")
            }

            if (quantity <= 0) {
                //throw new RuntimeException("Quantity must be greater than 0")
                transaction.errors.reject("Quantity must be greater than 0")
            }

            // Create transaction to handle transfer in / out
			transaction.transactionDate = new Date();
            transaction.inventory = inventory;
			transaction.order = command.order
            transaction.transactionNumber = generateTransactionNumber()
            transaction.destination = (transferOut) ? otherLocation : null
            transaction.source = (transferOut) ? null : otherLocation
			transaction.transactionType = (transferOut) ?
                    TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) :
                    TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID)

			// Add transaction entry to transaction
            transactionEntry.binLocation = binLocation
            transactionEntry.inventoryItem = inventoryItem;
            transactionEntry.quantity = quantity
			transaction.addToTransactionEntries(transactionEntry);

			if (!transaction.hasErrors() && transaction.save()) {

                Transaction mirroredTransaction = createMirroredTransaction(transaction)
                TransactionEntry mirroredTransactionEntry = mirroredTransaction.transactionEntries.first()
                mirroredTransactionEntry.binLocation = otherBinLocation

                if (!saveLocalTransfer(transaction, mirroredTransaction)) {
					throw new ValidationException("Unable to save local transfer", transaction.errors)
				}
			}
		}
		return transaction
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

	void deleteTransaction(Transaction transactionInstance) {
		if (isLocalTransfer(transactionInstance)) {
			deleteLocalTransfer(transactionInstance)
		}
		else {
			transactionInstance.delete(flush: true)
		}
	}



	Boolean saveLocalTransfer(Transaction baseTransaction) {
        return saveLocalTransfer(baseTransaction, null)
    }


	/**
	 * Creates or updates the local transfer associated with the given transaction
	 * Returns true if the save/update was successful
	 *
	 * @param baseTransaction
	 * @return
	 */
	Boolean saveLocalTransfer(Transaction baseTransaction, Transaction mirroredTransaction) {
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
		if (!mirroredTransaction) {
            mirroredTransaction = createMirroredTransaction(baseTransaction)
            mirroredTransaction.transactionNumber = generateTransactionNumber()
            if (!mirroredTransaction.save(flush: true)) {
                throw new RuntimeException("Unable to save mirrored transaction " + mirroredTransaction?.id)
            }
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

		mirroredTransaction.order = baseTransaction.order
        mirroredTransaction.requisition = baseTransaction.requisition
		mirroredTransaction.transactionDate = baseTransaction.transactionDate

		// create the transaction entries based on the base transaction
		baseTransaction.transactionEntries.each {
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
			inventoryItem { eq("product", product) }
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
					product { 'in'("category", categories) }
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
		//today.clearTime()
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
	def validateInventoryData(Map params, Errors errors) {
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


			Date today = new Date()

			def transactionInstance = new Transaction(transactionDate: today,
			transactionType: TransactionType.findById(Constants.INVENTORY_TRANSACTION_TYPE_ID),
			inventory: command?.location.inventory)

			// Iterate over each row
			command?.data?.each { Map params ->

                log.debug params

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
        log.debug "inventory " + inventory + " " + ", productIds: " + productIds
		def ids = productIds.collect{ "'${it}'"}.join(",")
        log.debug "ids: " + ids
		def result =[:]
		if (ids) {
            //
			//def sql = "select te from TransactionEntry as te where te.transaction.inventory.id='${inventory.id}' and te.inventoryItem.product.id in (${ids}) " +
            //        "order by te.transaction.transactionDate asc, te.transaction.dateCreated asc"

            def sql = "select te from TransactionEntry as te where te.transaction.inventory.id='${inventory.id}' and te.inventoryItem.product.id in (${ids})"
            log.debug "SQL: " + sql
			def transactionEntries = TransactionEntry.executeQuery(sql)
			log.debug "transactionEntries " + transactionEntries
            //transactionEntries.each{ println(it)}
			//transactionEntries.each{
            //    println it.transaction.transactionType.transactionCode.toString() + "," +
            //            it.transaction.transactionDate.toString() + "," +
            //            it.transaction.dateCreated.toString() + "," +
            //            it.inventoryItem.lotNumber + "," +
            //            it.quantity
            //}
			def map = getQuantityByProductMap(transactionEntries)
			map.keySet().each{ result[it.id] = map[it] }
		}
		log.debug "getQuantityForProducts " + result
		result
	}

	public Map<Product, List<InventoryItem>> getInventoryItemsWithQuantity(List<Product> products, Inventory inventory) {

		def transactionEntries = TransactionEntry.createCriteria().list() {
			transaction { eq("inventory", inventory) }
			if (products) {
				inventoryItem { 'in'("product", products) }
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


	/**
	 * @param product
	 * @return
	 */
	//public Integer getQuantityAvailableToPromise(Product product) {}
	//public Integer getQuantityOnHand(Product product) {}
	//public Integer getQuantityAvailableToPromise(InventoryItem inventoryItem) {}
	//public Integer getQuantityOnHand(InventoryItem inventoryItem) {}


	/**
	 * @return	a unique identifier to be assigned to a transaction
	 */
	String generateTransactionNumber() {
		return identifierService.generateTransactionIdentifier()
	}

    List<Transaction> getCreditsBetweenDates(List<Location> fromLocations, List<Location> toLocations, Date fromDate, Date toDate) {
        def transactions = Transaction.createCriteria().list() {
            transactionType {
                eq("transactionCode", TransactionCode.CREDIT)
            }
            if (fromLocations) {
                'in'("source", fromLocations)
            }
            if (toLocations) {
                'in'("inventory", toLocations.collect { it.inventory })
            }
            between('transactionDate', fromDate, toDate)
        }
        return transactions
    }

	List<Transaction> getDebitsBetweenDates(List<Location> fromLocations, List<Location> toLocations, Date fromDate, Date toDate) {
		getDebitsBetweenDates(fromLocations, toLocations, fromDate, toDate, null)
	}


	List<Transaction> getDebitsBetweenDates(List<Location> fromLocations, List<Location> toLocations, Date fromDate, Date toDate, List transactionTypes) {
        def transactions = Transaction.createCriteria().list() {
            transactionType {
                eq("transactionCode", TransactionCode.DEBIT)
            }
			if (transactionTypes) {
				'in'("transactionType", transactionTypes)
			}
			if (toLocations) {
				or {
					'in'("destination", toLocations)
					isNull("destination")
				}
			}
            if (fromLocations) {
				and {
					'in'("inventory", fromLocations.collect { it.inventory })

					not {
						'in'("destination", fromLocations)
					}
				}
            }
            between('transactionDate', fromDate, toDate)
        }
        return transactions
    }


    def getInventorySampling(Location location, Integer n) {
        def inventoryItems = []
        Map<InventoryItem, Integer> inventoryItemMap = getQuantityOnHandByInventoryItem(location);

        List inventoryItemKeys = inventoryItemMap.keySet().asList()
        Integer maxSize = inventoryItemKeys.size()

        if (n > maxSize) {
            n = maxSize
            //throw new RuntimeException("You cannot request more items than are available at this location [requested=${n},available=${maxSize}].")
        }

        Random random = new Random()
        def randomIntegerList = []
        (1..n).each {
            def randomIndex = random.nextInt(maxSize)
            def inventoryItem = inventoryItemKeys.get(randomIndex)
            inventoryItems << inventoryItem
        }
        return inventoryItems;
    }


    /**
     *  Returns the quantity on hand of each product at the given location as of the given date.
     */
    def getQuantityOnHandAsOfDate(Location location, Date date) {
        return getQuantityOnHandAsOfDate(location, date, null)
    }

    //def getQuantityOnHandAsOfDate(Location location, Date date, Tag tag) {
    //    return getQuantityOnHandAsOfDate(location, date, [tag])
    //}

        /**
     *  Returns the quantity on hand of each product for the given tag at the given location as of the given date.
     */
    def getQuantityOnHandAsOfDate(Location location, Date date, List tagIds) {
        def transactionEntries = getTransactionEntriesBeforeDate(location, date, tagIds)
        def quantityMap = getQuantityByProductMap(transactionEntries)

        // Make sure that ALL products in the tag are represented
        if (tagIds) {
            tagIds.each { tagId ->
                Tag tag = Tag.get(tagId)
                if (tag) {
                    tag.products.each { p ->
                        def product = Product.get(p.id)
                        def quantity = quantityMap[product]
                        if (!quantity) {
                            quantityMap[product] = 0
                        }
                    }
                }
            }
        }
        return quantityMap
    }


    def getTransactionEntriesBeforeDate(Location location, Date date) {
        return getTransactionEntriesBeforeDate(location, date, null)
    }

    def getTransactionEntriesBeforeDate(Location location, Date date, Tag tag) {
        return getTransactionEntriesBeforeDate(location, date, [tag.id] as List)
    }

    def getTransactionEntriesBeforeDate(Location location, Date date, List tagIds) {
        def startTime = System.currentTimeMillis()
        def criteria = TransactionEntry.createCriteria();
        def transactionEntries = []
        if (date) {
            def products = tagIds ? getProductsByTagId(tagIds) : []
            log.info "Get products by tag ${tagIds}: " + products.toString()
            transactionEntries = criteria.list {
                if (products) {
                    inventoryItem {
                        'in'("product", products)
                    }
                }
                transaction {
                    // All transactions before given date
                    lt("transactionDate", date)
                    eq("inventory", location?.inventory)
                    order("transactionDate", "asc")
                    order("dateCreated", "asc")

                }
            }
            // Show several other queries to achieve the same thing, but none of these improve query performance
//            transactionEntries = TransactionEntry.findAll("from TransactionEntry as te " +
//                    "where te.transaction.transactionDate < :date " +
//                    "and te.transaction.inventory = :inventory " +
//                    "order by te.transaction.transactionDate asc, te.transaction.dateCreated asc",
//                    [date: date, inventory: location.inventory])

//            transactionEntries = TransactionEntry.createCriteria().list {
//                createAlias('transaction', 't', CriteriaSpecification.INNER_JOIN)
//                and {
//                    if (products) {
//                        inventoryItem {
//                            'in'("product", products)
//                        }
//                    }
//                    // All transactions before given date
//                    lt("t.transactionDate", date)
//                    eq("t.inventory", location?.inventory)
//                }
//                order("t.transactionDate", "asc")
//                order("t.dateCreated", "asc")
//            }


            log.info "Get transaction entries before date: " + (System.currentTimeMillis() - startTime) + " ms"
        }
        return transactionEntries;
    }

    /**
     * Export given products.
     * @param products
     * @return
     */
    String exportBaselineQoH(products, quantityMapByDate) {
        def csvrows = []
        products.each { product ->
            //def inventoryLevel = inventoryLevelMap[product]
            def csvrow = [
                'Product code': product.productCode?:'',
                'Product': product.name,
                'UOM': product.unitOfMeasure,
                'Generic product': product?.genericProduct?.name?:"",
                'Category': product?.category?.name,
                'Manufacturer': product?.manufacturer?:"",
                'Manufacturer code': product?.manufacturerCode?:"",
                'Vendor': product?.vendor?:"",
                'Vendor code': product?.vendorCode?:""
                //'Bin Location': inventoryLevel?.binLocation?:"",
                //'Min': inventoryLevel?.minQuantity?:"",
                //'Reorder': inventoryLevel?.reorderQuantity?:"",
                //'Max': inventoryLevel?.maxQuantity?:""
            ]

            if (quantityMapByDate) {
                quantityMapByDate.each { key, value ->
                    csvrow[key.format("dd-MMM-yyyy")] = quantityMapByDate[key][product]
                }
            }

            csvrows << csvrow
        }
        return dataService.generateCsv(csvrows)
    }


    def getCurrentStockAllLocations(Product product, Location currentLocation, User currentUser) {
        log.info ("Get getQuantityOnHand() for product ${product?.name} at all locations")
        def locations = locationService.getLoginLocations(currentLocation)

		locations = locations.findAll { Location location ->
			location.inventory && location.isWarehouse() && currentUser.getEffectiveRoles(location) }

        locations = locations.collect { Location location ->
			def quantity = getQuantityOnHand(location, product)?:0
			def unitPrice = product?.pricePerUnit?:0
			[
					location: location,
					locationGroup: location?.locationGroup,
					quantity: quantity,
					value: quantity * unitPrice
			]
        }

		locations = locations.findAll { it?.quantity > 0 }
		locations.sort { it.locationGroup }

		def quantityMap = locations.groupBy { it?.locationGroup }.collect{ k, v ->
			[(k):[totalValue: v.value.sum(), totalQuantity: v.quantity.sum(), locations: v]]
		}

        return quantityMap
    }


    String exportLatestInventoryDate(location) {
        def formatDate = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss")
        def sw = new StringWriter()


        def quantityMap = getTotalStock(location);
        def statusMap = getInventoryStatus(location)
        def products = quantityMap.keySet()

        def latestInventoryDates = TransactionEntry.executeQuery("""
                select ii.product.id, max(t.transactionDate)
                from TransactionEntry as te
                left join te.inventoryItem as ii
                left join te.transaction as t
                where t.inventory = :inventory
                and t.transactionType.transactionCode in (:transactionCodes)
                group by ii.product
                """,
                [inventory: location.inventory, transactionCodes: [TransactionCode.PRODUCT_INVENTORY, TransactionCode.INVENTORY]])


        // Convert to map
        def latestInventoryDateMap = [:]
        latestInventoryDates.each {
            latestInventoryDateMap[it[0]] = it[1]
        }

        def inventoryLevelMap = [:]
        def inventoryLevels = InventoryLevel.findAllByInventory(location.inventory)
        inventoryLevels.each { inventoryLevel ->
            inventoryLevelMap[inventoryLevel.product] = inventoryLevel
        }


        def csvWriter = new CSVWriter(sw, {
            "Product Code" { it.productCode }
            "Name" { it.name }
            "Bin Location" { it.binLocation }
            "ABC" { it.abcClass }
            "Most Recent Stock Count" { it.latestInventoryDate }
            "QoH" { it.quantityOnHand }
            "Unit of Measure" { it.unitOfMeasure }
            "Date Created" { it.dateCreated }
            "Date Updated" { it.lastUpdated }
        })

        products.each { product ->
            def latestInventoryDate = latestInventoryDateMap[product.id]
            def row =  [
                    productCode: product.productCode?:"",
                    name: product.name,
                    unitOfMeasure: product.unitOfMeasure?:"",
                    abcClass: inventoryLevelMap[product]?.abcClass?:"",
                    binLocation: inventoryLevelMap[product]?.binLocation?:"",
                    latestInventoryDate: latestInventoryDate?"${formatDate.format(latestInventoryDate)}":"",
                    quantityOnHand: quantityMap[product]?:"",
                    dateCreated: product.dateCreated?"${formatDate.format(product.dateCreated)}":"",
                    lastUpdated: product.lastUpdated?"${formatDate.format(product.lastUpdated)}":"",
            ]
            csvWriter << row
        }
        return sw.toString()
    }

    /**
     *
     * @param command
     */
    def validateInventoryData(ImportDataCommand command) {

        def dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
        def calendar = Calendar.getInstance()
        command.data.eachWithIndex { row, index ->
            def rowIndex = index + 2

            if (!command.warnings[index]) {
                command.warnings[index] = []
            }

            def product = Product.findByProductCode(row.productCode)
            if (!product) {
                command.errors.reject("error.product.notExists", "Row ${rowIndex}: Product '${row.productCode}' does not exist");
                command.warnings[index] << "Product '${row.productCode}' does not exist"
            }
            else {
                def manufacturerCode = row.manufacturerCode
                if (manufacturerCode instanceof Double) {
                    //command.errors.reject("error.manufacturerCode.invalid", "Row ${rowIndex}: Manufacturer code '${manufacturerCode}' must be a string")
                    command.warnings[index] << "Manufacturer code '${manufacturerCode}' must be a string"
                    manufacturerCode = manufacturerCode.toInteger().toString()
                }
                if (row.manufacturer && product.manufacturer && row.manufacturer != product.manufacturer) {
                    command.warnings[index] << "Manufacturer [${row.manufacturer}] is not the same as in the database [${product.manufacturer}]"
                }

                if (row.manufacturerCode && product.manufacturerCode && row.manufacturerCode != product.manufacturerCode) {
                    command.warnings[index] << "Manufacturer code [${row.manufacturerCode}] is not the same as in the database [${product.manufacturerCode}]"
                }
                def lotNumber = row.lotNumber
                if (lotNumber instanceof Double) {
                    //command.errors.reject("error.lotNumber.invalid", "Row ${rowIndex}: Lot number '${lotNumber}' must be a string")
                    command.warnings[index] << "Lot number '${lotNumber}' must be a string"
                    lotNumber = lotNumber.toInteger().toString()
                }
                def inventoryItem = InventoryItem.findByProductAndLotNumber(product, lotNumber)
                if (!inventoryItem) {
                    command.warnings[index] << "Inventory item for lot number '${lotNumber}' does not exist and will be created"
                }

                def expirationDate = null
                try {
                    if (row.expirationDate) {
                        if (row.expirationDate instanceof String) {
                            expirationDate = dateFormatter.parse(row.expirationDate)
                            calendar.setTime(expirationDate)
                            expirationDate = calendar.getTime()
                        }
                        else if (row.expirationDate instanceof Date) {
                            expirationDate = row.expirationDate
                        }
                        else if (row.expirationDate instanceof LocalDate) {
                            expirationDate = row.expirationDate.toDate()
                        }
                        else {
                            expirationDate = row.expirationDate
                            command.warnings[index] << "Expiration date '${row.expirationDate}' has unknown format ${row?.expirationDate?.class}"
                        }

                        if (expirationDate <= new Date()) {
                            command.warnings[index] << "Expiration date '${row.expirationDate}' is not valid"
                        }

                    }
                } catch (ParseException e) {
                    command.errors.reject("error.expirationDate.invalid", "Row ${rowIndex}: Product '${row.productCode}' must have a valid date (or no date)")
                }


                //def minLength = Math.min(product.name.length(),row.product.length())
                def levenshteinDistance = StringUtils.getLevenshteinDistance(product.name, row.product)
                if (row.product && levenshteinDistance > 0) {
                    command.warnings[index] << "Product name [${row.product}] does not appear to be the same as in the database [${product.name}] (Levenshtein distance: ${levenshteinDistance})"
                }

            }

            if ((row.quantity as int) < 0) {
                command.errors.reject("error.quantity.negative", "Row ${rowIndex}: Product '${row.productCode}' must have positive quantity");
            }


        }
    }

    /**
     *
     * @param command
     * @return
     */
    def importInventoryData(ImportDataCommand command) {
        def dateFormatter = new SimpleDateFormat("yy-mm")
        def importer = new InventoryExcelImporter(command.importFile.absolutePath)
        def data = importer.data
        assert data != null
        log.info "Data to be imported: " + data

        def transaction = new Transaction()
        transaction.transactionDate = command.date
        transaction.transactionType = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)
        transaction.transactionNumber = generateTransactionNumber()
        transaction.comment = "Imported from ${command.importFile.name} on ${new Date()}"
        transaction.inventory = command.location.inventory

        def calendar = Calendar.getInstance()
        command.data.eachWithIndex { row, index ->
            println "${index}: ${row}"
            def transactionEntry = new TransactionEntry()
            transactionEntry.quantity = row.quantity.toInteger()
            transactionEntry.comments = row.comments

            // Find an existing product, should fail if not found
            def product = Product.findByProductCode(row.productCode)
            assert product != null

            // Check the Levenshtein distance between the given name and stored product name (make sure they're close)
            println "Levenshtein distance: " + StringUtils.getLevenshteinDistance(product.name, row.product)
            //assert product.name == row.product

            // Handler for the lot number
            def lotNumber = row.lotNumber
            if (lotNumber instanceof Double) {
                lotNumber = lotNumber.toInteger().toString()
            }
            println "Lot Number: " + lotNumber

            // Expiration date should be the last day of the month
            def expirationDate = null
            if (row.expirationDate instanceof String) {
                expirationDate = dateFormatter.parse(row.expirationDate)
                calendar.setTime(expirationDate)
                expirationDate = calendar.getTime()
            }
            else if (row.expirationDate instanceof Date) {
                expirationDate = row.expirationDate
            }
            else if (row.expirationDate instanceof LocalDate) {
                expirationDate = row.expirationDate.toDate()
            }
            else {
                expirationDate = row.expirationDate
            }

            // Find or create an inventory item
            def inventoryItem = findOrCreateInventoryItem(product, lotNumber, expirationDate)
            println "Inventory item: " + inventoryItem.id + " " + inventoryItem.dateCreated + " " + inventoryItem.lastUpdated
            transactionEntry.inventoryItem = inventoryItem

			// Find the bin location
			if (row.binLocation) {
				def binLocation = Location.findByNameAndParentLocation(row.binLocation, command.location)
				log.info "Bin location: " + row.binLocation
				log.info "Location: " + command.location
				assert binLocation != null
				transactionEntry.binLocation = binLocation
			}

            transaction.addToTransactionEntries(transactionEntry)
        }
        transaction.save(flush:true, failOnError: true);
        println "Transaction ${transaction?.transactionNumber} saved successfully! "
        println "Added ${transaction?.transactionEntries?.size()} transaction entries"
        return data
    }

    def getTransactionDates() {
        //return Transaction.executeQuery('select distinct transactionDate from Transaction')
        def transactionDates = []
        //def results = Transaction.executeQuery("select distinct year(transactionDate), month(transactionDate), day(transactionDate) from Transaction where inventory = :inventory",
        //        [inventory:location.inventory])

        def results = Transaction.executeQuery("select transactionDate from Transaction")

        results.each { date ->
            //def date = new Date().updated([year: it[0], month: it[1], day: it[2]])
            date.clearTime()
            transactionDates << date
        }
        return transactionDates.unique().sort().reverse()
    }



    def getTransactionDates(location, product) {
        def transactionDates = []
        def startDate = new Date() - 365 * 5
        def endDate = new Date()
        (endDate..startDate).each {
            it.clearTime()
            transactionDates.add(it)
        }

        /*
        Calendar.with {
            (2010..2014).each { year ->
                (JANUARY..DECEMBER).each { month ->
                    def calendar = instance
                    calendar[YEAR] = year
                    calendar[MONTH] = month
                    calendar[DAY_OF_MONTH] = 1
                    def date = calendar.getTime()
                    date.clearTime()
                    transactionDates.add(date)
                }
            }
        }
        */

        println "transactionDates: " + transactionDates
        /*
        def criteria = TransactionEntry.createCriteria()
        def results = criteria.list {
            projections {
                transaction {
                    distinct ("transactionDate")
                }
            }

            transaction {
                eq("inventory", location.inventory)
            }
            inventoryItem {
                eq("product", product)
            }
        }
        results.each { date ->
            date.clearTime()
            transactionDates << date
        }
        */

        return transactionDates
    }

    def getDepotLocations() {
        def locations = Location.findAll("from Location as l where l.inventory is not null")
        return locations.findAll { it.isWarehouse() }

    }

    //@Cacheable("inventorySnapshotCache")
    /*
	def findInventorySnapshotByDateAndLocation(Date date, Location location) {
        def data = []
        if (location && date) {
            //def quantityMap = inventoryService.getQuantityByProductMap(location.inventory)
            def inventorySnapshots = InventorySnapshot.findAllByLocationAndDate(location, date)
            inventorySnapshots.each {
                data << [   date           : it.date,
                            location       : it.location.name,
                            category       : it.product?.category?.name,
                            productCode    : it.product.productCode,
                            product        : it.product.name,
                            productGroup   : null, //it?.product?.genericProduct?.name,
                            quantityOnHand : it.quantityOnHand,
                            tags           : it.product.tagsToString(),
                            unitOfMeasure  : it?.product?.unitOfMeasure?:"EA"
                ]
            }
        }
        return data;
	}
	*/



    def findInventorySnapshotByLocation(Location location) {
        def date = getMostRecentInventorySnapshotDate()
        return findInventorySnapshotByDateAndLocation(date, location)
    }

    def findInventorySnapshotByDateAndLocation(Date date, Location location) {
        def data = []
        if (location && date) {

            long startTime = System.currentTimeMillis()

            //, productGroups, tags
            //left outer join fetch product.productGroups as productGroups
            //left outer join fetch product.tags as tags
            //
            def results = InventorySnapshot.executeQuery("""
                    select i.date, i.location.name as location, product, category.name, i.quantityOnHand
                    from InventorySnapshot i, Product product, Category category
                    where i.location = :location
                    and i.date = :date
                    and i.product = product
                    and i.product.category = category
                    group by i.date, i.location.name, product
                    """, [location:location, date: date])

            // group by i.date, i.location.name, product


            def inventoryLevelsByProduct = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product.id }

            log.info "Results: " + results.size()
            log.info "Query response time: " + (System.currentTimeMillis() - startTime)
            startTime = System.currentTimeMillis()

            results.each {
                Product product = it[2]
                InventoryLevel inventoryLevel = inventoryLevelsByProduct[product.id] ? inventoryLevelsByProduct[product.id][0] : null
                data << [
                        date                : it[0],
                        location            : it[1],
                        category            : it[3],
                        productCode         : product.productCode,
                        product             : product.name,
                        productGroup        : product?.genericProduct?.name,
                        tags                : product.tagsToString(),
                        //productGroup        : it[5]*.description?.join(":")?:"", //product?.genericProduct?.name,
                        //tags                : it[6]*.tag?.join(","),
                        status              : inventoryLevel?.status,
                        quantityOnHand      : it[4],
                        minQuantity         : inventoryLevel?.minQuantity?:0,
                        maxQuantity         : inventoryLevel?.maxQuantity?:0,
                        reorderQuantity     : inventoryLevel?.reorderQuantity?:0,
                        unitOfMeasure       : product?.unitOfMeasure?:"EA"
                ]
            }
            log.info "Post-processing response time: " + (System.currentTimeMillis() - startTime)
        }
        return data
    }

    /**
     * Create inventory snapshots for all dates and locations.
     *
     * @return
     */
	def createOrUpdateInventorySnapshot() {
		def startTime = System.currentTimeMillis()
		def transactionDates = getTransactionDates()
		transactionDates.each { Date transactionDate ->
			transactionDate.clearTime()
			def locations = getDepotLocations()
			locations.each { location ->
				log.debug "Creating or updating inventory snapshot for date ${transactionDate}, location ${location.name} ..."
				createOrUpdateInventorySnapshot(transactionDate, location)
			}
			log.info "Created inventory snapshot for all locations and products on ${transactionDate} in " + (System.currentTimeMillis() - startTime) + " ms"
		}
	}


    def createOrUpdateInventorySnapshot(Date date) {
        def startTime = System.currentTimeMillis()
        date.clearTime()
        def locations = getDepotLocations()
        locations.each { location ->
			log.debug "Creating or updating inventory snapshot for date ${date}, location ${location.name} ..."
			createOrUpdateInventorySnapshot(date, location)
        }

        log.info "Created inventory snapshot for ${date} in " + (System.currentTimeMillis() - startTime) + " ms"
    }


    def createOrUpdateInventorySnapshot(Date date, Location location) {
        try {
			log.debug "Create or update inventory snapshot for location ${location.name} on date ${date}"
			// Only process locations with inventory
			if (location?.inventory) {
                def startTime = System.currentTimeMillis()

                String dateString = date.format("yyyy-MM-dd HH:mm:ss")
				//def productQuantityMap = getQuantityByProductMap(location.inventory)
				def quantityMap = getQuantityOnHandAsOfDate(location, date)
				def products = quantityMap.keySet();

				log.debug "Calculated quantity on hand for ${products?.size()} products in ${System.currentTimeMillis()-startTime} ms"
                def startTime2 = System.currentTimeMillis()
				def sql = new Sql(dataSource)
				if (sql) {
					try {
						sql.withBatch(1000) { stmt ->
							products.eachWithIndex { product, index ->
								//log.info "Saving inventory snapshot for product[${index}]: " + product
								def onHandQuantity = quantityMap[product]
								def insertStmt = "insert into inventory_snapshot(id,version,date,location_id,product_id,inventory_item_id,quantity_on_hand,date_created,last_updated) " +
										"values ('${UUID.randomUUID().toString()}', 0,'${dateString}','${location?.id}','${product?.id}',NULL,${onHandQuantity},now(),now()) " +
										"ON DUPLICATE KEY UPDATE quantity_on_hand=${onHandQuantity},last_updated=now()"
								stmt.addBatch(insertStmt)
							}
							stmt.executeBatch()
						}
					} catch (BatchUpdateException e) {
						log.error("Error executing batch update for location ${location.name} " + e.message, e)
					}
				}
				log.info ("Time to execute batch statements " + (System.currentTimeMillis() - startTime2) + " ms")
                log.info "Saved ${products?.size()} snapshots for products=ALL, location=${location}, date=${date.format("MMM-dd-yyyy")} in ${(System.currentTimeMillis() - startTime)} ms"
			}
        } catch (Exception e) {
            log.error("Unable to complete snapshot process", e)
        }
    }

    def createOrUpdateInventorySnapshot(Location location, Product product) {
        try {
            def dates = getTransactionDates(location, product)
            dates.each { date ->
                def quantity = getQuantity(product, location, date)
                log.info "Create or update snapshot for product ${product} at location ${location.name} on date ${date} = ${quantity} ${product.unitOfMeasure}"
				createOrUpdateInventorySnapshot(date, product, location, quantity)
            }
			log.info "Saved snapshot for product=${product.productCode}, location=${location}, dates=ALL"
        } catch (Exception e) {
            log.error("Unable to complete snapshot process", e)
        }
    }



    def createOrUpdateInventorySnapshot(Date date, Location location, Product product) {
        try {
            def inventorySnapshots = InventorySnapshot.countByDateAndLocation(date, location)
            log.debug "Date ${date}, location ${location}: " + inventorySnapshots
            if (inventorySnapshots == 0) {
                log.debug "Create or update snapshot for location ${location.name} on date ${date}"
                def quantity = getQuantity(product, location, date)
				createOrUpdateInventorySnapshot(date, product, location, quantity)
            }
			log.debug "Saved snapshot for product=${product.productCode}, location=${location}, date=${date}"
        } catch (Exception e) {
            log.error("Unable to complete snapshot process", e)
        }
    }


    def createOrUpdateInventorySnapshot(Date date, Product product, Location location, Integer onHandQuantity) {
        log.info "Updating snapshot for product " + product.name + " @ " + location.name
        try {
			def inventorySnapshot = InventorySnapshot.findWhere(date: date, location: location, product:product)
            if (!inventorySnapshot) {
                inventorySnapshot = new InventorySnapshot(date: date, location: location, product: product)
            }
            //def pendingQuantity = calculatePendingQuantity(product, location)
            inventorySnapshot.quantityOnHand = onHandQuantity?:0
            //inventorySnapshot.quantityInbound = pendingQuantity[0]?:0
            //inventorySnapshot.quantityOutbound = pendingQuantity[1]?:0
            //inventorySnapshot.lastUpdated = new Date()
            inventorySnapshot.save(flush:true)
        }
        catch (Exception e) {
            log.error("Error saving snapshot for product " + product.name + " and location " + location.name, e)
            throw e;
        }
    }




	def createOrUpdateInventoryItemSnapshot(Date date) {
		def startTime = System.currentTimeMillis()
		date.clearTime()
		def locations = getDepotLocations()
		locations.each { location ->
			log.debug "Creating or updating item snapshot for date ${date} and location ${location.name} ..."
			createOrUpdateInventoryItemSnapshot(date, location)
		}
		println "Created item snapshot for ${date} in " + (System.currentTimeMillis() - startTime) + " ms"
	}



    def createOrUpdateInventoryItemSnapshot(Date date, Location location) {

        try {
            def inventoryItemSnapshots = InventoryItemSnapshot.countByDateAndLocation(date, location)
            log.debug "Date ${date}, location ${location}: " + inventoryItemSnapshots
            log.debug "Create or update snapshot for location ${location.name} on date ${date}"
            // Only process locations with inventory
            if (location.inventory) {

				// Get quantity on hand for all products at a given location
				def onHandQuantityMap = getQuantityForInventory(location.inventory)
				def inventoryItems = onHandQuantityMap.keySet();
				log.debug "Saving item snapshots for ${inventoryItems?.size()} inventory items"

				def startTime = System.currentTimeMillis()
				def sql = new Sql(dataSource)
				if (sql) {
					String dateString = date.format("yyyy-MM-dd HH:mm:ss")
					sql.withBatch(1000) { stmt ->
						inventoryItems.eachWithIndex { inventoryItem, index ->
							//log.info "Saving snapshot for product[${index}]: " + product
							def onHandQuantity = onHandQuantityMap[inventoryItem]

							//stmt.addBatch(date:date.format("yyyy-MM-dd hh:mm:ss"), locationId:location.id, productId:product.id, inventoryItemId:null, quantityOnHand:onHandQuantity)
							def insertStmt = "insert into inventory_item_snapshot(id,version,date,location_id,product_id,inventory_item_id,quantity_on_hand,date_created,last_updated) " +
									"values ('${UUID.randomUUID().toString()}', 0,'${dateString}','${location?.id}','${inventoryItem?.product?.id}','${inventoryItem?.id}',${onHandQuantity},now(),now()) " +
									"ON DUPLICATE KEY UPDATE quantity_on_hand=${onHandQuantity},last_updated=now()"
							stmt.addBatch(insertStmt)
						}
						stmt.executeBatch()
					}
				}
				log.debug ("Time to execute batch statements " + (System.currentTimeMillis() - startTime) + " ms")
                log.info "Saved item snapshot for products: ALL, location=${location.name}, date=${date.format("MMM/dd/yyyy")}" + ": " + (System.currentTimeMillis() - startTime) + " ms"

            }
        } catch (Exception e) {
            log.error("Unable to complete snapshot process", e)
        }
    }

	def createOrUpdateInventoryItemSnapshot(Date date, InventoryItem inventoryItem, Location location, Integer quantityOnHand) {
		log.info "Updating snapshot for product " + inventoryItem?.product?.name + " at location " + location.name
		try {
			def inventoryItemSnapshot = InventoryItemSnapshot.findWhere(date: date, location: location, product:inventoryItem?.product, inventoryItem: inventoryItem)
			if (!inventoryItemSnapshot) {
				inventoryItemSnapshot = new InventoryItemSnapshot(date: date, location: location, product: inventoryItem?.product, inventoryItem: inventoryItem)
			}
			//def pendingQuantity = calculatePendingQuantity(product, location)
			inventoryItemSnapshot.quantityOnHand = quantityOnHand?:0
			//inventorySnapshot.quantityInbound = pendingQuantity[0]?:0
			//inventorySnapshot.quantityOutbound = pendingQuantity[1]?:0
			//inventorySnapshot.lastUpdated = new Date()
			inventoryItemSnapshot.save()
		}
		catch (Exception e) {
			log.error("Error saving snapshot for inventory item " + inventoryItem + " and location " + location.name, e)
			throw e;
		}
	}

	/**
	 * Calculate pending quantity for a given product and location.
	 *
	 * @param product
	 * @param location
     * @return
     */
	def calculatePendingQuantity(product, location) {
        def inboundQuantity = 0;
        def outboundQuantity = 0;
        try {
            def shipmentItems = ShipmentItem.withCriteria {
                shipment {
                    eq("destination", location)
                }
                or {
                    inventoryItem {
                        eq("product", product)
                    }
                    eq("product", product)
                }
            }
            inboundQuantity = shipmentItems.sum { it.quantity }
            shipmentItems = ShipmentItem.withCriteria {
                shipment {
                    eq("origin", location)
                }
                or {
                    inventoryItem {
                        eq("product", product)
                    }
                    eq("product", product)
                }
            }
            outboundQuantity = shipmentItems.sum { it.quantity }

        } catch (Exception e) {
            log.info ("Error " + e.message)
        }

        [inboundQuantity, outboundQuantity]
    }

    def getProductsWithTransactions(Location location) {
        def products = TransactionEntry.createCriteria().list() {
            projections {
                inventoryItem {
                    distinct "product"
                }
            }
            transaction {
                eq("inventory", location.inventory)
            }
        }
        return products
    }


    def getDistinctProducts(Location location) {
        def productCount = TransactionEntry.createCriteria().get {
            projections {
                inventoryItem {
                    countDistinct "product.id"
                }
            }
            transaction {
                eq("inventory", location.inventory)
            }
        }
        log.info "Product count: " + productCount


        def params = [:]
        params.max = Math.min(params.max?.toInteger() ?: 1000, 1000)
        def offset = 0
        while(offset <= productCount) {
            params.offset = params.offset ? params.offset.toInteger() : 0
            def products = TransactionEntry.createCriteria().list(params) {
                projections {
                    inventoryItem {
                        distinct "product.id"
                    }
                }
                transaction {
                    eq("inventory", location.inventory)
                }
            }
            log.info "products " + products?.size()
            offset += params.max
        }
    }

    /**
     * Build quantity map for all products at a given location.
     *
     * @param location
     * @return
     */
    def getQuantityMap(Location location) {
        def quantityMap = getMostRecentQuantityOnHand(location)
        def productIds = getDistinctProducts(location)
        productIds.eachWithIndex { productId, index ->
            Product product = Product.load(productId)
            def stockCountEntry = quantityMap[product.id]
            def quantityOnHand = calculateQuantityOnHand(product, location, stockCountEntry?.quantityOnHand?:0, stockCountEntry?.stockCountDate)
			quantityMap[product.productCode] = quantityOnHand

        }
        return quantityMap
    }

    /**
     * Calculate the quantity on hand for a given inventory item and location.
     *
     * @param inventoryItem
     * @param location
     */
    def calculateQuantityOnHand(Product product, Location location, Long quantityOnHand, Timestamp stockCountDate) {
        def quantityDebit = getQuantityChangeSince(product, location, TransactionCode.DEBIT, stockCountDate)[0]?:0
        def quantityCredit = 0//getQuantityChangeSince(product, location, TransactionCode.CREDIT, stockCountDate)[0]?:0
        return quantityOnHand - quantityDebit + quantityCredit
    }

    //resultTransformer CriteriaSpecification.ALIAS_TO_ENTITY_MAP

    def getMostRecentQuantityOnHand(Location location) {
        def quantityMap = [:]
        def results = Transaction.executeQuery( """
                                    SELECT te.inventoryItem.product.id, max(te.transaction.transactionDate), sum(te.quantity)
                                    FROM TransactionEntry te
                                    JOIN te.transaction
                                    JOIN te.inventoryItem
                                    WHERE te.transaction.inventory = :inventory
                                    AND te.transaction.transactionType.transactionCode = :transactionCode
                                    group by te.transaction.transactionDate, te.inventoryItem.product
                                    order by te.transaction.transactionDate desc
                                    """, [inventory:location.inventory, transactionCode:TransactionCode.PRODUCT_INVENTORY] );


        results = results.collect { row ->
            quantityMap[row[0]] = [stockCountDate: row[1], quantityOnHand: row[2] ]
        }

        return quantityMap
    }


	/**
	 * Get most recent product inventory transaction.
	 *
	 * @param product
	 * @param location
     * @return
     */
    def getMostRecentQuantityOnHand(Product product, Location location) {
        def results = Transaction.executeQuery( """
                                    SELECT max(te.transaction.transactionDate), sum(te.quantity)
                                    FROM TransactionEntry te
                                    JOIN te.transaction
                                    JOIN te.inventoryItem
                                    WHERE te.inventoryItem.product = :product
                                    AND te.transaction.inventory = :inventory
                                    AND te.transaction.transactionType.transactionCode = :transactionCode
                                    group by te.transaction.transactionDate
                                    order by te.transaction.transactionDate desc
                                    """, [max: 1, product:product, inventory:location.inventory, transactionCode:TransactionCode.PRODUCT_INVENTORY] );

        //return results ? results[0] : null
        return results
    }

    /**
     * Get quantity delta since the given date for the given product, location, and transaction code (debit, credit).
     *
     * @param product
     * @param location
     * @param transactionCode
     * @param transactionDate
     * @return
     */
    def getQuantityChangeSince(Product product, Location location, TransactionCode transactionCode, Date transactionDate) {
        def results
        if (transactionDate) {
            results = Transaction.executeQuery( """
                                        SELECT sum(te.quantity)
                                        FROM TransactionEntry te
                                        JOIN te.transaction
                                        JOIN te.inventoryItem
                                        WHERE te.inventoryItem.product = :product
                                        AND te.transaction.inventory = :inventory
                                        AND te.transaction.transactionType.transactionCode = :transactionCode
                                        AND te.transaction.transactionDate >= :transactionDate
                                        """, [product:product, inventory:location.inventory, transactionCode:transactionCode, transactionDate: transactionDate] );
        }
        else {
            results = Transaction.executeQuery( """
                                        SELECT sum(te.quantity)
                                        FROM TransactionEntry te
                                        JOIN te.transaction
                                        JOIN te.inventoryItem
                                        WHERE te.inventoryItem.product = :product
                                        AND te.transaction.inventory = :inventory
                                        AND te.transaction.transactionType.transactionCode = :transactionCode
                                        """, [product:product, inventory:location.inventory, transactionCode:transactionCode] );

        }
        return results
    }

    /**
     * Generic product summary widget on the dashboard.
     *
     * @param location
     * @return
     */
    def getGenericProductSummary(location) {

        def genericProductMap = [:]


        // Create list of entries with quantity on hand for each product
        def entries = []
        def quantityMap = getQuantityOnHandByProduct(location)
        quantityMap.each { key, value ->
            entries << [product: key, genericProduct: key.genericProduct, currentQuantity: (value>0)?value:0]    // make sure currentQuantity >= 0
        }
        entries.sort { it.product.name }


        // Get the inventory levels for all products at the given location
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory)?.groupBy { it.product }

        // Group entries by generic product
        genericProductMap = entries.inject([:].withDefault { [
                status:null,
                name:null,
                minQuantity:0,
                reorderQuantity:0,
                maxQuantity:0,
                currentQuantity:0,
                hasPreferred:false,
                lastUpdated:null,
                // Debugging information
                productCount:0,
                product:null,
                genericProduct:null,
                type:null,
                inventoryLevel:null,
                message:"",
                products:[]]
        } ) { map, entry ->
            def product = entry?.product
            def inventoryLevel = (inventoryLevelMap[product])?inventoryLevelMap[product][0]:null
            def nameKey = entry?.genericProduct?.name?:entry?.product?.name
            map[nameKey].name = nameKey

            if (entry?.genericProduct) {
                map[nameKey].genericProduct = entry?.genericProduct?.id
                map[nameKey].type = "Generic Product"
            }
            else {
                map[nameKey].product = entry?.product?.id
                map[nameKey].type = "Product"
            }
            map[nameKey].currentQuantity += entry.currentQuantity?:0
            map[nameKey].productCount++

            // If there's no preferred already
            if (!map[nameKey].hasPreferred) {
                // If this inventory level is preferred
                if (inventoryLevel?.preferred) {
                    map[nameKey].hasPreferred = true
                    map[nameKey].minQuantity = inventoryLevel?.minQuantity?:0
                    map[nameKey].reorderQuantity = inventoryLevel?.reorderQuantity?:0
                    map[nameKey].maxQuantity = inventoryLevel?.maxQuantity?:0
                    map[nameKey].inventoryLevel = inventoryLevel
                    map[nameKey].lastUpdated = inventoryLevel?.lastUpdated
                    map[nameKey].message = "INFO: Using preferred inventory level (${product?.productCode})."
                }
                // Otherwise, use the inventory level that was last updated
                else {
                    // jgreenspan says:
                    // I think that if a flag is not set then it should default to the most recently updated inventory level.
                    // If this is difficult to do then the report can show an error when there is no flag. Then this will force us to select a flag.
                    def lastUpdated1 = inventoryLevel?.lastUpdated
                    def lastUpdated2 = map[nameKey]?.lastUpdated

                    // If there's no existing inventory level OR current inventory level was updated after the existing
                    // inventory level then we should use the current inventory level
                    if (!lastUpdated2 || (lastUpdated1 && lastUpdated2 && lastUpdated1.after(lastUpdated2))) {
                        map[nameKey].minQuantity = inventoryLevel?.minQuantity?:0
                        map[nameKey].reorderQuantity = inventoryLevel?.reorderQuantity?:0
                        map[nameKey].maxQuantity = inventoryLevel?.maxQuantity?:0
                        map[nameKey].inventoryLevel = inventoryLevel
                        map[nameKey].lastUpdated = lastUpdated1
                    }
                    map[nameKey].message = "WARN: No preferred items, using last updated inventory level (${inventoryLevel?.product?.productCode})."
                }
            }
            // If there are more than one (preferred) inventory levels then we should use the latest
            else {
                if (inventoryLevel?.preferred) {
                    map[nameKey].minQuantity = 0
                    map[nameKey].reorderQuantity = 0
                    map[nameKey].maxQuantity = 0
                    map[nameKey].message = "ERROR: More than one preferred item, cannot determine inventory level to use."
                }
            }

            map
        }


        // Assign status based on inventory level values
        genericProductMap.each { k, v ->
            genericProductMap[k].status = getStatusMessage(null, v.minQuantity, v.reorderQuantity, v.maxQuantity, v.currentQuantity)
            //InventoryUtil.getStatusMessage(null, v.minQuantity, v.reorderQuantity, v.maxQuantity, v.currentQuantity)
        }
        return genericProductMap.values().groupBy { it.status }

    }

    /**
     * The logic for this method is located in several locations because the logic is specific to
     * a single product / inventoryLevel, whereas this method is for the entire product group.
     *
     * FIXME Move to InteventoryUtil when complete
     * FIXME Add unit tests for this logic
     *
     * @param inventoryStatus
     * @param minQuantity
     * @param reorderQuantity
     * @param maxQuantity
     * @param currentQuantity
     * @return
     */
    def String getStatusMessage(inventoryStatus, minQuantity, reorderQuantity, maxQuantity, currentQuantity) {
        def statusMessage = ""
        if (inventoryStatus == InventoryStatus.SUPPORTED  || !inventoryStatus) {
            if (currentQuantity <= 0) {
                statusMessage = "STOCK_OUT"
            }
            else {
                if (minQuantity && minQuantity > 0 && currentQuantity <= minQuantity ) {
                    statusMessage = "LOW_STOCK"
                }
                else if (reorderQuantity && reorderQuantity > 0 && currentQuantity <= reorderQuantity ) {
                    statusMessage = "REORDER"
                }
                else if (maxQuantity && maxQuantity > 0 && currentQuantity > maxQuantity ) {
                    statusMessage = "OVERSTOCK"
                }
                else if (currentQuantity > 0) {
					statusMessage = "IN_STOCK"
				}
                else {
                    statusMessage = "OBSOLETE"
                }
            }
        }
        else if (inventoryStatus == InventoryStatus.NOT_SUPPORTED) {
            statusMessage = "NOT_SUPPORTED"
        }
        else if (inventoryStatus == InventoryStatus.SUPPORTED_NON_INVENTORY) {
            statusMessage = "SUPPORTED_NON_INVENTORY"
        }
        else {
            statusMessage = "UNAVAILABLE"
        }
        //log.info "getStatusMessage(${inventoryStatus}, ${minQuantity}, ${reorderQuantity}, ${maxQuantity}, ${currentQuantity}) = ${statusMessage}"
        return statusMessage
    }

    /**
     * Get fast moving items based on requisition data.
     *
     * @param location
     * @param date
     * @param max
     * @return
     */
    def getFastMovers(location, date, max) {
        def startTime = System.currentTimeMillis()
        def data = [:]
        try {
            data.location = location.id
            data.startDate = date-30
            data.endDate = date

            def criteria = RequisitionItem.createCriteria()
            def results = criteria.list {
                requisition {
                    eq("origin", location)
                    between("dateRequested", date-30, date)
                }
                projections {
                    //product {
                    //    groupProperty('id')
                    //    groupProperty('name')
                    //    groupProperty('productCode')
                    //}
                    groupProperty("product")
                    countDistinct('id', "occurrences")
                    sum("quantity", "quantity")
                }
                order('occurrences','desc')
                order('quantity','desc')
                if (max) { maxResults(max) }
            }

            def quantityMap = getCurrentInventory(location)
            //println "quantityMap: " + quantityMap

            def count = 1;
            data.results = results.collect {

                def quantityOnHand = quantityMap[it[0]]?:0
                [
                        rank: count++,
                        id: it[0].id,
                        productCode: it[0].productCode,
                        name: it[0].name,
                        //genericProduct: it[0]?.genericProduct?.name?:"",
                        category: it[0]?.category?.name?:"",
                        requisitionCount: it[1],
                        quantityRequested: it[2],
                        quantityOnHand: quantityOnHand,
                ]
            }
            data.responseTime = (System.currentTimeMillis() - startTime) + " ms"


        } catch (Exception e) {
            log.error("Error occurred while getting requisition items " + e.message, e)
            data = e.message
        }
        return data
    }

    Map getBinLocationReport(Location location) {

        Map binLocationReport = [:]

        final List transactionEntries = getTransactionEntriesWithAssociations(location)
        List binLocations = getQuantityByBinLocation(transactionEntries, true)

        binLocationReport.data = binLocations
        binLocationReport.summary = getBinLocationSummary(binLocations)

        return binLocationReport
    }


    List getBinLocationSummary(List binLocations) {

        List results = []
        List defaultStatuses = ["inStock", "outOfStock"]

        def byStatus = { binLocation -> binLocation.quantity > 0 ? "inStock" : "outOfStock" }
        def binLocationsByStatus = binLocations.groupBy(byStatus)

        defaultStatuses.each { status ->
            def list = binLocationsByStatus[status]
            String messageCode = "binLocationSummary.${status}.label"
            String label = messageService.getMessage(messageCode)
            if (!list) {
                results << [status:status, label: label, count: 0]
            }
            else {
                results << [status: status, label: label, count: list.size()]
            }
        }
        return results
    }


    List getTransactionEntriesWithAssociations(Location location) {
        def startTime = System.currentTimeMillis()

        if (!location?.inventory) {
            throw new RuntimeException("Location must have an inventory")
        }

        def criteria = TransactionEntry.createCriteria();
        def transactionEntries = criteria.list {
            // eager fetch transaction and transaction type
			fetchMode("transaction", org.hibernate.FetchMode.JOIN)
            fetchMode("transaction.transactionType", org.hibernate.FetchMode.JOIN)

            transaction {
                eq("inventory", location.inventory)
                order("transactionDate", "asc")
                order("dateCreated", "asc")
            }
        }

		log.info "transactionEntries " + transactionEntries.size()

        log.info "getTransactionEntriesByInventory(): " + (System.currentTimeMillis() - startTime) + " ms"

        return transactionEntries;
    }


	def getAvailableBinLocations(Location location, Product product) {
		return getAvailableBinLocations(location, [product], false)
	}

	def getAvailableBinLocations(Location location, Product product, boolean excludeOutOfStock) {
		return getAvailableBinLocations(location, [product], excludeOutOfStock)
	}

	def getAvailableBinLocations(Location location, List products, boolean excludeOutOfStock = false) {
		def availableBinLocations = getProductQuantityByBinLocation(location, products)

		availableBinLocations = availableBinLocations.collect {
            return new AvailableItem(
					inventoryItem: it?.inventoryItem,
					binLocation: it?.binLocation,
					quantityAvailable: it.quantity
            )
		}
		availableBinLocations = availableBinLocations.findAll { it.quantityAvailable > 0 }

		// Sort bins  by available quantity
		availableBinLocations = availableBinLocations.sort { a, b ->
			a?.quantityAvailable <=> b?.quantityAvailable
		}

		// Sort empty expiration dates last
		availableBinLocations = availableBinLocations.sort { a, b ->
			!a?.inventoryItem?.expirationDate ?
					!b?.inventoryItem?.expirationDate ? 0 : 1 :
					!b?.inventoryItem?.expirationDate ? -1 :
							a?.inventoryItem?.expirationDate <=> b?.inventoryItem?.expirationDate
		}


		return availableBinLocations
	}


	def getAvailableItems(Location location, Product product) {
		return getAvailableItems(location, [product])
	}

	def getAvailableItems(Location location, List products, boolean excludeOutOfStock = true) {
		def availableItemsMap = getQuantityByInventoryItemMap(location, products)

		def inventoryItems = products.collect { it.inventoryItems }.flatten()
		log.info "inventory items: " + inventoryItems
		def availableItems = inventoryItems.collect {
			new AvailableItem(inventoryItem: it, binLocation: null, quantityAvailable: availableItemsMap[it]?:0)
		}

		if (excludeOutOfStock) {
			availableItems = availableItems.findAll { it.quantityAvailable > 0 }
		}

		return availableItems
	}

	def getAvailableProducts(Location location, Product product) {
		return getAvailableProducts(location, [product])
	}

	def getAvailableProducts(Location location, List products) {
		def availableItemsMap = getQuantityByInventoryItemMap(location, products)

		def inventoryItems = products.collect { it.inventoryItems }.flatten()
		log.info "inventory items: " + inventoryItems
		def availableItems = inventoryItems.collect { InventoryItem inventoryItem ->
			return [
					"inventoryItem.id": inventoryItem.id,
					lotNumber: inventoryItem.lotNumber,
					expirationDate: inventoryItem.expirationDate,
					quantity: availableItemsMap[inventoryItem]
			]
		}
		availableItems = availableItems.findAll { it.quantity > 0 }
		return availableItems
	}

}

