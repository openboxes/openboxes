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
import org.apache.commons.lang.StringEscapeUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.hibernate.Criteria
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.ApplicationExceptionEvent
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.SynonymTypeCode
import org.pih.warehouse.jobs.RefreshProductAvailabilityJob
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductActivityCode
import org.pih.warehouse.product.ProductAvailability
import org.pih.warehouse.requisition.RequisitionStatus

import java.text.SimpleDateFormat

class ProductAvailabilityService {

    boolean transactional = true

    def dataSource
    def gparsService
    def grailsApplication
    def persistenceInterceptor
    def locationService
    def inventoryService
    def dataService

    def triggerRefreshProductAvailability(String locationId, List<String> productIds, Boolean forceRefresh) {
        Boolean delayStart = grailsApplication.config.openboxes.jobs.refreshProductAvailabilityJob.delayStart
        def delayInMilliseconds = delayStart ?
            Integer.valueOf(grailsApplication.config.openboxes.jobs.refreshProductAvailabilityJob.delayInMilliseconds) : 0
        Date runAt = new Date(System.currentTimeMillis() + delayInMilliseconds)
        log.info "Triggering refresh product availability with ${delayInMilliseconds} ms delay"
        RefreshProductAvailabilityJob.schedule(runAt,
            [locationId: locationId, productIds: productIds, forceRefresh: forceRefresh])
    }

    def refreshProductsAvailability(String locationId, List<String> productIds, Boolean forceRefresh) {
        // Calculate product availability for a single location/product, or all products within a single location
        if (locationId) {
            Location location = Location.load(locationId)
            if (productIds && locationId) {
                productIds.unique().each { productId ->
                    Product product = Product.load(productId)
                    refreshProductAvailability(location, product, forceRefresh)
                }
            }
            else {
                refreshProductAvailability(location, forceRefresh)
            }
        }
        // Calculate product availability for a single product within all locations
        else if (productIds) {
            productIds.unique().each { productId ->
                Product product = Product.load(productId)
                refreshProductAvailability(product, forceRefresh)
            }
        }
        // Calculate product availability for all products within all locations
        else {
            refreshProductAvailability(forceRefresh)
        }
    }

