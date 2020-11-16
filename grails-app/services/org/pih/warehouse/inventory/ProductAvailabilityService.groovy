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

}
