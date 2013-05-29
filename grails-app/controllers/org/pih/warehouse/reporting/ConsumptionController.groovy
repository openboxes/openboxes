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

class ConsumptionController {

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
        command.transactions = inventoryService.getDebitsBetweenDates(fromLocations, selectedLocations, products, command.fromDate, command.toDate)

        //command.toLocations.clear();
        // Iterate over all transactions
        command.transactions.each { transaction ->

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


                    def transferOutQuantity = command.rows[product].transferOutMap.get(transaction.destination, 0)
                    if (!transferOutQuantity) {
                        command.rows[product].transferOutMap[transaction.destination] += transactionEntry.quantity
                    }
                }
                else if (transaction.transactionType.id == Constants.EXPIRATION_TRANSACTION_TYPE_ID) {
                    command.rows[product].expiredQuantity += transactionEntry.quantity
                    command.rows[product].expiredTransactions << transaction
                }
                else if (transaction.transactionType.id == Constants.DAMAGE_TRANSACTION_TYPE_ID) {
                    command.rows[product].damagedQuantity += transactionEntry.quantity
                    command.rows[product].damagedTransactions << transaction
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


        def someValue2 = { param ->
            if (true) return "true "
            else return "false "
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
                        productCode: row.product.productCode?:'',
                        name: row.product.name,
                        category: row.product?.category?.name,
                        unitOfMeasure: row.product.unitOfMeasure?:'',
                        transferOutQuantity: g.formatNumber(number: row.transferOutQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        transferOutCount: g.formatNumber(number: row.transferOutTransactions.size(), format: '###.#', maxFractionDigits: 1)?:'',
                        expiredQuantity: g.formatNumber(number: row.expiredQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        expiredCount: g.formatNumber(number: row.expiredTransactions.size(), format: '###.#', maxFractionDigits: 1)?:'',
                        damagedQuantity: g.formatNumber(number: row.damagedQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        damagedCount: g.formatNumber(number: row.damagedTransactions.size(), format: '###.#', maxFractionDigits: 1)?:'',
                        monthlyQuantity: g.formatNumber(number: row.monthlyQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        weeklyQuantity: g.formatNumber(number: row.weeklyQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        dailyQuantity: g.formatNumber(number: row.dailyQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        onHandQuantity: g.formatNumber(number: row.onHandQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        numberOfMonthsRemaining: g.formatNumber(number: row.numberOfMonthsRemaining, format: '###.#', maxFractionDigits: 1)?:'',

                ]

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
                    def values = csvrow.values().collect { '"' + it.toString().replace('"','""') + '"' }
                    sw.append(values.join(","))
                    sw.append("\n")
                }
            }

            println "Location breakdown " + (command.includeLocationBreakdown?'yes':'no')
            println "Selected locations " + command.selectedLocations

            //response.contentType = "text/csv;charset=utf-8"

            response.setHeader("Content-disposition", "attachment; filename=openboxes-consumption-${new Date().format("yyyyMMdd-hhmmss")}.csv")
            render(contentType:"text/csv", text: sw.toString(), encoding:"UTF-8")
        }
        else {
            [command:command]
        }
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
    List<Location> fromLocations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class));
    List<Location> toLocations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class));
    List<TransactionType> transactionTypes = []
    // Fields to allow user to choose
    List<Location> selectedLocations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class));
    List<Category> selectedCategories = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Category.class));
    List<Tag> selectedTags = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Tag.class));

    Boolean includeLocationBreakdown = false

    def productDomain = new DefaultGrailsDomainClass( Product.class )

    def selectedProperties = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(String.class));

    // Payload
    Set<Transaction> transactions = []
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


    Map<Location, Integer> transferOutMap = new TreeMap<Location, Integer>();

    static constraints = {

    }

    Float getMonthlyQuantity() {
        transferOutQuantity / command.numberOfMonths
    }

    Float getWeeklyQuantity() {
        transferOutQuantity / command.numberOfWeeks
    }

    Float getDailyQuantity() {
        transferOutQuantity / command.numberOfDays
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