    def refreshProductAvailability(Boolean forceRefresh) {
        // Compute bin locations from transaction entries for all products over all depot locations
        // Uses GPars to improve performance (OBNAV Benchmark: 5 minutes without, 45 seconds with)
        def startTime = System.currentTimeMillis()
        gparsService.withPool('RefreshAllProducts') {
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
        gparsService.withPool('RefreshSingleProduct') {
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
        binLocations = transformBinLocations(binLocations, [])
        return binLocations
    }

    def calculateBinLocations(Location location) {
        def binLocations = inventoryService.getBinLocationDetails(location)
        def picked = getQuantityPickedByProductAndLocation(location, null)
        binLocations = transformBinLocations(binLocations, picked)
        return binLocations
    }

    def calculateBinLocations(Location location, Product product) {
        def binLocations = inventoryService.getProductQuantityByBinLocation(location, product, Boolean.TRUE)
        def picked = getQuantityPickedByProductAndLocation(location, product)
        binLocations = transformBinLocations(binLocations, picked)
        return binLocations
    }

    def getQuantityPickedByProductAndLocation(Location location, Product product) {
        def results = Picklist.executeQuery("""
            SELECT
                pli.binLocation,
                pli.inventoryItem,
                sum(pli.quantity)*((count(distinct pli.requisitionItem) + count(distinct pli.orderItem) )/ count(*))
            FROM PicklistItem pli
            INNER JOIN pli.picklist pl
            LEFT JOIN pli.requisitionItem ri
            LEFT JOIN pl.requisition r
            LEFT JOIN pli.orderItem oi
            LEFT JOIN pl.order o
            LEFT JOIN pli.inventoryItem ii
            LEFT JOIN pli.binLocation l
            LEFT JOIN l.supportedActivities s
            WHERE ((r.origin = :location AND r.status IN (:pendingRequisitionStatus)) OR (o.origin = :location AND o.status IN (:pendingOrderStatus)))
              AND (oi IS NOT NULL OR (ri IS NOT NULL AND (ii.lotStatus IS NULL OR ii.lotStatus != 'RECALLED') AND NOT ('HOLD_STOCK' IN ELEMENTS(l.supportedActivities))))
              AND (:product = '' OR ri.product.id = :product OR oi.product.id = :product)
            GROUP BY pli.binLocation, pli.inventoryItem
        """, [
                location                    : location,
                pendingRequisitionStatus    : RequisitionStatus.listPending(),
                pendingOrderStatus          : OrderStatus.listPending(),
                product                     : product?.id ?: ''
        ], [readOnly: true])

        return results.collect { [binLocation: it[0]?.id, inventoryItem: it[1]?.id, quantityAllocated: it[2]] }
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
        Integer quantityAvailableToPromise = onHandQuantity - quantityAllocated - quantityOnHold
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

    def transformBinLocations(List binLocations, List picked) {
        def binLocationsTransformed = binLocations.collect {
            [
                product          : [id: it?.product?.id, productCode: it?.product?.productCode, name: it?.product?.name],
                inventoryItem    : [id: it?.inventoryItem?.id, lotNumber: it?.inventoryItem?.lotNumber, expirationDate: it?.inventoryItem?.expirationDate],
                binLocation      : [id: it?.binLocation?.id, name: it?.binLocation?.name],
                quantity         : it.quantity,
                quantityAllocated: picked ? (picked.findAll { row -> row.binLocation == it?.binLocation?.id && row.inventoryItem == it?.inventoryItem?.id }?.sum { it.quantityAllocated } ?: 0) : 0,
                quantityOnHold   : it?.binLocation?.isOnHold() || it?.inventoryItem?.lotStatus == LotStatusCode.RECALLED ? it.quantity : 0
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

    def getQuantityNotPickedInBinLocation(InventoryItem inventoryItem, Location location, Location binLocation) {
        return ProductAvailability.createCriteria().get {
            projections {
                sum("quantityNotPicked")
            }
            eq("location", location)
            eq("inventoryItem", inventoryItem)
            if (binLocation) {
                eq("binLocation", binLocation)
            } else {
                isNull("binLocation")
            }
        }
    }

    def getQuantityNotPickedInLocation(Product product, Location location) {
        return  ProductAvailability.createCriteria().get {
            projections {
                sum("quantityNotPicked")
            }
            eq("product", product)
            eq("location", location)
        }
    }

    Map<Product, Integer> getCurrentInventory(Location location) {
        return getQuantityOnHandByProduct(location)
    }

    def getInventoryByProduct(Location location) {
        def quantityMap = [:]
        if (location) {
            def results = ProductAvailability.executeQuery("""
						select pa.product, sum(pa.quantityOnHand),
						 sum(case when pa.quantityAvailableToPromise > 0 then pa.quantityAvailableToPromise else 0 end)
						from ProductAvailability pa
						where pa.location = :location
						group by pa.product
						""", [location: location])
            results.each {
                quantityMap[it[0]] = [
                        quantityOnHand              : it[1],
                        quantityAvailableToPromise  : it[2]
                ]
            }
        }

        return quantityMap
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

    Map<Product, Integer> getQuantityAvailableToPromiseByProduct(Location location, List<Product> products) {
        def quantityMap = [:]
        if (location) {
            def results = ProductAvailability.executeQuery("""
						select pa.product.id, sum(case when pa.quantityAvailableToPromise > 0 then pa.quantityAvailableToPromise else 0 end)
						from ProductAvailability pa
						where pa.location = :location
						and pa.product in (:products)
						group by pa.product
						""", [location: location, products:products])

            results.each {
                quantityMap[it[0]] = it[1]
            }
        }

        if (products.size() != quantityMap.size()) {
            def missingProducts = products*.id - quantityMap.keySet()
            missingProducts.each { productId ->
                if (!quantityMap[productId]) {
                    quantityMap[productId] = 0
                }
            }
        }

        return quantityMap
    }

    Map<Product, Map<Location, Integer>> getQuantityOnHandByProduct(Location[] locations) {
        def quantityMap = [:]
        if (locations) {
            def results = ProductAvailability.executeQuery("""
						select product, pa.location, category.name, sum(pa.quantityOnHand),
						 sum(case when pa.quantityAvailableToPromise > 0 then pa.quantityAvailableToPromise else 0 end)
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
                quantityMap[it[0]][it[1]?.id] = [quantityOnHand: it[3], quantityAvailableToPromise: it[4]]
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
						    sum(pa.quantityOnHand),
						    sum(case when pa.quantityAvailableToPromise > 0 then pa.quantityAvailableToPromise else 0 end)
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
						    sum(pa.quantityOnHand),
                            sum(pa.quantityAvailableToPromise)
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

    List getAvailableItems(Location location, List<Product> products) {
        log.info("getQuantityOnHandByBinLocation: location=${location} product=${products}")
        def data = []
        if (location) {
            def results = ProductAvailability.executeQuery("""
						select 
						    pa.product, 
						    ii,
						    pa.binLocation,
						    pa.quantityOnHand,
						    pa.quantityAvailableToPromise
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
                        status                      : status(quantity),
                        product                     : it[0],
                        inventoryItem               : inventoryItem,
                        binLocation                 : binLocation,
                        quantityOnHand              : quantity,
                        quantityAvailableToPromise  : it[4]
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
        def availableBinLocations = getAvailableItems(location, products)

        List<AvailableItem> availableItems = availableBinLocations.collect {
            return new AvailableItem(
                    inventoryItem: it?.inventoryItem,
                    binLocation: it?.binLocation,
                    quantityAvailable: it.quantityAvailableToPromise,
                    quantityOnHand: it.quantityOnHand
            )
        }

        availableItems = availableItems.findAll { it.quantityOnHand > 0 }

        availableItems = sortAvailableItems(availableItems)
        return availableItems
    }

    // Include also bin locations with negative qty (needed for edit page items)
    List<AvailableItem> getAllAvailableBinLocations(Location location, List products) {
        def availableBinLocations = getAvailableItems(location, products)

        List<AvailableItem> availableItems = availableBinLocations.collect {
            return new AvailableItem(
                    inventoryItem: it?.inventoryItem,
                    binLocation: it?.binLocation,
                    quantityAvailable: it.quantityAvailableToPromise,
                    quantityOnHand: it.quantityOnHand
            )
        }

        return availableItems
    }

    /**
     * Sorting used by first expiry, first out algorithm
     */
    List<AvailableItem> sortAvailableItems(List<AvailableItem> availableItems) {
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

        // Move items with zero available quantity to the end
        availableItems = availableItems.sort { a, b ->
            (a?.quantityAvailable <= 0) <=> (b?.quantityAvailable <= 0)
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
            BigDecimal quantityAvailableToPromise = it[4]?:0.0
            BigDecimal unitCost = product.pricePerUnit?:0.0
            BigDecimal totalValue = quantity * unitCost

            [
                    status                      : getStatus(quantity),
                    product                     : product,
                    inventoryItem               : inventoryItem,
                    binLocation                 : bin,
                    quantity                    : quantity,
                    quantityAvailableToPromise  : quantityAvailableToPromise,
                    unitCost                    : unitCost,
                    totalValue                  : totalValue
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
        if (!product?.id) {
            return
        }
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
        log.info "Updated ${results} product availability records for product ${product.productCode}"
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
                property 'id'
            }
            and {
                searchTerms.each { searchTerm ->
                    or {
                        ilike("name", "%" + searchTerm + "%")
                        synonyms {
                            and {
                                ilike("name", "%" + searchTerm + "%")
                                eq("synonymTypeCode", SynonymTypeCode.DISPLAY_NAME)
                            }
                        }
                        inventoryItems {
                            ilike("lotNumber", "%" + searchTerm + "%")
                        }
                    }
                }
            }
        }.unique()

        def paginationParams = searchTerms ? [:] : [max: command.maxResults, offset: command.offset]

        def products = Product.createCriteria().list(paginationParams) {
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

        def quantityMap = products ? getQuantityOnHandByProduct(command.location, products) : []

        def items = []

        products.each { Product product ->
            def quantity = quantityMap[product] ?: 0

            if (product.productType && !product.productType.supportedActivities?.contains(ProductActivityCode.SEARCHABLE_NO_STOCK)) {
                if (!product.productType.supportedActivities?.contains(ProductActivityCode.SEARCHABLE)) {
                    return
                } else if (quantity == 0) {
                    return
                }
            }

            items << [
                id   : product.id,
                product: product,
                quantityOnHand: quantity
            ]
        }

        return searchTerms ? items : new PagedResultList(items, products.totalCount)
    }

    List<ProductAvailability> getStockTransferCandidates(Location location) {
        return ProductAvailability.createCriteria().list {
            eq("location", location)
            gt("quantityOnHand", 0)
        }
    }

    List<ProductAvailability> getStockTransferCandidates(Location location, Map params) {
        if (!params) {
            return getStockTransferCandidates(location)
        }

        Location bin = params.binLocationId ? Location.get(params.binLocationId) : null
        Product product = params.productId ? Product.get(params.productId) : null
        def dateFormat = new SimpleDateFormat("MM/dd/yyyy")
        Date expirationDate = params.expirationDate ? dateFormat.parse(params.expirationDate) : null
        return ProductAvailability.createCriteria().list {
            eq("location", location)
            or {
                if (product) {
                    eq("product", product)
                }
                if (bin) {
                    eq("binLocation", bin)
                }
                if (params.lotNumber) {
                    ilike("lotNumber", "%${params.lotNumber}%")
                }
                if (expirationDate) {
                    inventoryItem {
                        eq("expirationDate", expirationDate)
                    }
                }
            }
            gt("quantityOnHand", 0)
        }
    }

    // Get quantity available to promise (with negative values)
    def getQuantityAvailableToPromise(Location location, Location binLocation, InventoryItem inventoryItem) {
         def quantityAvailableToPromise = ProductAvailability.createCriteria().get {
            projections {
                sum("quantityAvailableToPromise")
            }

            eq("location", location)
            eq("inventoryItem", inventoryItem)
            if (binLocation) {
                eq("binLocation", binLocation)
            } else {
                isNull("binLocation")
            }
        }

        return quantityAvailableToPromise ?: 0
    }

    def getQuantityAvailableToPromiseByProductNotInBin(Location location, Location binLocation, Product product) {
        def quantityAvailableToPromiseByProductNotInBin = ProductAvailability.createCriteria().get {
            projections {
                sum("quantityAvailableToPromise")
            }

            eq("location", location)
            eq("product", product)
            if (binLocation) {
                or {
                    ne("binLocation", binLocation)
                    isNull("binLocation")
                }
            } else {
                isNotNull("binLocation")
            }
        }

        return quantityAvailableToPromiseByProductNotInBin ?: 0
    }
}
