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
import org.pih.warehouse.core.Tag
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.reporting.TransactionFact

import java.text.DateFormat
import java.text.SimpleDateFormat

class InventorySnapshotService {

    boolean transactional = true

    def dataSource
    def locationService
    def inventoryService
    def persistenceInterceptor
    def grailsApplication

    def populateInventorySnapshots(Date date) {
        populateInventorySnapshots(date, false)
    }

    def populateInventorySnapshots(Date date, Boolean enableOptimization) {
        def results
        def startTime = System.currentTimeMillis()

        // Compute bin locations from transaction entries for given location and date
        // Uses GPars to improve performance
        GParsPool.withPool {
            def depotLocations = locationService.getDepots()
            results = depotLocations.collectParallel { Location loc ->
                def innerStartTime = System.currentTimeMillis()
                persistenceInterceptor.init()
                Location location = Location.get(loc.id)
                Date lastUpdatedDate = InventorySnapshot.lastUpdatedDate(loc.id).list()
                Integer transactionCount = Transaction.countByLocationAsOf(location, lastUpdatedDate).list()
                Boolean skipCalculation = enableOptimization && transactionCount == 0
                def binLocations = (!skipCalculation) ? calculateBinLocations(location, date) : []
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
            saveInventorySnapshots(result.date, result.location, result.binLocations)
        }
        log.info("Total write time: " + (System.currentTimeMillis() - startTime) + "ms")
    }

    def populateInventorySnapshots(Location location) {
        // Get most recent inventory snapshot date (or tomorrow's date)
        Date date = getMostRecentInventorySnapshotDate() ?: new Date() + 1
        populateInventorySnapshots(date, location)
    }

    def populateInventorySnapshots(Date date, Location location) {

        // Calculate current stock for given location
        def startTime = System.currentTimeMillis()
        def binLocations = calculateBinLocations(location, date)
        def readTime = (System.currentTimeMillis() - startTime)
        log.info "Read ${binLocations?.size()} inventory snapshots for location ${location} on date ${date.format("MMM-dd-yyyy")} in ${readTime}ms"

        // Save inventory snapshots to database
        saveInventorySnapshots(date, location, binLocations)
    }

    def populateInventorySnapshots(Location location, Product product) {
        def transactionDates = getTransactionDates(location, product)
        for (Date date : transactionDates) {
            populateInventorySnapshots(date, location, product)
        }
    }

    def populateInventorySnapshots(Date date, Location location, Product product) {
        def binLocations = calculateBinLocations(location, product)
        saveInventorySnapshots(date, location, binLocations)
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

    def saveInventorySnapshots(Date date, Location location, List binLocations) {
        def startTime = System.currentTimeMillis()
        def batchSize = ConfigurationHolder.config.openboxes.inventorySnapshot.batchSize ?: 1000
        Sql sql = new Sql(dataSource)


        try {
            // Clear time in case caller did not
            date.clearTime()
            String dateString = date.format("yyyy-MM-dd HH:mm:ss")
            DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

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

    List<AvailableItem> getAvailableBinLocations(Location location, Product product) {
        return getAvailableBinLocations(location, product, false)
    }

    List<AvailableItem> getAvailableBinLocations(Location location, Product product, boolean excludeOutOfStock) {
        return getAvailableBinLocations(location, [product], excludeOutOfStock)
    }

    List<AvailableItem> getAvailableBinLocations(Location location, List products, boolean excludeOutOfStock = false) {
        def startTime = System.currentTimeMillis()
        def availableBinLocations = getQuantityOnHandByBinLocation(location, products)

        List<AvailableItem> availableItems = availableBinLocations.collect {
            return new AvailableItem(
                    inventoryItem: it?.inventoryItem,
                    binLocation: it?.binLocation,
                    quantityAvailable: it.quantity
            )
        }

        availableItems = sortAvailableItems(availableItems)
        log.info("getAvailableItems(): ${System.currentTimeMillis() - startTime} ms")
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
						select i.date, product, i.location, category.name, sum(i.quantityOnHand)
						from InventorySnapshot i, Product product, Category category
						where i.location in (:locations)
						and i.date = :date
						and i.product = product
						and i.product.category = category
						group by i.date, product, i.location, category.name
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
        Date date = getMostRecentInventorySnapshotDate()
        return getQuantityOnHandByBinLocation(location, date)
    }

    List getQuantityOnHandByBinLocation(Location location, Date date) {
        def data = []

        if (location) {
            def results = InventorySnapshot.executeQuery("""
						select 
						    iis.product, 
						    iis.inventoryItem,
						    iis.binLocation,
						    sum(iis.quantityOnHand)
						from InventorySnapshot iis
						left outer join iis.inventoryItem ii
						left outer join iis.binLocation bl
						where iis.location = :location
						and iis.date = :date
						group by iis.product, iis.inventoryItem, iis.binLocation
						""", [location: location, date: date])

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
        Date date = getMostRecentInventorySnapshotDate()

        return getQuantityOnHandByBinLocation(location, date, products)
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

    def getQuantityOnHand(List<Product> products, Location location, Date date) {
        return InventorySnapshot.createCriteria().list {
            resultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
            projections {
                // Need to use alias other than product to prevent conflict
                groupProperty("product", "p")
                sum("quantityOnHand", "quantityOnHand")
            }
            eq("location", location)
            eq("date", date)
            'in'("product", products)
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
                        lotNumber      : inventoryItem.lotNumber
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
                        binLocationName: binLocation.name
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
                between("date", startDate, endDate+1)
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

        def transactionTypeNames = transactionData.collect { it.transactionTypeName }.unique().sort()

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

        def data = products.findAll { (categories.contains(it.category) && ((tags && it.hasOneOfTags(tags)) || (catalogs && it.hasOneOfCatalogs(catalogs)))) ||
                (!tags && !catalogs && categories.contains(it.category)) }.collect { Product product ->

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
            transactionTypeNames.each { transactionTypeName ->
                def quantity =
                        transactionData.find {
                            it.productCode == product.productCode && it.transactionTypeName == transactionTypeName
                        }?.quantity?:0
                row[transactionTypeName] = quantity
            }

            row.put("Adjustments", quantityAdjustments)
            row.put("Closing", balanceClosing)
            return row;
        }
        data = data.sort { it."Code" }
        return data
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
        def data = products.findAll { (categories.contains(it.category) && ((tags && it.hasOneOfTags(tags)) || (catalogs && it.hasOneOfCatalogs(catalogs)))) ||
                (!tags && !catalogs && categories.contains(it.category)) }.collect { Product product ->

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
            [
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
        return data
    }
}
