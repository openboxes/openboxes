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

import grails.core.GrailsApplication
import grails.gorm.PagedResultList
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.BatchingStatementWrapper
import groovy.sql.Sql
import org.apache.commons.lang.StringEscapeUtils
import org.hibernate.Criteria
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.DetachedCriteria
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.hibernate.criterion.Subqueries
import org.hibernate.SQLQuery
import org.hibernate.sql.JoinType
import org.hibernate.type.StandardBasicTypes
import org.pih.warehouse.PaginatedList
import org.pih.warehouse.api.AllocatedItem
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.ApplicationExceptionEvent
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.jobs.RefreshProductAvailabilityJob
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductActivityCode
import org.pih.warehouse.product.ProductAvailability
import org.pih.warehouse.product.ProductType
import org.pih.warehouse.requisition.RequisitionStatus

import java.text.SimpleDateFormat

@Transactional
class ProductAvailabilityService {

    def dataSource
    def gparsService
    GrailsApplication grailsApplication
    def persistenceInterceptor
    def productService
    def locationService
    def inventoryService
    def dataService
    def sessionFactory

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

    def refreshProductsAvailability(String locationId, List<String> productIds, List<String> binLocationIds, Boolean forceRefresh) {
        if (locationId && productIds && !binLocationIds) {
            refreshProductsAvailability(locationId, productIds, forceRefresh)
            return
        }

        if (!locationId || !productIds) {
            log.info "Stopping refreshing product availability because of lack of location id or product id"
            return
        }

        Location location = Location.load(locationId)
        List<Location> binLocations = []
        binLocationIds.unique().each {
            if (it) {
                binLocations << Location.load(it)
            } else {
                binLocations << null
            }
        }

        productIds.each { String productId ->
            Product product = Product.load(productId)
            refreshProductAvailability(location, product, binLocations, forceRefresh)
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
        return refreshProductAvailability(location, null, forceRefresh)
    }

    def refreshProductAvailability(Location location, Product product, Boolean forceRefresh) {
        log.info "Refreshing product availability location ${location}, product ${product}, forceRefresh ${forceRefresh}..."
        def startTime = System.currentTimeMillis()
        List binLocations = product ? calculateBinLocations(location, product) : calculateBinLocations(location)
        // FIXME: if this deadlocks, it safe to auto-retry here?
        boolean success = saveProductAvailability(location, product, binLocations, forceRefresh)
        if (success) {
            log.info "Refreshed ${binLocations?.size()} product availability records for product (${product}) and location (${location}) in ${System.currentTimeMillis() - startTime}ms"
        } else {
            log.error "Could not refresh ${binLocations?.size()} product availability records for product (${product}) and location (${location}) after ${System.currentTimeMillis() - startTime}ms"
        }
    }

    def refreshProductAvailability(Location location, Product product, List<Location> binLocations, Boolean forceRefresh) {
        log.info "Refreshing product availability location ${location}, product ${product}, binLocations ${binLocations}, forceRefresh ${forceRefresh}..."
        def startTime = System.currentTimeMillis()
        List calculatedBinLocations = calculateBinLocations(location, product, binLocations)
        // FIXME: if this deadlocks, it safe to auto-retry here?
        boolean success = saveProductAvailability(location, product, calculatedBinLocations, forceRefresh)
        if (success) {
            log.info "Refreshed ${calculatedBinLocations?.size()} product availability records for product (${product?.productCode}), binLocations ${binLocations} and location (${location}) in ${System.currentTimeMillis() - startTime}ms"
        } else {
            log.error "Could not refresh ${calculatedBinLocations?.size()} product availability records for product (${product?.productCode}) and location (${location}) after ${System.currentTimeMillis() - startTime}ms"
        }
    }

    def calculateBinLocationsAsOfDate(Location location, Date date) {
        def binLocations = inventoryService.getBinLocationDetails(location, date)
        binLocations = transformBinLocations(binLocations, [])
        return binLocations
    }

    def calculateBinLocations(Location location) {
        return calculateBinLocations(location, null as Product)
    }

    def calculateBinLocations(Location location, Product product) {
        List<BinLocationItem> binLocations
        if (product) {
            binLocations = inventoryService.getProductQuantityByBinLocation(location, product, Boolean.TRUE)
        } else {
            binLocations = inventoryService.getBinLocationDetails(location)
        }
        List<AllocatedItem> picked =
                getQuantityPickedByProductAndLocation(location, product)
        return transformBinLocations(binLocations, picked)
    }

    def calculateBinLocations(Location location, Product product, List<Location> binLocations) {
        def binLocationsWithQuantity = inventoryService.getProductQuantityByBinLocation(location, product, binLocations, Boolean.TRUE)
        // cleanse bin locations, to ensure that we accidentally don't overwrite
        // (in case INVENTORY or PRODUCT_INVENTORY transaction had entries with other bins)
        if (binLocations) {
            binLocationsWithQuantity = binLocationsWithQuantity.findAll { binLocations*.id?.contains(it.id) }
        }
        def picked = getQuantityPickedByProductAndLocation(location, product)
        return transformBinLocations(binLocationsWithQuantity, picked)
    }

    /**
     * Get quantity allocated for bin locations and inventory items for a specific location (origin) and product.
     * Data is pulled from:
     *  1. Picklist items from pending requisitions (outbound stock movements) with origin being provided location
     *     that are *NOT* having RECALLED inventory item (inventoryItem.lotStatus) or bin location *WITHOUT*
     *     HOLD_STOCK in the supported activity (location.supportedActivities).
     *  2. Picklist items from pending orders (outbound returns) with origin being provided location.
     *     (IMPORTANT: Outbound returns can have picked items with RECALLED lots and bins with HOLD_STOCK activity)
     * */
    List<AllocatedItem> getQuantityPickedByProductAndLocation(Location location, Product product) {
        def query = """
            SELECT 
                bin_location_id as bin_location_id, 
                inventory_item_id as inventory_item_id, 
                SUM(quantity_picked) as quantity_allocated
            FROM (
                SELECT
                    pli.bin_location_id as bin_location_id,
                    pli.inventory_item_id as inventory_item_id,
                    sum(pli.quantity) as quantity_picked
                FROM picklist_item pli
                    INNER JOIN picklist p ON pli.picklist_id = p.id
                    LEFT JOIN requisition_item ri ON pli.requisition_item_id = ri.id
                    LEFT JOIN requisition r ON p.requisition_id = r.id
                    LEFT JOIN inventory_item ii ON pli.inventory_item_id = ii.id
                    LEFT JOIN location l ON pli.bin_location_id = l.id
                    LEFT JOIN (
                        SELECT
                            GROUP_CONCAT(lsa_select.supported_activities_string) as activities,
                            lsa_select.location_id as location_id
                        FROM location_supported_activities lsa_select
                        GROUP BY lsa_select.location_id
                    ) lsa ON lsa.location_id = l.id
                WHERE (r.origin_id = :locationId 
                    AND r.status IN (:pendingRequisitionStatuses))
                  AND (ri.id IS NOT NULL AND (ii.lot_status IS NULL OR ii.lot_status != 'RECALLED') 
                  AND (lsa.activities IS NULL OR lsa.activities NOT LIKE '%HOLD_STOCK%'))
                  AND (:productId = '' OR ri.product_id = :productId)
                GROUP BY pli.bin_location_id, pli.inventory_item_id
                UNION
                SELECT
                    pli.bin_location_id as bin_location_id,
                    pli.inventory_item_id as inventory_item_id,
                    sum(pli.quantity) as quantity_picked
                FROM picklist_item pli
                    INNER JOIN picklist p ON pli.picklist_id = p.id
                    LEFT JOIN order_item oi ON pli.order_item_id = oi.id
                    LEFT JOIN `order` o ON p.order_id = o.id
                    LEFT JOIN inventory_item ii ON pli.inventory_item_id = ii.id
                WHERE (o.origin_id = :locationId 
                    AND o.status IN (:pendingOrderStatuses))
                  AND (oi.id IS NOT NULL)
                  AND (:productId = '' OR oi.product_id = :productId)
                GROUP BY pli.bin_location_id, pli.inventory_item_id
            ) as requisition_order_union
            GROUP BY bin_location_id, inventory_item_id;
        """

        SQLQuery sqlQuery = sessionFactory.currentSession.createSQLQuery(query)
        List results = sqlQuery.addScalar("bin_location_id", StandardBasicTypes.STRING)
                .addScalar("inventory_item_id", StandardBasicTypes.STRING)
                .addScalar("quantity_allocated", StandardBasicTypes.BIG_DECIMAL)
                .setString("locationId", location?.id)
                .setString("productId", product?.id ?: '')
                .setParameterList("pendingRequisitionStatuses", RequisitionStatus.listPending().collect { it.name() })
                .setParameterList("pendingOrderStatuses", OrderStatus.listPending().collect { it.name() })
                .setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
                .list()

        return results.collect { result ->
            new AllocatedItem([
                    binLocation      : Location.load(result["bin_location_id"]),
                    inventoryItem    : InventoryItem.load(result["inventory_item_id"]),
                    quantityAllocated: result["quantity_allocated"]
            ])
        }
    }

    boolean saveProductAvailability(Location location, Product product, List binLocations, Boolean forceRefresh) {
        log.info "Saving product availability for product=${product?.productCode}, location=${location}"
        def batchSize = Holders.config.openboxes.inventorySnapshot.batchSize ?: 1000
        def startTime = System.currentTimeMillis()

        try {
            Sql sql = new Sql(dataSource)

            // Execute SQL in batches
            // FIXME recover from deadlocks
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
            grailsApplication.mainContext.publishEvent(new ApplicationExceptionEvent(e, location))
            return false
        }

        return true
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

    def transformBinLocations(List<BinLocationItem> binLocations, List<AllocatedItem> pickedItems) {
        long startTime = System.currentTimeMillis()
        // Group picked items by inventory item/bin location pair to use the advantage of O(1) for read operations of Map
        // instead of O(n) for List in the collect below (OBGM-508)
        Map<List<String>, List<AllocatedItem>> pickedItemsGrouped =
                pickedItems.groupBy{ [it.inventoryItem.id, it.binLocation?.id ]}
        List<Map<String, Object>> binLocationsTransformed = binLocations.collect { it ->
            [
                product          : [id: it?.product?.id, productCode: it?.product?.productCode, name: it?.product?.name],
                inventoryItem    : [id: it?.inventoryItem?.id, lotNumber: it?.inventoryItem?.lotNumber, expirationDate: it?.inventoryItem?.expirationDate],
                binLocation      : [id: it?.binLocation?.id, name: it?.binLocation?.name],
                quantity         : it.quantity,
                quantityAllocated: pickedItems
                        ? (pickedItemsGrouped[[it.inventoryItem?.id, it.binLocation?.id]]?.sum{ AllocatedItem  val -> val.quantityAllocated } ?: 0)
                        : 0,
                quantityOnHold   : it?.isOnHold || it?.inventoryItem?.lotStatus == LotStatusCode.RECALLED ? it.quantity : 0
            ]
        }
        log.debug("Collecting inside transformBinLocations took: " + (System.currentTimeMillis() - startTime) + " ms")

        // Attempting to prevent deadlock due to gap locks
        binLocationsTransformed = binLocationsTransformed.sort { a, b ->
            a?.binLocation?.name <=> b?.binLocation?.name ?:
                    a?.product?.productCode <=> b?.product?.productCode ?:
                            a?.inventoryItem?.lotNumber <=> b?.inventoryItem?.lotNumber
        }

        return binLocationsTransformed
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
        return getInventoryByProduct(location, [])
    }

    def getInventoryByProduct(Location location, List<Category> categories) {
        def categoriesQuery = "";
        def queryArguments = [location: location]

        if (categories) {
            categoriesQuery = "and pa.product.category.id in (:categories)"
            queryArguments += [categories: categories.id]
        }

        def quantityMap = [:]
        if (location) {
            def results = ProductAvailability.executeQuery("""
						select pa.product, sum(pa.quantityOnHand),
						 sum(case when pa.quantityAvailableToPromise > 0 then pa.quantityAvailableToPromise else 0 end)
						from ProductAvailability pa
						where pa.location = :location
						${categoriesQuery}
						group by pa.product
						""".toString(), queryArguments)
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

    Map<Product, Map<Location, Integer>> getQuantityOnHandByProduct(List<Location> locations, List<Category> categories) {
        def categoriesQuery = "";
        def queryArguments = [locations: locations]
        // if we don't have categories and checkbox is checked, then we should get all of the products
        if (categories) {
            categoriesQuery = "and category.id in (:categories)"
            queryArguments += [categories: categories.collect { it.id }]
        }
        def quantityMap = [:]
        if (locations) {
            def results = ProductAvailability.executeQuery("""
						select product, pa.location, category.name, sum(pa.quantityOnHand),
						 sum(case when pa.quantityAvailableToPromise > 0 then pa.quantityAvailableToPromise else 0 end)
						from ProductAvailability pa, Product product, Category category
						where pa.location in (:locations)
						${categoriesQuery}
						and pa.product = product
						and pa.product.category = category
						group by product, pa.location, category.name
						""".toString(), queryArguments)

            results.each {
                if (!quantityMap[it[0]]) {
                    quantityMap[it[0]] = [:]
                }
                quantityMap[it[0]][it[1]?.id] = [quantityOnHand: it[3], quantityAvailableToPromise: it[4]]
            }
        }

        return quantityMap
    }

    List getQuantityOnHandByBinLocation(Location location, List<InventoryItem> inventoryItems = []) {
        if (!location) {
            return []
        }
        Map arguments = [ location: location ]
        if (inventoryItems) {
            arguments.inventoryItems = inventoryItems.id
        }
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
                    where pa.location = :location""" +
                "${inventoryItems ? " and pa.inventoryItem.id in (:inventoryItems) " : " "}" +
                """group by pa.product, pa.inventoryItem, pa.binLocation""", arguments)

        return collectQuantityOnHandByBinLocation(results)
    }

    List getAvailableQuantityOnHandByBinLocation(Location location, List<InventoryItem> inventoryItems = []) {
        def data = []

        Map arguments = [ location: location ]
        if (inventoryItems) {
            arguments.inventoryItems = inventoryItems.id
        }

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
						where pa.location = :location and pa.quantityOnHand > 0""" +
                        "${inventoryItems ? " and pa.inventoryItem.id in (:inventoryItems) " : " "}" +
						"""group by pa.product, pa.inventoryItem, pa.binLocation""", arguments)

            data = collectQuantityOnHandByBinLocation(results)
        }
        return data
    }

    List getAvailableItems(Location location, List<String> productsIds, boolean excludeNegativeQuantity = false) {
        log.info("getQuantityOnHandByBinLocation: location=${location} product=${productsIds}")
        List<AvailableItem> data = []
        if (location) {
            def results = ProductAvailability.executeQuery("""
						select 
						    ii,
						    pa.binLocation,
						    pa.quantityOnHand,
						    pa.quantityAvailableToPromise
						from ProductAvailability pa
						left outer join pa.inventoryItem ii
						left outer join pa.binLocation bl
						where pa.location = :location
						""" +
                        "${excludeNegativeQuantity ? "and pa.quantityOnHand > 0" : ""}" +
                        "and pa.product.id in (:products)", [location: location, products: productsIds])

            data = results.collect {
                InventoryItem inventoryItem = it[0]
                Location binLocation = it[1]
                Integer quantityOnHand = it[2]
                Integer quantityAvailableToPromise = it[3]

                return new AvailableItem(
                        inventoryItem               : inventoryItem,
                        binLocation                 : binLocation,
                        quantityOnHand              : quantityOnHand,
                        quantityAvailable           : quantityAvailableToPromise
                )
            }
        }
        return data
    }

    Map<InventoryItem, Integer> getQuantityOnHandByInventoryItem(Location location, List<InventoryItem> inventoryItems = []) {
        if (!location) {
            return [:]
        }
        List results = ProductAvailability.createCriteria().list {
            projections {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                groupProperty("inventoryItem", "inventoryItem")
                sum("quantityOnHand", "quantityOnHand")
            }
            eq("location", location)
            if (inventoryItems) {
                'in'("inventoryItem.id", inventoryItems.id)
            }
        }
        return results.inject([:]) { map, it -> map << [(it.inventoryItem): it.quantityOnHand] }
    }

    List<AvailableItem> getAvailableBinLocations(Location location, String productId) {
        return getAvailableBinLocations(location, [productId])
    }

    List<AvailableItem> getAllAvailableBinLocations(Location location, String productId) {
        return getAllAvailableBinLocations(location, [productId])
    }

    List<AvailableItem> getAvailableBinLocations(Location location, List<String> productsIds) {
        List<AvailableItem> availableItems = getAvailableItems(location, productsIds, true)

        availableItems = sortAvailableItems(availableItems)

        return availableItems
    }

    // Include also bin locations with negative qty (needed for edit page items)
    List<AvailableItem> getAllAvailableBinLocations(Location location, List products) {
        List<AvailableItem> availableBinLocations = getAvailableItems(location, products)

        return availableBinLocations
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
        if (!product?.id || !product?.productCode) {
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
        def innerProductIds = searchTerms ?
                productService.searchProducts(searchTerms.toArray(), [])?.collect { it.id } : []

        // Retrieve all product types with SEARCHABLE and SEARCHABLE_NO_STOCK activity codes
        String productTypeQuery = "select pt from ProductType pt left join pt.supportedActivities sa where sa=:productActivityCode"
        def searchableProductTypes = ProductType.executeQuery(productTypeQuery, [productActivityCode: ProductActivityCode.SEARCHABLE])
        def searchableNoStockProductTypes = ProductType.executeQuery(productTypeQuery, [productActivityCode: ProductActivityCode.SEARCHABLE_NO_STOCK])

        // Detached criteria used as a subquery to get aggregated quantity on hand value for a product location pair
        DetachedCriteria aggregatedQuantityQuery = DetachedCriteria.forClass(ProductAvailability, 'pa').with {
            setProjection Projections.sum('pa.quantityOnHand')
            add(Restrictions.eqProperty('pa.product.id', 'this.id'))
            add(Restrictions.eq('pa.location.id', command.location.id))
        }

        PagedResultList products = Product.createCriteria().list([max: command.maxResults, offset: command.offset]) {
            eq("active", true)

            // Restrict products by selected product types
            if (command.productTypes) {
                'in'("productType", command.productTypes)
                if (!command.showOutOfStockProducts) {
                    // Read: 0 is less than result from subquery
                    add Subqueries.lt(0L, aggregatedQuantityQuery)
                }
            }
            // Or apply default product type restrictions:
            // return products with product type with activity searchable no stock OR (searchable AND qoh > 0)
            else {
                if (!command.showOutOfStockProducts) {

                    // SUM(product availability.quantity_on_hand) > 0
                    def quantityGreaterThanZero = Subqueries.lt(0L, aggregatedQuantityQuery)
                    // productType in (:searchableProductTypes)
                    def inSearchableProductTypes = Restrictions.in("productType", searchableProductTypes)
                    // productType in (:searchableNoStockProductTypes)
                    def inSearchableNoStockProductTypes = Restrictions.in("productType", searchableNoStockProductTypes)

                    // Create a disjunction with none, one or both of the searchable product type restrictions
                    def disjunction = Restrictions.disjunction()

                    if (searchableNoStockProductTypes)
                        disjunction.add(inSearchableNoStockProductTypes)

                    if (searchableProductTypes)
                        disjunction.add(Restrictions.conjunction().add(quantityGreaterThanZero).add(inSearchableProductTypes))

                    add(disjunction)
                }
            }

            // Restrict products by selected categories
            if (categories) {
                'in'("category", categories)
            }

            // Restrict products by selected tags
            if (command.tags) {
                tags {
                    'in'("id", command.tags*.id)
                }
            }

            // Restrict products by selected catalogs
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
            order("productCode")
        }

        def quantityMap = products ? getQuantityOnHandByProduct(command.location, products) : [:]
        def items = products.collect { Product product ->
            [
                id   : product.id,
                product: product,
                quantityOnHand: quantityMap[product] ?: 0
            ]
        }

        return new PaginatedList(items, products.totalCount)
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

    Integer getQuantityAvailableToPromiseForProductInBin(Location origin, Location binLocation, InventoryItem inventoryItem) {
        return ProductAvailability.createCriteria().get {
            projections {
                property("quantityAvailableToPromise")
            }
            eq("inventoryItem", inventoryItem)
            eq("location", origin)
            if (binLocation) {
                eq("binLocation", binLocation)
            } else {
                isNull("binLocation")
            }
        }
    }

    /**
     * Used for product merge feature (when primary product *had not*
     * the same lot as obsolete product). Change product to primary for rows
     * with given inventory item
     * */
    void updateProductAvailabilityOnMergeProduct(InventoryItem obsoleteInventoryItem, Product primaryProduct, Product obsoleteProduct) {
        if (!obsoleteProduct?.id || !primaryProduct?.id || !obsoleteInventoryItem?.id) {
            return
        }

        // First update records that won't violate product_availability_uniq_idx (location_id, product_code, lot_number, bin_location_name)
        String updateStatement = """
            UPDATE IGNORE product_availability
            SET product_code = '${primaryProduct.productCode}', 
                product_id = '${primaryProduct.id}' 
            WHERE inventory_item_id = '${obsoleteInventoryItem.id}';
        """
        dataService.executeStatement(updateStatement)
        log.info "Updated product availabilities for product: ${primaryProduct?.productCode} and " +
            "inventory item: ${obsoleteInventoryItem?.id}"

        // Cupy/sum all the remaining availabilities that violated product_availability_uniq_idx
        processIgnoredProductAvailabilitiesOnProductMerge(primaryProduct, obsoleteInventoryItem, null)
    }

    /**
     * Used for product merge feature (when primary product *had* the same lot as obsolete)
     * Change product and inventory to primary for rows with given obsolete inventory item
     * */
    void updateProductAvailabilityOnMergeProduct(InventoryItem primaryInventoryItem, InventoryItem obsoleteInventoryItem, Product primaryProduct, Product obsoleteProduct) {
        if (!primaryProduct?.id || !primaryInventoryItem?.id || !obsoleteInventoryItem?.id) {
            return
        }

        // First update records that won't violate product_availability_uniq_idx (location_id, product_code, lot_number, bin_location_name)
        String updateStatement = """
            UPDATE IGNORE product_availability
            SET product_code = '${primaryProduct.productCode}', 
                product_id = '${primaryProduct.id}', 
                inventory_item_id = '${primaryInventoryItem.id}', 
                lot_number = '${primaryInventoryItem.lotNumber ?: 'DEFAULT'}' 
            WHERE inventory_item_id = '${obsoleteInventoryItem.id}';
        """
        dataService.executeStatement(updateStatement)
        log.info "Updated product availabilities for product: ${primaryProduct?.productCode} and " +
            "inventory item: ${primaryInventoryItem?.id} with obsolete inventory item: ${obsoleteInventoryItem.id}"

        // Cupy/sum all the remaining availabilities that violated product_availability_uniq_idx
        processIgnoredProductAvailabilitiesOnProductMerge(primaryProduct, obsoleteInventoryItem, primaryInventoryItem)
    }

    /**
     * Used for product merge feature when initial update of product and inventory item was ignored due to the
     * unique product_availability_uniq_idx violation.
     * */
    void processIgnoredProductAvailabilitiesOnProductMerge(Product primaryProduct, InventoryItem obsoleteInventoryItem, InventoryItem primaryInventoryItem) {
        // Get all remaining obsolete product availabilities with obsolete inventory item
        List<ProductAvailability> remainingProductAvailabilities = ProductAvailability.findAllByInventoryItem(obsoleteInventoryItem)
        remainingProductAvailabilities?.each { ProductAvailability remainingProductAvailability ->
            // Check if product availabilities are already existing for a product_availability_uniq_idx
            ProductAvailability existingProductAvailability = ProductAvailability.createCriteria().get {
                eq("location", remainingProductAvailability.location)
                eq("productCode", primaryProduct.productCode)
                eq("lotNumber", remainingProductAvailability.lotNumber?.trim() ?: 'DEFAULT')
                eq("binLocationName", remainingProductAvailability.binLocationName)
            }

            // If exists and it is not just updated availability in the UPDATE query, then add quantities from obsolete PA to the new main one
            if (existingProductAvailability && existingProductAvailability.id != remainingProductAvailability.id) {
                existingProductAvailability.quantityOnHand += remainingProductAvailability.quantityOnHand
                existingProductAvailability.quantityAllocated += remainingProductAvailability.quantityAllocated
                existingProductAvailability.quantityNotPicked += remainingProductAvailability.quantityNotPicked
                existingProductAvailability.quantityOnHold += remainingProductAvailability.quantityOnHold
                existingProductAvailability.quantityAvailableToPromise += remainingProductAvailability.quantityAvailableToPromise
                existingProductAvailability.save(flush: true)

                remainingProductAvailability.quantityOnHand = 0
                remainingProductAvailability.quantityAllocated = 0
                remainingProductAvailability.quantityNotPicked = 0
                remainingProductAvailability.quantityOnHold = 0
                remainingProductAvailability.quantityAvailableToPromise = 0
                remainingProductAvailability.save(flush: true)

                return
            }

            // Otherwise swap product (and inventory item) on the remaining obsolete PA with primary ones
            remainingProductAvailability.product = primaryProduct
            remainingProductAvailability.productCode = primaryProduct.productCode
            if (primaryInventoryItem) {
                remainingProductAvailability.inventoryItem = primaryInventoryItem
                remainingProductAvailability.lotNumber = primaryInventoryItem.lotNumber ?: 'DEFAULT'
            }
            remainingProductAvailability.save(flush: true)
        }
    }
}
