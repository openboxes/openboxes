/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.reporting

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.apache.commons.lang.StringEscapeUtils
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.web.json.JSONObject
import org.grails.plugins.csv.CSVWriter
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.ProductService
import org.pih.warehouse.requisition.Requisition

class ConsumptionController {

    def dataService
    ProductService productService
    InventoryService inventoryService

    def show = { ShowConsumptionCommand command ->

        if (command.hasErrors()) {
            render(view: "show", model: [command:command])
            return;
        }


        // Hack to fix PIMS-2728
        if (command.selectedProperties) {
            if (command.selectedProperties instanceof java.lang.String) {
                command.selectedProperties = [command.selectedProperties]
            }
        }

        def tags = command.selectedTags.collect { it.tag}.asList()
        def products = tags ? inventoryService.getProductsByTags(tags) : null

        // Add an entire day to account for the 24 hour period on the end date
        Date endDate = command.toDate ? command.toDate + 1 : null

        // Get all transactions
        command.debits = inventoryService.getDebitsBetweenDates(command.fromLocations,
                command.selectedLocations, command.fromDate, endDate,
                command.selectedTransactionTypes)

        def transactions = []
        transactions.addAll(command.debits)
        //transactions.addAll(command.credits)

        // Sort transaction by date ascending
        transactions = transactions.sort { it.transactionDate }

        // Used within the transaction block to see if we need to add all destinations to command.toLocations
        // which occurs if there are no toLocations selected
        boolean toLocationsEmpty = command.toLocations.empty
        boolean fromLocationsEmpty = command.fromLocations.empty
        boolean transactionTypesEmpty = command.transactionTypes.empty

        // Some transactions don't have a destination (e.g. expired, consumed, etc)
        if (toLocationsEmpty) {
            command.toLocations = transactions.findAll { it.destination != null }.collect { it.destination }
        }

        // Keep track of all the transaction types (we may want to select a subset of these)
        command.transactionTypes = transactions*.transactionType

        // Iterate over all transactions
        transactions.each { transaction ->

            // Iterate over all transaction entries
            transaction.transactionEntries.each { transactionEntry ->
                def product = transactionEntry.inventoryItem.product
                def currentRow = command.rows[product]
                if (!currentRow) {
                    command.rows[product] = new ShowConsumptionRowCommand()
                    command.rows[product].command = command
                    command.rows[product].product = product
                }

                // Keep track of quantity out based on transaction type
                if (transaction.transactionType.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
                    command.rows[product].transferOutQuantity += transactionEntry.quantity
                    command.rows[product].transferOutTransactions << transaction

                    // Initialize transfer out by location map
                    def transferOutQuantity = command.rows[product].transferOutMap[transaction.destination]
                    if (!transferOutQuantity) {
                        command.rows[product].transferOutMap[transaction.destination] = 0
                    }

                    // Add to the total transfer out per location
                    command.rows[product].transferOutMap[transaction.destination] += transactionEntry.quantity
                }
                else if (transaction.transactionType.id == Constants.EXPIRATION_TRANSACTION_TYPE_ID) {
                    command.rows[product].expiredQuantity += transactionEntry.quantity
                    command.rows[product].expiredTransactions << transaction
                }
                else if (transaction.transactionType.id == Constants.DAMAGE_TRANSACTION_TYPE_ID) {
                    command.rows[product].damagedQuantity += transactionEntry.quantity
                    command.rows[product].damagedTransactions << transaction
                }
                else if (transaction.transactionType.id == Constants.TRANSFER_IN_TRANSACTION_TYPE_ID) {
                    command.rows[product].transferInQuantity += transactionEntry.quantity
                    command.rows[product].transferInTransactions << transaction

                    // Initialize transfer out by location map
                    def transferInQuantity = command.rows[product].transferInMap[transaction.source]
                    if (!transferInQuantity) {
                        command.rows[product].transferInMap[transaction.source] = 0
                    }

                    // Add to the total transfer out per location
                    command.rows[product].transferInMap[transaction.source] += transactionEntry.quantity
                }
                else {
                    command.rows[product].otherQuantity += transactionEntry.quantity
                    command.rows[product].otherTransactions << transaction
                }


                String dateKey = transaction.transactionDate.format("yyyy-MM")
                command.selectedDates.add(dateKey)

                // Capture month breakdown for all debits and credits
                if (transaction.transactionType.transactionCode == TransactionCode.DEBIT) {
                    // Add to total transfer out by month (initialize transfer out by month map)
                    def transferOutMonthlyQuantity = command.rows[product].transferOutMonthlyMap[dateKey]
                    if (!transferOutMonthlyQuantity) {
                        command.rows[product].transferOutMonthlyMap[dateKey] = 0
                    }
                    command.rows[product].transferOutMonthlyMap[dateKey] += transactionEntry.quantity
                }
                else if (transaction.transactionType.transactionCode == TransactionCode.CREDIT) {
                    // Add to total transfer in by month (initialize transfer out by month map)
                    def transferInMonthlyQuantity = command.rows[product].transferInMonthlyMap[dateKey]
                    if (!transferInMonthlyQuantity) {
                        command.rows[product].transferInMonthlyMap[dateKey] = 0
                    }
                    command.rows[product].transferInMonthlyMap[dateKey] += transactionEntry.quantity
                }

                // All transactions
                command.rows[product].transactions << transaction
            }
        }

        // Calculate the on hand quantity for all products returned by the getTransactions() call above
        if (command.fromLocations) {

            //def products = command.productMap.keySet().asList()
            products = command.rows.keySet().asList()


            // Filter products by tags
            if (command.selectedTags) {
                def productsToRemove = products.findAll { product ->
                    !command.selectedTags.intersect(product.tags)
                }

                def iterator = command.rows.keySet().iterator()
                while (iterator.hasNext()) {
                    if (productsToRemove.contains(iterator.next())) {
                        iterator.remove()
                    }
                }
            }

            // Filter products by categories
            if (command.selectedCategories) {
                def productsToRemove = products.findAll { product ->
                    !command.selectedCategories.contains(product.category)
                }

                def iterator = command.rows.keySet().iterator()
                while (iterator.hasNext()) {
                    if (productsToRemove.contains(iterator.next())) {
                        iterator.remove()
                    }
                }
            }
            products = command.rows.keySet().asList()

            // Calculate quantity on hand for filtered products
            if (!fromLocationsEmpty && command.includeQuantityOnHand) {
                command.fromLocations.each { location ->
                    if (location.inventory) {
                        def onHandQuantityMap = inventoryService.getQuantityByProductMap(location.inventory, products)

                        // For each product, add to the onhand quantity map
                        products.each { product ->
                            def onHandQuantity = onHandQuantityMap[product];
                            //println "onHandQuantity: " + onHandQuantity
                            if (onHandQuantity) {
                                command.rows[product].onHandQuantity += onHandQuantity
                            }
                        }
                    }
                }
            }
        }

        // We want to sort the transaction types and toLocations
        command?.transactionTypes?.unique()?.sort()
        command?.toLocations?.unique()?.sort()

        // If there are no selected locations, we select all of the possible destinations
        if (!command?.selectedLocations) {
            command.selectedLocations = command.toLocations
        }

        if (!command?.selectedTransactionTypes) {
            command.selectedTransactionTypes = command.transactionTypes
        }

        // Export as CSV
        if (params.format == "csv") {

            def csvrows = []
            command.rows.each { key, row ->
                def csvrow =  [
                        'Product code': row.product.productCode?:'',
                        'Product': row.product.name,
                        'Generic product': row.product?.genericProduct?.name?:"",
                        'Category': row.product?.category?.name,
                        'UoM': row.product.unitOfMeasure?:'',
                        'Bin Location': row?.product?.getBinLocation(session.warehouse.id)?:'',
                        'Qty transfer out': g.formatNumber(number: row.transferOutQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        'Count transfer out': g.formatNumber(number: row.transferOutTransactions.size(), format: '###.#', maxFractionDigits: 1)?:'',
                        'Qty transfer in': g.formatNumber(number: row.transferInQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        'Count transfer in': g.formatNumber(number: row.transferInTransactions.size(), format: '###.#', maxFractionDigits: 1)?:'',
                        'Qty transfer balance':g.formatNumber(number: row.transferBalance, format: '###.#', maxFractionDigits: 1)?:'',
                        'Qty expired': g.formatNumber(number: row.expiredQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        'Count expired': g.formatNumber(number: row.expiredTransactions.size(), format: '###.#', maxFractionDigits: 1)?:'',
                        'Qty damaged': g.formatNumber(number: row.damagedQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        'Count damaged': g.formatNumber(number: row.damagedTransactions.size(), format: '###.#', maxFractionDigits: 1)?:'',
                        'Consumed monthly': g.formatNumber(number: row.monthlyQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        'Consumed weekly': g.formatNumber(number: row.weeklyQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        'Consumed daily': g.formatNumber(number: row.dailyQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        'Quantity on hand': g.formatNumber(number: row.onHandQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        'Months remaining': g.formatNumber(number: row.numberOfMonthsRemaining, format: '###.#', maxFractionDigits: 1)?:'',
                ]

                if (command.selectedProperties) {
                    command.selectedProperties.each { property ->
                        csvrow[property] = row.product."${property}"
                    }
                }

                if (command.includeMonthlyBreakdown) {
                    command.selectedDates.each { date ->
                        csvrow["Out: " + date.toString()] = row.transferOutMonthlyMap[date]?:""
                        csvrow["In: " + date.toString()] = row.transferInMonthlyMap[date]?:""
                    }

                }


                if (command.includeLocationBreakdown) {
                    command.selectedLocations.each { location ->
                        csvrow["To: " + location.name] = row.transferOutMap[location]?:""
                    }
                }

                csvrows << csvrow

            }

            def csv = dataService.generateCsv(csvrows)
            response.setHeader("Content-disposition", "attachment; filename=\"Consumption-${new Date().format("dd MMM yyyy hhmmss")}.csv\"")
            render(contentType:"text/csv", text: csv.toString(), encoding:"UTF-8")
            return
        }
        else {
            println "Render as HTML " + params

            [command:command]
        }
    }


    def product = {
        Product product = Product.get(params.id)
        render (template: "product", model: [product:product])
    }

}


class ShowConsumptionCommand {

    // Map of product to ShowConsumptionRowCommand
    def rows = new TreeMap();

    // Filters
    Date fromDate
    Date toDate

    List<Tag> tags = []
    List<Category> categories = []
    List<Product> products = []
    List<Location> fromLocations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class));
    List<Location> toLocations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class));
    List<TransactionType> transactionTypes = []
    List<TransactionType> selectedTransactionTypes = []
    List <String> selectedDates = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(String.class));
    List<Location> selectedLocations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class));
    List<Category> selectedCategories = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Category.class));
    List<Tag> selectedTags = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Tag.class));

    Boolean includeLocationBreakdown = false
    Boolean includeMonthlyBreakdown = false
    Boolean includeQuantityOnHand = false

    def productDomain = new DefaultGrailsDomainClass( Product.class )

    def selectedProperties = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(String.class));

    // Payload
    Set<Transaction> debits = []
    Set<Transaction> credits = []
    Set<Requisition> requisitions = []
    Set<TransactionEntry> transactionEntries = []
    def productMap = new TreeMap();
    def onHandQuantityMap = new TreeMap();
    def transferOutMap = [:]

    static constraints = {
        fromLocations(nullable: false)
        toLocations(nullable: true)
        fromDate(nullable: true)
        toDate(nullable: true)
    }


    Integer getNumberOfDays() {
        return (toDate - fromDate)
    }

    Float getNumberOfWeeks() {
        return numberOfDays / 7
    }

    Float getNumberOfMonths() {

        //println "numberOfDays: " + numberOfDays
        return numberOfDays / 30
    }

}

