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
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.grails.plugins.csv.CSVWriter
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.ProductService
import org.pih.warehouse.requisition.Requisition

class ConsumptionController {

    def consoleService
    ProductService productService
    InventoryService inventoryService

    def show = { ShowConsumptionCommand command ->
        log.info "Show consumption " + params
        println "Bind errors: " + command.errors
        if (command.hasErrors()) {
            render(view: "show", model: [command:command])
            return;
        }

        println "selectedTags " + command.selectedTags
        println "selectedCategories " + command.selectedCategories
        println "toLocations " + command.toLocations
        println "fromLocations " + command.fromLocations

        //if (!command.fromLocation) {
        //    command.fromLocation = Location.get(session.warehouse.id)
        //}


        List selectedLocations = [] //= session.invoiceList
        params.each {
            if (it.key.contains("selectedLocation_")){
                if (it.value.contains("on")){
                    //InvoiceItem invoiceItem = invoiceList.get((it.key - "invoiceItem_") as Integer)
                    Location location = Location.get((it.key - "selectedLocation_"))
                    if (location) {
                        selectedLocations << location
                    }
                }
            }
        }
        println "selectedProperties: " + command.selectedProperties
        println "fromLocations: " + command.fromLocations.size()
        println "toLocations: " + command.toLocations.size()
        println "selectedLocations: " + selectedLocations.size()
        command.selectedLocations = selectedLocations

        def fromLocations = []
        command.fromLocations.each {
            fromLocations << it
        }

        def tags = command.selectedTags.collect { it.tag}.asList()
        def products = tags ? inventoryService.getProductsByTags(tags) : null

        // Get all transactions
        command.debits = inventoryService.getDebitsBetweenDates(fromLocations, selectedLocations, command.fromDate, command.toDate)
        command.credits = inventoryService.getCreditsBetweenDates(selectedLocations, fromLocations, command.fromDate, command.toDate)

        println command.credits

        def transactions = []
        transactions.addAll(command.debits)
        transactions.addAll(command.credits)


        //command.toLocations.clear();
        // Iterate over all transactions
        transactions.each { transaction ->

            // Some transactions don't have a destination (e.g. expired, consumed, etc)
            if (transaction.destination) {
                command.toLocations << transaction.destination
            }

            // Keep track of all the transaction types (we may want to select a subset of these)
            command.transactionTypes << transaction.transactionType

            // Iterate over all transaction entries
            transaction.transactionEntries.each { transactionEntry ->
                def product = transactionEntry.inventoryItem.product
                def currentRow = command.rows[product]
                if (!currentRow) {
                    command.rows[product] = new ShowConsumptionRowCommand()
                    command.rows[product].command = command
                    command.rows[product].product = product
                }

                // Keep track of quantity out based on transasction type
                if (transaction.transactionType.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
                    command.rows[product].transferOutQuantity += transactionEntry.quantity
                    command.rows[product].transferOutTransactions << transaction

                    // Initialize transfer out by location
                    def transferOutQuantity = command.rows[product].transferOutMap[transaction.destination]
                    if (!transferOutQuantity) { command.rows[product].transferOutMap[transaction.destination] = 0 }

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
                }

                // All transactions
                command.rows[product].transactions << transaction

                //def currentProductQuantity = command.productMap[transactionEntry.inventoryItem.product]
                //if (!currentProductQuantity) {
                //    command.productMap[transactionEntry.inventoryItem.product] = 0
                //}
                //command.productMap[transactionEntry.inventoryItem.product] += transactionEntry.quantity
            }
        }

        // Calculate the on hand quantity for all products returned by the getTransactions() call above
        if (command.fromLocations) {

            //def products = command.productMap.keySet().asList()
            products = command.rows.keySet().asList()


            println "Products: " + products.size()

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
            println "Products after filter by tags: " + command.rows.size()

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
            println "Products after filter by categories: " + command.rows.size()
            products = command.rows.keySet().asList()

            command.fromLocations.each { location ->
                def onHandQuantityMap = inventoryService.getQuantityByProductMap(location.inventory, products)

                //println "onHandQuantityMap: " + onHandQuantityMap
                // For each product, add to the onhand quantity map
                products.each { product ->
                    def onHandQuantity = onHandQuantityMap[product];
                    //println "onHandQuantity: " + onHandQuantity
                    if(onHandQuantity) {
                        command.rows[product].onHandQuantity += onHandQuantity
                    }
                    //def onHandQuantity = command.onHandQuantityMap[product]
                    //if (!onHandQuantity) {
                    //    command.onHandQuantityMap[product] = 0;
                    //}

                    //if (onHandQuantityMap[product]) {
                    //    command.onHandQuantityMap[product] += onHandQuantityMap[product]
                    //}
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

        // Export as CSV
        if (params.format == "csv") {

            /*
            def csvWriter = new CSVWriter(sw, {
                "Product code" { it.productCode }
                "Name" { it.name }
                "Category" { it.category }
                "Unit of Measure" { it.unitOfMeasure }
                "Total" { it.transferOutQuantity }
                "Monthly" { it.monthlyQuantity }
                "Weekly" { it.weeklyQuantity }
                "Daily" { it.dailyQuantity }
                "On hand quantity" { it.onHandQuantity }
                "Months left" { it.numberOfMonthsRemaining }
            })
            */
            def csvrows = []
            command.rows.each { key, row ->
                def csvrow =  [
                        'Product code': row.product.productCode?:'',
                        'Product': row.product.name,
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
                        csvrow[property] = row.product."$property"
                    }
                }

                if (command.includeLocationBreakdown) {
                    command.selectedLocations.each { location ->
                        //println "location " + it.name + " = " + row.transferOutMap[it]
                        csvrow[location.name] = row.transferOutMap[location]?:""
                    }
                }

                csvrows << csvrow

                //println csvRow
                //csvWriter << csvRow
            }
            //println "CSV: " + sw.toString()

            def sw = new StringWriter()
            if (csvrows) {
                sw.append(csvrows[0].keySet().join(",")).append("\n")
                csvrows.each { csvrow ->
                    def values = csvrow.values().collect { value ->
                        if (value?.toString()?.isNumber()) {
                            value
                        }
                        else {
                            '"' + value.toString().replace('"','""') + '"'
                        }
                    }
                    sw.append(values.join(","))
                    sw.append("\n")
                }
            }

            println "Location breakdown " + (command.includeLocationBreakdown?'yes':'no')
            println "Selected locations " + command.selectedLocations

            //response.contentType = "text/csv;charset=utf-8"

            response.setHeader("Content-disposition", "attachment; filename=consumption-${new Date().format("yyyyMMdd-hhmmss")}.csv")
            render(contentType:"text/csv", text: sw.toString(), encoding:"UTF-8")
        }
        else {
            [command:command]
        }
    }

    // Proof of concept to see if we could evalute a string of code
    // Could be used to create dynamic indicators for the dashboard
    def evaluate = {
        String code = """
            import org.pih.warehouse.product.Product;
            def products = Product.list();
            return products.size()
        """

        // String code, boolean captureStdout, request
        render consoleService.eval(code, true, request)
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
    List<Location> selectedLocations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class));
    List<Category> selectedCategories = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Category.class));
    List<Tag> selectedTags = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Tag.class));

    Boolean includeLocationBreakdown = false

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
        fromDate(nullable: false)
        toDate(nullable: false)
        fromLocations(nullable: false)
        toLocations(nullable: true)
        //fromLocation(nullable: true)
        //toLocation(nullable: true)
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
    Integer debitQuantity = 0;

    Set<Transaction> transferOutTransactions = []
    Set<Transaction> expiredTransactions = []
    Set<Transaction> damagedTransactions = []
    Set<Transaction> transactions = []
    Set<Transaction> transferInTransactions = []


    Map<Location, Integer> transferOutMap = new TreeMap<Location, Integer>();

    static constraints = {

    }

    Integer getTransferBalance() {
        return transferOutQuantity - transferInQuantity
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
        return onHandQuantity / getMonthlyQuantity()
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
