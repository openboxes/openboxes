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

import groovy.sql.Sql
import org.apache.commons.lang.StringEscapeUtils
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.Category

import java.sql.BatchUpdateException
import java.text.DateFormat
import java.text.SimpleDateFormat

class InventorySnapshotService {

    boolean transactional = true

    def dataSource
    def inventoryService

    def refreshInventorySnapshots(Date date) {
        deleteInventorySnapshots(date)
        populateInventorySnapshots(date)
    }


    def populateInventorySnapshots() {
        def transactionDates = getTransactionDates()
        for (Date date: transactionDates) {
            populateInventorySnapshots(date)
        }
    }


    def populateInventorySnapshots(Date date) {
        def startTime = System.currentTimeMillis()
        def locations = getDepotLocations()
        locations.each { location ->
            log.debug "Creating or updating inventory snapshot for date ${date}, location ${location.name} ..."
            populateInventorySnapshots(date, location)
        }
        log.info "Created inventory snapshot for ${date} in " + (System.currentTimeMillis() - startTime) + " ms"
    }

    def populateInventorySnapshots(Location location) {
        populateInventorySnapshots(new Date(), location)
    }

    def populateInventorySnapshots(Date date, Location location) {
        populateInventorySnapshots(date, location, null)
    }

    def populateInventorySnapshots(Date date, Location location, Product product) {
        def startTime = System.currentTimeMillis()
        def binLocations = calculateBinLocations(location, product)
        def readTime = (System.currentTimeMillis()-startTime)
        startTime = System.currentTimeMillis()
        saveInventorySnapshots(date, location, binLocations)
        def writeTime = System.currentTimeMillis()-startTime
        log.info "Saved ${binLocations?.size()} snapshots location ${location} on date ${date.format("MMM-dd-yyyy")}: ${readTime}ms/${writeTime}ms"
    }

    def calculateBinLocations(Location location, Product product) {
        def binLocations = product ? inventoryService.getProductQuantityByBinLocation(location, product) :
                inventoryService.getBinLocationDetails(location)
        binLocations = transformBinLocations(binLocations)
        return binLocations
    }

    def deleteInventorySnapshots(Date date) {
        deleteInventorySnapshots(date, null, null)
    }

    def deleteInventorySnapshots(Date date, Location location) {
        deleteInventorySnapshots(date, location, null)
    }


    def deleteInventorySnapshots(Date date, Location location, Product product) {
        Map params = [:]

        String deleteStmt = """delete from InventorySnapshot snapshot where snapshot.date = :date"""
        params.put("date", date)

        if (location) {
            deleteStmt + " and snapshot.location = :location"
            params.put("location", location)
        }

        if (product) {
            deleteStmt + " and snapshot.product = :product"
            params.put("product", product)
        }

        InventorySnapshot.executeUpdate(deleteStmt, params)
    }

    def transformBinLocations(List binLocations) {
        return binLocations.collect {
            [
                    product      : [id: it?.product?.id, productCode: it?.product?.productCode, name: it?.product?.name],
                    inventoryItem: [id: it?.inventoryItem?.id, lotNumber: it?.inventoryItem?.lotNumber, expirationDate: it?.inventoryItem?.expirationDate],
                    binLocation  : [id: it?.binLocation?.id, name: it?.binLocation?.name],
                    quantity     : it.quantity
            ]
        }

    }

    def saveInventorySnapshots(Date date, Location location, List binLocations) {
        def startTime = System.currentTimeMillis()
        def batchSize = 1000
        def sql = new Sql(dataSource)
        if (sql) {
            try {
                // Clear time in case caller did not
                date.clearTime()
                String dateString = date.format("yyyy-MM-dd HH:mm:ss")
                DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                // Execute inventory snapshot insert/update in batches
                sql.withBatch(batchSize) { stmt ->
                    binLocations.eachWithIndex { entry, index ->

                        def onHandQuantity = entry.quantity
                        String productId = "${StringEscapeUtils.escapeSql(entry.product?.id)}"
                        String productCode = "${StringEscapeUtils.escapeSql(entry.product?.productCode)}"
                        String lotNumber = entry?.inventoryItem?.lotNumber ?
                                "'${StringEscapeUtils.escapeSql(entry?.inventoryItem?.lotNumber)}'" : "'DEFAULT'"
                        String expirationDate = entry?.inventoryItem?.expirationDate ?
                                "'${DATE_FORMAT.format(entry?.inventoryItem?.expirationDate)}'" : "NULL"
                        String inventoryItemId = entry?.inventoryItem?.id ?
                                "'${StringEscapeUtils.escapeSql(entry?.inventoryItem?.id)}'" : "NULL"
                        String binLocationId = entry?.binLocation?.id ?
                                "'${StringEscapeUtils.escapeSql(entry?.binLocation?.id)}'" : "NULL"
                        String binLocationName = entry?.binLocation?.name ?
                                "'${StringEscapeUtils.escapeSql(entry?.binLocation?.name)}'" : "'DEFAULT'"

                        // '${UUID.randomUUID().toString()}',
                        def insertStmt =
                                "insert into inventory_snapshot(version, date, location_id, product_id, product_code," +
                                        "inventory_item_id, lot_number, expiration_date, bin_location_id, bin_location_name, " +
                                        "quantity_on_hand, date_created, last_updated) " +
                                        "values (0, '${dateString}', '${location?.id}', " +
                                        "'${productId}', '${productCode}', " +
                                        "${inventoryItemId}, ${lotNumber}, ${expirationDate}, " +
                                        "${binLocationId}, ${binLocationName}, ${onHandQuantity}, now(), now()) " +
                                        "ON DUPLICATE KEY UPDATE quantity_on_hand=${onHandQuantity}, version=version+1, last_updated=now()"


                        //log.info ("insertStmt: ${insertStmt}")

                        stmt.addBatch(insertStmt)
                    }
                    stmt.executeBatch()
                }
            } catch (BatchUpdateException e) {
                log.error("Error executing batch update for location ${location.name}" + e.message, e)
            }
        }
    }

