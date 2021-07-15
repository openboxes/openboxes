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
import groovy.sql.BatchingStatementWrapper
import groovy.sql.Sql
import groovy.time.TimeCategory
import groovyx.gpars.GParsPool
import org.apache.commons.lang.StringEscapeUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.hibernate.Criteria
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.ApplicationExceptionEvent
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.jobs.RefreshProductAvailabilityJob
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductActivityCode
import org.pih.warehouse.product.ProductAvailability
import org.pih.warehouse.product.ProductSearch
import org.pih.warehouse.product.ProductType

class ProductAvailabilityService {

    boolean transactional = true

    def dataSource
    def grailsApplication
    def persistenceInterceptor
    def locationService
    def inventoryService
    def dataService
    def picklistService

    def triggerRefreshProductAvailability(String locationId, List<String> productIds, Boolean forceRefresh) {
        log.info "Triggering refresh product availability"
        use(TimeCategory) {
            Boolean delayStart = grailsApplication.config.openboxes.jobs.refreshProductAvailabilityJob.delayStart
            def delayInMilliseconds = delayStart ?
                    grailsApplication.config.openboxes.jobs.refreshProductAvailabilityJob.delayInMilliseconds : 0
            Date runAt = new Date() + delayInMilliseconds.milliseconds
            log.info "Triggering refresh product availability with ${delayInMilliseconds} ms delay"
            RefreshProductAvailabilityJob.schedule(runAt,
                    [locationId: locationId, productIds: productIds, forceRefresh: forceRefresh])
        }
    }

    def refreshProductAvailability(Boolean forceRefresh) {
        // Compute bin locations from transaction entries for all products over all depot locations
        // Uses GPars to improve performance (OBNAV Benchmark: 5 minutes without, 45 seconds with)
        def startTime = System.currentTimeMillis()
        GParsPool.withPool {
            locationService.depots.eachParallel { Location loc ->
                persistenceInterceptor.init()
                Location location = Location.get(loc.id)
                refreshProductAvailability(location, forceRefresh)
                persistenceInterceptor.flush()
                persistenceInterceptor.destroy()
            }
        }
        log.info "Refreshed product availability in ${System.currentTimeMillis() - startTime}ms"
    }

    def refreshProductAvailability(Product product, Boolean forceRefresh) {
        // Compute bin locations from transaction entries for specific product over all depot locations
        // Uses GPars to improve performance (OBNAV Benchmark: 5 minutes without, 45 seconds with)
        def startTime = System.currentTimeMillis()
        GParsPool.withPool {
            locationService.depots.eachParallel { Location loc ->
                persistenceInterceptor.init()
                Location location = Location.get(loc.id)
                refreshProductAvailability(location, product, forceRefresh)
                persistenceInterceptor.flush()
                persistenceInterceptor.destroy()
            }
        }
        log.info "Refreshed product availability in ${System.currentTimeMillis() - startTime}ms"
    }

    def refreshProductAvailability(Location location, Boolean forceRefresh) {
        log.info "Refreshing product availability location (${location}), forceRefresh (${forceRefresh}) ..."
        def startTime = System.currentTimeMillis()
        List binLocations = calculateBinLocations(location)
        saveProductAvailability(location, null, binLocations, forceRefresh)
        log.info "Refreshed  ${binLocations?.size()} product availability records for location (${location}) in ${System.currentTimeMillis() - startTime}ms"
    }

    def refreshProductAvailability(Location location, Product product, Boolean forceRefresh) {
        log.info "Refreshing product availability location ${location}, product ${product}, forceRefresh ${forceRefresh}..."
        def startTime = System.currentTimeMillis()
        List binLocations = calculateBinLocations(location, product)
        saveProductAvailability(location, product, binLocations, forceRefresh)
        log.info "Refreshed ${binLocations?.size()} product availability records for product (${product}) and location (${location}) in ${System.currentTimeMillis() - startTime}ms"
    }

    def calculateBinLocations(Location location, Date date) {
        def binLocations = inventoryService.getBinLocationDetails(location, date)
        binLocations = transformBinLocations(binLocations, [], [])
        return binLocations
    }

