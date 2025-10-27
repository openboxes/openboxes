/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import grails.gorm.transactions.Transactional
import grails.plugins.csv.CSVWriter
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.hibernate.sql.JoinType

import org.pih.warehouse.DateUtil
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.forecasting.ForecastingService
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryLevelStatus
import org.pih.warehouse.inventory.InventoryStatus
import org.pih.warehouse.inventory.ReorderReportFilterCommand
import org.pih.warehouse.inventory.ReorderReportItemDto
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.product.availability.InventoryByProduct
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.report.InventoryReportCommand
import org.pih.warehouse.requisition.RequisitionItem

import java.sql.Timestamp
import java.text.SimpleDateFormat

@Transactional(readOnly=true)
class DashboardService {

    ConfigService configService
    def productAvailabilityService
    ForecastingService forecastingService
    UserService userService
    MessageLocalizer messageLocalizer

    /**
     * Get fast moving items based on requisition data.
     *
     * @param location
     * @param date
     * @param max
     * @return
     */
    def getFastMovers(Location location, Date date, Integer max) {
        def startTime = System.currentTimeMillis()
        def data = [:]
        try {
            data.location = location.id
            data.startDate = date - 30
            data.endDate = date

            def criteria = RequisitionItem.createCriteria()
            def results = criteria.list {
                requisition {
                    eq("origin", location)
                    between("dateRequested", date - 30, date)
                }
                projections {
                    groupProperty("product")
                    countDistinct('id', "occurrences")
                    sum("quantity", "quantity")
                }
                order('occurrences', 'desc')
                order('quantity', 'desc')
                if (max) {
                    maxResults(max)
                }
            }

            def quantityMap = productAvailabilityService.getCurrentInventory(location)

            def count = 1
            data.results = results.collect {

                def quantityOnHand = quantityMap[it[0]] ?: 0
                [
                        rank             : count++,
                        id               : it[0].id,
                        productCode      : it[0].productCode,
                        name             : it[0].name,
                        requisitionCount : it[1],
                        quantityRequested: it[2],
                        quantityOnHand   : quantityOnHand,
                ]
            }
            data.responseTime = (System.currentTimeMillis() - startTime) + " ms"


        } catch (Exception e) {
            log.error("Error occurred while getting requisition items " + e.message, e)
            data = e.message
        }
        return data
    }


    def getExpirationSummary(location) {
        def expirationSummary = [:]
        def expirationAlerts = getExpirationAlerts(location)
        def daysToExpiry = [30, 60, 90, 180, 365]
        expirationAlerts.each {
            if(it.inventoryItem.expires != "never") {
                if (it.daysToExpiry > 0) {
                    if (it.daysToExpiry > 365) {
                        expirationSummary["greaterThan365Days"] = expirationSummary["greaterThan365Days"] ? expirationSummary["greaterThan365Days"] + 1 : 1
                    }
                    daysToExpiry.each { day ->
                        if (it.daysToExpiry <= day ) {
                            expirationSummary["within${day}Days"] =  expirationSummary["within${day}Days"] ? expirationSummary["within${day}Days"] + 1 : 1
                        }
                    }
                } else {
                    expirationSummary["expired"] = expirationSummary["expired"] ? expirationSummary["expired"] + 1 : 1
                }
            }
        }

        // FIXME Clean this up a bit
        expirationSummary["totalExpiring"] = expirationSummary.findAll {
            it.key != "never"
        }.values().sum()

        return expirationSummary
    }

    def getExpirationAlerts(location) {
        def startTime = System.currentTimeMillis()

        def expirationAlerts = []
        def today = new Date()
        def quantityMap = productAvailabilityService.getQuantityOnHandByInventoryItem(location)
        quantityMap.each { key, value ->
            if (value > 0) {
                def daysToExpiry = key.expirationDate ? (key.expirationDate - today) : null
                expirationAlerts << [id            : key.id, lotNumber: key.lotNumber, quantity: value,
                                     expirationDate: key.expirationDate, daysToExpiry: daysToExpiry,
                                     product       : key.product.toJson(), inventoryItem: key.toJson()
                ]
            }
        }

        log.info "Expiration alerts: " + (System.currentTimeMillis() - startTime) + " ms"

        return expirationAlerts

    }

