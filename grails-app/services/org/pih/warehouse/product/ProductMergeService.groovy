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

import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Synonym
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventorySnapshot
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.jobs.RefreshDemandDataJob
import org.pih.warehouse.jobs.RefreshOrderSummaryJob
import org.pih.warehouse.jobs.RefreshProductAvailabilityJob
import org.pih.warehouse.jobs.RefreshStockoutDataJob
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderSummary
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.ShipmentItem

class ProductMergeService {

    def inventorySnapshotService
    def picklistService
    def productAvailabilityService

    /**
     * Preforms product swapping for a product pairs passed as params
     * */
    def mergeObsoletedProduct(Product primary, Product obsolete) {
        /**
         * ================================
         * === Swap Product's relations ===
         * ================================
         *
         * === "Easy" relations ===
         * - ProductAssociation.product
         * - ProductComponent.assemblyProduct
         * - ProductPackage <-- compare products packages for both items and replace product only for the package that is not available for primary
         * - ProductSupplier <-- probably same case as ProductPackage
         * - Synonym <-- if the same synonym does not exist on primary product, then can be swapped
         * - Document <-- probably can be swapped (?)
         *
         * === "Complex" relations ===
         * - InventoryItem <-- find inventory items and replace product only for InventoryItems with lotNumber that are not exisitng for primary product
         *                     But if we're gonna use the InventoryItem from primary Product, then obsoleted InventoryItem relations should be updated accordingly.
         *                     (ShipmentItems, TransactionEntries and so on)
         * - InventorySnapshot <-- Adjusted during InventoryItems management for specific inventory items and products
         * - ProductAvailability <-- Adjusted during InventoryItems management for specific inventory items and products
         *
         * - RequisitionItem <-- probably can be swapped, but watch out for InventoryItems (in case the there is existing one on primary product)
         * - ShipmentItem <-- probably can be swapped, but watch out for InventoryItems (in case the there is existing one on primary product)
         * - OrderItem <-- probably can be swapped, but watch out for InventoryItems (in case the there is existing one on primary product)
         * - ReceiptItem <-- probably can be swapped, but watch out for InventoryItems (in case the there is existing one on primary product)
         *
         * - TransactionEntry <-- probably can be swapped, but watch out for InventoryItems (in case the there is existing one on primary product)
         *
         * === Tables potentially requiring refresh ===
         * - StockoutData - skipped for now
         * - DemandData - skipped for now
         * - OrderSummary - product merge should not affect the order summary data since it is not product bound
         *
         * === "Ignored" relations ===
         * - ProductSummary <-- probably can be ignored (not changed)
         * - InventoryLevel <-- probably can be ignored (not changed)
         * - Category <-- probably can be ignored (not changed)
         * - ProductAttribute <-- probably can be ignored (not changed)
         * - ProductCatalogItem <-- probably can be ignored (not changed)
         * - ProductGroup <-- probably can be ignored (not changed)
         * - Tag <-- probably can be ignored (not changed)
         * */

        /**
         * SWAP TRIVIAL RELATIONS
         * */

        // 1. ProductAssociation <-- swap only associations that does not exist for primary product
        List<ProductAssociation> obsoleteAssociations = ProductAssociation.findAllByProduct(obsolete)
        obsoleteAssociations?.each { ProductAssociation obsoleteAssociation ->
            // Find if primary product has already the same association
            ProductAssociation primaryAssociation = ProductAssociation.findByProductAndAssociatedProduct(
                primary, obsoleteAssociation.associatedProduct
            )

            if (primaryAssociation) {
                // if product association already exists, then do nothing with it
                return
            }

            logProductMergeData(primary, obsolete, obsoleteAssociation)

            // Swap product to primary
            obsoleteAssociation.product = primary
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoleteAssociation.save(flush: true)
        }

        // 2. ProductComponent <-- swap only components that does not exist for primary product
        List<ProductComponent> obsoletedComponents = ProductComponent.findAllByAssemblyProduct(obsolete)
        obsoletedComponents?.each { ProductComponent obsoletedComponent ->
            // Find if primary product has already the same component
            ProductComponent primaryComponent = ProductComponent.findAllByAssemblyProductAndComponentProduct(
                primary, obsoletedComponent.componentProduct
            )

            if (primaryComponent) {
                // if product component already exists, then do nothing with it
                return
            }

            logProductMergeData(primary, obsolete, obsoletedComponent)

            // Swap assemblyProduct to primary
            obsoletedComponent.assemblyProduct = primary
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoletedComponent.save(flush: true)
        }

        // 3. ProductPackage <-- compare products packages for both items and replace product only for the package that is not available for primary
        List<ProductPackage> obsoletedPackages = ProductPackage.findAllByProduct(obsolete)
        obsoletedPackages?.each { ProductPackage obsoletedPackage ->
            // Find if primary product has already the same package
            ProductPackage primaryPackage = ProductPackage.findByProductAndName(primary, obsoletedPackage.name)

            if (primaryPackage) {
                // if product package already exists, then do nothing with it
                return
            }

            logProductMergeData(primary, obsolete, obsoletedPackage)

            // Swap product to primary
            obsoletedPackage.product = primary
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoletedPackage.save(flush: true)
        }

        // 4. ProductSupplier <-- probably same case as ProductPackage
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

        // 5. Synonym <-- if the same synonym does not exist on primary product, then can be swapped
        List<Synonym> obsoletedSynonyms = Synonym.findAllByProduct(obsolete)
        obsoletedSynonyms?.each { Synonym obsoletedSynonym ->
            // Find if primary product has already the same synonym
            Synonym primarySynonym = Synonym.findByProductAndName(primary, obsoletedSynonym.name)

            if (primarySynonym) {
                // if product synonym already exists, then do nothing with it
                return
            }

            logProductMergeData(primary, obsolete, obsoletedSynonym)

            // Swap assemblyProduct to primary
            obsoletedSynonym.product = primary
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoletedSynonym.save(flush: true)
        }

        // 6. Documents
        obsolete.documents?.each { Document obsoleteDocument ->
            logProductMergeData(primary, obsolete, obsoleteDocument)
            obsolete.removeFromDocuments(obsoleteDocument)
            primary.addToDocuments(obsoleteDocument)
        }

        /**
         * INVENTORY ITEMS PLUS "ITEM" RELATIONS
         * */

        // 1. Copy Inventory items that are not already existing on the primary product (and InventorySnapshot change)
        Set<InventoryItem> obsoleteInventoryItems = obsolete.inventoryItems
        obsoleteInventoryItems?.each { InventoryItem obsoleteInventoryItem ->
            // Check if this lot already exists for the primary product
            InventoryItem primaryInventoryItem = InventoryItem.findByProductAndLotNumber(primary, obsoleteInventoryItem.lotNumber)

            int inventorySnapshotsCount = InventorySnapshot.countByInventoryItem(obsoleteInventoryItem)
            int productAvailabilitiesCount = ProductAvailability.countByInventoryItem(obsoleteInventoryItem)
            if (primaryInventoryItem) {
                // if product inventory item already exists, then check if obsolete InventoryItem has InventorySnapshot
                if (inventorySnapshotsCount > 0) {
                    inventorySnapshotService.updateInventorySnapshots(primaryInventoryItem, obsoleteInventoryItem, primary)
                }
                // if product inventory item already exists, then check if obsolete InventoryItem has ProductAvailability
                if (productAvailabilitiesCount > 0) {
                    productAvailabilityService.updateProductAvailabilityOnProductMerge(primaryInventoryItem, obsoleteInventoryItem, primary)
                }
                // if product inventory item already exists, then check if obsolete InventoryItem has PicklistItems
                int picklistItemsCount = PicklistItem.countByInventoryItem(obsoleteInventoryItem)
                if (picklistItemsCount > 0) {
                    picklistService.updatePicklistItemsOnProductMerge(primaryInventoryItem, obsoleteInventoryItem)
                }

                return
            }

            logProductMergeData(primary, obsolete, obsoleteInventoryItem)

            // Swap inventory snapshot product for the records with inventory item that will have changed products
            if (inventorySnapshotsCount) {
                inventorySnapshotService.updateInventorySnapshots(obsoleteInventoryItem, primary)
            }

            // Swap product availability product for the records with inventory item that will have changed products
            if (productAvailabilitiesCount) {
                productAvailabilityService.updateProductAvailabilityOnProductMerge(obsoleteInventoryItem, primary)
            }

            // Swap product to primary
            obsoleteInventoryItem.product = primary
            obsoleteInventoryItem.disableRefresh = true
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoleteInventoryItem.save(flush: true)
        }

        // 2. Find all items with obsolete product and swap products and inventory items

        // 2.1 RequisitionItem
        List<RequisitionItem> obsoleteRequisitionItems = RequisitionItem.findAllByProduct(obsolete)
        obsoleteRequisitionItems?.each { RequisitionItem obsoleteRequisitionItem ->
            logProductMergeData(primary, obsolete, obsoleteRequisitionItem)

            // Swap product
            obsoleteRequisitionItem.product = primary

            // Swap inventory item (in case the primary product had the same lot as obsolete)
            def obsoleteLotNumber = obsoleteRequisitionItem.inventoryItem?.lotNumber
            def primaryInventoryItem = primary.inventoryItems?.find {
                it.lotNumber == obsoleteLotNumber
            }
            obsoleteRequisitionItem.inventoryItem = primaryInventoryItem
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoleteRequisitionItem.save(flush: true)
        }

        // 2.2 ShipmentItem
        List<ShipmentItem> obsoleteShipmentItems = ShipmentItem.findAllByProduct(obsolete)
        obsoleteShipmentItems?.each { ShipmentItem obsoleteShipmentItem ->
            logProductMergeData(primary, obsolete, obsoleteShipmentItem)

            // Swap product
            obsoleteShipmentItem.product = primary

            // Swap inventory item (in case the primary product had the same lot as obsolete)
            def obsoleteLotNumber = obsoleteShipmentItem.inventoryItem?.lotNumber
            def primaryInventoryItem = primary.inventoryItems?.find {
                it.lotNumber == obsoleteLotNumber
            }
            obsoleteShipmentItem.inventoryItem = primaryInventoryItem
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoleteShipmentItem.save(flush: true)
        }

        // 2.3 OrderItem
        List<OrderItem> obsoleteOrderItems = OrderItem.findAllByProduct(obsolete)
        obsoleteOrderItems?.each { OrderItem obsoleteOrderItem ->
            logProductMergeData(primary, obsolete, obsoleteOrderItem)

            // Swap product
            obsoleteOrderItem.product = primary

            // Swap inventory item (in case the primary product had the same lot as obsolete)
            def obsoleteLotNumber = obsoleteOrderItem.inventoryItem?.lotNumber
            def primaryInventoryItem = primary.inventoryItems?.find {
                it.lotNumber == obsoleteLotNumber
            }
            obsoleteOrderItem.inventoryItem = primaryInventoryItem
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoleteOrderItem.save(flush: true)
        }

        // 2.4 ReceiptItem
        List<ReceiptItem> obsoleteReceiptItems = ReceiptItem.findAllByProduct(obsolete)
        obsoleteReceiptItems?.each { ReceiptItem obsoleteReceiptItem ->
            logProductMergeData(primary, obsolete, obsoleteReceiptItem)

            // Swap product
            obsoleteReceiptItem.product = primary

            // Swap inventory item (in case the primary product had the same lot as obsolete)
            def obsoleteLotNumber = obsoleteReceiptItem.inventoryItem?.lotNumber
            def primaryInventoryItem = primary.inventoryItems?.find {
                it.lotNumber == obsoleteLotNumber
            }
            obsoleteReceiptItem.inventoryItem = primaryInventoryItem
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoleteReceiptItem.save(flush: true)
        }

        // 2.5 TransactionEntry
        List<TransactionEntry> obsoleteTransactionEntries = TransactionEntry.findAllByProduct(obsolete)
        obsoleteTransactionEntries?.each { TransactionEntry obsoleteTransactionEntry ->
            logProductMergeData(primary, obsolete, obsoleteTransactionEntry)

            // Swap product
            obsoleteTransactionEntry.product = primary

            // Swap inventory item (in case the primary product had the same lot as obsolete)
            def obsoleteLotNumber = obsoleteTransactionEntry.inventoryItem?.lotNumber
            def primaryInventoryItem = primary.inventoryItems?.find {
                it.lotNumber == obsoleteLotNumber
            }
            obsoleteTransactionEntry.inventoryItem = primaryInventoryItem
            // Note: needs flush because of "User.locationRoles not processed by flush"
            obsoleteTransactionEntry.save(flush: true)
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
