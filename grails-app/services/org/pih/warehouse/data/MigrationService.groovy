/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.data


import grails.validation.ValidationException
import groovy.sql.Sql
import groovyx.gpars.GParsPool
import org.hibernate.criterion.CriteriaSpecification
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyRole
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.PreferenceTypeCode
import org.pih.warehouse.core.RatingTypeCode
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptStatusCode
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode

class MigrationService {

    def dataService
    def inventoryService
    def persistenceInterceptor
    def mailService
    def dataSource
    def sessionFactory

    boolean transactional = true

    def getReceiptsWithoutTransaction() {
        return Receipt.createCriteria().list() {
            eq("receiptStatusCode", ReceiptStatusCode.RECEIVED)
            transaction {
                isNull("id")
            }
        }
    }

    def getShipmentsWithoutTransaction() {
        return Shipment.createCriteria().list() {
            not {
                'in'("currentStatus", [ShipmentStatusCode.PENDING])
            }
            outgoingTransactions {
                isNull("id")
            }
            origin {
                locationType {
                    eq("locationTypeCode", LocationTypeCode.DEPOT)
                }
            }
        }
    }


    def getCurrentInventory(List<Location> locations) {

        def currentInventory

        GParsPool.withPool {

            log.info("locations: ${locations.size()}")
            currentInventory = locations.collectParallel { Location location ->
                persistenceInterceptor.init()
                def currentInventoryMap
                try {
                    def startTime = System.currentTimeMillis()
                    Map<Product, Integer> quantityMap = inventoryService.getQuantityByProductMap(location)

                    log.info "Calculated current inventory for ${location.name} in ${(System.currentTimeMillis() - startTime)} ms"
                    currentInventoryMap = [
                            location   : location.name,
                            products   : quantityMap.keySet()?.size() ?: 0,
                            checksum   : quantityMap.values().sum(),
                            quantityMap: quantityMap
                    ]
                    persistenceInterceptor.flush()
                } catch (Exception e) {
                    log.error("Exception occurred while calculating current inventory for ${location.name}")
                } finally {
                    persistenceInterceptor.destroy()
                }
                return currentInventoryMap
            }
        }


        // Filter out locations with no inventory
        currentInventory = currentInventory.findAll { it.products > 0 }

        // Convert from a list of quantity maps to a list of tuples (location, product, quantity)
        def data = []
        currentInventory.each { result ->
            if (result) {
                result.quantityMap.keySet().collect { product ->
                    def quantity = result.quantityMap[product]
                    data << [location   : result?.location,
                             productCode: product?.productCode,
                             productName: product?.name,
                             quantity   : quantity]
                }
            }
        }

        return data
    }

