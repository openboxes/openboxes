/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.report


import org.apache.http.client.HttpClient
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.DefaultHttpClient
import org.docx4j.org.xhtmlrenderer.pdf.ITextRenderer
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.product.Product
import org.pih.warehouse.reporting.DateDimension
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.w3c.dom.Document
import org.xml.sax.InputSource
import util.InventoryUtil

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import java.text.NumberFormat

class ReportService implements ApplicationContextAware {

    def dataService
    def inventoryService
    def dashboardService
    def grailsApplication
    def userService

    ApplicationContext applicationContext

    boolean transactional = false

    void generateShippingReport(ChecklistReportCommand command) {
        def shipmentItems = command?.shipment?.shipmentItems?.sort()
        shipmentItems.each { shipmentItem ->
            command.checklistReportEntryList << new ChecklistReportEntryCommand(shipmentItem: shipmentItem)
        }
    }

    void generateProductReport(ProductReportCommand command) {

        command.inventoryItems = InventoryItem.findAllByProduct(command?.product)
        command.quantityInitial = inventoryService.getInitialQuantity(command?.product, command?.location, command?.startDate)

        def transactionEntries = inventoryService.getTransactionEntries(command?.product, command?.location, command?.startDate, command?.endDate)

        // Calculate quantity at each transaction entry point.
        def quantity = command?.quantityInitial
        transactionEntries.each { transactionEntry ->
            def productReportEntry = new ProductReportEntryCommand(transactionEntry: transactionEntry, balance: 0)
            productReportEntry.balance = inventoryService.adjustQuantity(quantity, transactionEntry)
            command.productReportEntryList << productReportEntry

            // Need to keep track of the running total so we can adjust the balance as we go
            quantity = productReportEntry.balance
        }
        command.quantityFinal = inventoryService.getCurrentQuantity(command?.product, command?.location, command?.endDate)
    }

    TransactionEntry getEarliestTransactionEntry(Product product, Inventory inventory) {
        def list = TransactionEntry.createCriteria().list() {
            and {
                inventoryItem {
                    eq("product.id", product?.id)
                }
                transaction {
                    eq("inventory", inventory)
                    order("transactionDate", "asc")
                    order("dateCreated", "asc")
                }
            }
            maxResults(1)
        }

        return list[0]
    }

