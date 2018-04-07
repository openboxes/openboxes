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
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product
import org.pih.warehouse.reporting.Consumption

class ConsumptionService {

    def persistenceInterceptor

    boolean transactional = true

    List<String> refreshConsumptionData(Location location) {
        // Delete all consumption rows
        Integer deletedRecords = Consumption.executeUpdate("delete Consumption c where location.id = :locationId", [locationId: location?.id])

        log.info "Deleted ${deletedRecords} records"

        def consumptionList = []
        //List<String> ids = getTransactionEntries(location)
        //List<String> ids = getTransactions(location)
        List<Transaction> transactions = getTransactions(location)


        log.info ("Calculating consumption based on ${transactions.size()} transactions")
        GParsPool.withPool {
            consumptionList = transactions.collectParallel { transaction ->
                //log.info ("Calcuating consumption for transaction ${transaction?.id}")
                persistenceInterceptor.init()
                try {
                    //Transaction transaction = Transaction.findById(id, [fetch: [outboundTransfer: 'eager', inboundTransfer: 'eager', transactionEntries: 'eager']])
                    if (transaction) {
                        transaction.transactionEntries.each { transactionEntry ->
                            return calculateConsumption(transactionEntry)
                        }
                    }
                    persistenceInterceptor.flush()
                } catch (Exception e) {
                    log.error("Error calculating consumption data " + e.message, e)

                } finally {
                    persistenceInterceptor.destroy()
                }
            }
        }
        log.info "Calculated consumption for ${consumptionList?.size()} transactions"
        return consumptionList
    }


//    List<String> getTransactionEntries(Location location) {
//
//        def transactionIds = TransactionEntry.createCriteria().listDistinct {
//            projections {
//                property("id")
//            }
//            transaction {
//                transactionType {
//                    eq("transactionCode", TransactionCode.DEBIT)
//                }
//                eq("inventory", location.inventory)
//            }
//
//            maxResults(100)
//        }
//        return transactionIds;
//    }

//    List<String> getTransactions(Location location) {
//
//        def transactionIds = Transaction.createCriteria().listDistinct {
//            projections {
//                property("id")
//            }
//            transactionType {
//                eq("transactionCode", TransactionCode.DEBIT)
//            }
//            eq("inventory", location.inventory)
//            //maxResults(1000)
//        }
//        return transactionIds;
//    }

    List<Transaction> getTransactionEntries(Location location) {
        def transactions = TransactionEntry.createCriteria().list([fetch: [outboundTransfer: 'eager', inboundTransfer: 'eager', transactionEntries: 'eager']]) {
            transactionType {
                eq("transactionCode", TransactionCode.DEBIT)
            }
            eq("inventory", location.inventory)
            maxResults(100)
        }
        return transactions;
    }



    Consumption calculateConsumption(TransactionEntry transactionEntry) {

        //TransactionEntry transactionEntry = TransactionEntry.get(id)

        if (transactionEntry) {
            Transaction transaction = transactionEntry.transaction
            def consumption = [:]
//            def consumption = [
//                    product: transactionEntry.inventoryItem.product,
//                    productCode: transactionEntry.inventoryItem.product?.productCode,
//                    productName: transactionEntry.inventoryItem.product?.name,
//                    categoryName: transactionEntry.inventoryItem?.product?.category?.name,
//
//                    inventoryItem: transactionEntry.inventoryItem,
//                    lotNumber: transactionEntry?.inventoryItem?.lotNumber,
//                    expirationDate: transactionEntry?.inventoryItem?.expirationDate,
//
//                    quantityIssued: transactionEntry.quantity,
//                    quantityCanceled: 0,
//                    quantityDemand: 0,
//                    quantityRequested: 0,
//                    quantitySubstituted: 0,
//                    quantityModified: 0,
//
//                    transaction: transaction,
//                    transactionNumber: transaction.transactionNumber,
//                    transactionDate: transaction.transactionDate,
//                    transactionCode: transaction.transactionType.transactionCode.toString(),
//                    transactionType: transaction.transactionType.name,
//
//                    canceled: false,
//                    substituted: false,
//                    modified: false,
//
//                    reasonCode: "None",
//
//                    location: transaction.inventory.warehouse,
//                    locationName: transaction.inventory.warehouse.name,
//                    locationType: transaction.inventory.warehouse.locationType?.name,
//                    locationGroup: transaction.inventory.warehouse.locationGroup?.name,
//
//                    month: transaction.transactionDate.month,
//                    day: transaction.transactionDate.day,
//                    year: transaction.transactionDate.year + 1900
//            ]

            //consumption.save(failOnError: true)
            return consumption
        }


    }

    /**
     *
     * @return
     */
    def getConsumptionTransactionsBetween(Location location, Date startDate, Date endDate) {
        log.info("location " + location + "startDate = " + startDate + " endDate = " + endDate)
        def criteria = Consumption.createCriteria()
        def results = criteria.list {
            if (startDate && endDate) {
                between('transactionDate', startDate, endDate)
            }
            eq("location", location)
        }

        return results
    }

    /**
     *
     * @return
     */
    def getConsumptions(Location location, Date startDate, Date endDate, String groupBy) {
        log.debug("startDate = " + startDate + " endDate = " + endDate)
        def criteria = Consumption.createCriteria()
        def results = criteria.list {
            if (startDate && endDate) {
                between('transactionDate', startDate, endDate)
            }
            eq("location", location)
            projections {
                sum('quantityIssued')
                groupProperty('product')
                groupProperty('transactionDate')
            }
        }

        return results
    }

}