    def calculateBinLocations(Location location) {
        def binLocations = inventoryService.getBinLocationDetails(location)
        def picked = picklistService.getQuantityPickedByProductAndLocation(location, null)
        def onHold = getQuantityOnHold(location, null)
        binLocations = transformBinLocations(binLocations, picked, onHold)
        return binLocations
    }

    def calculateBinLocations(Location location, Product product) {
        def binLocations = inventoryService.getProductQuantityByBinLocation(location, product, Boolean.TRUE)
        def picked = picklistService.getQuantityPickedByProductAndLocation(location, product)
        def onHold = getQuantityOnHold(location, product)
        binLocations = transformBinLocations(binLocations, picked, onHold)
        return binLocations
    }

    def getQuantityOnHold(Location location, Product product){
        return ProductAvailability.createCriteria().list {
            projections {
                groupProperty("binLocation.id", "binLocation")
                groupProperty("inventoryItem.id", "inventoryItem")
                sum("quantityOnHand", "quantityOnHold")
            }
            eq("location", location)
            if (product) {
                eq("product", product)
            }
            inventoryItem {
                eq("lotStatus", LotStatusCode.RECALLED)
            }
        }.collect { [binLocation: it[0], inventoryItem: it[1], quantityOnHold: it[2]] }
    }

    def saveProductAvailability(Location location, Product product, List binLocations, Boolean forceRefresh) {
        log.info "Saving product availability for product=${product?.productCode}, location=${location}"
        def batchSize = ConfigurationHolder.config.openboxes.inventorySnapshot.batchSize ?: 1000
        def startTime = System.currentTimeMillis()

        try {
            Sql sql = new Sql(dataSource)

            // Execute SQL in batches
            sql.withBatch(batchSize) { BatchingStatementWrapper stmt ->
                // If we need to force refresh then we want to set quantity on hand for all
                // matching records to 0.
                if (forceRefresh) {
                    // Allows the SQL IFNULL to work properly
                    //   IFNULL(null, product_id)
                    //   IFNULL('10003', product_id)
                    String productId = product?.id?"'${product?.id}'":null
                    String forceRefreshStatement = String.format(
                            "delete from product_availability " +
                                    "where location_id = '${location.id}' " +
                                    "and product_id = IFNULL(%s, product_id);", productId)
                    stmt.addBatch(forceRefreshStatement)
                }
                binLocations.eachWithIndex { Map binLocationEntry, index ->
                    String insertStatement = generateInsertStatement(location, binLocationEntry)
                    stmt.addBatch(insertStatement)
                }
                stmt.executeBatch()
            }
            log.info "Saved ${binLocations?.size()} records for location ${location} in ${System.currentTimeMillis() - startTime}ms"

            // Refresh inventory snapshot
            refreshInventorySnapshot(location, product, forceRefresh)

        } catch (Exception e) {
            log.error("Error executing batch update for ${location.name}: " + e.message, e)
            publishEvent(new ApplicationExceptionEvent(e, location))
            throw e;
        }
    }

    String generateInsertStatement(Location location, Map entry) {
        String productId = "${StringEscapeUtils.escapeSql(entry.product?.id)}"
        String productCode = "${StringEscapeUtils.escapeSql(entry.product?.productCode)}"
        String lotNumber = entry?.inventoryItem?.lotNumber ?
                "'${StringEscapeUtils.escapeSql(entry?.inventoryItem?.lotNumber)}'" : "'DEFAULT'"
        String inventoryItemId = entry?.inventoryItem?.id ?
                "'${StringEscapeUtils.escapeSql(entry?.inventoryItem?.id)}'" : "NULL"
        String binLocationId = entry?.binLocation?.id ?
                "'${StringEscapeUtils.escapeSql(entry?.binLocation?.id)}'" : "NULL"
        String binLocationName = entry?.binLocation?.name ?
                "'${StringEscapeUtils.escapeSql(entry?.binLocation?.name)}'" : "'DEFAULT'"

        Integer onHandQuantity = entry.quantity?:0
        Integer quantityAllocated = entry.quantityAllocated?:0
        Integer quantityOnHold = entry.quantityOnHold?:0
        Integer quantityAvailableToPromise = calculateQuantityAvailableToPromise(onHandQuantity, quantityAllocated, quantityOnHold)
        def insertStatement =
                "INSERT INTO product_availability (id, version, location_id, product_id, product_code, " +
                        "inventory_item_id, lot_number, bin_location_id, bin_location_name, " +
                        "quantity_on_hand, quantity_allocated, quantity_on_hold, quantity_available_to_promise, date_created, last_updated) " +
                        "values ('${UUID.randomUUID().toString()}', 0, '${location?.id}', " +
                        "'${productId}', '${productCode}', " +
                        "${inventoryItemId}, ${lotNumber}, " +
                        "${binLocationId}, ${binLocationName}, ${onHandQuantity}, ${quantityAllocated}, ${quantityOnHold}, ${quantityAvailableToPromise}, now(), now()) " +
                        "ON DUPLICATE KEY UPDATE quantity_on_hand=${onHandQuantity}, quantity_allocated=${quantityAllocated}, quantity_on_hold=${quantityOnHold}, quantity_available_to_promise=${quantityAvailableToPromise}, version=version+1, last_updated=now()"
        return insertStatement
    }

