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
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.jobs.RefreshProductAvailabilityJob
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductSummary
import org.pih.warehouse.reporting.TransactionFact
import org.pih.warehouse.util.LocalizationUtil

import java.text.DateFormat
import java.text.SimpleDateFormat

class InventorySnapshotService {

    boolean transactional = true

    def dataSource
    def locationService
    def productAvailabilityService
    def persistenceInterceptor

    def populateInventorySnapshots(Date date) {
        populateInventorySnapshots(date, false)
    }

    def populateInventorySnapshots(Date date, Boolean forceRefresh) {
        def results
        def startTime = System.currentTimeMillis()

        // Compute bin locations from transaction entries for given location and date
        // Uses GPars to improve performance
        GParsPool.withPool {
            def depotLocations = locationService.getDepots()
            results = depotLocations.collectParallel { Location loc ->
                def binLocations
                def innerStartTime = System.currentTimeMillis()
                persistenceInterceptor.init()
                Location location = Location.get(loc.id)
                binLocations = productAvailabilityService.calculateBinLocations(location, date)
                def readTime = (System.currentTimeMillis() - innerStartTime)
                log.info "Read ${binLocations?.size()} inventory snapshots for location ${location} on date ${date.format("MMM-dd-yyyy")} in ${readTime}ms"
                persistenceInterceptor.flush()
                persistenceInterceptor.destroy()
                return [binLocations: binLocations, location: location, date: date]
            }
        }
        log.info("Total read time: " + (System.currentTimeMillis() - startTime) + "ms")

        // Write all inventory snapshots to the database synchronously
        // Does not use GPars in order to avoid lock wait timeouts
        startTime = System.currentTimeMillis()
        for (result in results) {
            saveInventorySnapshots(result.date, result.location, result.binLocations, forceRefresh)
        }
        log.info("Total write time: " + (System.currentTimeMillis() - startTime) + "ms")
    }

    def populateInventorySnapshots(Location location) {
        // Get most recent inventory snapshot date (or tomorrow's date)
        Date date = getMostRecentInventorySnapshotDate() ?: new Date() + 1
        populateInventorySnapshots(date, location, Boolean.FALSE)
    }

    def populateInventorySnapshots(Date date, Location location, Boolean forceRefresh) {
        def startTime = System.currentTimeMillis()
        def binLocations = productAvailabilityService.calculateBinLocations(location, date)
        def readTime = (System.currentTimeMillis() - startTime)
        log.info "Read ${binLocations?.size()} inventory snapshots for location ${location} on date ${date.format("MMM-dd-yyyy")} in ${readTime}ms"

        // Save inventory snapshots to database
        saveInventorySnapshots(date, location, binLocations, forceRefresh)
    }

    def populateInventorySnapshots(Location location, Product product) {
        def transactionDates = getTransactionDates(location, product)
        for (Date date : transactionDates) {
            populateInventorySnapshots(date, location, product)
        }
    }

    def populateInventorySnapshots(Date date, Location location, Product product) {
        def binLocations = productAvailabilityService.calculateBinLocations(location, product)
        saveInventorySnapshots(date, location, binLocations, Boolean.FALSE)
    }

    def deleteInventorySnapshots(Date date) {
        deleteInventorySnapshots(date, null)
    }

    def deleteInventorySnapshots(Location location) {
        Date date = getMostRecentInventorySnapshotDate() ?: new Date() + 1
        deleteInventorySnapshots(date, location)
    }

    def deleteInventorySnapshots(Location location, Product product) {
        Date date = getMostRecentInventorySnapshotDate() ?: new Date() + 1
        deleteInventorySnapshots(date, location, product)
    }

    def deleteInventorySnapshots(Date date, Location location) {
        deleteInventorySnapshots(date, location, null)
    }

    def deleteInventorySnapshots(Date date, Location location, Product product) {
        Map params = [:]

        String deleteStmt = """delete from InventorySnapshot snapshot where snapshot.date = :date"""
        params.put("date", date)

        if (location) {
            deleteStmt += " and snapshot.location = :location"
            params.put("location", location)
        }

        if (product) {
            deleteStmt += " and snapshot.product = :product"
            params.put("product", product)
        }

        def results = InventorySnapshot.executeUpdate(deleteStmt, params)
        log.info "Deleted ${results} inventory snapshots for date ${date}, location ${location}, product ${product}"
    }

    def saveInventorySnapshots(Date date, Location location, List binLocations, Boolean forceRefresh) {
        saveInventorySnapshots(date, location, null, binLocations, forceRefresh)
    }