    /**
     * Get all expired inventory items for the given category and location.
     *
     * @param category
     * @param location
     * @return
     */
    List getExpiredStock(InventoryReportCommand command) {

        long startTime = System.currentTimeMillis()

        List<InventoryItem> expiredStock = InventoryItem.createCriteria().list {
            createAlias("product", "product", JoinType.LEFT_OUTER_JOIN)

            lt("expirationDate", new Date())

            if (command.category) {
                eq("product.category", command.category)
            }

            if (command.startDate) {
                ge("expirationDate", DateUtil.asDate(command.startDate.plusDays(1)))
            }

            if (command.endDate) {
                le("expirationDate", DateUtil.asDate(command.endDate))
            }

            order("expirationDate", "desc")
        }

        Map<InventoryItem, Integer> quantityMap =
                productAvailabilityService.getQuantityOnHandByInventoryItem(command.location)
        expiredStock = expiredStock.findAll { quantityMap[it] > 0 }


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
    List getExpiringStock(InventoryReportCommand command) {
        long startTime = System.currentTimeMillis()

        Date today = new Date()
        today.clearTime()
        // Get all stock expiring ever (we'll filter later)
        List<InventoryItem> expiringStock = InventoryItem.createCriteria().list {
            createAlias("product", "product", JoinType.LEFT_OUTER_JOIN)

            ge("expirationDate", today + 1)

            if (command.category) {
                eq("product.category", command.category)
            }

            if (command.startDate) {
                ge("expirationDate", DateUtil.asDate(command.startDate.plusDays(1)))
            }

            if (command.endDate) {
                le("expirationDate", DateUtil.asDate(command.endDate))
            }

            order("expirationDate", "asc")
        }

        def quantityMap = productAvailabilityService.getQuantityOnHandByInventoryItem(command.location)
        expiringStock = expiringStock.findAll { quantityMap[it] > 0 }

        if (command.status) {
            def daysToExpiry = [30, 60, 90, 180, 365]

            expiringStock = expiringStock.findAll { inventoryItem ->
                if (command.status == "greaterThan365Days") {
                    inventoryItem.expirationDate - today > 365
                } else if (command.status == "expired") {
                    inventoryItem.expirationDate - today <= 0
                } else inventoryItem
            }

            daysToExpiry.each { day ->
                if (command.status == "within${day}Days") {
                    expiringStock = expiringStock.findAll { inventoryItem ->
                        inventoryItem.expirationDate - today <= day
                    }
                }
            }
        }

        log.debug "Get expiring stock: " + (System.currentTimeMillis() - startTime) + " ms"
        return expiringStock
    }
    def getInventoryItems(Location location) {
        return getInventoryItems(location, [])
    }

    def getInventoryItems(Location location, List<Category> categories) {
        def inventoryByProduct = productAvailabilityService.getInventoryByProduct(location, categories)
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy {
            it.product
        }

        def inventoryItems = []
        inventoryByProduct.each { product, inventoryItem ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def inventoryStatus

            if (inventoryLevel) {
                inventoryStatus = inventoryLevel?.statusMessage(inventoryItem.quantityOnHand) ?: "${inventoryLevel?.id}"
            } else {
                inventoryStatus = inventoryItem.quantityOnHand > 0 ? "IN_STOCK" : "STOCK_OUT"
            }

            inventoryItems << [
                    status: inventoryStatus,
                    product: product,
                    quantity: inventoryItem.quantityOnHand,
                    quantityAvailableToPromise: inventoryItem.quantityAvailableToPromise,
                    inventoryLevel: inventoryLevel
            ]
        }

        return inventoryItems
    }

    def getReconditionedStock(Location location, List<Category> categories) {
        long startTime = System.currentTimeMillis()
        def inventoryItems = getInventoryItems(location, categories)
        def reconditionedStock = inventoryItems.findAll { it.product.reconditioned }
        log.debug "Get reconditioned stock: " + (System.currentTimeMillis() - startTime) + " ms"
        return reconditionedStock
    }


    def getTotalStock(Location location, List<Category> categories = []) {
        long startTime = System.currentTimeMillis()
        def inventoryItems = getInventoryItems(location, categories)
        log.debug "Get total stock: " + (System.currentTimeMillis() - startTime) + " ms"
        return inventoryItems
    }

    def getInStock(Location location, List<Category> categories) {
        long startTime = System.currentTimeMillis()
        def inventoryItems = getInventoryItems(location, categories)
        def inStock = inventoryItems.findAll { it.quantity > 0 }
        log.debug "Get in stock: " + (System.currentTimeMillis() - startTime) + " ms"
        return inStock
    }


    def getTotalStockValue(Location location) {
        def hitCount = 0
        def missCount = 0
        def totalCount = 0
        def totalStockValue = 0.0
        def stockValueByProduct = [:]
        if (location.inventory) {
            def quantityMap = productAvailabilityService.getCurrentInventory(location)
            quantityMap.each { product, quantity ->
                if (product.pricePerUnit) {
                    def stockValueForProduct = product.pricePerUnit * quantity
                    if (stockValueForProduct > 0) {
                        stockValueByProduct[product] = stockValueForProduct
                        totalStockValue += stockValueForProduct
                    }
                    hitCount++
                } else {
                    missCount++
                }
            }
            totalCount = quantityMap?.keySet()?.size()
        }
        return [totalStockValue: totalStockValue, hitCount: hitCount, missCount: missCount, totalCount: totalCount, stockValueByProduct: stockValueByProduct]

    }

    String exportLatestInventoryDate(location) {
        SimpleDateFormat formatDate = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss")
        StringWriter sw = new StringWriter()

        Map<Product, Object> inventoryItemsMap = getTotalStock(location).collectEntries { [it.product, it ] }
        Set<Product> products = inventoryItemsMap.keySet()

        List<String> transactionTypes = configService.getProperty('openboxes.inventoryCount.transactionTypes', List) as List<String>

        List<Object[]> latestInventoryDates = TransactionEntry.executeQuery("""
                select ii.product.id, max(t.transactionDate)
                from TransactionEntry as te
                left join te.inventoryItem as ii
                left join te.transaction as t
                where t.inventory = :inventory
                and t.transactionType.id in (:transactionTypeIds)
                and (t.comment <> :commentToFilter or t.comment IS NULL)
                group by ii.product
                """,
                [inventory: location.inventory, transactionTypeIds: transactionTypes, commentToFilter: Constants.INVENTORY_BASELINE_MIGRATION_TRANSACTION_COMMENT])

        // Convert to map
        Map<String, Timestamp> latestInventoryDateMap = latestInventoryDates.collectEntries { [it[0], it[1]] }

        Map<Product, InventoryLevel> inventoryLevelMap = InventoryLevel
                .findAllByInventory(location.inventory)
                .collectEntries { [it.product, it] }

        CSVWriter csvWriter = new CSVWriter(sw, {
            "Product Code" { it.productCode }
            "Name" { it.name }
            "ABC" { it.abcClass }
            "Most Recent Stock Count" { it.latestInventoryDate }
            "QoH" { it.quantityOnHand }
            "Unit of Measure" { it.unitOfMeasure }
        })

        products.each { product ->
            Timestamp latestInventoryDate = latestInventoryDateMap[product.id]
            csvWriter << [
                    productCode        : product.productCode ?: "",
                    name               : product.name,
                    unitOfMeasure      : product.unitOfMeasure ?: "",
                    abcClass           : inventoryLevelMap[product]?.abcClass ?: "",
                    latestInventoryDate: latestInventoryDate ? "${formatDate.format(latestInventoryDate)}" : "",
                    quantityOnHand     : inventoryItemsMap[product]?.quantity ?: ""
            ]
        }
        return sw.toString()
    }


    def getDashboardAlerts(Location location) {
        log.info "Dashboard alerts for ${location}"

        long startTime = System.currentTimeMillis()
        def quantityMap = productAvailabilityService.getCurrentInventory(location)
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy {
            it.product
        }

        def totalStock = quantityMap
        def reconditionedStock = quantityMap.findAll { it.key.reconditioned }
        def onHandQuantityZero = quantityMap.findAll { it.value <= 0 }
        def inStock = quantityMap.findAll { it.value > 0 }

        def outOfStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0
        }

        def lowStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def minQuantity = inventoryLevel?.minQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && minQuantity && quantity <= minQuantity
        }