    /**
     *
     * @param command
     */
    void generateTransactionReport(InventoryReportCommand command) {

        // Ensure that the includeChildren flag is disabled
        command?.includeChildren = false

        def products =
                (command?.includeChildren) ? inventoryService.getProductsByNestedCategory(command.category) :
                        inventoryService.getProductsByCategory(command.category)

        if (command?.showEntireHistory) {
            def earliestDate = getEarliestTransactionEntry(command?.product, command?.location?.inventory)?.transaction?.transactionDate
            command.startDate = earliestDate ?: command.startDate
            command.endDate = new Date() + 1
        }

        // TODO Need to restrict by date and category
        def transactionEntries = inventoryService.getTransactionEntries(command.location, command.category, command?.startDate, command?.endDate)
        def transactionEntriesByProduct = transactionEntries.groupBy { it?.inventoryItem?.product }


        log.info "Products (" + products.size() + ") -> " + products
        // Initialize the report map to reference all products to be displayed
        products.each { product ->

            def productTransactionEntries = transactionEntriesByProduct[product]
            def includeProduct = (command?.hideInactiveProducts && productTransactionEntries || !command?.hideInactiveProducts)

            if (includeProduct) {
                def productEntry = command.entries[product]
                if (!productEntry) {
                    productEntry = new InventoryReportEntryCommand(product: product)
                    command.entries[product] = productEntry
                }
                productEntry.quantityInitial = inventoryService.getInitialQuantity(product, command?.location, command?.startDate ?: null)
                productEntry.quantityFinal = inventoryService.getCurrentQuantity(product, command?.location, command?.endDate ?: new Date())

                // Initialize the product map to reference all inventory items for that product
                def inventoryItems = inventoryService.getInventoryItemsByProduct(product)
                inventoryItems?.each { inventoryItem ->
                    def inventoryItemEntry = productEntry?.entries[inventoryItem]
                    if (!inventoryItemEntry) {
                        inventoryItemEntry = new InventoryReportEntryCommand(product: product, inventoryItem: inventoryItem)
                        productEntry.entries[inventoryItem] = inventoryItemEntry
                    }
                    inventoryItemEntry.quantityInitial = inventoryService.getQuantity(inventoryItem, command.location, command.startDate ?: null)
                    inventoryItemEntry.quantityFinal = inventoryService.getQuantity(inventoryItem, command.location, command.endDate ?: new Date())
                    inventoryItemEntry.quantityRunning = inventoryItemEntry.quantityInitial
                }
            }
        }


        log.info "transactionEntries (" + transactionEntries.size() + ") -> " + transactionEntries
        // Iterate over the transaction entries for the given time period to tabulate totals.
        // Each time we encounter an INVENTORY, compare that quantity with the running total,
        // and add / subract to "adjustment" as appropriate.  Then set the running
        // total to the new inventory and continue with the running total...
        transactionEntries.each {
            def inventoryItem = it?.inventoryItem
            def transactionType = it?.transaction?.transactionType

            log.debug "transactionEntry -> " + it.transaction.transactionType.name + " = " + it.quantity

            def productEntry = command.entries[inventoryItem.product]
            if (productEntry) {
                def inventoryItemEntry = productEntry.entries[inventoryItem]


                if (inventoryItemEntry) {

                    if (transactionType?.id == Constants.CONSUMPTION_TRANSACTION_TYPE_ID) {
                        inventoryItemEntry.quantityRunning += it.quantity
                        inventoryItemEntry.quantityConsumed += it.quantity
                        inventoryItemEntry.quantityTotalOut += it.quantity
                    } else if (transactionType?.id == Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID) {
                        inventoryItemEntry.quantityRunning += it.quantity
                        inventoryItemEntry.quantityFound += it.quantity
                        inventoryItemEntry.quantityAdjusted += it.quantity
                        inventoryItemEntry.quantityTotalIn += it.quantity
                    } else if (transactionType?.id == Constants.EXPIRATION_TRANSACTION_TYPE_ID) {
                        inventoryItemEntry.quantityRunning -= it.quantity
                        inventoryItemEntry.quantityExpired += it.quantity
                        inventoryItemEntry.quantityTotalOut += it.quantity
                    } else if (transactionType?.id == Constants.DAMAGE_TRANSACTION_TYPE_ID) {
                        inventoryItemEntry.quantityRunning -= it.quantity
                        inventoryItemEntry.quantityDamaged += it.quantity
                        inventoryItemEntry.quantityTotalOut += it.quantity
                    } else if (transactionType?.id == Constants.TRANSFER_IN_TRANSACTION_TYPE_ID) {
                        inventoryItemEntry.quantityRunning += it.quantity
                        inventoryItemEntry.quantityTransferredIn += it.quantity
                        inventoryItemEntry.quantityTotalIn += it.quantity
                        if (!inventoryItemEntry.quantityTransferredInByLocation[it.transaction.source]) {
                            inventoryItemEntry.quantityTransferredInByLocation[it.transaction.source] = 0
                        }
                        inventoryItemEntry.quantityTransferredInByLocation[it.transaction.source] += it.quantity
                    } else if (transactionType?.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
                        inventoryItemEntry.quantityRunning -= it.quantity
                        inventoryItemEntry.quantityTransferredOut += it.quantity
                        inventoryItemEntry.quantityTotalOut += it.quantity
                        if (!inventoryItemEntry.quantityTransferredOutByLocation[it.transaction.destination]) {
                            inventoryItemEntry.quantityTransferredOutByLocation[it.transaction.destination] = 0
                        }
                        inventoryItemEntry.quantityTransferredOutByLocation[it.transaction.destination] += it.quantity
                    } else if (transactionType?.id == Constants.ADJUSTMENT_DEBIT_TRANSACTION_TYPE_ID) {
                        inventoryItemEntry.quantityRunning -= it.quantity
                        inventoryItemEntry.quantityLost += it.quantity
                        inventoryItemEntry.quantityAdjusted -= it.quantity
                        inventoryItemEntry.quantityTotalOut += it.quantity
                    } else if (transactionType?.id == Constants.INVENTORY_TRANSACTION_TYPE_ID) {
                        def diff = it.quantity - inventoryItemEntry.quantityRunning
                        inventoryItemEntry.quantityAdjusted += diff
                        inventoryItemEntry.quantityRunning = it.quantity
                        if (diff > 0) {
                            inventoryItemEntry.quantityFound += diff
                            inventoryItemEntry.quantityTotalIn += diff
                        } else {
                            inventoryItemEntry.quantityLost += diff
                            inventoryItemEntry.quantityTotalOut += diff
                        }
                    } else if (transactionType?.id == Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID) {
                        def diff = it.quantity - inventoryItemEntry.quantityRunning
                        inventoryItemEntry.quantityAdjusted += diff
                        inventoryItemEntry.quantityRunning = it.quantity
                        if (diff > 0) {
                            inventoryItemEntry.quantityFound += diff
                            inventoryItemEntry.quantityTotalIn += diff
                        } else {
                            inventoryItemEntry.quantityLost += diff
                            inventoryItemEntry.quantityTotalOut += diff
                        }
                    }

                    // Add transaction entry
                    def balance = inventoryItemEntry.quantityRunning
                    inventoryItemEntry.transactionEntries << new ProductReportEntryCommand(transactionEntry: it, balance: balance)

                }
            }
        }
    }