    def saveInventorySnapshots(Date date, Location location, Product product, List binLocations, Boolean forceRefresh) {
        def startTime = System.currentTimeMillis()
        def batchSize = ConfigurationHolder.config.openboxes.inventorySnapshot.batchSize ?: 1000
        Sql sql = new Sql(dataSource)

        try {
            // Clear time in case caller did not
            date.clearTime()
            String dateString = date.format("yyyy-MM-dd HH:mm:ss")
            DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

            if (forceRefresh) {
                deleteInventorySnapshots(location, product)
            }

            // Execute inventory snapshot insert/update in batches
            sql.withBatch(batchSize) { BatchingStatementWrapper stmt ->
                binLocations.eachWithIndex { Map binLocationEntry, index ->
                    String insertStatement = generateInsertInventorySnapshotStatement(location, dateString, DATE_FORMAT, binLocationEntry)
                    stmt.addBatch(insertStatement)
                }
                stmt.executeBatch()
            }
            log.info "Saved ${binLocations?.size()} inventory snapshots for location ${location} on date ${date.format("MMM-dd-yyyy")} in ${System.currentTimeMillis() - startTime}ms"
        } catch (Exception e) {
            log.error("Error executing batch update for ${location.name}: " + e.message, e)
            publishEvent(new ApplicationExceptionEvent(e, location))
            throw e;
        } finally {
            sql.close()
        }
    }

    String generateInsertInventorySnapshotStatement(Location location, String dateString, DateFormat DATE_FORMAT, Map entry) {
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

        def insertStatement =
                "INSERT INTO inventory_snapshot (id, version, date, location_id, product_id, product_code, " +
                        "inventory_item_id, lot_number, expiration_date, bin_location_id, bin_location_name, " +
                        "quantity_on_hand, date_created, last_updated) " +
                        "values ('${UUID.randomUUID().toString()}', 0, '${dateString}', '${location?.id}', " +
                        "'${productId}', '${productCode}', " +
                        "${inventoryItemId}, ${lotNumber}, ${expirationDate}, " +
                        "${binLocationId}, ${binLocationName}, ${onHandQuantity}, now(), now()) " +
                        "ON DUPLICATE KEY UPDATE quantity_on_hand=${onHandQuantity}, version=version+1, last_updated=now()"
        return insertStatement
    }


    def getTransactionDates() {
        return Transaction.executeQuery("select distinct(date(transactionDate)) from Transaction order by date(transactionDate) desc")
    }

