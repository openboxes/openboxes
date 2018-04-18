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

import groovyx.gpars.GParsPool
import org.apache.commons.lang.StringEscapeUtils
import org.hibernate.FetchMode
import org.hibernate.classic.Session
import org.hibernate.criterion.CriteriaSpecification
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Category
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.product.Product
import org.pih.warehouse.reporting.ConsumptionFact

import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat

class ConsumptionService {

    def dataService
    def sessionFactory
    def persistenceInterceptor
    boolean transactional = false

    List refreshConsumptionData(Location location) {
        long startTime = System.currentTimeMillis()

        // Delete all consumption rows
        Integer deletedRecords = ConsumptionFact.executeUpdate("delete ConsumptionFact c where location.id = :locationId", [locationId: location?.id])
        log.info "Deleted ${deletedRecords} records in ${System.currentTimeMillis()-startTime}"

        log.info ("Processing consumption for location ${location?.name}")
        persistenceInterceptor.init()

        def transactionEntries = getTransactionEntries(location)
        if (transactionEntries) {
            log.info("Processing ${transactionEntries.size()} transactions for location ${location?.name}")
            try {
                List consumptionRecords = []

                transactionEntries.each { transactionEntry ->
                    def consumptionRecord = calculateConsumption(transactionEntry)
                    consumptionRecords.add(consumptionRecord)
                }
                long saveStartTime = System.currentTimeMillis()
                saveConsumptionRecords(consumptionRecords)
                log.info("Saved ${consumptionRecords.size()} consumption records in ${System.currentTimeMillis()-saveStartTime} ms")

                persistenceInterceptor.flush()
            } catch (Exception e) {
                log.error("Error calculating consumption data " + e.message, e)

            } finally {
                persistenceInterceptor.destroy()
            }
        }
        log.info("Refreshing consumption data took ${System.currentTimeMillis()-startTime} ms")
        return [location]
    }


    List refreshConsumptionData() {
        def locationList = []
        List<Location> locations = Location.list()
        log.info ("Calculating consumption based on ${locations.size()} transactions")
        GParsPool.withPool {
            locationList = locations.collectParallel { Location location ->
                return refreshConsumptionData(location)
            }
        }
        log.info "Calculated consumption for ${locationList?.size()} locations"

        log.info "Persisting ${locationList.size()} consumption records"


        return locationList
    }


    void saveConsumptionRecords(List consumptionRecords) {
        Session session = sessionFactory.openSession();
        org.hibernate.Transaction tx = session.beginTransaction();
        consumptionRecords.eachWithIndex { consumption, index ->
            session.save(consumption)
            // Flush and clear the session every 100 records
            if(index.mod(100)==0) {
                session.flush();
                session.clear();
            }
        }
        tx.commit();
        session.close();
    }


    List<Transaction> getTransactions(Location location) {
        def transactions = Transaction.createCriteria().list {
            transactionType {
                eq("transactionCode", TransactionCode.DEBIT)
            }
            eq("inventory", location.inventory)
        }
        return transactions;
    }

    List <TransactionEntry> getTransactionEntries(Location location) {
        def transactionEntries = TransactionEntry.createCriteria().list {
            fetchMode 'transaction', FetchMode.JOIN
            //fetchMode 'transaction.outboundTransfer', FetchMode.JOIN
            //fetchMode 'transaction.inboundTransfer', FetchMode.JOIN
            fetchMode 'inventoryItem', FetchMode.JOIN
            fetchMode 'inventoryItem.product', FetchMode.JOIN
            fetchMode 'inventoryItem.product.productGroups', FetchMode.JOIN
            transaction {
                transactionType {
                    eq("transactionCode", TransactionCode.DEBIT)
                }
                eq("inventory", location.inventory)
            }
        }
        return transactionEntries;

    }


    ConsumptionFact calculateConsumption(TransactionEntry transactionEntry) {

        if (transactionEntry) {
            Transaction transaction = transactionEntry.transaction
            def consumptionFact = new ConsumptionFact(
                    product: transactionEntry.inventoryItem.product,
                    genericProduct: transactionEntry.inventoryItem.product?.genericProduct,

                    productCode: transactionEntry.inventoryItem.product?.productCode,
                    productName: transactionEntry.inventoryItem.product?.name,

                    category: transactionEntry.inventoryItem?.product?.category,
                    categoryName: transactionEntry.inventoryItem?.product?.category?.name,
                    unitCost: transactionEntry?.inventoryItem?.product?.pricePerUnit,
                    unitPrice: 0.0,

                    inventoryItem: transactionEntry.inventoryItem,
                    lotNumber: transactionEntry?.inventoryItem?.lotNumber,
                    expirationDate: transactionEntry?.inventoryItem?.expirationDate,

                    quantityIssued: transactionEntry.quantity,
                    quantityCanceled: 0,
                    quantityDemand: 0,
                    quantityRequested: 0,
                    quantitySubstituted: 0,
                    quantityModified: 0,

                    transaction: transaction,
                    transactionNumber: transaction.transactionNumber,
                    transactionCode: transaction.transactionType.transactionCode.toString(),
                    transactionType: transaction.transactionType.name,

                    canceled: false,
                    substituted: false,
                    modified: false,

                    reasonCode: "None",

                    location: transaction.inventory.warehouse,
                    locationName: transaction.inventory.warehouse.name,
                    locationType: transaction.inventory.warehouse.locationType?.name,
                    locationGroup: transaction.inventory.warehouse.locationGroup?.name,

                    transactionDate: transaction.transactionDate,
                    week: Constants.weekFormat.format(transaction.transactionDate),
                    month: Constants.monthFormat.format(transaction.transactionDate),
                    day: Constants.dayFormat.format(transaction.transactionDate),
                    year: Constants.yearFormat.format(transaction.transactionDate),
                    monthYear: Constants.yearMonthFormat.format(transaction.transactionDate)
            )
            //consumption.save(failOnError: true)
            return consumptionFact
        }


    }


