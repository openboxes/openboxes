/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.importer

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import org.apache.commons.lang.StringUtils
import org.joda.time.LocalDate
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product

import java.text.ParseException
import java.text.SimpleDateFormat

@Transactional
class InventoryImportDataService implements ImportDataService {
    InventoryService inventoryService
    GrailsApplication grailsApplication

    @Override
    void validateData(ImportDataCommand command) {
        def dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
        def calendar = Calendar.getInstance()
        command.data.eachWithIndex { row, index ->
            def rowIndex = index + 1

            if (!command.warnings[index]) {
                command.warnings[index] = []
            }

            def product = Product.findByProductCode(row.productCode)
            if (!product) {
                command.errors.reject("error.product.notExists", "Row ${rowIndex}: Product '${row.productCode}' does not exist")
                command.warnings[index] << "Product '${row.productCode}' does not exist"
            } else {
                def lotNumber = row.lotNumber
                if (lotNumber instanceof Double) {
                    command.warnings[index] << "Lot number '${lotNumber}' must be a string"
                    row.lotNumber = lotNumber.toInteger().toString()
                }

                row.isNewItem = false
                def inventoryItem = InventoryItem.findByProductAndLotNumber(product, lotNumber)
                if (!inventoryItem) {
                    row.isNewItem = true
                    command.warnings[index] << "Inventory item for lot number '${lotNumber}' does not exist and will be created"
                }

                Location location = inventoryService.getCurrentLocation()
                if (row.binLocation == 'Default') {
                    row.binLocation = null
                }
                def binLocation = Location.findByParentLocationAndName(location, row.binLocation)
                if (!binLocation && row.binLocation) {
                    command.errors.reject("error.product.notExists", "Row ${rowIndex}: Bin location '${row.binLocation.trim()}' does not exist in this depot")
                    command.warnings[index] << "Bin location '${row.binLocation.trim()}' does not exist in this depot"
                }

                if (row.expirationDate && !row.lotNumber) {
                    command.errors.reject("error.lotNumber.notExists", "Row ${rowIndex}: Items with an expiry date must also have a lot number")
                }

                if (product.lotAndExpiryControl && (!row.expirationDate || !row.lotNumber)) {
                    command.errors.reject(
                            "error.lotAndExpiryControl.required",
                            "Row ${rowIndex}: Both lot number and expiry date are required for the '${product.productCode} ${product.name}' product."
                    )
                }

                def expirationDate = null
                try {
                    if (row.expirationDate) {
                        if (row.expirationDate instanceof String) {
                            expirationDate = dateFormatter.parse(row.expirationDate)
                            calendar.setTime(expirationDate)
                            expirationDate = calendar.getTime()
                        } else if (row.expirationDate instanceof Date) {
                            expirationDate = row.expirationDate
                        } else if (row.expirationDate instanceof LocalDate) {
                            expirationDate = row.expirationDate.toDate()
                        } else {
                            expirationDate = row.expirationDate
                            command.warnings[index] << "Expiration date '${row.expirationDate}' has unknown format ${row?.expirationDate?.class}"
                        }
                        // Minimum date is either configured or we use the epoch date
                        Date minExpirationDate = grailsApplication.config.getProperty("openboxes.expirationDate.minValue", Date.class, new Date(0L))
                        if (minExpirationDate > expirationDate) {
                            command.errors.reject("Expiration date for item ${row.productCode} is not valid. Please enter a date after ${minExpirationDate.getYear() + 1900}.")
                        }

                        row.isNewExpirationDate = inventoryItem?.expirationDate && expirationDate != inventoryItem.expirationDate

                        if (expirationDate <= new Date()) {
                            command.warnings[index] << "Expiration date '${row.expirationDate}' is not valid"
                        }

                    }
                } catch (ParseException e) {
                    command.errors.reject("error.expirationDate.invalid", "Row ${rowIndex}: Product '${row.productCode}' must have a valid date (or no date)")
                }


                def levenshteinDistance = StringUtils.getLevenshteinDistance(product.name, row.product)
                if (row.product && levenshteinDistance > 0) {
                    command.warnings[index] << "Product name [${row.product}] does not appear to be the same as in the database [${product.name}] (Levenshtein distance: ${levenshteinDistance})"
                }

            }

            if (row.quantity && (row.quantity as int) < 0) {
                command.errors.reject("error.quantity.negative", "Row ${rowIndex}: Product '${row.productCode}' must have positive quantity")
            }
        }
    }

    @Override
    void importData(ImportDataCommand command) {
        def dateFormatter = new SimpleDateFormat("yy-mm")

        def transaction = new Transaction()
        transaction.transactionDate = command.date
        transaction.transactionType = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)
        transaction.transactionNumber = inventoryService.generateTransactionNumber()
        transaction.comment = "Imported from ${command.filename} on ${new Date()}"
        transaction.inventory = command.location.inventory

        def calendar = Calendar.getInstance()
        command.data.eachWithIndex { row, index ->
            println "${index}: ${row}"
            // ignore a line if physical qoh is empty
            if (row.quantity == null) {
                return
            }
            def transactionEntry = new TransactionEntry()
            transactionEntry.quantity = row.quantity.toInteger()
            transactionEntry.comments = row.comments

            // Find an existing product, should fail if not found
            def product = Product.findByProductCode(row.productCode)
            assert product != null

            // Check the Levenshtein distance between the given name and stored product name (make sure they're close)
            println "Levenshtein distance: " + StringUtils.getLevenshteinDistance(product.name, row.product)

            // Handler for the lot number
            def lotNumber = row.lotNumber
            if (lotNumber instanceof Double) {
                lotNumber = lotNumber.toInteger().toString()
            }
            println "Lot Number: " + lotNumber

            // Expiration date should be the last day of the month
            def expirationDate = null
            if (row.expirationDate instanceof String) {
                expirationDate = dateFormatter.parse(row.expirationDate)
                calendar.setTime(expirationDate)
                expirationDate = calendar.getTime()
            } else if (row.expirationDate instanceof Date) {
                expirationDate = row.expirationDate
            } else if (row.expirationDate instanceof LocalDate) {
                expirationDate = row.expirationDate.toDate()
            } else {
                expirationDate = row.expirationDate
            }

            // Find or create an inventory item
            def inventoryItem = inventoryService.findAndUpdateOrCreateInventoryItem(product, lotNumber, expirationDate)
            println "Inventory item: " + inventoryItem.id + " " + inventoryItem.dateCreated + " " + inventoryItem.lastUpdated
            transactionEntry.inventoryItem = inventoryItem

            // Find the bin location
            if (row.binLocation) {
                def binLocation = Location.findByNameAndParentLocation(row.binLocation, command.location)
                log.info "Bin location: " + row.binLocation
                log.info "Location: " + command.location
                assert binLocation != null
                transactionEntry.binLocation = binLocation
            }

            transaction.addToTransactionEntries(transactionEntry)
        }
        // Force refresh of inventory snapshot table
        transaction.forceRefresh = Boolean.TRUE
        transaction.save(flush: true, failOnError: true)
        log.info "Transaction ${transaction?.transactionNumber} saved successfully! "
        log.info "Added ${transaction?.transactionEntries?.size()} transaction entries"
    }
}
