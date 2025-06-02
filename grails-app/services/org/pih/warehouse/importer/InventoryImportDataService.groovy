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
import grails.validation.ValidationException
import org.apache.commons.lang.StringUtils
import org.joda.time.LocalDate

import org.pih.warehouse.DateUtil
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryImportProductInventoryTransactionService
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product

import java.text.ParseException
import java.text.SimpleDateFormat

@Transactional
class InventoryImportDataService implements ImportDataService {
    private static final SimpleDateFormat EXPIRATION_DATE_FORMAT = new SimpleDateFormat("yy-mm")

    InventoryService inventoryService
    ProductAvailabilityService productAvailabilityService
    InventoryImportProductInventoryTransactionService inventoryImportProductInventoryTransactionService
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
        InventoryImportData inventoryImportData = parseData(command)

        Date baselineTransactionDate = command.date

        // Get the stock for all items in the import at the date that the baseline transaction will be created.
        Map<String, AvailableItem> availableItems = productAvailabilityService.getAvailableItemsAtDateAsMap(
                command.location,
                inventoryImportData.products,
                baselineTransactionDate)

        String comment = "Imported from ${command.filename} on ${new Date()}"

        inventoryImportProductInventoryTransactionService.createInventoryBaselineTransactionForGivenStock(
                command.location, command, availableItems.values(), baselineTransactionDate, comment)

        // Date objects are mutable, so we use Instant to clone the date in the command and avoid directly modifying it.
        Date adjustmentTransactionDate = DateUtil.asDate(DateUtil.asInstant(baselineTransactionDate).plusSeconds(1))

        // We let the adjustment transaction be built from the same available items that we built the baseline
        // transaction with. The adjustment transaction is dated one second after the baseline transaction so it
        // could have a different stock history, but we error if there are any other transactions that exist at
        // that time, so we can guarantee that the available items will be the same for both the baseline
        // and adjustment. This avoids needing to fetch available items twice (which is slow).
        createAdjustmentTransaction(
                command.location, inventoryImportData, availableItems, adjustmentTransactionDate, comment)
    }

    /**
     * Create the adjustment transaction based on the difference between the QoH in the system at the date specified
     * by the import and the quantity specified in the import.
     */
    private Transaction createAdjustmentTransaction(Location facility,
                                                    InventoryImportData inventoryImportData,
                                                    Map<String, AvailableItem> availableItems,
                                                    Date transactionDate,
                                                    String comment) {

        // We'd have weird behaviour if we allowed two transactions to exist at the same exact time (precision at the
        // database level is to the second) so fail if there's already a transaction on the items for the given date.
        if (inventoryService.hasTransactionEntriesOnDate(facility, transactionDate, inventoryImportData.inventoryItems)) {
            throw new IllegalArgumentException("A transaction already exists at time ${transactionDate}")
        }

        // Don't bother populating the transaction's fields until we know we'll need one.
        Transaction transaction = new Transaction()

        for (entry in inventoryImportData.rows) {
            String key = entry.key
            InventoryImportDataRow row = entry.value

            // If we have no product availability record for the item, we assume that means we have no quantity.
            int quantityOnHand = availableItems.get(key)?.quantityOnHand ?: 0

            int adjustmentQuantity = row.quantity - quantityOnHand
            if (adjustmentQuantity == 0) {
                continue
            }

            TransactionEntry transactionEntry = new TransactionEntry(
                    transaction: transaction,
                    quantity: adjustmentQuantity,
                    product: row.product,
                    binLocation: row.binLocation,
                    inventoryItem: row.inventoryItem,
                    comments: row.comments,
            )
            transaction.addToTransactionEntries(transactionEntry)
        }

        // If there aren't any adjustments, don't bother creating an empty transaction.
        if (!transaction.transactionEntries) {
            return null
        }

        // Now that we know that we need a transaction, we can populate it.
        transaction.transactionDate = transactionDate
        transaction.transactionType = TransactionType.get(Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID)
        transaction.transactionNumber = inventoryService.generateTransactionNumber(transaction)
        transaction.comment = comment
        transaction.inventory = facility.inventory

        if (!transaction.save()) {
            throw new ValidationException("Invalid transaction", transaction.errors)
        }

        return transaction
    }

    /**
     * Construct an InventoryImportData object from the given ImportDataCommand.
     */
    private InventoryImportData parseData(ImportDataCommand command) {
        InventoryImportData inventoryImportData = new InventoryImportData()
        for (def row in command.data) {
            // We've already warned the user during the validation step about any rows with a null QoH, so if there
            // are any that are still null at this point, simply ignore them.
            if (row.quantity == null) {
                continue
            }

            Product product = parseProduct(row.productCode as String)
            InventoryItem inventoryItem = parseInventoryItem(product, row.lotNumber, row.expirationDate)
            Location binLocation = parseBinLocation(row.binLocation as String, command.location)

            inventoryImportData.put(binLocation, inventoryItem, row)
        }
        return inventoryImportData
    }

    private Product parseProduct(String productCode) {
        Product product = Product.findByProductCode(productCode)
        assert product != null
        return product
    }

    private InventoryItem parseInventoryItem(Product product, def lotNumberRaw, def expirationDateRaw) {
        String lotNumber = lotNumberRaw instanceof Double ? lotNumberRaw.toInteger() : lotNumberRaw

        Date expirationDate = null
        if (expirationDateRaw instanceof String) {
            expirationDate = EXPIRATION_DATE_FORMAT.parse(expirationDateRaw)
            Calendar calendar = Calendar.getInstance()
            calendar.setTime(expirationDate)
            expirationDate = calendar.getTime()
        } else if (expirationDateRaw instanceof Date) {
            expirationDate = expirationDateRaw
        } else if (expirationDateRaw instanceof LocalDate) {
            expirationDate = expirationDateRaw.toDate()
        }

        return inventoryService.findAndUpdateOrCreateInventoryItem(product, lotNumber, expirationDate)
    }

    private Location parseBinLocation(String binLocationName, Location parentLocation) {
        if (binLocationName == null) {
            return null
        }

        Location binLocation = Location.findByNameAndParentLocation(binLocationName, parentLocation)
        assert binLocation != null
        return binLocation
    }

    /**
     * Convenience POJO for holding the results of the inventory import.
     */
    private class InventoryImportData {
        Map<String, InventoryImportDataRow> rows = [:]
        Set<Product> products = []

        void put(Location binLocation, InventoryItem inventoryItem, def rowRaw) {
            products.add(inventoryItem.product)

            String key = ProductAvailabilityService.constructAvailableItemKey(binLocation, inventoryItem)
            rows.put(key, new InventoryImportDataRow(
                    binLocation,
                    inventoryItem,
                    rowRaw.quantity.toInteger() as Integer,
                    rowRaw.comments as String))
        }

        InventoryImportDataRow get(Location binLocation, InventoryItem inventoryItem) {
            String key = ProductAvailabilityService.constructAvailableItemKey(binLocation, inventoryItem)
            return rows.get(key)
        }

        List<InventoryItem> getInventoryItems() {
            return rows.values().inventoryItem
        }
    }

    /**
     * Convenience POJO for holding the results of an individual inventory import row/entry.
     */
    private class InventoryImportDataRow {
        Product product
        Location binLocation
        InventoryItem inventoryItem
        Integer quantity
        String comments

        InventoryImportDataRow(Location binLocation, InventoryItem inventoryItem, Integer quantity, String comments) {
            this.product = inventoryItem.product
            this.binLocation = binLocation
            this.inventoryItem = inventoryItem
            this.quantity = quantity
            this.comments = comments
        }
    }
}
