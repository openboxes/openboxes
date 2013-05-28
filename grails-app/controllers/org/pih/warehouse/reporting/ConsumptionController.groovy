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
import org.grails.plugins.csv.CSVWriter
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.Category

class ConsumptionController {

    InventoryService inventoryService

    def show = { ShowConsumptionCommand command ->
        log.info "Show consumption " + params
        println "Bind errors: " + command.errors
        if (command.hasErrors()) {
            render(view: "show", model: [command:command])
            return;
        }

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

        println "fromLocations: " + command.fromLocations.size()
        println "toLocations: " + command.toLocations.size()
        println "selectedLocations: " + selectedLocations.size()
        command.selectedLocations = selectedLocations

        def fromLocations = []
        command.fromLocations.each {
            fromLocations << it
        }

        // Get all transactions
        command.transactions = inventoryService.getDebitsBetweenDates(fromLocations, selectedLocations, command.fromDate, command.toDate)


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

                command.rows[product].transferOutQuantity += transactionEntry.quantity

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
            def products = command.rows.keySet().asList()
            println "Products: " + products

            command.fromLocations.each { location ->
                def onHandQuantityMap = inventoryService.getQuantityByProductMap(location.inventory, products)

                //println "onHandQuantityMap: " + onHandQuantityMap
                // For each product, add to the onhand quantity map
                products.each { product ->
                    def onHandQuantity = onHandQuantityMap[product];
                    println "onHandQuantity: " + onHandQuantity
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
            def date = new Date()
            response.setHeader("Content-disposition",
                    "attachment; filename=consumption-${date.format("yyyyMMdd-hhmmss")}.csv")
            response.contentType = "text/csv;charset=utf-8"

            //response.setHeader("Content-disposition", "attachment; filename=Consumption.csv")
            //response.contentType = "text/csv"
            def sw = new StringWriter()

            def csvWriter = new CSVWriter(sw, {
                "SKU" { it.productCode }
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

            command.rows.each { key, row ->
                def csvRow =  [
                        productCode: row.product.productCode?:'',
                        name: row.product.name,
                        category: row.product?.category?.name,
                        unitOfMeasure: row.product.unitOfMeasure?:'',
                        transferOutQuantity: g.formatNumber(number: row.transferOutQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        monthlyQuantity: g.formatNumber(number: row.monthlyQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        weeklyQuantity: g.formatNumber(number: row.weeklyQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        dailyQuantity: g.formatNumber(number: row.dailyQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        onHandQuantity: g.formatNumber(number: row.onHandQuantity, format: '###.#', maxFractionDigits: 1)?:'',
                        numberOfMonthsRemaining: g.formatNumber(number: row.numberOfMonthsRemaining, format: '###.#', maxFractionDigits: 1)?:'',
                ]
                println csvRow

                csvWriter << csvRow
            }
            println "CSV: " + sw.toString()

            render sw.toString()
            //return;
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
    List<Category> categories = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Category.class));
    List<Location> fromLocations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class));
    List<Location> toLocations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class));
    List<TransactionType> transactionTypes = []

    // Fields to allow user to choose
    List<Location> selectedLocations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class));
    List<Category> selectedCategories = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Category.class));


    // Payload
    List<Transaction> transactions = []
    List<TransactionEntry> transactionEntries = []
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


}