    Integer calculateQuantityAvailableToPromise(Integer onHandQuantity, Integer quantityAllocated, Integer quantityOnHold) {
        if (onHandQuantity == quantityOnHold) {
            return 0
        }
        def quantityAvailableToPromise = onHandQuantity - quantityAllocated - quantityOnHold
        return quantityAvailableToPromise >= 0 ? quantityAvailableToPromise : 0
    }

    def transformBinLocations(List binLocations, List picked, List onHold) {
        def binLocationsTransformed = binLocations.collect {
            [
                product          : [id: it?.product?.id, productCode: it?.product?.productCode, name: it?.product?.name],
                inventoryItem    : [id: it?.inventoryItem?.id, lotNumber: it?.inventoryItem?.lotNumber, expirationDate: it?.inventoryItem?.expirationDate],
                binLocation      : [id: it?.binLocation?.id, name: it?.binLocation?.name],
                quantity         : it.quantity,
                quantityAllocated: picked ? (picked.find { row -> row.binLocation == it?.binLocation?.id && row.inventoryItem == it?.inventoryItem?.id }?.quantityAllocated?:0) : 0,
                quantityOnHold   : onHold ? (onHold.find { row -> row.binLocation == it?.binLocation?.id && row.inventoryItem == it?.inventoryItem?.id }?.quantityOnHold?:0) : 0
            ]
        }

        // Attempting to prevent deadlock due to gap locks
        binLocationsTransformed = binLocationsTransformed.sort { a, b ->
            a?.binLocation?.name <=> b?.binLocation?.name ?:
                    a?.product?.productCode <=> b?.product?.productCode ?:
                            a?.inventoryItem?.lotNumber <=> b?.inventoryItem?.lotNumber
        }

        return binLocationsTransformed
    }

    def deleteProductAvailability() {
        deleteProductAvailability(null, null)
    }

    def deleteProductAvailability(Location location) {
        deleteProductAvailability(location, null)
    }

    def deleteProductAvailability(Location location, Product product) {
        Map params = [:]

        String deleteStmt = """delete from ProductAvailability pa where 1=1"""

        if (location) {
            deleteStmt += " and pa.location = :location"
            params.put("location", location)
        }

        if (product) {
            deleteStmt += " and pa.product = :product"
            params.put("product", product)
        }

        def results = ProductAvailability.executeUpdate(deleteStmt, params)
        log.info "Deleted ${results} records for location ${location}, product ${product}"
    }

    def refreshInventorySnapshot(Location location, Product product, Boolean forceRefresh) {
        String productId = product ? "'${product?.id}'" : null
        if (forceRefresh) {
            String forceRefreshStatement = """
                DELETE FROM inventory_snapshot 
                WHERE date = DATE_ADD(CURDATE(),INTERVAL 1 DAY)
                AND location_id = '${location.id}' 
                AND product_id = IFNULL(${productId}, product_id);
            """
            dataService.executeStatement(forceRefreshStatement)
        }
        String updateStatement = """
            REPLACE INTO inventory_snapshot 
            (
                id, version, date, location_id, product_id, product_code, 
                inventory_item_id, lot_number, bin_location_id, bin_location_name, 
                quantity_on_hand, date_created, last_updated
            ) 
            SELECT 
                id, version, DATE_ADD(CURDATE(),INTERVAL 1 DAY), location_id, product_id, product_code, 
                inventory_item_id, lot_number, bin_location_id, bin_location_name,
                quantity_on_hand, date_created, last_updated 
            FROM product_availability 
            WHERE location_id = '${location.id}' 
            AND product_id = IFNULL(${productId}, product_id);
        """
        dataService.executeStatement(updateStatement)
    }