    def getTransactionDates() {
        return Transaction.executeQuery("select distinct(date(transactionDate)) from Transaction order by transactionDate desc")
    }


    def getTransactionDates(Location location, Product product) {

        String query = """
            select distinct(date(t.transactionDate)) 
            from TransactionEntry as te 
            join te.transaction as t
            join te.inventoryItem as ii
            where ii.product = :product
            and t.inventory = :inventory
            order by t.transactionDate desc
        """
        return TransactionEntry.executeQuery(query, [product: product, inventory: location.inventory])
    }

    def getDepotLocations() {
        def locations = Location.findAll("from Location as l where l.inventory is not null")
        return locations.findAll { it.isWarehouse() }
    }


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
            def readTime, writeTime
            log.debug "Create or update inventory snapshot for location ${location.name} on date ${date}"
            // Only process locations with inventory
            if (location?.inventory) {

                String dateString = date.format("yyyy-MM-dd HH:mm:ss")
                //def productQuantityMap = getQuantityByProductMap(location.inventory)
                def startTime = System.currentTimeMillis()
                def quantityMap = inventoryService.getQuantityOnHandAsOfDate(location, date)
                def products = quantityMap.keySet();
                readTime = System.currentTimeMillis() - startTime

                log.debug "Calculated quantity on hand for ${products?.size()} products in ${System.currentTimeMillis()-startTime} ms"
                startTime = System.currentTimeMillis()
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
                writeTime = System.currentTimeMillis()-startTime
                log.info "Saved ${products?.size()} snapshots location ${location} on date ${date.format("MMM-dd-yyyy")}: ${readTime}ms/${writeTime}ms"
            }
        } catch (Exception e) {
            log.error("Unable to complete snapshot process", e)
        }
    }

    def createOrUpdateInventorySnapshot(Location location, Product product) {
        try {
            def dates = getTransactionDates(location, product)
            dates.each { date ->
                def quantity = inventoryService.getQuantity(product, location, date)
                log.info "Create or update snapshot for product ${product} at location ${location.name} on date ${date} = ${quantity} ${product.unitOfMeasure}"
                createOrUpdateInventorySnapshot(date, product, location, quantity)
            }
            log.info "Saved snapshot for product=${product.productCode}, location=${location}, dates=ALL"
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
            def results = InventorySnapshot.executeQuery("""
						select i.date, i.product, sum(i.quantityOnHand)
						from InventorySnapshot i
						inner join i.product
						where i.location = :location
						and i.date = :date
						group by i.product
						""", [location: location, date: date])
            results.each {
                quantityMap[it[1]] = it[2]
            }
        }

        return quantityMap
    }

    /**
     * Get quantity on hand by product for the given locations.
     *
     * @param location
     * @return
     */
    Map<Product, Map<Location, Integer>> getQuantityOnHandByProduct(Location[] locations) {
        def quantityMap = [:]
        if (locations) {
            Date date = getMostRecentInventorySnapshotDate()
            def results = InventorySnapshot.executeQuery("""
						select i.date, product, i.location, category.name, i.quantityOnHand
						from InventorySnapshot i, Product product, Category category
						where i.location in (:locations)
						and i.date = :date
						and i.product = product
						and i.product.category = category
						""", [locations: locations, date: date])

            results.each {
                if (!quantityMap[it[1]]) {
                    quantityMap[it[1]] = [:]
                }
                quantityMap[it[1]][it[2]?.id] = it[4]
            }
        }

        return quantityMap
    }


    /**
     * Get quantity on hand by inventory item for the given location and date.
     *
     * @param location
     * @return
     */
    Map<InventoryItem, Integer> getQuantityOnHandByInventoryItem(Location location) {
        def quantityMap = [:]
        Date date = getMostRecentInventorySnapshotDate()
        if (location && date) {
            def results = InventorySnapshot.executeQuery("""
						select ii, sum(iis.quantityOnHand)
						from InventorySnapshot iis, InventoryItem ii
						where iis.location = :location
						and iis.date = :date
						and iis.inventoryItem = ii
						group by ii
						""", [location: location, date: date])

            results.each {
                quantityMap[it[0]] = it[1]
            }
        }
        return quantityMap
    }

    List getQuantityOnHandByBinLocation(Location location) {
        def data = []
        Date date = getMostRecentInventorySnapshotDate()
        if (location && date) {
            def results = InventorySnapshot.executeQuery("""
						select 
						    iis.product, 
						    ii,
						    iis.binLocation,
						    iis.quantityOnHand
						from InventorySnapshot iis
						left outer join iis.inventoryItem ii
						left outer join iis.binLocation bl
						where iis.location = :location
						and iis.date = :date
						""", [location: location, date: date])
            //data = results
            def status = { quantity -> quantity > 0 ? "inStock" : "outOfStock" }
            data = results.collect {
                def product = it[0]
                def inventoryItem = it[1]
                def binLocation = it[2]
                def quantity = it[3]

                [
                        status        : status(quantity),
                        product       : product,
                        inventoryItem : inventoryItem,
                        binLocation   : binLocation,
                        quantity      : quantity
                ]
            }
        }
        return data
    }
}