    def getLocationsWithTransactions(List<TransactionCode> transactionCodes) {
        def results = TransactionEntry.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                transaction {
                    inventory {
                        warehouse {
                            groupProperty "id", "locationId"
                            groupProperty "name", "locationName"
                        }
                    }
                }
                count "id", 'transactionCount'
            }
            if (transactionCodes) {
                transaction {
                    transactionType {
                        'in'("transactionCode", transactionCodes)
                    }
                }
            }
        }

        return results
    }

    def getProductsWithTransactions(Location location, List<TransactionCode> transactionCodes) {
        return TransactionEntry.createCriteria().list {
            projections {
                inventoryItem {
                    distinct("product")
                }
            }
            transaction {
                if (transactionCodes) {
                    transactionType {
                        'in'("transactionCode", transactionCodes)
                    }
                }
                eq("inventory", location.inventory)
                order("transactionDate", "asc")
                order("dateCreated", "asc")
            }
        }
    }

    def getTransactionEntries(Location location, Product product) {
        TransactionEntry.createCriteria().list {
            //fetchMode 'transaction', FetchMode.JOIN
            inventoryItem {
                eq("product", product)
            }
            transaction {
                eq("inventory", location.inventory)
                order("transactionDate", "asc")
                order("dateCreated", "asc")
                transactionType {
                    order("transactionCode", "desc")
                }
            }
        }
    }

    /**
     * Migrate all inventory transactions across all locations.
     *
     * @return
     */
    def migrateInventoryTransactions() {
        def locationCounts = getLocationsWithTransactions([TransactionCode.INVENTORY])

        GParsPool.withPool {
            locationCounts.eachParallel {
                persistenceInterceptor.init()
                def location = Location.get(it.locationId)
                log.info("Migrating ${it.transactionCount} inventory transactions for location ${location.name}")
                migrateInventoryTransactions(location, true)
                persistenceInterceptor.flush()
                persistenceInterceptor.destroy()
            }
        }
    }


    /**
     * Migrate all inventory transactions for the given location.
     *
     * @param location
     * @param performMigration
     * @return
     */
    def migrateInventoryTransactions(Location location, boolean performMigration) {
        def products = getProductsWithTransactions(location, [TransactionCode.INVENTORY])
        migrateInventoryTransactions(location, products, performMigration)
    }

    /**
     * Migrate all inventory transactions for the given location and products.
     *
     * @param location
     * @param performMigration
     * @return
     */
    def migrateInventoryTransactions(Location location, List<Product> products, Boolean performMigration) {

        def results = products.collect { product ->

            def stockHistory = migrateInventoryTransactions(location, product, performMigration)

            return [
                    productCode : product?.productCode,
                    location    : location?.name,
                    stockHistory: stockHistory
            ]
        }
        return results
    }


    def migrateInventoryTransactions(Location location, Product product, boolean performMigration) {

        log.info("Migrating inventory transactions for product ${product.productCode} ${product.name} at location ${location.name} ${performMigration}")

        def runningBalance
        def previousTransaction
        def runningBalanceMap = [:]
        def adjustments = []

        // Keep track of changes for preview and auditing purposes
        def results = []
        results << "DATE".padRight(25) + "CODE".padRight(20) + "ITEMKEY".padRight(50) + "QTY".padRight(10) + "PRE".padRight(10) + "BAL".padRight(10) + "ADJ".padRight(10)

        // Always create a stock snapshot before modifying any transactions in order to prevent quantity on hand
        // differences for edge cases that were not addressed
        if (performMigration) {
            inventoryService.createStockSnapshot(location, product)
        }

        def transactionEntries = getTransactionEntries(location, product)
        for (transactionEntry in transactionEntries) {

            boolean sameTransaction = (previousTransaction == transactionEntry.transaction)
            if (!sameTransaction && transactionEntry.transaction.transactionType.transactionCode == TransactionCode.PRODUCT_INVENTORY) {
                runningBalanceMap = [:]
            }
            def itemKey = "${transactionEntry?.inventoryItem?.product?.productCode}:${transactionEntry?.inventoryItem?.lotNumber}:${transactionEntry?.binLocation?.name}"
            Integer runningBalanceBefore = runningBalanceMap[itemKey] ?: 0

            runningBalance = applyTransactionEntry(runningBalanceBefore, transactionEntry, sameTransaction)
            runningBalanceMap[itemKey] = runningBalance
            Integer adjustmentQuantity
            Integer oldQuantity = transactionEntry.quantity
            Integer productBalance = runningBalanceMap.values().sum()
            String comments

            if (transactionEntry?.transaction?.transactionType?.transactionCode == TransactionCode.INVENTORY) {

                adjustmentQuantity = runningBalance - runningBalanceBefore

                // Convert inventory transaction to adjustment
                comments = "Automatically converted transaction from INVENTORY ${oldQuantity} " +
                        "to ADJUSTMENT ${adjustmentQuantity} with expected BALANCE ${productBalance} on ${new Date()}"

                adjustments << [
                        transactionEntry: transactionEntry,
                        transaction     : transactionEntry.transaction,
                        quantity        : adjustmentQuantity,
                        comments        : comments
                ]
            }

            // Stock history showing each transaction applied
            results << "${transactionEntry?.transaction?.transactionDate.toString().padRight(25)}" +
                    "${transactionEntry?.transaction?.transactionType?.transactionCode.name().padRight(20)}" +
                    "${itemKey?.padRight(50)}" +
                    "${transactionEntry?.quantity?.toString().padRight(10)}" +
                    "${runningBalanceBefore.toString()?.padRight(10)}" +
                    "${runningBalance.toString()?.padRight(10)}" +
                    "${adjustmentQuantity.toString()?.padRight(10)}"

            previousTransaction = transactionEntry.transaction
        }

        if (performMigration) {
            if (adjustments) {
                def sql = new Sql(dataSource)
                def adjustmentsByTransaction = adjustments.groupBy { it?.transaction }
                sql.withBatch(1000) { statement ->
                    adjustmentsByTransaction.keySet().each { Transaction transaction ->
                        def transactionUpdate = "update transaction set transaction_type_id = '${Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID}' where id = '${transaction.id}'"
                        statement.addBatch(transactionUpdate)
                        def adjustmentsWithinTransaction = adjustmentsByTransaction[transaction]
                        adjustmentsWithinTransaction.each { adjustment ->
                            def transactionEntryUpdate =
                                    "update transaction_entry set quantity = ${adjustment.quantity}, comments = '${adjustment.comments}' where id = '${adjustment?.transactionEntry?.id}'"
                            statement.addBatch(transactionEntryUpdate)
                        }
                    }
                    statement.executeBatch()
                }
            }
        }
        return results
    }

    /**
     *
     * @param initialQuantity
     * @param transactionEntry
     * @return
     */
    def applyTransactionEntry(BigDecimal runningBalance, TransactionEntry transactionEntry, boolean sameTransaction) {
        BigDecimal balance = runningBalance

        def transactionCode = transactionEntry?.transaction?.transactionType?.transactionCode
        if (transactionCode == TransactionCode.PRODUCT_INVENTORY) {
            if (sameTransaction) {
                balance += transactionEntry.quantity
            } else {
                balance = transactionEntry.quantity
            }
        } else if (transactionCode == TransactionCode.INVENTORY) {
            balance = transactionEntry.quantity
        } else if (transactionCode == TransactionCode.DEBIT) {
            balance -= transactionEntry.quantity
        } else if (transactionCode == TransactionCode.CREDIT) {
            balance += transactionEntry.quantity
        }
        return balance
    }

    def getQuantityBeforeTransactionEntry(TransactionEntry transactionEntry) {
        def quantity = 0
        InventoryItem inventoryItem = transactionEntry.inventoryItem
        Location location = transactionEntry.transaction.inventory.warehouse
        Date date = transactionEntry.transaction.transactionDate
        def transactionEntries = inventoryService.getTransactionEntriesOnOrBeforeDate(inventoryItem, location, date)
        log.info("Transaction entries: " + transactionEntries)
        transactionEntries.remove(transactionEntry)
        quantity = inventoryService.adjustQuantity(quantity, transactionEntries)
        return quantity
    }

    def getQuantityByBinLocationsBeforeTransactionEntry(TransactionEntry transactionEntry) {
        InventoryItem inventoryItem = transactionEntry.inventoryItem
        Location location = transactionEntry.transaction.inventory.warehouse
        Date date = transactionEntry.transaction.transactionDate
        def transactionEntries = inventoryService.getTransactionEntriesOnOrBeforeDate(inventoryItem, location, date)
        transactionEntries.removeAll(transactionEntry.transaction?.transactionEntries)
        def binLocations = inventoryService.getQuantityByBinLocation(transactionEntries, true)
        binLocations = binLocations.collect { it.value }
        return binLocations
    }


    def migrateInventoryTransaction(TransactionEntry transactionEntry) {

        InventoryItem inventoryItem = transactionEntry?.inventoryItem
        Location transactionLocation = transactionEntry?.transaction?.inventory?.warehouse
        Location binLocation = transactionEntry?.binLocation
        Date transactionDate = transactionEntry.transaction?.transactionDate
        Product product = transactionEntry.inventoryItem?.product
        log.info "Process transaction entry ${transactionEntry.id} for product ${product.productCode}"

        BigDecimal quantityOnHandThen = getQuantityBeforeTransactionEntry(transactionEntry)
        def adjustmentDebit = TransactionType.load(Constants.ADJUSTMENT_DEBIT_TRANSACTION_TYPE_ID)
        def adjustmentCredit = TransactionType.load(Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID)
        BigDecimal newQuantity = transactionEntry.quantity - quantityOnHandThen
        def oldQuantity = transactionEntry?.quantity
        def adjustmentType
        def newTransactionType
        if (newQuantity >= 0) {
            newTransactionType = adjustmentCredit
            adjustmentType = "CREDIT"
        } else {
            adjustmentType = "DEBIT"
            newQuantity = -newQuantity
            newTransactionType = adjustmentDebit
        }

        def binLocations = getQuantityByBinLocationsBeforeTransactionEntry(transactionEntry)
        BigDecimal productQuantityOnHandNow = inventoryService.getQuantityOnHand(transactionLocation, product)

        return [
                locationId        : transactionLocation.id,
                locationName      : transactionLocation.name,
                productId         : inventoryItem?.product?.id,
                code              : inventoryItem.product?.productCode,
                lotNumber         : inventoryItem.lotNumber,
                binLocation       : binLocation?.name,
                transactionEntry  : transactionEntry?.id,
                transactionDate   : transactionEntry?.transaction?.transactionDate,
                transactionCode   : transactionEntry?.transaction?.transactionType?.transactionCode,
                adjustmentType    : adjustmentType,
                oldQuantity       : oldQuantity,
                quantityOnHandThen: quantityOnHandThen,
                newQuantity       : newQuantity,
                productQohNow     : productQuantityOnHandNow,
                binLocations      : binLocations
        ]


    }


    def migrateOrganizations() {
        def migratedList = []
        List suppliers = getSuppliersForMigration()
        PartyType partyType = PartyType.findByCode("ORG")
        suppliers.each { Location supplier ->
            if (!supplier.organization) {
                def organization = migrateOrganization(supplier, partyType)
                migratedList.add(organization)
            }
        }
        return migratedList
    }

    def migrationOrganizationsInParallel() {

        def migratedList = []
        List suppliers = getSuppliersForMigration()
        PartyType partyType = PartyType.findByCode("ORG")

        GParsPool.withPool {
            migratedList = suppliers.collectParallel { supplier ->
                persistenceInterceptor.init()
                supplier = Location.load(supplier.id)
                if (!supplier.organization) {
                    def organization = migrateOrganization(supplier, partyType)
                    persistenceInterceptor.flush()
                    return organization
                }
                persistenceInterceptor.destroy()
            }
        }

        log.info("migrated: ${migratedList.size()}")

        return migratedList

    }

    def migrateOrganization(Location supplier, PartyType partyType) {
        def organization = findOrCreateOrganization(supplier.name, supplier.description, partyType, [RoleType.ROLE_SUPPLIER])
        if (!organization.save(flush: true)) {
            log.info("errors: " + organization.errors)
            throw new ValidationException("Cannot create organization ${organization?.name}: ", organization.errors)
        }
        supplier.organization = organization
        if (supplier.hasErrors() || !supplier.save(flush: true)) {
            log.info("errors: " + supplier.errors)
            throw new ValidationException("Cannot migrate supplier ${supplier?.name}: ", supplier.errors)
        }
        return organization
    }


    def getSuppliersForMigration() {
        LocationType supplierType = LocationType.findByLocationTypeCode(LocationTypeCode.SUPPLIER)
        def suppliers = Location.createCriteria().list {
            eq("active", true)
            eq("locationType", supplierType)
            isNull("organization")
        }
        return suppliers
    }


    def migrateProductSuppliersInParallel() {
        def migratedList = []
        def ids = getProductsForMigration()
        GParsPool.withPool {
            migratedList = ids.collectParallel { id ->
                persistenceInterceptor.init()
                try {
                    def productSupplier = migrateProductSupplier(id)
                    persistenceInterceptor.flush()
                    return productSupplier
                } catch (Exception e) {
                    log.error("Error migrating product supplier " + e.message, e)

                } finally {
                    persistenceInterceptor.destroy()
                }
            }
        }

        log.info("migrated: ${migratedList.size()}")

        return migratedList

    }


    def migrateProductSuppliers() {
        def migratedList = []
        def ids = getProductsForMigration()
        ids.eachWithIndex { id, index ->
            def productSupplier = migrateProductSupplier(id)
            if (index % 50 == 0) {
                persistenceInterceptor.flush()
            }
            migratedList.add(productSupplier)
        }
        return migratedList
    }

    def getProductsForMigration() {

        def products = Product.createCriteria().list {
            projections {
                property("id")
            }
            or {
                isNotNull("manufacturer")
                isNotNull("manufacturerName")
                isNotNull("manufacturerCode")
                isNotNull("vendor")
                isNotNull("vendorName")
                isNotNull("vendorCode")
                isNotNull("brandName")
                isNotNull("modelNumber")
                isNotNull("upc")
                isNotNull("ndc")
            }
        }
        return products
    }


    def migrateProductSupplier(String id) {

        def now = new Date()
        Product product = Product.load(id)
        PartyType orgType = PartyType.findByCode("ORG")
        def productSupplier = new ProductSupplier()
        productSupplier.productCode = product.productCode
        productSupplier.name = product.name
        productSupplier.product = product
        productSupplier.description = product.description

        if (product.manufacturer) {
            def manufacturer = findOrCreateOrganization(product.manufacturer, null, orgType, [RoleType.ROLE_MANUFACTURER])
            productSupplier.manufacturer = manufacturer
            if (!productSupplier.manufacturer) {
                productSupplier.errors.rejectValue("manufacturer", "productSupplier.invalid.manufacturer", "Manufacturer ${product?.manufacturer} does not exist.")
            }
        }

        if (product.vendor) {
            def supplier = findOrCreateOrganization(product.vendor, null, orgType, [RoleType.ROLE_SUPPLIER])
            productSupplier.supplier = supplier
            if (!productSupplier.supplier) {
                productSupplier.errors.rejectValue("supplier", "productSupplier.invalid.supplier", "Supplier ${product?.vendor} does not exist.")
            }
        }

        productSupplier.manufacturerCode = product.manufacturerCode
        productSupplier.manufacturerName = product.manufacturerName
        productSupplier.supplierCode = product.vendorCode
        productSupplier.supplierName = product.vendorName
        productSupplier.upc = product.upc
        productSupplier.ndc = product.ndc
        productSupplier.brandName = product.brandName
        productSupplier.modelNumber = product.modelNumber
        productSupplier.ratingTypeCode = RatingTypeCode.NOT_RATED
        productSupplier.preferenceTypeCode = PreferenceTypeCode.NOT_PREFERRED

        if (!ProductSupplier.find(productSupplier)) {
            // To ensure that we can find the product supplier using the finder above we needed to
            // postponse setting of these two fields
            def index = (product.productSuppliers?.size() ?: 0) + 1
            productSupplier.code = product.productCode + "-${index}"
            productSupplier.comments = "Migrated ${now}"

            if (productSupplier.hasErrors() || !productSupplier.save()) {
                log.info("Product supplier " + productSupplier.errors)
                throw new ValidationException("Cannot migrate supplier ${productSupplier?.name}: ", productSupplier?.errors)
            }
        }
    }


    def findOrCreateOrganization(String name, String description, PartyType partyType, List roleTypes) {

        Organization organization = Organization.findByName(name)
        if (!organization) {
            organization = new Organization()
            organization.name = name
            organization.description = description
            organization.partyType = partyType
        }

        if (roleTypes) {
            roleTypes.each { roleType ->
                if (!organization.roles.find { it.roleType == roleType }) {
                    organization.addToRoles(new PartyRole(roleType: roleType))
                }
            }
        }

        if (organization.hasErrors() || !organization.save()) {
            throw new ValidationException("Validation error", organization.errors)
        }


        return organization
    }

    def deleteOrganizations() {

        int deletedCount

        LocationType supplierType = LocationType.findByLocationTypeCode(LocationTypeCode.SUPPLIER)
        def suppliers = Location.createCriteria().list {
            eq("active", true)
            eq("locationType", supplierType)
            isNotNull("organization")
        }

        if (suppliers) {
            suppliers.each { supplier ->
                supplier.organization.delete()
                supplier.organization = null
            }
            deletedCount = suppliers.size()

        } else {
            // FIXME Need to remove
            PartyRole.executeUpdate("delete from PartyRole")
            deletedCount = Organization.executeUpdate("delete from Organization")
        }

        return deletedCount

    }


    def deleteProductSuppliers() {
        def productSuppliers = ProductSupplier.createCriteria().list {
            ilike("comments", "Migrated%")
        }
        productSuppliers.each { productSupplier ->
            productSupplier.product?.removeFromProductSuppliers(productSupplier)
            productSupplier.delete()
        }

        return productSuppliers?.size()
    }
}
