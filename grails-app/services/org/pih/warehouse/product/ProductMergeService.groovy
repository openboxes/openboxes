/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.product

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Constants
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.ShipmentItem

class ProductMergeService {

    def inventoryService
    def picklistService
    def productAvailabilityService

    /**
     * Preforms product swapping for a product pairs passed as params
     * */
    def mergeProduct(Product primary, Product obsolete) {
        /**
         * ================================
         * === Swap Product's relations ===
         * ================================
         *
         * === "Easy" relations ===
         * - ProductSupplier - Swap product if this supplier does not exist for primary (check product and code pair)
         * - ProductComponent (additionally, not mentioned in the ticket)
         *
         * === "Complex" relations ===
         * - Transaction (with PRODUCT_INVENTORY and INVENTORY types)
         * - InventoryItem <-- find inventory items and replace product only for InventoryItems with lotNumber that are not exisitng for primary product
         *                     But if we're gonna use the InventoryItem from primary Product, then obsoleted InventoryItem relations should be updated accordingly.
         *                     (ShipmentItems, TransactionEntries and so on)
         * - InventorySnapshot <-- Adjusted during InventoryItems management for specific inventory items and products
         * - ProductAvailability <-- Adjusted during InventoryItems management for specific inventory items and products
         * - TransactionEntry
         *
         * - RequisitionItem
         * - ShipmentItem
         * - OrderItem
         * - ReceiptItem
         *
         * === Ignored relations (see: https://pihemr.atlassian.net/browse/OBPIH-3187 description) ===
         * - Document
         * - ProductPackage
         * - ProductAssociation
         * - ProductAttribute
         * - ProductCatalogItem (Catalogs)
         * - Tag
         * - InventoryLevel
         * - Synonyms
         * - ProductSummary (additionally, not mentioned in the ticket)
         * - Category (additionally, not mentioned in the ticket)
         * - ProductGroup (additionally, not mentioned in the ticket)
         *
         * === Post processing ===
         * - Mark obsolete product as inactive
         *
         * === Tables potentially requiring refresh ===
         * - StockoutData - skipped for now
         * - DemandData - skipped for now
         * - OrderSummary - product merge should not affect the order summary data since it is not product bound
         *
         * */

        /**
         * SWAP TRIVIAL RELATIONS
         * */

        log.info "Merging ${obsolete.productCode} product into ${primary.productCode}"

        // 1. ProductSupplier
        List<ProductSupplier> obsoletedSuppliers = ProductSupplier.findAllByProduct(obsolete)
        log.info "Moving ${obsoletedSuppliers?.size() ?: 0} Product Suppliers"
        obsoletedSuppliers?.each { ProductSupplier obsoletedSupplier ->
            // Find if primary product has already the same supplier
            ProductSupplier primarySupplier = ProductSupplier.findByProductAndCode(primary, obsoletedSupplier.code)

            if (primarySupplier) {
                // if product supplier already exists, then do nothing with it
                return
            }

            logProductMergeData(primary, obsolete, obsoletedSupplier)

            // Swap assemblyProduct to primary
            obsoletedSupplier.product = primary
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoletedSupplier.save(flush: true)
        }

        // 2. ProductComponent <-- swap only components that does not exist for primary product
        List<ProductComponent> obsoletedComponents = ProductComponent.findAllByAssemblyProduct(obsolete)
        log.info "Moving ${obsoletedComponents?.size() ?: 0} Product Components"
        obsoletedComponents?.each { ProductComponent obsoletedComponent ->
            // Find if primary product has already the same component
            List<ProductComponent> primaryComponents = ProductComponent.findAllByAssemblyProductAndComponentProduct(
                primary, obsoletedComponent.componentProduct
            )

            if (primaryComponents) {
                // if product component already exists, then do nothing with it
                return
            }

            logProductMergeData(primary, obsolete, obsoletedComponent)

            // Swap assemblyProduct to primary
            obsoletedComponent.assemblyProduct = primary
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoletedComponent.save(flush: true)
        }

        /**
         * TRANSACTIONS, INVENTORY ITEMS AND OTHER "ITEM" RELATIONS
         * */

        // 1. Create a new common transaction that will contain current stock of both products. This is required
        //    for inventories that have transaction entries for bot products. If this transaction won't be created,
        //    then current stock for primary after merge might not be a proper sum of current stock of obsolete
        //    and primary before merge action.

        // 1.1. Get each inventory of transaction entries for obsolete product
        List<Inventory> obsoleteInventories = inventoryService.getInventoriesWithTransactionsByProduct(obsolete)
        // 1.2. Get each inventory of transaction entries for primary product
        List<Inventory> primaryInventories = inventoryService.getInventoriesWithTransactionsByProduct(primary)
        // 1.3. For each intersecting inventory create a new transaction
        List<Inventory> intersectingInventories = obsoleteInventories.intersect(primaryInventories)
        log.info "Creating transactions for ${intersectingInventories?.size() ?: 0} intersecting inventories"
        intersectingInventories?.each { Inventory inventory ->
            List<TransactionEntry> transactionEntries = []

            // a. Calculate current stock of obsolete and primary
            List<TransactionEntry> obsoleteTransactionEntries = inventoryService.getTransactionEntriesByInventoryAndProduct(inventory, [obsolete])
            def obsoleteQuantityAvailableInventoryItemMap = inventoryService.getQuantityAvailableByProductAndInventoryItemMap(obsolete, inventory.warehouse)
            def obsoleteAvailableItems = inventoryService.getAvailableItems(
                obsoleteTransactionEntries,
                obsoleteQuantityAvailableInventoryItemMap
            )
            obsoleteAvailableItems?.each { AvailableItem it ->
                transactionEntries << new TransactionEntry(
                    quantity: (it.quantityOnHand as Integer),
                    product: obsolete,
                    inventoryItem: it.inventoryItem,
                    binLocation: it.binLocation
                )
            }

            List<TransactionEntry> primaryTransactionEntries = inventoryService.getTransactionEntriesByInventoryAndProduct(inventory, [primary])
            def primaryQuantityAvailableInventoryItemMap = inventoryService.getQuantityAvailableByProductAndInventoryItemMap(primary, inventory.warehouse)
            def primaryAvailableItems = inventoryService.getAvailableItems(
                primaryTransactionEntries,
                primaryQuantityAvailableInventoryItemMap
            )
            primaryAvailableItems?.each { AvailableItem it ->
                transactionEntries << new TransactionEntry(
                    quantity: (it.quantityOnHand as Integer),
                    product: obsolete,
                    inventoryItem: it.inventoryItem,
                    binLocation: it.binLocation
                )
            }

            // b. Create a transaction from available items
            log.info "Creating transaction for ${inventory.warehouse.name}"
            Transaction transaction = new Transaction()
            transaction.inventory = inventory
            transaction.transactionType = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)
            transaction.transactionNumber = inventoryService.generateTransactionNumber()
            transaction.transactionDate = new Date()
            transaction.comment = "Created while merging product ${obsolete.productCode} into ${primary.productCode} " +
                "(this happens if both products have transaction entries in the same inventory)"
            transaction.disableRefresh = true // there is no need to refresh product availability, because this should not change
            transactionEntries?.each { TransactionEntry it -> transaction.addToTransactionEntries(it) }
            transaction.save(flush: true)
        }

