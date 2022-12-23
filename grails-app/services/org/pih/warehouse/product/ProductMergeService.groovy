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

import org.pih.warehouse.core.Constants
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

        // 1. ProductSupplier
        List<ProductSupplier> obsoletedSuppliers = ProductSupplier.findAllByProduct(obsolete)
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

        // 1. Merge most recent obsolete and primary PRODUCT_INVENTORY or INVENTORY Transactions (Transaction
        //    entries require to be moved to the primary transaction, because for stock card and product
        //    availability calculations we always look into the most recent transaction with one of these two types).

        // Move TransactionEntries for the most recent PRODUCT_INVENTORY or INVENTORY Transaction (if any
        // one of these exists for both products)
        TransactionType productInventoryType = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)
        TransactionType inventoryType = TransactionType.get(Constants.INVENTORY_TRANSACTION_TYPE_ID)
        def transactionTypes = [productInventoryType, inventoryType]
        List<Transaction> obsoleteTransactions = inventoryService.getMostRecentTransactionsForProductAndTypeByInventory(obsolete, transactionTypes)
        List<Transaction> primaryTransactions = inventoryService.getMostRecentTransactionsForProductAndTypeByInventory(primary, transactionTypes)
        if (obsoleteTransactions && primaryTransactions) {
            obsoleteTransactions.each { Transaction obsoleteTransaction ->
                Transaction primaryTransaction = primaryTransactions.find { it.inventory?.id == obsoleteTransaction.inventory?.id }
                if (!primaryTransaction) {
                    return
                }

                // Refetch entries by obsolete transaction and product to avoid running into ConcurrentModificationException
                def obsoleteEntries = TransactionEntry.findAllByTransactionAndProduct(obsoleteTransaction, obsolete)
                obsoleteEntries?.each { TransactionEntry entry ->
                    moveTransactionEntry(
                        entry,
                        obsoleteTransaction,
                        primaryTransaction,
                        primary
                    )
                }
            }
        }

        // 2. Copy Inventory items that are not already existing on the primary product (and InventorySnapshot change)
        Set<InventoryItem> primaryInventoryItems = InventoryItem.findAllByProduct(primary)
        Set<InventoryItem> obsoleteInventoryItems = InventoryItem.findAllByProduct(obsolete)
        obsoleteInventoryItems?.each { InventoryItem obsoleteInventoryItem ->
            // Check if this lot already exists for the primary product
            def obsoleteLotNumber = obsoleteInventoryItem?.lotNumber
            InventoryItem primaryInventoryItem = primaryInventoryItems.find { it.lotNumber == obsoleteLotNumber}

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
        obsoleteTransactionEntries?.each { TransactionEntry obsoleteTransactionEntry ->
            logProductMergeData(primary, obsolete, obsoleteTransactionEntry)

            // Swap product
            obsoleteTransactionEntry.product = primary

            // Swap inventory item (in case the primary product had the same lot as obsolete)
            def obsoleteLotNumber = obsoleteTransactionEntry.inventoryItem?.lotNumber
            def primaryInventoryItem = primaryInventoryItems.find { it.lotNumber == obsoleteLotNumber}
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
        obsoleteRequisitionItems?.each { RequisitionItem obsoleteRequisitionItem ->
            logProductMergeData(primary, obsolete, obsoleteRequisitionItem)

            // Swap product
            obsoleteRequisitionItem.product = primary

            // Swap inventory item (in case the primary product had the same lot as obsolete)
            def obsoleteLotNumber = obsoleteRequisitionItem.inventoryItem?.lotNumber
            def primaryInventoryItem = primaryInventoryItems.find { it.lotNumber == obsoleteLotNumber}
            if (primaryInventoryItem) {
                obsoleteRequisitionItem.inventoryItem = primaryInventoryItem
            }
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoleteRequisitionItem.save(flush: true)
        }

        // 4.2 ShipmentItem
        def shipmentItemCriteria = ShipmentItem.createCriteria()
        List<ShipmentItem> obsoleteShipmentItems = getRelatedObjectsForProduct(shipmentItemCriteria, obsolete)
        obsoleteShipmentItems?.each { ShipmentItem obsoleteShipmentItem ->
            logProductMergeData(primary, obsolete, obsoleteShipmentItem)

            // Swap product
            obsoleteShipmentItem.product = primary

            // Swap inventory item (in case the primary product had the same lot as obsolete)
            def obsoleteLotNumber = obsoleteShipmentItem.inventoryItem?.lotNumber
            def primaryInventoryItem = primaryInventoryItems.find { it.lotNumber == obsoleteLotNumber}
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
        obsoleteOrderItems?.each { OrderItem obsoleteOrderItem ->
            logProductMergeData(primary, obsolete, obsoleteOrderItem)

            // Swap product
            obsoleteOrderItem.product = primary

            // Swap inventory item (in case the primary product had the same lot as obsolete)
            def obsoleteLotNumber = obsoleteOrderItem.inventoryItem?.lotNumber
            def primaryInventoryItem = primaryInventoryItems.find { it.lotNumber == obsoleteLotNumber}
            if (primaryInventoryItem) {
                obsoleteOrderItem.inventoryItem = primaryInventoryItem
            }
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoleteOrderItem.save(flush: true)
        }

        // 4.4 ReceiptItem
        def receiptItemCriteria = ReceiptItem.createCriteria()
        List<ReceiptItem> obsoleteReceiptItems = getRelatedObjectsForProduct(receiptItemCriteria, obsolete)
        obsoleteReceiptItems?.each { ReceiptItem obsoleteReceiptItem ->
            logProductMergeData(primary, obsolete, obsoleteReceiptItem)

            // Swap product
            obsoleteReceiptItem.product = primary

            // Swap inventory item (in case the primary product had the same lot as obsolete)
            def obsoleteLotNumber = obsoleteReceiptItem.inventoryItem?.lotNumber
            def primaryInventoryItem = primaryInventoryItems.find { it.lotNumber == obsoleteLotNumber}
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

    /**
     * Moves TransactionEntry of obsolete product from one Transaction to another for primary product. In case
     * both transactions have both obsolete and primary product or the same lot for these entries, then
     * sum quantities of entries and remove obsolete one.
     * */
    void moveTransactionEntry(TransactionEntry entry, Transaction fromTransaction, Transaction toTransaction, Product primary) {
        // First check if the toTransaction has already entry with the same lot number and bin location for primary product
        TransactionEntry sameEntry = toTransaction?.transactionEntries?.find {
            (it?.product?.id == primary?.id || it?.inventoryItem?.product?.id == primary?.id) &&
            it?.inventoryItem?.lotNumber == entry?.inventoryItem?.lotNumber &&
            it?.binLocation?.id == entry?.binLocation?.id
        }
        if (sameEntry) {
            // If toTransaction has already the same entry for primary product, then sum quantities
            sameEntry.quantity += entry.quantity
            sameEntry.save(flush: true)
        } else {
            // Clone the entry from the obsolete transaction
            TransactionEntry clonedEntry = new TransactionEntry(
                quantity: entry.quantity,
                product: entry.product,
                inventoryItem: entry.inventoryItem,
                binLocation: entry.binLocation,
                reasonCode: entry.reasonCode,
                comments: entry.comments
            )

            // Add cloned entry to the primary transaction
            toTransaction.disableRefresh = true
            toTransaction.addToTransactionEntries(clonedEntry)
            toTransaction.save(flush: true)
        }

        // Remove cloned/moved entry from obsolete transaction
        fromTransaction.disableRefresh = true
        fromTransaction.removeFromTransactionEntries(entry)
        fromTransaction.save(flush: true)
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