    def getQuantityOnHand(Location location) {
        def productAvailability = ProductAvailability.createCriteria().list {
            resultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
            projections {
                // Need to use alias other than product to prevent conflict
                groupProperty("product", "p")
                sum("quantityOnHand", "quantityOnHand")
            }
            eq("location", location)
        }

        return productAvailability
    }

    def getQuantityOnHand(InventoryItem inventoryItem) {
        def quantityOnHand = ProductAvailability.createCriteria().get {
            projections {
                sum("quantityOnHand")
            }
            eq("inventoryItem", inventoryItem)
        }

        return quantityOnHand
    }

    Integer getQuantityOnHand(Product product, Location location) {
        def productAvailability = ProductAvailability.createCriteria().list {
            resultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
            projections {
                sum("quantityOnHand", "quantityOnHand")
            }
            eq("location", location)
            eq("product", product)
        }

        return productAvailability?.get(0)?.quantityOnHand ?: 0
    }

    def getQuantityOnHand(List<Product> products, Location location) {
        def productAvailability = ProductAvailability.createCriteria().list {
            resultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
            projections {
                // Need to use alias other than product to prevent conflict
                groupProperty("product", "prod")
                sum("quantityOnHand", "quantityOnHand")
            }
            eq("location", location)
            'in'("product", products)
        }

        return productAvailability
    }

    def getQuantityOnHandInBinLocation(InventoryItem inventoryItem, Location binLocation) {
        def quantityOnHand = ProductAvailability.createCriteria().get {
            projections {
                sum("quantityOnHand")
            }
            eq("inventoryItem", inventoryItem)
            eq("binLocation", binLocation)
        }

        return quantityOnHand
    }

    Map<Product, Integer> getCurrentInventory(Location location) {
        return getQuantityOnHandByProduct(location)
    }

    Map<Product, Integer> getQuantityOnHandByProduct(Location location) {
        def quantityMap = [:]
        if (location) {
            def results = ProductAvailability.executeQuery("""
						select pa.product, sum(pa.quantityOnHand)
						from ProductAvailability pa
						where pa.location = :location
						group by pa.product
						""", [location: location])
            results.each {
                quantityMap[it[0]] = it[1]
            }
        }

        return quantityMap
    }

    Map<Product, Integer> getQuantityOnHandByProduct(Location location, List<Product> products) {
        def quantityMap = [:]
        if (location) {
            def results = ProductAvailability.executeQuery("""
						select pa.product, sum(pa.quantityOnHand)
						from ProductAvailability pa
						where pa.location = :location
						and pa.product in (:products)
						group by pa.product
						""", [location: location, products:products])
            results.each {
                quantityMap[it[0]] = it[1]
            }
        }

        return quantityMap
    }

    Map<Product, Map<Location, Integer>> getQuantityOnHandByProduct(Location[] locations) {
        def quantityMap = [:]
        if (locations) {
            def results = ProductAvailability.executeQuery("""
						select product, pa.location, category.name, sum(pa.quantityOnHand)
						from ProductAvailability pa, Product product, Category category
						where pa.location in (:locations)
						and pa.product = product
						and pa.product.category = category
						group by product, pa.location, category.name
						""", [locations: locations])

            results.each {
                if (!quantityMap[it[0]]) {
                    quantityMap[it[0]] = [:]
                }
                quantityMap[it[0]][it[1]?.id] = it[3]
            }
        }

        return quantityMap
    }

    List getQuantityOnHandByBinLocation(Location location) {
        def data = []

        if (location) {
            def results = ProductAvailability.executeQuery("""
						select 
						    pa.product, 
						    pa.inventoryItem,
						    pa.binLocation,
						    sum(pa.quantityOnHand)
						from ProductAvailability pa
						left outer join pa.inventoryItem ii
						left outer join pa.binLocation bl
						where pa.location = :location
						group by pa.product, pa.inventoryItem, pa.binLocation
						""", [location: location])

            data = collectQuantityOnHandByBinLocation(results)
        }
        return data
    }