        // 2. Copy Inventory items that are not already existing on the primary product (and InventorySnapshot change)
        Set<InventoryItem> primaryInventoryItems = InventoryItem.findAllByProduct(primary)
        Set<InventoryItem> obsoleteInventoryItems = InventoryItem.findAllByProduct(obsolete)
        log.info "Moving ${obsoleteInventoryItems?.size() ?: 0} Inventory items"
        obsoleteInventoryItems?.each { InventoryItem obsoleteInventoryItem ->
            // Check if this lot already exists for the primary product
            InventoryItem primaryInventoryItem = getPrimaryInventoryItem(primaryInventoryItems, obsoleteInventoryItem)

            int productAvailabilitiesCount = ProductAvailability.countByInventoryItem(obsoleteInventoryItem)
            if (primaryInventoryItem) {
                // if product inventory item already exists, then check if obsolete InventoryItem has InventorySnapshot
                // FIXME: Temporary disabled inventory snapshot update

                // if product inventory item already exists, then check if obsolete InventoryItem has ProductAvailability
                if (productAvailabilitiesCount > 0) {
                    productAvailabilityService.updateProductAvailabilityOnMergeProduct(primaryInventoryItem, obsoleteInventoryItem, primary, obsolete)
                }
                // if product inventory item already exists, then check if obsolete InventoryItem has PicklistItems
                int picklistItemsCount = PicklistItem.countByInventoryItem(obsoleteInventoryItem)
                if (picklistItemsCount > 0) {
                    picklistService.updatePicklistItemsOnProductMerge(primaryInventoryItem, obsoleteInventoryItem)
                }

                // return, to not duplicate the lot number if it already exists for primary
                return
            }

            logProductMergeData(primary, obsolete, obsoleteInventoryItem)

            // Swap product to primary
            obsoleteInventoryItem.product = primary
            obsoleteInventoryItem.disableRefresh = true
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoleteInventoryItem.save(flush: true)

            // Swap inventory snapshot product for the records with inventory item that had changed products
            // FIXME: Temporary removed inventory snapshot update

            // Swap product availability product for the records with inventory item that had changed products
            if (productAvailabilitiesCount) {
                productAvailabilityService.updateProductAvailabilityOnMergeProduct(obsoleteInventoryItem, primary, obsolete)
            }
        }

