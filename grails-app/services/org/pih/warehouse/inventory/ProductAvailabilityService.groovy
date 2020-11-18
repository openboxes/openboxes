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

import groovy.sql.BatchingStatementWrapper
import groovy.sql.Sql
import groovyx.gpars.GParsPool
import org.apache.commons.lang.StringEscapeUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.hibernate.Criteria
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.ApplicationExceptionEvent
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.data.DataService
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability

class ProductAvailabilityService {

    boolean transactional = true

    def dataSource
    def persistenceInterceptor
    LocationService locationService
    InventoryService inventoryService
    DataService dataService

    def refreshProductAvailability(Boolean forceRefresh) {
        // Compute bin locations from transaction entries for all products over all depot locations
        // Uses GPars to improve performance
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

    def refreshProductAvailability(Location location, Boolean forceRefresh) {
        def startTime = System.currentTimeMillis()
        List binLocations = calculateBinLocations(location)
        if (forceRefresh) {
            deleteProductAvailability(location)
        }
        saveProductAvailability(location, binLocations)
        refreshInventorySnapshot(location, forceRefresh)
        log.info "Refreshed product availability for location ${location} in ${System.currentTimeMillis() - startTime}ms"
    }

    def refreshProductAvailability(Location location, Product product, Boolean forceRefresh) {
        def startTime = System.currentTimeMillis()
        List binLocations = calculateBinLocations(location, product)
        if (forceRefresh) {
            deleteProductAvailability(location, product)
        }
        saveProductAvailability(location, binLocations)
        refreshInventorySnapshot(location, product, forceRefresh)
        log.info "Refreshed product availability for product ${product} and location ${location} in ${System.currentTimeMillis() - startTime}ms"
    }

    def calculateBinLocations(Location location, Date date) {
        def binLocations = inventoryService.getBinLocationDetails(location, date)
        binLocations = transformBinLocations(binLocations)
        return binLocations
    }

    def calculateBinLocations(Location location) {
        def binLocations = inventoryService.getBinLocationDetails(location)
        binLocations = transformBinLocations(binLocations)
        return binLocations
    }

    def calculateBinLocations(Location location, Product product) {
        def binLocations = inventoryService.getProductQuantityByBinLocation(location, product)
        binLocations = transformBinLocations(binLocations)
        return binLocations
    }


    def saveProductAvailability(Location location, List binLocations) {
        def batchSize = ConfigurationHolder.config.openboxes.inventorySnapshot.batchSize ?: 1000
        def startTime = System.currentTimeMillis()
        try {
            Sql sql = new Sql(dataSource)
            // Execute inventory snapshot insert/update in batches
            sql.execute("SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;")
            sql.withBatch(batchSize) { BatchingStatementWrapper stmt ->
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
        Integer onHandQuantity = entry.quantity
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

        def insertStatement =
                "REPLACE product_availability (id, version, location_id, product_id, product_code, " +
                        "inventory_item_id, lot_number, bin_location_id, bin_location_name, " +
                        "quantity_on_hand, date_created, last_updated) " +
                        "values ('${UUID.randomUUID().toString()}', 0, '${location?.id}', " +
                        "'${productId}', '${productCode}', " +
                        "${inventoryItemId}, ${lotNumber}, " +
                        "${binLocationId}, ${binLocationName}, ${onHandQuantity}, now(), now()) "
                        //"ON DUPLICATE KEY UPDATE quantity_on_hand=${onHandQuantity}, version=version+1, last_updated=now()"
        return insertStatement
    }

    def transformBinLocations(List binLocations) {
        def binLocationsTransformed = binLocations.collect {
            [
                    product      : [id: it?.product?.id, productCode: it?.product?.productCode, name: it?.product?.name],
                    inventoryItem: [id: it?.inventoryItem?.id, lotNumber: it?.inventoryItem?.lotNumber, expirationDate: it?.inventoryItem?.expirationDate],
                    binLocation  : [id: it?.binLocation?.id, name: it?.binLocation?.name],
                    quantity     : it.quantity
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
        String deleteStatement = """
            DELETE FROM inventory_snapshot 
            WHERE date = DATE_ADD(CURDATE(),INTERVAL 1 DAY)
            AND location_id = '${location.id}' 
            AND product_id = '${product.id}'
        """
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
            AND product_id = '${product.id}'
        """
        if (forceRefresh) {
            dataService.executeStatement(deleteStatement)
        }
        dataService.executeStatement(updateStatement)
    }

    def refreshInventorySnapshot(Location location, Boolean forceRefresh) {
        String deleteStatement = """
            DELETE FROM inventory_snapshot 
            WHERE date = DATE_ADD(CURDATE(),INTERVAL 1 DAY)
            AND location_id = '${location.id}'
        """
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
        """
        if (forceRefresh) {
            dataService.executeStatement(deleteStatement)
        }
        dataService.executeStatement(updateStatement)
    }

    def refreshInventorySnapshot(Boolean forceRefresh) {
        String deleteStatement = """
            DELETE FROM inventory_snapshot 
            WHERE date = DATE_ADD(CURDATE(),INTERVAL 1 DAY)
        """
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
        """
        if (forceRefresh) {
            dataService.executeStatement(deleteStatement)
        }
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

    def getQuantityOnHand(List<Product> products, Location location) {
        def productAvailability = ProductAvailability.createCriteria().list {
            resultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
            projections {
                // Need to use alias other than product to prevent conflict
                groupProperty("product", "p")
                sum("quantityOnHand", "quantityOnHand")
            }
            eq("location", location)
            'in'("product", products)
        }

        return productAvailability
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
						inner join pa.product
						where pa.location = :location
						group by pa.product
						""", [location: location])
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

            def getStatus = { quantity -> quantity > 0 ? "inStock" : "outOfStock" }

            data = results.collect {
                Product product = it[0]
                InventoryItem inventoryItem = it[1]
                Location binLocation = it[2]
                BigDecimal quantity = it[3]?:0.0
                BigDecimal unitCost = product.pricePerUnit?:0.0
                BigDecimal totalValue = quantity * unitCost

                [
                        status       : getStatus(quantity),
                        product      : product,
                        inventoryItem: inventoryItem,
                        binLocation  : binLocation,
                        quantity     : quantity,
                        unitCost     : unitCost,
                        totalValue   : totalValue

                ]
            }
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
}