    List getAvailableQuantityOnHandByBinLocation(Location location) {
        def data = []

        if (location) {
            def results = ProductAvailability.executeQuery("""
						select 
						    pa.product, 
						    pa.inventoryItem,
						    pa.binLocation,
						    sum(pa.quantityOnHand)
						from ProductAvailability pa
						left outer join pa.inventoryItem ii
						left outer join pa.binLocation bl
						where pa.location = :location and pa.quantityOnHand > 0
						group by pa.product, pa.inventoryItem, pa.binLocation
						""", [location: location])

            data = collectQuantityOnHandByBinLocation(results)
        }
        return data
    }

    List getQuantityOnHandByBinLocation(Location location, List<Product> products) {
        log.info("getQuantityOnHandByBinLocation: location=${location} product=${products}")
        def data = []
        if (location) {
            def results = ProductAvailability.executeQuery("""
						select 
						    pa.product, 
						    ii,
						    pa.binLocation,
						    pa.quantityOnHand
						from ProductAvailability pa
						left outer join pa.inventoryItem ii
						left outer join pa.binLocation bl
						where pa.location = :location
						and pa.product in (:products)
						""", [location: location, products: products])
            def status = { quantity -> quantity > 0 ? "inStock" : "outOfStock" }
            data = results.collect {
                def inventoryItem = it[1]
                def binLocation = it[2]
                def quantity = it[3]

                [
                        status       : status(quantity),
                        product      : it[0],
                        inventoryItem: inventoryItem,
                        binLocation  : binLocation,
                        quantity     : quantity
                ]
            }
        }
        return data
    }

    Map<InventoryItem, Integer> getQuantityOnHandByInventoryItem(Location location) {
        def quantityMap = [:]
        if (location) {
            def results = ProductAvailability.executeQuery("""
						select ii, sum(pa.quantityOnHand)
						from ProductAvailability pa, InventoryItem ii
						where pa.location = :location
						and pa.inventoryItem = ii
						group by ii
						""", [location: location])

            results.each {
                quantityMap[it[0]] = it[1]
            }
        }
        return quantityMap
    }

    List<AvailableItem> getAvailableBinLocations(Location location, Product product) {
        return getAvailableBinLocations(location, [product])
    }

    List<AvailableItem> getAllAvailableBinLocations(Location location, Product product) {
        return getAllAvailableBinLocations(location, [product])
    }

    List<AvailableItem> getAvailableBinLocations(Location location, List products) {
        def availableBinLocations = getQuantityOnHandByBinLocation(location, products)

        List<AvailableItem> availableItems = availableBinLocations.collect {
            return new AvailableItem(
                    inventoryItem: it?.inventoryItem,
                    binLocation: it?.binLocation,
                    quantityAvailable: it.quantity
            )
        }

        availableItems = sortAvailableItems(availableItems)
        return availableItems
    }

    // Include also bin locations with negative qty (needed for edit page items)
    List<AvailableItem> getAllAvailableBinLocations(Location location, List products) {
        def availableBinLocations = getQuantityOnHandByBinLocation(location, products)

        List<AvailableItem> availableItems = availableBinLocations.collect {
            return new AvailableItem(
                    inventoryItem: it?.inventoryItem,
                    binLocation: it?.binLocation,
                    quantityAvailable: it.quantity
            )
        }

        return availableItems
    }

    List<AvailableItem> sortAvailableItems(List<AvailableItem> availableItems) {
        availableItems = availableItems.findAll { it.quantityAvailable > 0 }

        // Sort bins  by available quantity
        availableItems = availableItems.sort { a, b ->
            a?.quantityAvailable <=> b?.quantityAvailable
        }

        // Sort empty expiration dates last
        availableItems = availableItems.sort { a, b ->
            !a?.inventoryItem?.expirationDate ?
                    !b?.inventoryItem?.expirationDate ? 0 : 1 :
                    !b?.inventoryItem?.expirationDate ? -1 :
                            a?.inventoryItem?.expirationDate <=> b?.inventoryItem?.expirationDate
        }

        return availableItems
    }