        def reorderStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def reorderQuantity = inventoryLevel?.reorderQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && reorderQuantity && quantity <= reorderQuantity
        }

        def healthyStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def reorderQuantity = inventoryLevelMap[product]?.first()?.reorderQuantity
            def maxQuantity = inventoryLevelMap[product]?.first()?.maxQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity > reorderQuantity && quantity <= maxQuantity
        }


        def overStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def maxQuantity = inventoryLevel?.maxQuantity
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

        log.debug "Get low stock: " + (System.currentTimeMillis() - startTime) + " ms"

        [lowStock               : lowStock.keySet().size(),
         lowStockCost           : getTotalCost(lowStock),
         reorderStock           : reorderStock.keySet().size(),
         reorderStockCost       : getTotalCost(reorderStock),
         overStock              : overStock.keySet().size(),
         overStockCost          : getTotalCost(overStock),
         totalStock             : totalStock.keySet().size(),
         totalStockCost         : getTotalCost(totalStock),
         reconditionedStock     : reconditionedStock.keySet().size(),
         reconditionedStockCost : getTotalCost(reconditionedStock),
         healthyStock           : healthyStock.keySet().size(),
         healthyStockCost       : getTotalCost(healthyStock),
         outOfStock             : outOfStock.keySet().size(),
         outOfStockCost         : getTotalCost(outOfStock),
         outOfStockClassA       : outOfStockClassA.keySet().size(),
         outOfStockCostClassA   : getTotalCost(outOfStockClassA),
         outOfStockClassB       : outOfStockClassB.keySet().size(),
         outOfStockCostClassB   : getTotalCost(outOfStockClassB),
         outOfStockClassC       : outOfStockClassC.keySet().size(),
         outOfStockCostClassC   : getTotalCost(outOfStockClassC),
         outOfStockClassNone    : outOfStockClassNone.keySet().size(),
         outOfStockCostClassNone: getTotalCost(outOfStockClassNone),

         onHandQuantityZero     : onHandQuantityZero.keySet().size(),
         onHandQuantityZeroCost : getTotalCost(onHandQuantityZero),
         inStock                : inStock.keySet().size(),
         inStockCost            : getTotalCost(inStock),
        ]
    }


    def getTotalCost(quantityMap) {
        def totalCost = 0
        quantityMap.each { k, v ->
            totalCost += k.pricePerUnit ?: 0 * v ?: 0
        }
        return totalCost
    }

    def getInventoryStatus(Location location) {
        def quantityMap = productAvailabilityService.getCurrentInventory(location)
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy {
            it.product
        }
        def inventoryStatusMap = [:]
        quantityMap.each { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            if (inventoryLevel) {
                inventoryStatusMap[product] = inventoryLevel?.statusMessage(quantity) ?: "${inventoryLevel?.id}"
            } else {
                inventoryStatusMap[product] = quantity > 0 ? "IN_STOCK" : "STOCK_OUT"
            }

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
        def quantityMap = productAvailabilityService.getCurrentInventory(location)
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy {
            it.product
        }
        def inventoryStatusMap = [:]
        quantityMap.each { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def status = inventoryLevel?.statusMessage(quantity) ?: "NONE"
            inventoryStatusMap[product] = [inventoryLevel: inventoryLevel, status: status, onHandQuantity: quantity]
        }
        return inventoryStatusMap
    }

    def getQuantityOnHandZero(Location location, List<Category> categories) {
        def inventoryItems = getInventoryItems(location, categories)

        def stockOut = inventoryItems.findAll { it.quantity <= 0 }
        return stockOut

    }

    def getOutOfStock(Location location, String abcClass, List<Category> categories) {
        def inventoryItems = getInventoryItems(location, categories)

        def stockOut = inventoryItems.findAll { inventoryItem ->
            def inventoryLevel = inventoryItem.inventoryLevel
            def quantity = inventoryItem.quantity

            if (abcClass)
                inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0 && (abcClass == inventoryLevel.abcClass)
            else
                inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0
        }
        return stockOut
    }

    def getLowStock(Location location, List<Category> categories) {
        def inventoryItems = getInventoryItems(location, categories)

        def lowStock = inventoryItems.findAll { inventoryItem ->
            def quantity = inventoryItem.quantity
            def inventoryLevel = inventoryItem.inventoryLevel
            def minQuantity = inventoryLevel?.minQuantity

            inventoryLevel?.status >= InventoryStatus.SUPPORTED && minQuantity && quantity <= minQuantity
        }
        return lowStock
    }

    def getReorderStock(Location location, List<Category> categories) {
        def inventoryItems = getInventoryItems(location, categories)

        def reorderStock = inventoryItems.findAll { inventoryItem ->
            def quantity = inventoryItem.quantity
            def inventoryLevel = inventoryItem.inventoryLevel
            def reorderQuantity = inventoryLevel?.reorderQuantity

            inventoryLevel?.status >= InventoryStatus.SUPPORTED && reorderQuantity && quantity <= reorderQuantity
        }
        return reorderStock
    }

    List<ReorderReportItemDto> getReorderReport(ReorderReportFilterCommand command) {
        // Find inventory levels that don't have bin location set and have AT LEAST one of min/max qty set
        List<InventoryLevel> inventoryLevels = InventoryLevel.createCriteria().list {
            eq("inventory", AuthService.currentLocation.inventory)
            isNull("internalLocation")
            or {
                and {
                    isNotNull("minQuantity")
                    gt("minQuantity", 0)
                }
                and {
                    isNotNull("reorderQuantity")
                    gt("reorderQuantity", 0)
                }
            }
        }
        List<InventoryByProduct> inventoriesByProduct = productAvailabilityService.getInventoriesByProduct(command.additionalLocations,
                command.categories,
                command.tags,
                command.expiration)

        // Build a map of inventory levels keyed on product to have O(1) read access in the .findResults below instead of O(n) if we were to do .find on List
        Map<Product, InventoryLevel> inventoryLevelByProduct = inventoryLevels.collectEntries { [it.product, it] }

        // .findResults is a shortened way of doing .findAll{...}.collect{...}
        List<ReorderReportItemDto> reorderReportItems = inventoriesByProduct.findResults { InventoryByProduct item ->
            InventoryLevel inventoryLevel = inventoryLevelByProduct[item.product]

            // Depending on the provided inventory level status filter value, we have a different condition for filtering out the items
            Map<InventoryLevelStatus, Closure<Boolean>> inventoryLevelStatusFilterCondition = [
                    (InventoryLevelStatus.IN_STOCK): { item.quantityAvailableToPromise > 0 },
                    (InventoryLevelStatus.ABOVE_MAXIMUM): { inventoryLevel?.maxQuantity && item.quantityAvailableToPromise > inventoryLevel?.maxQuantity },
                    (InventoryLevelStatus.BELOW_REORDER): { inventoryLevel?.reorderQuantity && item.quantityAvailableToPromise <= inventoryLevel?.reorderQuantity },
                    (InventoryLevelStatus.BELOW_MINIMUM): { inventoryLevel?.minQuantity && item.quantityAvailableToPromise <= inventoryLevel?.minQuantity }
            ]

            // If an item satisfies the predicate, call the buildReorderReportItem, otherwise return null so that .findResults filters out the record in the end
            return inventoryLevelStatusFilterCondition[command.inventoryLevelStatus](item) ? buildReorderReportItem(item, inventoryLevel) : null
        }

        return reorderReportItems
    }

    private ReorderReportItemDto buildReorderReportItem(InventoryByProduct item, InventoryLevel inventoryLevel) {
        Map<String, Number> monthlyDemand = forecastingService.getDemand(AuthService.currentLocation, null, item.product)
        Integer quantityToOrder = inventoryLevel?.maxQuantity != null ? inventoryLevel.maxQuantity - item.quantityAvailableToPromise : null
        Boolean hasRoleFinance = userService.hasRoleFinance(AuthService.currentUser)
        BigDecimal unitCost = hasRoleFinance ? item.product.costPerUnit : null
        BigDecimal expectedReorderCost = hasRoleFinance && quantityToOrder && item.product.costPerUnit != null
                ? quantityToOrder * unitCost
                : null
        Closure determineInventoryStatus = { InventoryLevel inventoryLevel1, Integer quantityOnHand ->
            if (inventoryLevel1) {
                return inventoryLevel1.statusMessage(quantityOnHand)
            }
            return quantityOnHand > 0 ? "IN_STOCK" : "STOCK_OUT"
        }
        return new ReorderReportItemDto(
                inventoryStatus: determineInventoryStatus(inventoryLevel, item.quantityOnHand),
                product: item.product,
                tags: item.product.tags,
                inventoryLevel: inventoryLevel,
                monthlyDemand: monthlyDemand?.monthlyDemand ?: 0,
                quantityAvailableToPromise: item.quantityAvailableToPromise,
                quantityToOrder: quantityToOrder,
                unitCost: unitCost,
                expectedReorderCost: expectedReorderCost
        )
    }

    String getReorderReportCsv(List<ReorderReportItemDto> reorderReport) {
        StringWriter sw = new StringWriter()
        Boolean hasRoleFinance = userService.hasRoleFinance(AuthService.currentUser)
        CSVWriter csv = new CSVWriter(sw, {
            "${messageLocalizer.localize("inventoryLevel.status.label")}" { it?.status }
            "${messageLocalizer.localize("product.productCode.label")}" { it?.productCode }
            "${messageLocalizer.localize("product.label")}" { it?.product }
            "${messageLocalizer.localize("category.label")}" { it?.category }
            "${messageLocalizer.localize("product.tags.label", "Tags")}" { it?.tags }
            "${messageLocalizer.localize("product.unitOfMeasure.label", "Unit of measure")}" { it?.unitOfMeasure }
            "${messageLocalizer.localize("product.vendor.label", "Vendor")}" { it?.vendor }
            "${messageLocalizer.localize("product.vendorCode.label", "Vendor code")}" { it?.vendorCode }
            "${messageLocalizer.localize("inventoryLevel.minQuantity.label", "Min quantity")}" { it?.minQuantity }
            "${messageLocalizer.localize("inventoryLevel.reorderQuantity.label", "Reorder quantity")}" { it?.reorderQuantity }
            "${messageLocalizer.localize("inventoryLevel.maxQuantity.label", "Max quantity")}" { it?.maxQuantity }
            "${messageLocalizer.localize("report.averageMonthlyDemand.label", "Average Monthly Demand")}" { it?.averageMonthlyDemand }
            "${messageLocalizer.localize("product.quantityAvailableToPromise.label", "Quantity Available")}" { it?.quantityAvailableToPromise }
            "${messageLocalizer.localize("inventory.quantityToOrder.message", "Quantity to Order")}" { it?.quantityToOrder }

            if (hasRoleFinance) {
                "${messageLocalizer.localize("product.unitCost.label", "Unit Cost")}" { it?.unitCost }
                "${messageLocalizer.localize("inventory.expectedReorderCost.label", "Expected Reorder Cost")}" { it?.expectedReorderCost }
            }
        })
        Closure getQuantityToOrderDisplayValue = { Integer maxQuantity, Integer quantityToOrder ->
            return maxQuantity == null ? messageLocalizer.localize("reorderReport.noMaxQtySet.label") : quantityToOrder
        }
        reorderReport.each { ReorderReportItemDto item ->
            csv << [
                    status: "${messageLocalizer.localize("enum.InventoryLevelStatusCsv." + item.inventoryStatus)}",
                    productCode: item.product.productCode,
                    product: item.product.displayNameOrDefaultName,
                    category: item.product.category?.name ?: "",
                    tags: item.product.tagsToString(),
                    unitOfMeasure: item.product.unitOfMeasure ?: "",
                    vendor: item.product.vendor ?: "",
                    vendorCode: item.product.vendorCode ?: "",
                    minQuantity: item.inventoryLevel?.minQuantity ?: "",
                    reorderQuantity: item.inventoryLevel?.reorderQuantity ?: "",
                    maxQuantity: item.inventoryLevel?.maxQuantity ?: "",
                    averageMonthlyDemand: item.monthlyDemand ?: 0,
                    quantityAvailableToPromise: item.quantityAvailableToPromise,
                    quantityToOrder: getQuantityToOrderDisplayValue(item.inventoryLevel?.maxQuantity, item.quantityToOrder) ?: "",
                    unitCost: item.unitCost ?: "",
                    expectedReorderCost: item.expectedReorderCost ?: "",
            ]
        }
        return CSVUtils.prependBomToCsvString(csv.writer.toString())
    }

    def getReorderReport(Location location) {
        long startTime = System.currentTimeMillis()
        ArrayList<Map> inventoryItems = getInventoryItems(location)

        ArrayList<Map> reorderStock = inventoryItems.findAll { Map inventoryItem ->
            def inventoryLevel = inventoryItem?.inventoryLevel as InventoryLevel

            Integer minQuantity = inventoryLevel?.minQuantity
            Integer reorderQuantity = inventoryLevel?.reorderQuantity

            // If an inventoryLevel does have a bin location,
            // or minQuantity and reorderQuantity not set, the item should not appear in the report
            if (inventoryLevel?.internalLocation || (!minQuantity && !reorderQuantity)) {
                return false
            }

            def quantityATP = inventoryItem.quantityAvailableToPromise as Number

            def quantityToOrder = null
            def expectedReorderCost = null
            def unitCost =  inventoryLevel?.product?.costPerUnit

            if (inventoryLevel?.maxQuantity != null) {
                quantityToOrder = inventoryLevel.maxQuantity - quantityATP

                if(unitCost != null) {
                    expectedReorderCost = quantityToOrder * unitCost
                }
            }
            inventoryItem.put("quantityToOrder", quantityToOrder);
            inventoryItem.put("unitCost", unitCost);
            inventoryItem.put("expectedReorderCost", expectedReorderCost);

            // If minQuantity not null and qatp < minQty OR reorderQty not null and qatp < reorderAtp, the item SHOULD appear in the report
            if ((minQuantity && quantityATP < minQuantity) || (reorderQuantity && quantityATP < reorderQuantity)) {
                return true
            }
            return false
        }
        log.info "Get simple reorder stock: " + (System.currentTimeMillis() - startTime) + " ms"
        return reorderStock
    }

    def getOverStock(Location location, List<Category> categories) {
        def inventoryItems = getInventoryItems(location, categories)

        def overStock = inventoryItems.findAll { inventoryItem ->
            def quantity = inventoryItem.quantity
            def inventoryLevel = inventoryItem.inventoryLevel
            def maxQuantity = inventoryLevel?.maxQuantity

            inventoryLevel?.status >= InventoryStatus.SUPPORTED && maxQuantity && quantity > maxQuantity
        }
        return overStock
    }

    def getHealthyStock(Location location, List<Category> categories) {
        def inventoryItems = getInventoryItems(location, categories)

        def healthyStock = inventoryItems.findAll { inventoryItem ->
            def quantity = inventoryItem.quantity
            def inventoryLevel = inventoryItem.inventoryLevel
            def reorderQuantity = inventoryLevel?.reorderQuantity
            def maxQuantity = inventoryLevel?.maxQuantity

            inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity > reorderQuantity && quantity <= maxQuantity
        }
        return healthyStock
    }
}
