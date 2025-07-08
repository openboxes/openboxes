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

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import groovy.sql.Sql
import org.hibernate.Criteria
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType
import org.pih.warehouse.DateUtil
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyRole
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.RatingTypeCode
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.ProductInventoryTransactionMigrationService
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

@Transactional
class MigrationService {

    def dataService
    def gparsService
    def inventoryService
    ProductAvailabilityService productAvailabilityService
    ProductInventoryTransactionMigrationService productInventoryTransactionMigrationService
    def persistenceInterceptor
    def dataSource

    def getStockMovementsWithoutShipmentItems() {
        String query = """
            select 
                requisition.id, 
                requisition.status,
                requisition.request_number, 
                requisition.date_created, 
                location.name as origin, 
                count(distinct(requisition_item.id)) as requested,
                count(distinct(picklist_item.id)) as picked,
                count(distinct(shipment_item.id)) as shipped,
                count(distinct(transaction_entry.id)) as issued
            from requisition 
            join requisition_item on requisition_item.requisition_id = requisition.id
            join location on location.id = requisition.origin_id
            join location_type on location_type.id = location.location_type_id
            join picklist_item on picklist_item.requisition_item_id = requisition_item.id
            left outer join shipment on shipment.requisition_id = requisition.id
            left outer join shipment_item on shipment_item.shipment_id = shipment.id
            left outer join transaction on transaction.outgoing_shipment_id = shipment.id
            left outer join transaction_entry on transaction_entry.transaction_id = transaction.id
            where shipment_item.id is null 
            and location_type.location_type_code = 'DEPOT'
            and requisition.status = 'ISSUED'
            and picklist_item.quantity > 0
            group by requisition.id, requisition.request_number, requisition.date_created, location.name
            order by requisition.date_created desc;"""
        return dataService.executeQuery(query)

    }


    def getReceiptsWithoutTransaction() {
        return Receipt.createCriteria().list() {
            eq("receiptStatusCode", ReceiptStatusCode.RECEIVED)
            transaction(JoinType.LEFT_OUTER_JOIN.joinTypeValue) {
                isNull("id")
            }
        }
    }