    private static List collectQuantityOnHandByBinLocation(List<ProductAvailability> productAvailabilities) {

        def getStatus = { quantity -> quantity > 0 ? "inStock" : "outOfStock" }

        def data = productAvailabilities.collect {
            Product product = it[0]
            InventoryItem inventoryItem = it[1]
            Location bin = it[2]
            BigDecimal quantity = it[3]?:0.0
            BigDecimal unitCost = product.pricePerUnit?:0.0
            BigDecimal totalValue = quantity * unitCost

            [
                    status       : getStatus(quantity),
                    product      : product,
                    inventoryItem: inventoryItem,
                    binLocation  : bin,
                    quantity     : quantity,
                    unitCost     : unitCost,
                    totalValue   : totalValue

            ]
        }
        return data
    }

    void updateProductAvailability(InventoryItem inventoryItem) {
        def results = ProductAvailability.executeUpdate(
                "update ProductAvailability a " +
                        "set a.lotNumber=:lotNumber " +
                        "where a.inventoryItem.id = :inventoryItemId " +
                        "and a.lotNumber != :lotNumber",
                [
                        inventoryItemId: inventoryItem.id,
                        lotNumber      : inventoryItem.lotNumber?:Constants.DEFAULT_LOT_NUMBER
                ]
        )
        log.info "Updated ${results} product availability records for inventory item ${inventoryItem?.lotNumber?:Constants.DEFAULT_LOT_NUMBER}"
    }


    void updateProductAvailability(Location location) {
        def isBinLocation = location?.isInternalLocation()
        if (isBinLocation) {
            def results = ProductAvailability.executeUpdate(
                    "update ProductAvailability a " +
                            "set a.binLocationName = :binLocationName " +
                            "where a.binLocation.id = :binLocationId " +
                            "and a.binLocationName != :binLocationName",
                    [
                            binLocationId  : location.id,
                            binLocationName: location.name?:Constants.DEFAULT_BIN_LOCATION_NAME
                    ]
            )
            log.info "Updated ${results} product availability records for bin location ${location?.name?:Constants.DEFAULT_BIN_LOCATION_NAME}"
        }
    }

    void updateProductAvailability(Product product) {
        def results = ProductAvailability.executeUpdate(
                "update ProductAvailability a " +
                        "set a.productCode = :productCode " +
                        "where a.product.id = :productId " +
                        "and a.productCode != :productCode",
                [
                        productId  : product.id,
                        productCode: product.productCode
                ]
        )
        log.info "Updated ${results} product availability records for product ${product?.productCode}"
    }


    /**
     *
     * @param commandInstance
     * @return
     */
    List searchProducts(InventoryCommand command) {
        def categories = inventoryService.getExplodedCategories([command.category])
        List searchTerms = (command?.searchTerms ? Arrays.asList(command?.searchTerms?.split(" ")) : null)

        // Only search if there are search terms otherwise the list of product IDs includes all products
        def innerProductIds = !searchTerms ? [] : Product.createCriteria().list {
            eq("active", true)
            projections {
                distinct 'id'
            }
            and {
                searchTerms.each { searchTerm ->
                    or {
                        ilike("name", "%" + searchTerm + "%")
                        inventoryItems {
                            ilike("lotNumber", "%" + searchTerm + "%")
                        }
                    }
                }
            }
        }

        return ProductSearch.createCriteria().list(max: command.maxResults, offset: command.offset) {
            product {
                eq("active", true)
                and {
                    if (categories) {
                        'in'("category", categories)
                    }
                    if (command.tags) {
                        tags {
                            'in'("id", command.tags*.id)
                        }
                    }
                    if (command.catalogs) {
                        productCatalogItems {
                            productCatalog {
                                'in'("id", command.catalogs*.id)
                            }
                        }
                    }
                    // This is pretty inefficient if the previous query does not narrow the results
                    // if the inner products list is empty, but there are search terms then return empty results
                    if (innerProductIds || searchTerms) {
                        'in'("id", innerProductIds ?: [null])
                    }
                }
            }
            eq("location", command.location)
            or {
                isNull("type")
                and {
                    eq("isSearchableType", Boolean.TRUE)
                    gt("quantityOnHand", 0)
                }
            }
        }
    }
}