    def aggregateConsumption(Location location, Category category, Date startDate, Date endDate) {
        def results = ConsumptionFact.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                groupProperty('product', "product")
                groupProperty('productCode', "Product Code")
                groupProperty('productName', "Product Name")
                groupProperty("categoryName", "Category Name")
                groupProperty("day", "Day")
                groupProperty("week", "Week")
                groupProperty("month", "Month")
                groupProperty("year", "Year")
                sum("quantityIssued", "Issued")
            }

            if (startDate && endDate) {
                between('transactionDate', startDate, endDate)
            }
            if (category) {
                eq("categoryName", category.name)
            }
            eq("location", location)
            order("productName", "asc")
        }
        return results

    }


    def listConsumption(Location location, Category category, Date startDate, Date endDate) {

        def results = ConsumptionFact.createCriteria().list {
            //resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)

            if (startDate && endDate) {
                between('transactionDate', startDate, endDate)
            }
            if (category) {
                eq("categoryName", category.name)
            }
            eq("location", location)
            order("productName", "asc")
        }

        log.info "list consumption: " + results

        return results
    }


    def generateCrossTab(List<ConsumptionFact> consumptionFactList, Date startDate, Date endDate, String groupBy) {


        def calendar = Calendar.instance
        def dateFormat = new SimpleDateFormat("ddMMyyyy")

        def dateKeys = (startDate..endDate).collect { date ->
            calendar.setTime(date);
            [
                    date: date,
                    day: calendar.get(Calendar.DAY_OF_MONTH),
                    week: calendar.get(Calendar.WEEK_OF_YEAR),
                    month: calendar.get(Calendar.MONTH),
                    year: calendar.get(Calendar.YEAR),
                    key: dateFormat.format(date)
            ]
        }.sort { it.date }


        def daysBetween = (groupBy!="default") ? -1 : endDate - startDate
        if (daysBetween > 365 || groupBy.equals("yearly")) {
            dateFormat = yearFormat
        }
        else if ((daysBetween > 61 && daysBetween < 365) || groupBy.equals("monthly")) {
            dateFormat = yearMonthFormat
        }
        else if (daysBetween > 14 && daysBetween < 60 || groupBy.equals("weekly")) {
            dateFormat = weekFormat
        }
        else if (daysBetween > 0 && daysBetween <= 14 || groupBy.equals("daily")) {
            dateFormat = dayFormat
        }
        else {
            dateFormat = yearMonthFormat
        }
        dateKeys = dateKeys.collect { dateFormat.format(it.date) }.unique()

        log.info ("consumptionFactList: " + consumptionFactList)

        def consumptionFactMap = consumptionFactList.inject([:]) { result, consumptionFact ->
            def productId = consumptionFact["productId"]
            def transactionDate = consumptionFact["transactionDate"]
            def quantityIssued = consumptionFact["quantityIssued"]
            def dateKey = dateFormat.format(transactionDate)
            def quantityMap = result[productId]
            if (!quantityMap) {
                quantityMap = [:]
            }
            def quantity = quantityMap[dateKey]?:0
            quantity += quantityIssued
            quantityMap[dateKey] = quantity
            result[productId] = quantityMap
            result
        }
        log.info "Consumption map: " + consumptionFactMap

        def crosstabRows = []
        def products = consumptionFactList.collect { it.product }.unique()
        products.each { Product product ->
            BigDecimal totalIssued = 0
            BigDecimal totalDemand = 0
            BigDecimal totalCanceled = 0
            BigDecimal unitCost = product?.costPerUnit?:product?.pricePerUnit?:0
            Map row = [
                    "Code": product?.productCode,
                    "Name": product?.name,
                    "Tags": StringEscapeUtils.escapeCsv(product.tagsToString()),
                    "Catalogs": StringEscapeUtils.escapeCsv(product.productCatalogsToString()),
                    "Unit Cost": NumberFormat.getNumberInstance().format(unitCost)
            ]

            def consumptionAggregated = consumptionFactMap[product?.id]
            dateKeys.each { dateKey ->
                def quantityIssued = consumptionAggregated[dateKey]?:0
                totalIssued += quantityIssued
                row += [ "${dateKey}" : quantityIssued ]
            }
            BigDecimal averageIssued = totalIssued / dateKeys.size()

            row += [
                    "Total Demand": totalDemand,
                    "Total Canceled": totalCanceled,
                    "Total Issued": totalIssued,
                    "Total Cost": NumberFormat.getNumberInstance().format(totalIssued * unitCost),
                    "Average Issued": averageIssued,
                    "Average Cost": NumberFormat.getNumberInstance().format(averageIssued * unitCost)
            ]
            crosstabRows << row
        }
        log.info "crosstabRows: " + crosstabRows
        return crosstabRows
    }

}