    void generatePdf(String url, OutputStream outputStream) {
        def html = ""
        log.info "Generate PDF for URL " + url
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance()
            builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            DocumentBuilder builder = builderFactory.newDocumentBuilder()

            html = getHtmlContent(url)

            ITextRenderer renderer = new ITextRenderer()
            renderer.setDocumentFromString(html)

            renderer.layout()
            renderer.createPDF(outputStream)

            outputStream.close()
            outputStream = null

        } catch (Exception e) {
            log.error("Cannot generate pdf due to error: " + e.message, e)
            log.error "Error caused by: " + html

        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private getHtmlContent(String url) {

        HttpClient httpclient = new DefaultHttpClient()
        try {
            HttpGet httpget = new HttpGet(url)
            // Create a response handler
            ResponseHandler<String> responseHandler = new BasicResponseHandler()
            String responseBody = httpclient.execute(httpget, responseHandler)
            println responseBody
            return responseBody


        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown()
        }
    }

    def calculateQuantityOnHandByProductGroup(locationId) {
        def items = []
        def startTime = System.currentTimeMillis()
        def location = Location.get(locationId)

        def quantityMap = dashboardService.getInventoryStatusAndLevel(location)
        def hasRoleFinance = userService.hasRoleFinance()

        quantityMap.each { Product product, Map map ->

            def status = map.status
            def onHandQuantity = map.onHandQuantity
            def inventoryLevel = map.inventoryLevel
            def unitPrice = hasRoleFinance ? product?.pricePerUnit : null
            def totalValue = hasRoleFinance ? ((product.pricePerUnit ?: 0) * (onHandQuantity ?: 0)) : null

            def imageUrl = (product.thumbnail) ? '/openboxes/product/renderImage/${product?.thumbnail?.id}' : ''

            items << [
                    id              : product.id,
                    name            : product.name,
                    status          : status,
                    productCode     : product.productCode,
                    genericProductId: product?.genericProduct?.id,
                    genericProduct  : product?.genericProduct?.name ?: product.name,
                    hasProductGroup : (product?.genericProduct?.id != null),
                    unitOfMeasure   : product.unitOfMeasure,
                    imageUrl        : imageUrl,
                    inventoryLevel  : inventoryLevel,
                    minQuantity     : inventoryLevel?.minQuantity ?: 0,
                    maxQuantity     : inventoryLevel?.maxQuantity ?: 0,
                    reorderQuantity : inventoryLevel?.reorderQuantity ?: 0,
                    unitPrice       : unitPrice ?: 0,
                    onHandQuantity  : onHandQuantity,
                    totalValue      : totalValue ?: 0
            ]
        }

        // Group all items by status
        def statusSummary = items.inject([:].withDefault { [count: 0, items: []] }) { map, item ->
            map[item.status].count++
            map
        }

        // Group entries by product group

        // Removed products:[]
        def productGroupMap = items.inject([:].withDefault {
            [id            : null, name: null, status: null, productCodes: [], unitPrice: 0, totalValue: 0, numProducts: 0, numInventoryLevels: 0,
             onHandQuantity: 0, minQuantity: 0, maxQuantity: 0, reorderQuantity: 0, inventoryStatus: null, hasInventoryLevel: false, hasProductGroup: false, inventoryLevelId: null]
        }) { map, item ->
            map[item.genericProduct].id = item.genericProductId
            map[item.genericProduct].name = item.genericProduct
            map[item.genericProduct].hasProductGroup = item.hasProductGroup
            map[item.genericProduct].numProducts++
            map[item.genericProduct].onHandQuantity += item.onHandQuantity
            map[item.genericProduct].productCodes << item.productCode
            map[item.genericProduct].totalValue += item.totalValue
            map[item.genericProduct].unitPrice += item.unitPrice

            if (item.inventoryLevel) {
                map[item.genericProduct].numInventoryLevels++
                map[item.genericProduct].hasInventoryLevel = true

                // Make sure we're using the latest version of the inventory level (the one where values are not set to 0)
                def currentInventoryLevel = map[item.genericProduct].inventotryLevel
                if (!currentInventoryLevel) {
                    map[item.genericProduct].inventoryLevelId = item?.inventoryLevel?.id
                    map[item.genericProduct].inventoryLevel = item.inventoryLevel
                    map[item.genericProduct].inventoryStatus = item?.inventoryLevel?.status?.name()
                    map[item.genericProduct].minQuantity = item.minQuantity
                    map[item.genericProduct].reorderQuantity = item.reorderQuantity
                    map[item.genericProduct].maxQuantity = item.maxQuantity
                }
            }

            map
        }


        NumberFormat numberFormat = NumberFormat.getNumberInstance()
        String currencyCode = grailsApplication.config.openboxes.locale.defaultCurrencyCode ?: "USD"
        numberFormat.currency = Currency.getInstance(currencyCode)
        numberFormat.maximumFractionDigits = 2
        numberFormat.minimumFractionDigits = 2

        // Set status for all rows
        productGroupMap.each { k, v ->
            v.status = InventoryUtil.getStatusMessage(v?.inventoryLevel?.status, v?.inventoryLevel?.minQuantity ?: 0, v?.inventoryLevel?.reorderQuantity ?: 0, v?.inventoryLevel?.maxQuantity ?: 0, v?.onHandQuantity ?: 0)
            v.unitPriceFormatted = numberFormat.format(v.unitPrice)
            v.totalValueFormatted = numberFormat.format(v.totalValue)
        }


        //def noInventoryLevels = productGroupMap.findAll { k,v -> !v.inventoryLevel }
        def hasInventoryLevel = productGroupMap.findAll { k, v -> v.hasInventoryLevel }
        def hasNoInventoryLevel = productGroupMap.findAll { k, v -> !v.hasInventoryLevel }
        def zeroInventoryLevels = productGroupMap.findAll { k, v -> v.numInventoryLevels == 0 }
        def multipleInventoryLevels = productGroupMap.findAll { k, v -> v.numInventoryLevels > 1 }
        def singleInventoryLevel = productGroupMap.findAll { k, v -> v.numInventoryLevels == 1 }

        def hasInventoryLevelCount = hasInventoryLevel.size()
        def hasNoInventoryLevelCount = hasNoInventoryLevel.size()

        def zeroInventoryLevelsCount = zeroInventoryLevels.size()
        def multipleInventoryLevelsCount = multipleInventoryLevels.size()
        def singleInventoryLevelCount = singleInventoryLevel.size()
        def inventoryLevelsCount = zeroInventoryLevelsCount + multipleInventoryLevelsCount + singleInventoryLevelCount

        // Process all product groups
        def notStocked = productGroupMap.findAll { k, v -> v.onHandQuantity <= 0 && !v.hasInventoryLevel }
        def outOfStock = productGroupMap.findAll { k, v -> v.onHandQuantity <= 0 && v.hasInventoryLevel }
        def lowStock = productGroupMap.findAll { k, v -> v.onHandQuantity > 0 && v.onHandQuantity <= v.minQuantity && v.minQuantity > 0 }
        //&& v.minQuantity > 0
        def reorderStock = productGroupMap.findAll { k, v -> v.onHandQuantity > v.minQuantity && v.onHandQuantity <= v.reorderQuantity && v.reorderQuantity > 0 }
//v.reorderQuantity > 0
        def inStock = productGroupMap.findAll { k, v -> v.onHandQuantity > 0 && v.reorderQuantity == 0 && v.maxQuantity == 0 && v.minQuantity == 0 }
        def idealStock = productGroupMap.findAll { k, v -> v.onHandQuantity > v.reorderQuantity && v.onHandQuantity <= v.maxQuantity }
//&& v.maxQuantity > 0
        def overStock = productGroupMap.findAll { k, v -> v.onHandQuantity > v.maxQuantity && v.maxQuantity > 0 }
        //v.maxQuantity > 0

        println "Not stocked: " + notStocked.size()
        println "Out of stock: " + outOfStock.size()

        // Get product group sizes
        def notStockedCount = notStocked.size()
        def outOfStockCount = outOfStock.size()
        def lowStockCount = lowStock.size()
        def reorderStockCount = reorderStock.size()
        def inStockCount = inStock.size()
        def idealStockCount = idealStock.size()
        def overStockCount = overStock.size()

        def all = productGroupMap
        def accounted = notStocked + outOfStock + lowStock + reorderStock + idealStock + overStock + inStock
        def invalid = all - accounted
        def invalidCount = invalid.size()

        def totalCountActual = outOfStockCount + lowStockCount + reorderStockCount + idealStockCount + inStockCount + overStockCount + notStockedCount + invalidCount
        def totalCountFromSummary = statusSummary.values()*.count.sum()


        def elapsedTime = (System.currentTimeMillis() - startTime) / 1000

        return [
                responseTime         : elapsedTime + "s",
                productSummary       : [
                        statusSummary: statusSummary,
                        totalCount   : totalCountFromSummary
                ],
                inventoryLevelSummary: [
                        totalCount             : inventoryLevelsCount,
                        hasInventoryLevel      : hasInventoryLevelCount,
                        hasNoInventoryLevel    : hasNoInventoryLevelCount,
                        zeroInventoryLevels    : zeroInventoryLevelsCount,
                        singleInventoryLevel   : singleInventoryLevelCount,
                        multipleInventoryLevels: multipleInventoryLevelsCount
                ],
                productGroupSummary  : [
                        totalCountProductsExpected     : productGroupMap.values().sum {
                            it.numProducts
                        },
                        totalCountProductGroupsExpected: productGroupMap.keySet().size(),
                        totalCountProductGroupsActual  : totalCountActual,
                        "NOT_STOCKED"                  : [numProductGroups: notStockedCount, percentage: notStockedCount / totalCountActual, numProducts: notStocked.values().sum {
                            it.numProducts
                        }],
                        "STOCK_OUT"                    : [numProductGroups: outOfStockCount, percentage: outOfStockCount / totalCountActual, numProducts: outOfStock.values().sum {
                            it.numProducts
                        }],
                        "LOW_STOCK"                    : [numProductGroups: lowStockCount, percentage: lowStockCount / totalCountActual, numProducts: lowStock.values().sum {
                            it.numProducts
                        }],
                        "REORDER"                      : [numProductGroups: reorderStockCount, percentage: reorderStockCount / totalCountActual, numProducts: reorderStock.values().sum {
                            it.numProducts
                        }],
                        "IN_STOCK"                     : [numProductGroups: inStockCount, percentage: inStockCount / totalCountActual, numProducts: inStock.values().sum {
                            it.numProducts
                        }],
                        "IDEAL_STOCK"                  : [numProductGroups: idealStockCount, percentage: idealStockCount / totalCountActual, numProducts: idealStock.values().sum {
                            it.numProducts
                        }],
                        "OVERSTOCK"                    : [numProductGroups: overStockCount, percentage: overStockCount / totalCountActual, numProducts: overStock.values().sum {
                            it.numProducts
                        }],
                        "INVALID"                      : [numProductGroups: invalidCount, percentage: invalidCount / totalCountActual, numProducts: invalid.values().sum {
                            it.numProducts
                        }]
                ],
                productGroupDetails  : [
                        "ALL"        : productGroupMap,
                        "NOT_STOCKED": notStocked,
                        "STOCK_OUT"  : outOfStock,
                        "LOW_STOCK"  : lowStock,
                        "REORDER"    : reorderStock,
                        "IN_STOCK"   : inStock,
                        "IDEAL_STOCK": idealStock,
                        "OVERSTOCK"  : overStock,
                        "INVALID"    : invalid
                ]
        ]
    }


    void buildDimensions() {
        truncateFacts()
        truncateDimensions()
        buildDateDimension()
        buildProductDimension()
        buildLocationDimension()
        buildTransactionTypeDimension()
        buildLotDimension()
    }

    void buildFacts() {
        truncateFacts()
        buildTransactionFact()
        buildConsumptionFact()
    }

    def truncateFacts() {
        dataService.executeStatements(["SET FOREIGN_KEY_CHECKS = 0",
                                       "delete from transaction_fact",
                                       "alter table transaction_fact AUTO_INCREMENT = 1",
                                       "delete from consumption_fact",
                                       "alter table consumption_fact AUTO_INCREMENT = 1",
                                       "SET FOREIGN_KEY_CHECKS = 1"])
    }

    def truncateDimensions() {
        dataService.executeStatements([
                "SET FOREIGN_KEY_CHECKS = 0",
                "delete from date_dimension",
                "alter table date_dimension AUTO_INCREMENT = 1",
                "delete from location_dimension",
                "alter table location_dimension AUTO_INCREMENT = 1",
                "delete from lot_dimension",
                "alter table lot_dimension AUTO_INCREMENT = 1",
                "delete from product_dimension",
                "alter table product_dimension AUTO_INCREMENT = 1",
                "delete from transaction_type_dimension",
                "alter table transaction_type_dimension AUTO_INCREMENT = 1",
                "SET FOREIGN_KEY_CHECKS = 1"])
    }




    void buildTransactionTypeDimension() {
        String insertStatement = """
            INSERT into transaction_type_dimension (version, transaction_code, transaction_type_name, transaction_type_id)
            SELECT 0, transaction_type.transaction_code, substring_index(transaction_type.name, '|', 1), transaction_type.id
            FROM transaction_type
        """
        dataService.executeStatements([insertStatement])
    }

    void buildLotDimension() {
        String insertStatement = """
            INSERT INTO lot_dimension (version, product_code, lot_number, expiration_date, inventory_item_id)
            SELECT 0, product.product_code, inventory_item.lot_number, inventory_item.expiration_date, inventory_item.id
            FROM inventory_item
            JOIN product ON product.id = inventory_item.product_id;
        """
        dataService.executeStatements([insertStatement])
    }

    void buildProductDimension() {
        String insertStatement = """
            INSERT INTO product_dimension (version, product_id, active, product_code, product_name, generic_product, category_name, abc_class, unit_cost, unit_price)
            SELECT 0, product.id, product.active, product.product_code, product.name, NULL, category.name, product.abc_class, product.cost_per_unit, product.price_per_unit
            FROM product
            JOIN category ON category.id = product.category_id
        """
        dataService.executeStatements([insertStatement])
    }

    void buildLocationDimension() {
        String insertStatement = """
            INSERT INTO location_dimension (version, location_name, location_number, location_type_code, location_type_name, location_group_name, parent_location_name, location_id)
            SELECT 0, location.name, location.location_number, location_type.location_type_code, location_type.name, location_group.name, parent_location.name, location.id
            FROM location
            JOIN location_type ON location_type.id = location.location_type_id
            LEFT JOIN location_group ON location_group.id = location.location_group_id
            LEFT JOIN location parent_location ON parent_location.id = location.parent_location_id;        """
        dataService.executeStatements([insertStatement])
    }

    void buildDateDimension() {
        def minTransactionDate = Transaction.minTransactionDate.list()
        log.info("minTransactionDate: " + minTransactionDate)
        Date today = new Date()
        (minTransactionDate..today).each { Date date ->
            date.clearTime()
            DateDimension dateDimension = new DateDimension()
            dateDimension.date = date
            dateDimension.dayOfMonth = date[Calendar.DAY_OF_MONTH]
            dateDimension.dayOfWeek = date[Calendar.DAY_OF_WEEK]
            dateDimension.month = date[Calendar.MONTH] + 1
            dateDimension.year = date[Calendar.YEAR]
            dateDimension.week = date[Calendar.WEEK_OF_YEAR]
            dateDimension.monthName = date.format("MMMMM")
            dateDimension.monthYear = date.format("MM-yyyy")
            dateDimension.weekdayName = date.format("EEEEE")
            dateDimension.save(flush: true)
        }
    }

    def buildTransactionFact() {
        String insertStatement = """
            insert into transaction_fact (version, 
                transaction_number, 
                product_key_id, 
                lot_key_id, 
                location_key_id, 
                transaction_date_key_id, 
                transaction_type_key_id,
                transaction_date, 
                quantity)
            select  
                0, 
                transaction.transaction_number,
                product_dimension.id as product_key,             
                lot_dimension.id as lot_key,
                location_dimension.id as location_key,
                transaction_date_dimension.id as transaction_date_key,
                transaction_type_dimension.id as transaction_type_key,
                transaction.transaction_date,
                transaction_entry.quantity
            from transaction_entry 
            join transaction on transaction.id = transaction_entry.transaction_id
			left join `order` on transaction.order_id = `order`.id
            join inventory on transaction.inventory_id = inventory.id 
            join location on location.inventory_id = transaction.inventory_id
            join transaction_type on transaction_type.id = transaction.transaction_type_id 
            join inventory_item on inventory_item.id = transaction_entry.inventory_item_id
            join lot_dimension on lot_dimension.inventory_item_id = transaction_entry.inventory_item_id
            join product_dimension on product_dimension.product_id = inventory_item.product_id
            join location_dimension on location_dimension.location_id = location.id
            join date_dimension transaction_date_dimension on transaction_date_dimension.date = date(transaction.transaction_date)
            join transaction_type_dimension on transaction_type_dimension.transaction_type_id = transaction_type.id
            where transaction.order_id is null 
            or `order`.order_type_code not in ('TRANSFER_ORDER') ;
        """
        dataService.executeStatements([insertStatement])
    }


    def buildConsumptionFact() {
        String insertStatement = """
            insert into consumption_fact (version, 
                transaction_number, 
                transaction_code,
                transaction_type,
                product_key_id, 
                lot_key_id, 
                location_key_id, 
                transaction_date_key_id, 
                quantity, 
                unit_cost,
                unit_price,
                date_created, 
                last_updated)
            select  
                0, 
                transaction_type.transaction_code,
                transaction_type.name,
                transaction.transaction_number,
                product_dimension.id as product_key,             
                lot_dimension.id as lot_key,
                location_dimension.id as location_key,
                transaction_date_dimension.id as transaction_date_key,
                transaction_entry.quantity,
                0.0,
                0.0,
                now(),
                now()
            from transaction_entry 
            join transaction on transaction.id = transaction_entry.transaction_id
            join inventory on transaction.inventory_id = inventory.id 
            join location on location.inventory_id = transaction.inventory_id
            join transaction_type on transaction_type.id = transaction.transaction_type_id 
            join inventory_item on inventory_item.id = transaction_entry.inventory_item_id
            join lot_dimension on lot_dimension.inventory_item_id = transaction_entry.inventory_item_id
            join product_dimension on product_dimension.product_id = inventory_item.product_id
            join location_dimension on location_dimension.location_id = location.id
            join date_dimension transaction_date_dimension on transaction_date_dimension.date = date(transaction.transaction_date)
            WHERE transaction_type.transaction_code = 'DEBIT'
        """
        dataService.executeStatements([insertStatement])
    }

    def refreshDemandData() {
        List ddlStatements = [
                "DROP TABLE IF EXISTS product_demand_details_tmp;",
                """CREATE TABLE product_demand_details_tmp AS
                    SELECT 
                        request_id,
                        request_status,
                        request_number,
                        date_created,
                        date_requested,
                        date_issued,
                        origin_id,
                        origin_name,
                        destination_id,
                        destination_name,
                        request_item_id,
                        product_id,
                        product_code,
                        product_name,
                        quantity_requested,
                        quantity_canceled,
                        quantity_approved,
                        quantity_modified,
                        quantity_substituted,
                        quantity_picked,
                        quantity_demand,
                        reason_code_classification
                    FROM product_demand;""",
                "DROP TABLE IF EXISTS product_demand_details;",
                "CREATE TABLE IF NOT EXISTS product_demand_details LIKE product_demand_details_tmp;",
                "TRUNCATE product_demand_details;",
                "INSERT INTO product_demand_details SELECT * FROM product_demand_details_tmp;"
        ]
        dataService.executeStatements(ddlStatements)
    }
}