        // Refetch primary inventory items after inventory items manipulation
        primaryInventoryItems = InventoryItem.findAllByProduct(primary)

        // 3. Swap data on the transaction entries
        List<TransactionEntry> obsoleteTransactionEntries = getRelatedObjectsForProduct(TransactionEntry.createCriteria(), obsolete)
        log.info "Moving ${obsoleteTransactionEntries?.size() ?: 0} Transaction Entries"
        obsoleteTransactionEntries?.each { TransactionEntry obsoleteTransactionEntry ->
            logProductMergeData(primary, obsolete, obsoleteTransactionEntry)

            // Swap product
            obsoleteTransactionEntry.product = primary

            // Swap inventory item (in case the primary product had the same lot as obsolete)
            InventoryItem primaryInventoryItem = getPrimaryInventoryItem(primaryInventoryItems, obsoleteTransactionEntry?.inventoryItem)
            if (primaryInventoryItem) {
                obsoleteTransactionEntry.inventoryItem = primaryInventoryItem
            }
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoleteTransactionEntry.save(flush: true)
        }

        // 4. Find all items with obsolete product and swap products and inventory items

        // 4.1 RequisitionItem
        def requisitionItemCriteria = RequisitionItem.createCriteria()
        List<RequisitionItem> obsoleteRequisitionItems = getRelatedObjectsForProduct(requisitionItemCriteria, obsolete)
        log.info "Moving ${obsoleteRequisitionItems?.size() ?: 0} Requisition items"
        obsoleteRequisitionItems?.each { RequisitionItem obsoleteRequisitionItem ->
            logProductMergeData(primary, obsolete, obsoleteRequisitionItem)

            // Swap product
            obsoleteRequisitionItem.product = primary

            // Swap inventory item (in case the primary product had the same lot as obsolete)
            InventoryItem primaryInventoryItem = getPrimaryInventoryItem(primaryInventoryItems, obsoleteRequisitionItem?.inventoryItem)
            if (primaryInventoryItem) {
                obsoleteRequisitionItem.inventoryItem = primaryInventoryItem
            }
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoleteRequisitionItem.save(flush: true)
        }

        // 4.2 ShipmentItem
        def shipmentItemCriteria = ShipmentItem.createCriteria()
        List<ShipmentItem> obsoleteShipmentItems = getRelatedObjectsForProduct(shipmentItemCriteria, obsolete)
        log.info "Moving ${obsoleteShipmentItems?.size() ?: 0} Shipmen items"
        obsoleteShipmentItems?.each { ShipmentItem obsoleteShipmentItem ->
            logProductMergeData(primary, obsolete, obsoleteShipmentItem)

            // Swap product
            obsoleteShipmentItem.product = primary

            // Swap inventory item (in case the primary product had the same lot as obsolete)
            InventoryItem primaryInventoryItem = getPrimaryInventoryItem(primaryInventoryItems, obsoleteShipmentItem?.inventoryItem)
            if (primaryInventoryItem) {
                obsoleteShipmentItem.inventoryItem = primaryInventoryItem
                obsoleteShipmentItem.lotNumber = primaryInventoryItem?.lotNumber
                obsoleteShipmentItem.expirationDate = primaryInventoryItem?.expirationDate
            }
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoleteShipmentItem.save(flush: true)
        }

        // 4.3 OrderItem
        def orderItemCriteria = OrderItem.createCriteria()
        List<OrderItem> obsoleteOrderItems = getRelatedObjectsForProduct(orderItemCriteria, obsolete)
        log.info "Moving ${obsoleteOrderItems?.size() ?: 0} Order items"
        obsoleteOrderItems?.each { OrderItem obsoleteOrderItem ->
            logProductMergeData(primary, obsolete, obsoleteOrderItem)

            // Swap product
            obsoleteOrderItem.product = primary

            // Swap inventory item (in case the primary product had the same lot as obsolete)
            InventoryItem primaryInventoryItem = getPrimaryInventoryItem(primaryInventoryItems, obsoleteOrderItem?.inventoryItem)
            if (primaryInventoryItem) {
                obsoleteOrderItem.inventoryItem = primaryInventoryItem
            }
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoleteOrderItem.save(flush: true)
        }