class ShowConsumptionRowCommand {

    Product product
    ShowConsumptionCommand command
    InventoryLevel inventoryLevel

    Integer onHandQuantity = 0
    Integer transferInQuantity = 0
    Integer transferOutQuantity = 0
    Integer expiredQuantity = 0
    Integer damagedQuantity = 0
    Integer otherQuantity = 0
    Integer debitQuantity = 0;

    Set<Transaction> transferOutTransactions = []
    Set<Transaction> expiredTransactions = []
    Set<Transaction> damagedTransactions = []
    Set<Transaction> transactions = []
    Set<Transaction> transferInTransactions = []
    Set<Transaction> otherTransactions = []

    // Location breakdown
    Map<Location, Integer> transferInMap = new TreeMap<Location, Integer>();
    Map<Location, Integer> transferOutMap = new TreeMap<Location, Integer>();

    // Monthly breakdown
    Map<String, Integer> transferInMonthlyMap = new TreeMap<String, Integer>();
    Map<String, Integer> transferOutMonthlyMap = new TreeMap<String, Integer>();

    static constraints = {

    }

    Integer getTransferBalance() {
        transferOutQuantity
    }

    Float getMonthlyQuantity() {
        transferBalance / command.numberOfMonths
    }

    Float getWeeklyQuantity() {
        transferBalance / command.numberOfWeeks
    }

    Float getDailyQuantity() {
        transferBalance / command.numberOfDays
    }

    Float getNumberOfMonthsRemaining() {
        if (getMonthlyQuantity()>0) {
            return onHandQuantity / getMonthlyQuantity()
        }
        else {
            return 0.0
        }
    }


    String transferOutLocations(List<Location> locations) {
        String transferOutLocations = ""
        if (locations) {
            locations.each { location ->
                transferOutLocations += transferOutMap[location]?:'0' + ","
            }
        }
        return transferOutLocations
    }

}