    def getTransactionDates(Location location, Product product) {
        String query = """
            select distinct(date(t.transactionDate)) 
            from TransactionEntry as te 
            join te.transaction as t
            join te.inventoryItem as ii
            where ii.product = :product
            and t.inventory = :inventory
            order by date(t.transactionDate) desc
        """
        return TransactionEntry.executeQuery(query, [product: product, inventory: location.inventory])
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
                    select i.date, i.location.name as location, product, category.name, sum(i.quantityOnHand)
                    from InventorySnapshot i, Product product, Category category
                    where i.location = :location
                    and i.date = :date
                    and i.product = product
                    and i.product.category = category
                    group by i.date, i.location.name, product
                    """, [location: location, date: date])

            def inventoryLevelsByProduct = InventoryLevel.findAllByInventory(location.inventory).groupBy {
                it.product.id
            }

            log.info "Query response time: " + (System.currentTimeMillis() - startTime)
            startTime = System.currentTimeMillis()

            results.each {
                Product product = it[2]
                InventoryLevel inventoryLevel = inventoryLevelsByProduct[product.id] ? inventoryLevelsByProduct[product.id][0] : null
                data << [
                        date           : it[0],
                        location       : it[1],
                        category       : it[3],
                        productCode    : product.productCode,
                        product        : product.name,
                        productGroup   : product?.genericProduct?.name,
                        tags           : product.tagsToString(),
                        status         : inventoryLevel?.status,
                        quantityOnHand : it[4],
                        minQuantity    : inventoryLevel?.minQuantity ?: 0,
                        maxQuantity    : inventoryLevel?.maxQuantity ?: 0,
                        reorderQuantity: inventoryLevel?.reorderQuantity ?: 0,
                        unitOfMeasure  : product?.unitOfMeasure ?: "EA"
                ]
            }
            log.info "Post-processing response time: " + (System.currentTimeMillis() - startTime)
        }
        return data
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
						select i.product, sum(i.quantityOnHand)
						from InventorySnapshot i
						inner join i.product
						where i.location = :location
						and i.date = :date
						group by i.product
						""", [location: location, date: date])
            results.each {
                quantityMap[it[0]] = it[1]
            }
        }

        return quantityMap
    }

    List getQuantityOnHandByBinLocation(Location location, Date date, List<Product> products) {
        log.info("getQuantityOnHandByBinLocation: location=${location} product=${products}")
        def data = []
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
						and iis.product in (:products)
						and iis.date = :date
						""", [location: location, products: products, date: date])
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

    List getQuantityOnHandBetweenDates(Product product, Location location, Date startDate, Date endDate) {
        return InventorySnapshot.createCriteria().list() {
            projections {
                groupProperty("date")
                groupProperty("location")
                groupProperty("product")
                sum("quantityOnHand", "quantityOnHand")
            }
            eq("product", product)
            eq("location", location)
            between("date", startDate, endDate)
            order("date", "asc")
        }
    }

    List getInventorySnapshots(Product product, Location location, Date date) {
        log.info "Find inventory snapshots by product ${product} location ${location} and date ${date}"
        return InventorySnapshot.createCriteria().list {
            eq("product", product)
            eq("location", location)
            eq("date", date)
            order("binLocation", "asc")
        }
    }

    void updateInventorySnapshots(InventoryItem inventoryItem) {
        def results = InventorySnapshot.executeUpdate(
                "update InventorySnapshot a " +
                        "set a.lotNumber=:lotNumber " +
                        "where a.inventoryItem.id = :inventoryItemId " +
                        "and a.lotNumber != :lotNumber",
                [
                        inventoryItemId: inventoryItem.id,
                        lotNumber      : inventoryItem.lotNumber?:Constants.DEFAULT_LOT_NUMBER
                ]
        )
        log.info "Updated ${results} inventory snapshots for inventory item ${inventoryItem}"
    }

    void updateInventorySnapshots(Location binLocation) {
        def results = InventorySnapshot.executeUpdate(
                "update InventorySnapshot a " +
                        "set a.binLocationName = :binLocationName " +
                        "where a.binLocation.id = :binLocationId " +
                        "and a.binLocationName != :binLocationName",
                [
                        binLocationId  : binLocation.id,
                        binLocationName: binLocation.name?:Constants.DEFAULT_BIN_LOCATION_NAME
                ]
        )
        log.info "Updated ${results} inventory snapshots for bin location ${binLocation}"
    }

    void updateInventorySnapshots(Product product) {
        def results = InventorySnapshot.executeUpdate(
                "update InventorySnapshot a " +
                        "set a.productCode = :productCode " +
                        "where a.product.id = :productId " +
                        "and a.productCode != :productCode",
                [
                        productId  : product.id,
                        productCode: product.productCode
                ]
        )
        log.info "Updated ${results} inventory snapshots for product ${product}"
    }

    def getTransactionReportData(Location location, Date startDate, Date endDate) {

        def transactionCodes = [TransactionCode.DEBIT, TransactionCode.CREDIT].collect { it.toString() }
        // Get all transactions between start and end dates for the given location
        def transactions = TransactionFact.createCriteria().list {
            projections {
                productKey {
                    groupProperty("productCode")
                }
                transactionTypeKey {
                    groupProperty("transactionCode")
                    groupProperty("transactionTypeName")
                }
                sum("quantity")
            }
            transactionTypeKey {
                'in'("transactionCode", transactionCodes)
            }
            transactionDateKey {
                between("date", startDate, endDate)
            }
            locationKey {
                eq("locationId", location.id)
            }
        }

        // Transform transaction facts
        transactions = transactions.collect {
            [
                    productCode        : it[0],
                    transactionCode    : it[1],
                    transactionTypeName: it[2],
                    quantity           : it[3]
            ]
        }
        return transactions
    }

    def getTransactionReportDetails(Location location, List<Category> categories, List<Tag> tags, List<ProductCatalog> catalogs, Date startDate, Date endDate) {

        def transactionData = getTransactionReportData(location, startDate, endDate)

        def transactionTypeNames = TransactionType.createCriteria().list {
            'in'("transactionCode", [TransactionCode.DEBIT, TransactionCode.CREDIT])
        }.collect { it.name }

        // Get starting balance
        def balanceOpeningMap = getQuantityOnHandByProduct(location, startDate)
        if (balanceOpeningMap.isEmpty()) {
            throw new IllegalStateException("No inventory snapshot for ${startDate}")
        }

        // Get ending balance
        def balanceClosingMap = getQuantityOnHandByProduct(location, endDate)
        if (balanceClosingMap.isEmpty()) {
            throw new IllegalStateException("No inventory snapshot for ${endDate}")
        }


        // We need all products that have either an opening balance or closing balance
        def products = new HashSet()
        products.addAll(balanceOpeningMap.keySet())
        products.addAll(balanceClosingMap.keySet())

        def data = []
        products.findAll { (categories.contains(it.category) && ((tags && it.hasOneOfTags(tags)) || (catalogs && it.hasOneOfCatalogs(catalogs)))) ||
                (!tags && !catalogs && categories.contains(it.category)) }.each { Product product ->

            // Get balances by product
            def balanceOpening = balanceOpeningMap.get(product) ?: 0
            def balanceClosing = balanceClosingMap.get(product) ?: 0

            // Get quantity by transaction
            def credits = transactionData.find {
                it.productCode == product.productCode && it.transactionCode.equals("CREDIT")
            }
            def debits = transactionData.find {
                it.productCode == product.productCode && it.transactionCode.equals("DEBIT")
            }

            def quantityInbound = credits?.quantity ?: 0
            def quantityOutbound = debits?.quantity ?: 0

            // Calculate discrepancy
            def quantityAdjustments = balanceClosing -
                    balanceOpening -
                    quantityInbound +
                    quantityOutbound

            def row = [
                    "Code"       : product.productCode,
                    "Name"       : product.name,
                    "Category"   : product.category.name,
                    "Formulary"  : product.productCatalogsToString(),
                    "Tag"        : product.tagsToString(),
                    "Unit Cost"  : product.pricePerUnit ?: ''
            ]
            row.put("Opening", balanceOpening)
            def includeRow = balanceOpening || balanceClosing || quantityAdjustments
            transactionTypeNames.each { String transactionTypeName ->
                if (transactionTypeName.contains(Constants.LOCALIZED_STRING_SEPARATOR)) {
                    transactionTypeName = LocalizationUtil.getLocalizedString(transactionTypeName)
                }
                def quantity =
                        transactionData.find {
                            it.productCode == product.productCode && it.transactionTypeName == transactionTypeName
                        }?.quantity?:0
                row[transactionTypeName] = quantity
                includeRow = quantity != 0 ? true : includeRow
            }

            row.put("Adjustments", quantityAdjustments)
            row.put("Closing", balanceClosing)

            if (includeRow) {
                data << row
            }
        }
        return data.sort { it."Code" }
    }

    def getTransactionReportSummary(Location location, List<Category> categories, List<Tag> tags, List<ProductCatalog> catalogs, Date startDate, Date endDate) {

        // Get starting balance
        def balanceOpeningMap = getQuantityOnHandByProduct(location, startDate)
        if (balanceOpeningMap.isEmpty()) {
            throw new IllegalStateException("No inventory snapshot for ${startDate}")
        }

        // Get ending balance
        def balanceClosingMap = getQuantityOnHandByProduct(location, endDate)
        if (balanceClosingMap.isEmpty()) {
            throw new IllegalStateException("No inventory snapshot for ${endDate}")
        }

        // We need all products that have either an opening balance or closing balance
        def products = new HashSet()
        products.addAll(balanceOpeningMap.keySet())
        products.addAll(balanceClosingMap.keySet())

        def transactionData = getTransactionReportData(location, startDate, endDate)

        // FIXME Category filtering should happen in the query but we need to add a category dimension
        // Flatten the data to make it easier to display
        def data = []
        products.findAll { (categories.contains(it.category) && ((tags && it.hasOneOfTags(tags)) || (catalogs && it.hasOneOfCatalogs(catalogs)))) ||
                (!tags && !catalogs && categories.contains(it.category)) }.each { Product product ->

            // Get balances by product
            def balanceOpening = balanceOpeningMap.get(product) ?: 0
            def balanceClosing = balanceClosingMap.get(product) ?: 0

            // Get quantity by transactionf
            def credits = transactionData.findAll {
                it.productCode == product.productCode && it.transactionCode.equals("CREDIT")
            }
            def debits = transactionData.findAll {
                it.productCode == product.productCode && it.transactionCode.equals("DEBIT")
            }

            def quantityInbound = credits?.sum { it.quantity } ?: 0
            def quantityOutbound = debits?.sum { it.quantity } ?: 0

            // Calculate discrepancy
            def quantityAdjustments = balanceClosing -
                    balanceOpening -
                    quantityInbound +
                    quantityOutbound

            // Transform data into inventory balance rows
            if (balanceOpening || quantityInbound || quantityOutbound || balanceClosing) {
                data << [
                            "Code"       : product.productCode,
                            "Name"       : product.name,
                            "Category"   : product.category.name,
                            "Unit Cost"  : product.pricePerUnit ?: '',
                            "Opening"    : balanceOpening,
                            "Credits"    : quantityInbound,
                            "Debits"     : quantityOutbound,
                            "Adjustments": quantityAdjustments,
                            "Closing"    : balanceClosing,
                        ]
            }
        }
        return data
    }
}