        // 4.4 ReceiptItem
        def receiptItemCriteria = ReceiptItem.createCriteria()
        List<ReceiptItem> obsoleteReceiptItems = getRelatedObjectsForProduct(receiptItemCriteria, obsolete)
        log.info "Moving ${obsoleteReceiptItems?.size() ?: 0} Receipt items"
        obsoleteReceiptItems?.each { ReceiptItem obsoleteReceiptItem ->
            logProductMergeData(primary, obsolete, obsoleteReceiptItem)

            // Swap product
            obsoleteReceiptItem.product = primary

            // Swap inventory item (in case the primary product had the same lot as obsolete)
            InventoryItem primaryInventoryItem = getPrimaryInventoryItem(primaryInventoryItems, obsoleteReceiptItem?.inventoryItem)
            if (primaryInventoryItem) {
                obsoleteReceiptItem.inventoryItem = primaryInventoryItem
                obsoleteReceiptItem.lotNumber = primaryInventoryItem?.lotNumber
                obsoleteReceiptItem.expirationDate = primaryInventoryItem?.expirationDate
            }
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoleteReceiptItem.save(flush: true)
        }

        /**
         * FINAL PROCESSING
         * */

        // 1. Deactivate obsoleted product
        obsolete.active = false
        if (!obsolete.save(flush: true)) {
            throw new Exception("Cannot save obsolete product due to: " + obsolete.errors?.toString())
        }
        if (!primary.save(flush: true)) {
            throw new Exception("Cannot save primary product due to: " + primary.errors?.toString())
        }

        // 2. Trigger refresh of product_demand, stockout_fact, product_summary views
        // Skipped for now because demand data might take too long and other are not that relevant (plus InventorySnapshot
        // and ProductAvailability are handled during inventory items swap).
        // TODO / FIXME: Consider running these sequentionally in a separate job if needed
        // RefreshStockoutDataJob.triggerNow()
        // RefreshDemandDataJob.triggerNow() // <- FIXME: probably can be skipped if takes too long time to process
        // RefreshOrderSummaryJob.triggerNow() // <- FIXME: maybe do a refresh by list of affected orders instead of full table
    }

    /**
     * Finds inventory item from primary product, that has the same lot and expiry date as obsoleteInventoryItem
     * (null lotNumber and empty string lotNumber are treated equally)
     * */
    def getPrimaryInventoryItem(Set<InventoryItem> primaryInventoryItems, InventoryItem obsoleteInventoryItem) {
        def obsoleteLotNumber = obsoleteInventoryItem?.lotNumber
        def obsoleteExpirationDate = obsoleteInventoryItem?.expirationDate
        return primaryInventoryItems?.find {
            def exactLotNumber = obsoleteLotNumber ? (it.lotNumber == obsoleteLotNumber) : (it.lotNumber == null || it.lotNumber == "")
            return exactLotNumber && it.expirationDate == obsoleteExpirationDate
        } ?: null
    }

    /**
     * For getting RequisitionItems, ShipmentItems, OrderItems, ReceiptItems, TransactionEntries, by given product
     * under product or inventoryItem.product
     * */
    def getRelatedObjectsForProduct(def criteria, Product product) {
        criteria.list {
            or {
                eq("product", product)
                inventoryItem {
                    eq("product", product)
                }
            }
        }
    }

    void logProductMergeData(Product primary, Product obsolete, def relatedObject) {
        ProductMergeLogger productMergeLogger = new ProductMergeLogger(
            primaryProduct: primary,
            obsoleteProduct: obsolete,
            relatedObjectId: relatedObject?.id,
            relatedObjectClassName: relatedObject?.class?.toString(),
            dateMerged: new Date()
        )
        log.info "Product merge logger - swapping product, from ${obsolete?.productCode} to ${primary?.productCode}" +
            " for ${relatedObject?.class?.toString()} with ID: ${relatedObject?.id}"
        if (!productMergeLogger.save(flush: true)) {
            throw new Exception("Cannot save product merge logger due to: " + productMergeLogger.errors?.toString())
        }
    }

    def getProductMergeLogs(Map params) {
        return ProductMergeLogger.createCriteria().list(params) {
            if (params.primaryProductCode) {
                primaryProduct {
                    eq("productCode", params.primaryProductCode)
                }
            }
            if (params.obsoleteProductCode) {
                obsoleteProduct {
                    eq("productCode", params.obsoleteProductCode)
                }
            }
        }
    }
}