    def getShipmentsWithoutTransactions() {
        return Shipment.createCriteria().list() {
            not {
                'in'("currentStatus", [ShipmentStatusCode.PENDING])
            }
            outgoingTransactions(JoinType.LEFT_OUTER_JOIN.joinTypeValue) {
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

        gparsService.withPool('CurrentInventory') {

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

    def getLocationsWithTransaction(TransactionType transactionType) {
        return Transaction.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                inventory {
                    warehouse {
                        groupProperty "id", "Location Id"
                        groupProperty "name", "Location Name"
                        groupProperty "active", "Location Active"
                    }
                }
                count "id", "Transaction Count"
            }
            eq("transactionType", transactionType)
        }
    }

    def getProductsWithTransactions(Location location, List<TransactionCode> transactionCodes) {
        return TransactionEntry.createCriteria().list {
            projections {
                inventoryItem {
                    property("product", "product")
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
            resultTransformer Criteria.DISTINCT_ROOT_ENTITY
        }
    }

    List<Product> getProductsWithTransactions(Location location, TransactionType transactionType) {
        return TransactionEntry.createCriteria().list {
            projections {
                inventoryItem {
                    property("product", "product")
                }
            }
            transaction {
                eq("transactionType", transactionType)
                eq("inventory", location.inventory)
                order("transactionDate", "asc")
            }
            resultTransformer Criteria.DISTINCT_ROOT_ENTITY
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

        gparsService.withPool('MigrateInventoryTransactions') {
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

    Map migrateProductInventoryTransactions(Location location, boolean performMigration) {
        log.info("Migrating Product inventory transactions at location ${location.name} ${performMigration}")

        // 1. Find all transactions with the old Product Inventory type (by PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)
        TransactionType oldProductInventoryType = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)
        List transactions = Transaction.findAllByInventoryAndTransactionType(location.inventory, oldProductInventoryType)?.sort { it.transactionDate}


        Map<String, List<String>> results = [:]
        recordMigrationResult(results, transactions, true)

        if (performMigration && transactions) {
            List<Product> products = transactions.transactionEntries?.flatten()?.collect { TransactionEntry te -> te.inventoryItem.product }?.unique()
            // Always create a stock snapshot before modifying any transactions in order to prevent quantity on hand
            // differences for edge cases that were not addressed
            log.debug("Creating inventory baseline for current stock for products found in old transactions")
            Transaction currentInventoryBaseline = productInventoryTransactionMigrationService.createInventoryBaselineTransaction(
                    location,
                    null,
                    products,
                    null,
                    "Inventory baseline created during old product inventory transactions migration",
                    null,
                    true,
                    true
            )

            transactions.each { Transaction it ->
                log.debug "Migrating transaction ${it.transactionNumber} with ${it.transactionEntries.size()} transaction entries"
                List<TransactionEntry> entries = it.transactionEntries
                // 2. Find all products in entries (and create inventory baseline for these)
                List<Product> currentTransactionProducts = entries?.collect { TransactionEntry te -> te.inventoryItem.product }?.unique()

                // 3. Create inventory baseline for old transaction that is being migrated (if enabled)
                Map<String, AvailableItem> availableItems = productAvailabilityService.getAvailableItemsAtDateAsMap(
                        location, currentTransactionProducts, it.transactionDate)

                Transaction baselineTransaction = productInventoryTransactionMigrationService.createInventoryBaselineTransactionForGivenStock(
                        location,
                        null,
                        availableItems.values(),
                        it.transactionDate,
                        "Migrated from single Product Inventory transaction to Baseline + Adjustment pair",
                        null,
                        // don't validate transaction date, there is old transaction at the same time, that will be removed
                        false,
                        true
                )
                if (baselineTransaction) {
                    baselineTransaction.disableRefresh = true
                    // Ugly workaround to omit auto-timestamping dateCreated
                    // Needs to flush it first to be able to overwrite dateCreated
                    baselineTransaction.save(flush: true)
                    // Unfortunately, we need to use raw SQL to update the dateCreated field (otherwise it will be
                    // auto-timestamped)
                    def sql = new Sql(dataSource)
                    sql.executeUpdate(
                            """UPDATE transaction set date_created = ? WHERE id = ?""",
                            [it.dateCreated, baselineTransaction.id]
                    )

                    recordMigrationResult(results, [baselineTransaction])
                }

                // 4. Create adjustment transactions for the old transaction that is being migrated
                // Date objects are mutable, so we use Instant to clone the date in the command and avoid directly modifying it.
                Date adjustmentTransactionDate = DateUtil.asDate(DateUtil.asInstant(it.transactionDate).plusSeconds(1))
                Transaction adjustmentTransaction = createAdjustmentTransaction(
                        location,
                        it.transactionEntries,
                        availableItems,
                        adjustmentTransactionDate,
                        "Migrated from single Product Inventory transaction to Baseline + Adjustment pair"
                )
                // Ugly workaround to omit auto-timestamping dateCreated
                if (adjustmentTransaction?.id) {
                    Date adjustmentCreatedDate = DateUtil.asDate(DateUtil.asInstant(it.dateCreated).plusSeconds(1))
                    // Unfortunately, we need to use raw SQL to update the dateCreated field (otherwise it will be
                    // auto-timestamped)
                    def sql = new Sql(dataSource)
                    sql.executeUpdate(
                            """UPDATE transaction set date_created = ? WHERE id = ?""",
                            [adjustmentCreatedDate, adjustmentTransaction.id]
                    )

                    recordMigrationResult(results, [adjustmentTransaction])
                }

                // 5. Delete the old transaction (with flush, to get rid of that transaction for next calculations)
                it.disableRefresh = true
                it.delete(flush: true, failOnError: true)
            }

            // Zero out stock for products that were migrated but had no stock initially (hence hand no baseline created)
            List<Product> shouldBeZeroedOut = products - currentInventoryBaseline?.transactionEntries?.collect { it.inventoryItem.product }?.unique()
            if (shouldBeZeroedOut) {
                log.debug("Check if there is need for 'zeroing out' adjustments for products that had no stock before migration")

                Map<String, AvailableItem> availableItems = productAvailabilityService.getAvailableItemsAtDateAsMap(
                        location,
                        shouldBeZeroedOut
                )

                createCleanupAdjustment(
                        location,
                        availableItems,
                        "After migration from single Product Inventory transaction to Baseline + Adjustment pair " +
                                "this transaction is created to ensure that products that had no stock before, " +
                                "are currently zeroed out too"
                )
            }

            // Refresh PA for all products that were migrated
            productAvailabilityService.triggerRefreshProductAvailability(location.id, products?.id, true)
        }

        return [
                "Location": location?.name,
                "Migration Results": results
        ]
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

        gparsService.withPool('MigrateOrganizations') {
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
        gparsService.withPool('MigrateProductSuppliers') {
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

    /**
     * Create the adjustment transaction based on the difference between the QoH in the system at the date specified
     * by the old product inventory transaction that is being migrated
     */
    private Transaction createAdjustmentTransaction(Location facility,
                                                    List<TransactionEntry> transactionEntries,
                                                    Map<String, AvailableItem> availableItems,
                                                    Date transactionDate,
                                                    String comment) {
        // Don't bother populating the transaction's fields until we know we'll need one.
        Transaction transaction = new Transaction()

        transactionEntries?.each {  TransactionEntry entry ->
            // Assuming there is no duplicated product-lot-bin combination in the transaction entries
            // If there are, it need to be done similarly like in the Inventory Import
            String key = productAvailabilityService.constructAvailableItemKey(entry.binLocation, entry.inventoryItem)
            int quantityOnHand = availableItems.get(key)?.quantityOnHand ?: 0
            int adjustmentQuantity = entry.quantity - quantityOnHand
            if (adjustmentQuantity == 0) {
                return
            }

            TransactionEntry transactionEntry = new TransactionEntry(
                    transaction: transaction,
                    quantity: adjustmentQuantity,
                    product: entry.product,
                    binLocation: entry.binLocation,
                    inventoryItem: entry.inventoryItem,
                    comments: entry.comments
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
        transaction.disableRefresh = true

        // Needs to be flushed here already, to be able to swap date created on that entry
        if (!transaction.save(flush: true, failOnError: true)) {
            throw new ValidationException("Invalid transaction", transaction.errors)
        }

        return transaction
    }

    /**
     * Create the adjustment transaction that zeroes out the quantity on hand for products that had no stock before
     * the migration process and somehow it's not empty (it might happen due to the backdated transecations)
     */
    private Transaction createCleanupAdjustment(Location facility,
                                                Map<String, AvailableItem> availableItems,
                                                String comment) {
        // Don't bother populating the transaction's fields until we know we'll need one.
        Transaction transaction = new Transaction()

        if (availableItems) {
            availableItems.each { String key, AvailableItem it ->
                int quantityOnHand = it?.quantityOnHand ?: 0
                // If there is already a item with qoh zero, we can skip it
                if (quantityOnHand == 0) {
                    return
                }

                TransactionEntry transactionEntry = new TransactionEntry(
                        transaction: transaction,
                        quantity: -quantityOnHand,
                        product: it.inventoryItem.product,
                        binLocation: it.binLocation,
                        inventoryItem: it.inventoryItem
                )
                transaction.addToTransactionEntries(transactionEntry)
            }
        }

        // If there aren't any adjustments, don't bother creating an empty transaction.
        if (!transaction.transactionEntries) {
            return null
        }

        // Now that we know that we need a transaction, we can populate it.
        transaction.transactionDate = new Date()
        transaction.transactionType = TransactionType.get(Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID)
        transaction.transactionNumber = inventoryService.generateTransactionNumber(transaction)
        transaction.comment = comment
        transaction.inventory = facility.inventory
        transaction.disableRefresh = true

        if (!transaction.save()) {
            throw new ValidationException("Invalid transaction", transaction.errors)
        }

        return transaction
    }

    private static void recordMigrationResult(Map migrationResults, List<Transaction> transactions, Boolean before = false) {
        transactions.each {Transaction t ->
            t.transactionEntries.each { TransactionEntry te ->
                if (!migrationResults.containsKey(te.inventoryItem.product.productCode)) {
                    migrationResults[te.inventoryItem.product.productCode] = [
                            "Status".padRight(10) + "Transaction Number".padRight(25) + "Transaction date".padRight(25) + "CODE".padRight(20) + "ITEMKEY".padRight(60) + "QTY".padRight(10)
                    ]
                }
                migrationResults[te.inventoryItem.product.productCode] << "${before ? "BEFORE" : "AFTER"}".padRight(10) +
                        "${t.transactionNumber.padRight(25)}" +
                        "${DateUtil.asDateTimeForDisplay(t.transactionDate).padRight(25)}" +
                        "${t.transactionType.transactionCode.name().padRight(20)}" +
                        "${te.inventoryItem.product.productCode}:${te.inventoryItem.lotNumber}:${te.binLocation?.name}".padRight(60) +
                        "${te.quantity.toString().padRight(10)}"
            }
        }
    }
}
