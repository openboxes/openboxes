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

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import org.apache.http.client.HttpClient
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.DefaultHttpClient
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.SynonymTypeCode
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.invoice.InvoiceType
import org.pih.warehouse.invoice.InvoiceTypeCode
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.reporting.DateDimension
import org.pih.warehouse.LocalizationUtil
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.xhtmlrenderer.pdf.ITextRenderer
import util.InventoryUtil

import java.text.NumberFormat

class ReportService implements ApplicationContextAware {

    def dataService
    def dashboardService

    GrailsApplication grailsApplication
    def userService
    def orderService
    def shipmentService

    ApplicationContext applicationContext

    void generateShippingReport(ChecklistReportCommand command) {
        def shipmentItems = command?.shipment?.shipmentItems?.sort()
        shipmentItems.each { shipmentItem ->
            command.checklistReportEntryList << new ChecklistReportEntryCommand(shipmentItem: shipmentItem)
        }
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

    void generatePdf(String url, OutputStream outputStream) {
        def html = ""
        log.info "Generate PDF for URL " + url
        try {
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

            def imageUrl = (product.thumbnail) ? '/product/renderImage/${product?.thumbnail?.id}' : ''

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
        buildTransactionFact()
        buildConsumptionFact()
        buildStockoutFact()
    }

    def truncateFacts() {
        dataService.executeStatements(["SET FOREIGN_KEY_CHECKS = 0",
                                       "delete from transaction_fact",
                                       "delete from consumption_fact",
                                       "delete from stockout_fact",
                                       "alter table transaction_fact AUTO_INCREMENT = 1",
                                       "alter table consumption_fact AUTO_INCREMENT = 1",
                                       "alter table stockout_fact AUTO_INCREMENT = 1",
                                       "SET FOREIGN_KEY_CHECKS = 1"])
    }

    def truncateDimensions() {
        dataService.executeStatements([
                "SET FOREIGN_KEY_CHECKS = 0",
                "delete from date_dimension",
                "delete from location_dimension",
                "delete from lot_dimension",
                "delete from product_dimension",
                "delete from transaction_type_dimension",
                "alter table date_dimension AUTO_INCREMENT = 1",
                "alter table location_dimension AUTO_INCREMENT = 1",
                "alter table lot_dimension AUTO_INCREMENT = 1",
                "alter table product_dimension AUTO_INCREMENT = 1",
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
        Date today = new Date()
        Date minTransactionDate = Transaction.minTransactionDate.get()
        log.info("minTransactionDate: " + minTransactionDate)
        if (minTransactionDate) {
            (minTransactionDate..today).each { Date date ->
                saveDateDimension(date)
            }
        }
        else {
            saveDateDimension(today)
        }
    }

    @Transactional
    def saveDateDimension(Date date) {
        date.clearTime()

        DateDimension dateDimension = DateDimension.findByDate(date)
        if (!dateDimension) {
            dateDimension = new DateDimension()
            dateDimension.date = date
            dateDimension.dayOfMonth = date[Calendar.DAY_OF_MONTH]
            dateDimension.dayOfWeek = date[Calendar.DAY_OF_WEEK]
            dateDimension.month = date[Calendar.MONTH] + 1
            dateDimension.year = date[Calendar.YEAR]
            dateDimension.week = date[Calendar.WEEK_OF_YEAR]
            dateDimension.monthName = date.format("MMMMM")
            dateDimension.monthYear = date.format("MM-yyyy")
            dateDimension.weekdayName = date.format("EEEEE")
            dateDimension.save()
        }
    }

    def buildTransactionFact() {
        String deleteStatement = """delete from transaction_fact;"""
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
            or `order`.order_type_id not in ('PUTAWAY_ORDER') ;
        """
        dataService.executeStatements([deleteStatement, insertStatement])
    }


    def buildConsumptionFact() {
        String deleteStatement = """delete from consumption_fact;"""
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
        dataService.executeStatements([deleteStatement, insertStatement])
    }


    def getStockoutData(Location location, Product product, int days) {
        String query = """
            SELECT count(*) as stockoutDays
            FROM stockout_fact 
            JOIN location_dimension ON location_dimension.id = stockout_fact.location_dimension_id
            JOIN product_dimension ON product_dimension.id = stockout_fact.product_dimension_id
            JOIN date_dimension ON date_dimension.id = stockout_fact.date_dimension_id
            WHERE product_id = :productId
            AND location_id = :locationId
            AND date BETWEEN (CURDATE() - INTERVAL :days DAY) AND NOW();
        """
        return dataService.executeQuery(query, [locationId:location.id,productId:product.id, days: days])
    }

    void createStockoutFact() {

        // No foreign keys because records in this table will reference
        // dimension tables that need to be rebuilt nightly
        String createTableStatement = """
            CREATE TABLE IF NOT EXISTS stockout_fact (
                date_dimension_id bigint(20),
                location_dimension_id bigint(20),
                product_dimension_id bigint(20),
                quantity_on_hand smallint,                
                primary key (date_dimension_id, location_dimension_id, product_dimension_id)
            );
            """

        dataService.executeStatements([createTableStatement])
    }

    void buildStockoutFact() {
        def yesterday = new Date()-1
        def monthAgo = new Date()-30
        (monthAgo .. yesterday).each { Date date ->
            buildStockoutFact(date)
        }
    }

    void buildStockoutFact(Date date) {
        saveDateDimension(date)
        createStockoutFact()
        deleteStockoutFact(date)
        populateStockoutFact(date)
    }

    void deleteStockoutFact(Date date) {

        date.clearTime()
        String dateParam = date.format("yyyy-MM-dd")

        String deleteStatement = """
            DELETE stockout_fact 
            FROM stockout_fact 
            JOIN date_dimension ON date_dimension.id = stockout_fact.date_dimension_id
            WHERE date_dimension.date = '${dateParam}';
        """
        dataService.executeStatement(deleteStatement)
    }

    void populateStockoutFact(Date date) {
        date.clearTime()
        String dateParam = date.format("yyyy-MM-dd")

        String insertStatement = """
            insert into stockout_fact (
                date_dimension_id, 
                location_dimension_id,
                product_dimension_id,
                quantity_on_hand)
            select * from (
                select 
                    date_dimension.id as date_dimension_id, 
                    location_dimension.id as location_dimension_id,
                    product_dimension.id as product_dimension_id,
                    sum(quantity_on_hand) as quantity_on_hand
                from inventory_snapshot 
                join date_dimension on inventory_snapshot.date = date_dimension.date 
                join product_dimension on inventory_snapshot.product_id = product_dimension.product_id
                join location_dimension on inventory_snapshot.location_id = location_dimension.location_id
                where date_dimension.date = '${dateParam}'
                group by product_dimension.id, location_dimension.id, date_dimension.id
                having sum(quantity_on_hand) <= 0
            ) as stockout_tmp
            on duplicate key update stockout_fact.quantity_on_hand = stockout_tmp.quantity_on_hand;
        """
        dataService.executeStatement(insertStatement)
    }

    def refreshProductDemandData() {
        List statements = [
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
                        quantity_picked,
                        quantity_demand,
                        reason_code,
                        reason_code_classification
                    FROM product_demand;""",
                "DROP TABLE IF EXISTS product_demand_details;",
                "CREATE TABLE IF NOT EXISTS product_demand_details LIKE product_demand_details_tmp;",
                "TRUNCATE product_demand_details;",
                "INSERT INTO product_demand_details SELECT * FROM product_demand_details_tmp;",
                "ALTER TABLE product_demand_details ADD INDEX (product_id, origin_id, destination_id, date_issued, date_requested)"
        ]
        dataService.executeStatements(statements)
    }

    List getOnOrderSummary(Location location) {
        String locale = LocalizationUtil.localizationService.getCurrentLocale().toString()

        String query = """
            select 
                product.product_code as productCode, 
                product.name as productName, 
                oos.quantity_ordered_not_shipped as qtyOrderedNotShipped,
                oos.quantity_shipped_not_received as qtyShippedNotReceived, 
                ps.quantity_on_hand as qtyOnHand,
                product_group.name as productFamilyName,
                category.name as productCategoryName,
                (
                select s.name from synonym s
                where s.product_id = product.id
                and s.synonym_type_code = :synonymTypeCode
                and s.locale = :locale
                limit 1
                ) as displayName,
                (
                select group_concat(product_catalog.name separator ', ')
                from product_catalog_item
                left outer join product_catalog on product_catalog_item.product_catalog_id = product_catalog.id
                where product_catalog_item.product_id = product.id
                group by product.name
                ) as productCatalogs
            from on_order_summary oos
            join product on oos.product_id = product.id
            left outer join product_group on product.product_family_id = product_group.id
            left outer join category on product.category_id = category.id
            left outer join product_snapshot ps on (product.id = ps.product_id 
                and ps.location_id = oos.destination_id)
            where destination_id = :locationId
            """
        def results = dataService.executeQuery(query,  [synonymTypeCode: SynonymTypeCode.DISPLAY_NAME.name(), locale: locale, locationId: location.id])
        def data = results.collect {
            def qtyOnHand = it.qtyOnHand ? it.qtyOnHand.toInteger() : 0
            def qtyOrderedNotShipped = it.qtyOrderedNotShipped ? it.qtyOrderedNotShipped.toInteger() : 0
            def qtyShippedNotReceived = it.qtyShippedNotReceived ? it.qtyShippedNotReceived : 0
            def displayNameWithLocaleCode = "${it.productName}${it.displayName ? " (${locale?.toUpperCase()}: ${it.displayName})" : ''}"
            println displayNameWithLocaleCode
            [
                    productCode                 : it.productCode,
                    productName                 : it.productName,
                    displayNameWithLocaleCode   : displayNameWithLocaleCode,
                    displayName                 : it.displayName,
                    productFamily               : it.productFamilyName ?: '',
                    category                    : it.productCategoryName ?: '',
                    productCatalogs             : it.productCatalogs ?: '',
                    qtyOrderedNotShipped        : qtyOrderedNotShipped ?: '',
                    qtyShippedNotReceived       : qtyShippedNotReceived ?: '',
                    totalOnOrder                : qtyOrderedNotShipped + qtyShippedNotReceived,
                    totalOnHand                 : qtyOnHand,
                    totalOnHandAndOnOrder       : qtyOrderedNotShipped + qtyShippedNotReceived + qtyOnHand,
            ]
        }
        return data
    }

    Map getQuantityOnOrder (String locationId, List<String> productIds) {
        def products = []
        productIds.each { products << Product.findById(it) }
        def location = Location.get(locationId)
        def items = orderService.getPendingInboundOrderItems(location, products)
        items += shipmentService.getPendingInboundShipmentItems(location, products)
        def itemsMap = [:]

        items.collect {
            def isOrderItem = it instanceof OrderItem
            [
                    productId  : it.product?.id,
                    qtyOrderedNotShipped : isOrderItem ? (it.quantityRemaining * it.quantityPerUom) : 0,
                    qtyShippedNotReceived : isOrderItem ? 0 : it.quantityRemaining.toInteger(),
            ]
        }.groupBy { it.productId }.collect { k, v ->
            itemsMap.put(k, [
                    qtyOrderedNotShipped : v.qtyOrderedNotShipped.sum(),
                    qtyShippedNotReceived : v.qtyShippedNotReceived.sum(),
            ]
            )
        }

        return itemsMap
    }

    List getOnOrderData(String locationId, List<String> productIds) {
        def onOrder = getQuantityOnOrder(locationId, productIds)

        String query = """
            select
                oos.product_id as productId,
                ps.quantity_on_hand as qtyOnHand
            from on_order_summary oos
            join product on oos.product_id = product.id
            left outer join product_snapshot ps on (product.id = ps.product_id 
                and ps.location_id = oos.destination_id)
            where destination_id = :locationId and oos.product_id in (${productIds.collect { "'$it'" }.join(',')})
            """
        def results = dataService.executeQuery(query, [locationId: locationId])
        def data = results.collect { it ->
            Integer qtyOrderedNotShipped = onOrder[it.productId] ? onOrder[it.productId].qtyOrderedNotShipped.toInteger() : 0
            Integer qtyShippedNotReceived = onOrder[it.productId] ? onOrder[it.productId].qtyShippedNotReceived.toInteger() : 0
            [
                    productId            : it.productId,
                    totalOnOrder         : qtyOrderedNotShipped + qtyShippedNotReceived,
                    totalOnHand          : it.qtyOnHand ? it.qtyOnHand.toInteger() : 0,
            ]
        }
        return data
    }

    def getForecastReport(Map params) {
        List data = []
        boolean forecastingEnabled = grailsApplication.config.openboxes.forecasting.enabled ?: false
        if (forecastingEnabled) {
            String query = """
            select 
                pdd.product_id,
                pdd.quantity_demand
            FROM product_demand_details pdd
            """

            if (params.category && params.category != "null") {
                query += " JOIN product ON product.id = pdd.product_id"
            }
            if (params.tags && params.tags != "null") {
                query += " LEFT JOIN product_tag ON product_tag.product_id = pdd.product_id"
            }
            if (params.catalogs && params.catalogs != "null") {
                query += " LEFT JOIN product_catalog_item ON product_catalog_item.product_id = pdd.product_id"
            }

            query += " WHERE date_issued BETWEEN :startDate AND :endDate AND pdd.origin_id = :originId"

            if (params.locations && params.locations != "null") {
                def destinations = []
                params.locations.getClass().isArray() ? params.locations.each { destinations << it } : destinations << params.locations
                query += " AND pdd.destination_id in (${destinations.collect { "'$it'" }.join(',')})"
            }

            if (params.category && params.category != "null") {
                def categories = []
                if (params.category.getClass().isArray()) {
                    params.category.each {
                        def category = Category.get(it)
                        if (category) {
                            categories += category.children
                            categories << category
                        }
                    }
                } else {
                    def category = Category.get(params.category)
                    if (category) {
                        categories += category.children
                        categories << category
                    }
                }

                categories = categories.unique()
                query += " AND product.category_id in (${categories.collect { "'$it.id'" }.join(',')})"
            }

            if (params.tags && params.tags != "null") {
                def tags = []
                params.tags.getClass().isArray() ? params.tags.each { tags << it } : tags << params.tags
                query += " AND product_tag.tag_id in (${tags.collect { "'$it'" }.join(',')})"
            }

            if (params.catalogs && params.catalogs != "null") {
                def catalogs = []
                params.catalogs.getClass().isArray() ? params.catalogs.each { catalogs << it } : catalogs << params.catalogs
                query += " AND product_catalog_item.product_catalog_id in (${catalogs.collect { "'$it'" }.join(',')})"
            }

            def results = dataService.executeQuery(query, params)
            if (results) {
                def onOrderData = getOnOrderData(params.originId, results.collect{it.product_id}.unique())
                def monthsInPeriod = (params.endDate - params.startDate) / 30
                data = results.groupBy { it.product_id}.collect { productId, demandDetails ->
                    [
                            productId             : productId,
                            totalOnOrder          : onOrderData.find {it.productId == productId}?.totalOnOrder ?: '',
                            totalOnHand           : onOrderData.find {it.productId == productId}?.totalOnHand ?: '',
                            averageMonthlyDemand  : demandDetails.collect {it.quantity_demand}.sum() / monthsInPeriod
                    ]
                }
            }
        }

        return data
    }

    /**
     * Report should show all order items or adjustments that are ordered, not cancelled, and not fully
     * invoiced (Meaning qty ordered>qty invoiced. Prepayment invoices should not count as invoices.
     * Exclude cancelled items and adjustments.
     * */
    def getAmountOutstandingOnOrders(String currentLocationId) {
        Location currentLocation = Location.get(currentLocationId)
        def additionalFilter = ""
        def prepaymentInvoice = InvoiceType.findByCode(InvoiceTypeCode.PREPAYMENT_INVOICE)
        def queryParams
        if (currentLocation?.supports(ActivityCode.ENABLE_CENTRAL_PURCHASING)) {
            /**
             * In location with activity code enable_central_purchasing:
             *  - Filter by buyer organization = org of the location you are currently in
             *  - Do not filter by destination
             */
            additionalFilter = "AND o.destination_party_id = :buyerOrganizationId "
            queryParams = [prepaymentInvoiceId: prepaymentInvoice?.id, buyerOrganizationId: currentLocation?.organization?.id]
        } else {
            /**
             * In locations without that activity code:
             *  - Filter by destination location = location you are currently in
             *  - Do not filter by buyer org
             * */
            additionalFilter = "AND o.destination_id = :currentLocationId "
            queryParams = [prepaymentInvoiceId: prepaymentInvoice?.id, currentLocationId: currentLocation?.id]
        }

        String orderItemsQuery = """
            SELECT 
                order_item_invoice_summary.id, 
                order_item_invoice_summary.order_id,
                order_item_invoice_summary.quantity_ordered,
                SUM(order_item_invoice_summary.quantity_shipped) AS quantity_shipped, 
                SUM(order_item_invoice_summary.quantity_invoiced) AS quantity_invoiced
            FROM (
                SELECT 
                    order_item.id AS id,
                    o.id AS order_id,
                    order_item.quantity AS quantity_ordered,
                    CASE
                        WHEN shipment.current_status IN ('SHIPPED', 'PARTIALLY_RECEIVED', 'RECEIVED') THEN IFNULL(shipment_item.quantity / order_item.quantity_per_uom, 0)
                        ELSE 0
                    END AS quantity_shipped,
                    SUM(IF(invoice.date_posted IS NULL, 0, IFNULL(invoice_item.quantity, 0))) AS quantity_invoiced
                FROM `order` o
                    LEFT OUTER JOIN order_item ON o.id = order_item.order_id
                    LEFT OUTER JOIN order_shipment ON order_item.id = order_shipment.order_item_id
                    LEFT OUTER JOIN shipment_item ON shipment_item.id = order_shipment.shipment_item_id
                    LEFT OUTER JOIN shipment ON shipment.id = shipment_item.shipment_id
                    LEFT OUTER JOIN shipment_invoice ON shipment_invoice.shipment_item_id = shipment_item.id
                    LEFT OUTER JOIN invoice_item ON invoice_item.id = shipment_invoice.invoice_item_id
                    LEFT OUTER JOIN invoice ON invoice_item.invoice_id = invoice.id
                WHERE o.order_type_id = 'PURCHASE_ORDER'
                    AND order_item.order_item_status_code != 'CANCELED'
                    AND (invoice.invoice_type_id != :prepaymentInvoiceId OR invoice.invoice_type_id IS NULL)
                    AND (invoice_item.inverse IS NULL OR invoice_item.inverse = FALSE)
                    ${additionalFilter}
                GROUP BY o.id, order_item.id, shipment_item.id
            ) AS order_item_invoice_summary 
            GROUP BY id
            HAVING order_item_invoice_summary.quantity_ordered > SUM(order_item_invoice_summary.quantity_invoiced);
        """

        String orderAdjustmentsQuery = """
            SELECT
                order_adjustment.id AS id,
                o.id AS order_id
            FROM `order` o
                     LEFT OUTER JOIN order_adjustment ON order_adjustment.order_id = o.id
                     LEFT OUTER JOIN order_adjustment_invoice ON order_adjustment_invoice.order_adjustment_id = order_adjustment.id
                     LEFT OUTER JOIN invoice_item ON invoice_item.id = order_adjustment_invoice.invoice_item_id
                     LEFT OUTER JOIN invoice ON invoice_item.invoice_id = invoice.id
                     LEFT OUTER JOIN (
                        SELECT adjustment_id, SUM(invoiced_amount) as total_invoiced_amount
                        FROM order_adjustment_payment_status
                        GROUP BY adjustment_id
                    ) as adjustment_invoice_amount ON adjustment_invoice_amount.adjustment_id = order_adjustment.id
                    LEFT OUTER JOIN order_adjustment_details ON order_adjustment_details.id = order_adjustment.id
            WHERE o.order_type_id = 'PURCHASE_ORDER'
              AND order_adjustment.canceled IS NOT TRUE
              AND (invoice_item.inverse IS NULL OR invoice_item.inverse = FALSE)
                ${additionalFilter}
            GROUP BY o.id, order_adjustment.id
            HAVING SUM(
                       CASE
                           WHEN (
                               invoice.invoice_type_id = :prepaymentInvoiceId
                                   OR invoice.invoice_type_id IS NULL
                                   OR invoice.date_posted IS NULL
                                   OR (invoice.date_posted IS NOT NULL AND ABS(adjustment_invoice_amount.total_invoiced_amount) != order_adjustment_details.total_adjustment)
                               ) THEN 0
                           ELSE 1
                           END
                   ) = 0;
        """

        List orderItemResults = dataService.executeQuery(orderItemsQuery, queryParams)
        List orderAdjustmentResults = dataService.executeQuery(orderAdjustmentsQuery, queryParams)

        List rows = parseItemsForAmountOutstandingOnOrdersReport(orderItemResults) +
            parseAdjustmentsForAmountOutstandingOnOrdersReport(orderAdjustmentResults)

        if (rows.size() == 0) {
            String currencyCode = grailsApplication.config.openboxes.locale.defaultCurrencyCode ?: "USD"
            return [["Supplier": "", "Destination Name": "", "PO Number": "", "Type": "", "Code": "", "Description": "",
                 "UOM": "", "Cost per UOM (${currencyCode})": "", "Qty Ordered not shipped (UOM)": "",
                 "Qty Ordered not shipped (Each)": "", "Value ordered not shipped": "", "Qty Shipped not Invoiced (UOM)": "",
                 "Qty Shipped not Invoiced (Each)": "", "Value Shipped not invoiced": "", "Total Qty not Invoiced (UOM)": "",
                 "Total Qty not Invoiced (Each)": "", "Total Value not invoiced": "", "Budget Code": "", "Payment Terms": "",
                 "Recipient": "", "Estimated Ready Date": "", "Actual Ready Date": ""]]
        }

        return rows.sort { it["PO Number"] }
    }

    def parseItemsForAmountOutstandingOnOrdersReport(List results) {
        def rows = []
        results.collect { result ->
            OrderItem orderItem = OrderItem.get(result["id"])
            if (!orderItem) {
                return
            }
            Organization organization = orderItem?.order?.origin?.organization

            def quantityOrdered = result["quantity_ordered"] ? result["quantity_ordered"] : 0
            def quantityShipped = result["quantity_shipped"] ? result["quantity_shipped"] : 0
            def quantityInvoiced = result["quantity_invoiced"] ? result["quantity_invoiced"] : 0

            def orderedNotShipped = quantityOrdered - quantityShipped > 0 ? quantityOrdered - quantityShipped : 0
            def shippedNotInvoiced = quantityShipped - quantityInvoiced > 0 ? quantityShipped - quantityInvoiced : 0
            def quantityNotInvoiced = quantityOrdered - quantityInvoiced > 0 ? quantityOrdered - quantityInvoiced : 0

            NumberFormat currencyNumberFormat = getCurrencyNumberFormat()
            NumberFormat integerFormat =  NumberFormat.getIntegerInstance()

            def printRow = [
                "Supplier"                                          : "${organization?.code} - ${organization?.name}",
                "Destination Name"                                  : orderItem?.order?.destination?.name,
                "PO Number"                                         : orderItem?.order?.orderNumber,
                "Type"                                              : "Item",
                "Code"                                              : orderItem?.product?.productCode,
                "Product name"                                      : orderItem?.product?.displayNameOrDefaultName,
                "UOM"                                               : orderItem?.unitOfMeasure,
                "Cost per UOM (${currencyNumberFormat.currency})"   : currencyNumberFormat.format(orderItem?.unitPrice ?: 0),
                "Qty Ordered not shipped (UOM)"                     : integerFormat.format(orderedNotShipped),
                "Qty Ordered not shipped (Each)"                    : integerFormat.format(orderedNotShipped * orderItem?.quantityPerUom),
                "Value ordered not shipped"                         : currencyNumberFormat.format(orderedNotShipped * (orderItem?.unitPrice ?: 0)),
                "Qty Shipped not Invoiced (UOM)"                    : integerFormat.format(shippedNotInvoiced),
                "Qty Shipped not Invoiced (Each)"                   : integerFormat.format(shippedNotInvoiced * orderItem?.quantityPerUom),
                "Value Shipped not invoiced"                        : currencyNumberFormat.format(shippedNotInvoiced * (orderItem?.unitPrice ?: 0)),
                "Total Qty not Invoiced (UOM)"                      : integerFormat.format(quantityNotInvoiced),
                "Total Qty not Invoiced (Each)"                     : integerFormat.format(quantityNotInvoiced * orderItem?.quantityPerUom),
                "Total Value not invoiced"                          : currencyNumberFormat.format(quantityNotInvoiced * (orderItem?.unitPrice ?: 0)),
                "Budget Code"                                       : orderItem?.budgetCode?.code,
                "Payment Terms"                                     : orderItem?.order.paymentTerm?.name,
                "Recipient"                                         : orderItem?.recipient?.name,
                "Estimated Ready Date"                              : orderItem?.estimatedReadyDate?.format("MM/dd/yyyy"),
                "Actual Ready Date"                                 : orderItem?.actualReadyDate?.format("MM/dd/yyyy"),
            ]

            rows << printRow
        }

        return rows
    }

    def parseAdjustmentsForAmountOutstandingOnOrdersReport(List results) {
        def rows = []
        results.collect { result ->
            OrderAdjustment orderAdjustment = OrderAdjustment.get(result["id"])
            if (!orderAdjustment) {
                return
            }
            Organization organization = orderAdjustment?.order?.origin?.organization

            NumberFormat currencyNumberFormat = getCurrencyNumberFormat()

            def printRow = [
                "Supplier"                                          : "${organization?.code} - ${organization?.name}",
                "Destination Name"                                  : orderAdjustment?.order?.destination?.name,
                "PO Number"                                         : orderAdjustment?.order?.orderNumber,
                "Type"                                              : "Adjustment",
                "Code"                                              : orderAdjustment?.orderItem?.product?.productCode ?: "",
                "Product name"                                      : orderAdjustment?.orderItem?.product?.displayNameOrDefaultName,
                "UOM"                                               : "",
                "Cost per UOM (${currencyNumberFormat.currency})"   : currencyNumberFormat.format((orderAdjustment?.totalAdjustments) ?: 0),
                "Qty Ordered not shipped (UOM)"                     : "",
                "Qty Ordered not shipped (Each)"                    : "",
                "Value ordered not shipped"                         : "",
                "Qty Shipped not Invoiced (UOM)"                    : "",
                "Qty Shipped not Invoiced (Each)"                   : "",
                "Value Shipped not invoiced"                        : "",
                "Total Qty not Invoiced (UOM)"                      : "",
                "Total Qty not Invoiced (Each)"                     : 1,
                "Total Value not invoiced"                          : currencyNumberFormat.format((orderAdjustment?.totalAmountNotInvoiced) ?: 0),
                "Budget Code"                                       : orderAdjustment?.budgetCode?.code,
                "Payment Terms"                                     : orderAdjustment?.order?.paymentTerm?.name,
                "Recipient"                                         : "",
                "Estimated Ready Date"                              : "",
                "Actual Ready Date"                                 : "",
            ]

            rows << printRow
        }

        return rows
    }

    def getCurrencyNumberFormat() {
        NumberFormat numberFormat = NumberFormat.getNumberInstance()
        String currencyCode = grailsApplication.config.openboxes.locale.defaultCurrencyCode ?: "USD"
        numberFormat.currency = Currency.getInstance(currencyCode)
        numberFormat.maximumFractionDigits = 2
        numberFormat.minimumFractionDigits = 2
        return numberFormat
    }
}
