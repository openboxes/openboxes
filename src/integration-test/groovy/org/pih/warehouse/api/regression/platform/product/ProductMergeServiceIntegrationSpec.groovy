package org.pih.warehouse.api.regression.platform.product

import org.pih.warehouse.product.*
import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType

/**
 * Product merge against a real database: the left outer join that finds related rows through their inventory item,
 * and a whole merge of two products that both have stock in one depot.
 */
class ProductMergeServiceIntegrationSpec extends IntegrationSpec {

    ProductMergeService productMergeService

    /** Ids of what a feature created, for cleanup. */
    Map<String, String> ids = [:]

    void cleanup() {
        if (!ids) {
            return
        }
        try {
            // Bulk deletes, so the stock rows go without the domain events of a delete: deleting an inventory item
            // refreshes the availability of its product at every location on worker threads, and the worker for
            // this depot then waits on the locks this transaction holds until the database gives up (50 s a lot).
            // The fixture has no availability rows to refresh: its baseline is saved with disableRefresh.
            Product.withNewTransaction {
                List<String> productIds = [ids.primary, ids.obsolete]
                ProductMergeLogger.executeUpdate('delete from ProductMergeLogger l where l.primaryProduct.id = :id',
                        [id: ids.primary])
                TransactionEntry.executeUpdate('delete from TransactionEntry e where e.transaction.id = :id',
                        [id: ids.baseline])
                Transaction.executeUpdate('delete from Transaction t where t.id = :id', [id: ids.baseline])
                ProductSupplier.executeUpdate('delete from ProductSupplier s where s.product.id in (:ids)',
                        [ids: productIds])
                InventoryItem.executeUpdate('delete from InventoryItem i where i.product.id in (:ids)',
                        [ids: productIds])
            }
            Product.withNewTransaction {
                Product.get(ids.primary)?.delete()
                Product.get(ids.obsolete)?.delete()
                Location.get(ids.depot)?.delete()
                Category.get(ids.category)?.delete()
            }
        } catch (Exception ignored) {
            // Refresh events of the application may still reference the rows; leftovers are harmless test data
        }
    }

    /**
     * A depot where the primary product has lot A and the obsolete product has lots A and B, each with a baseline
     * entry. The entry of obsolete lot B has no product of its own, only its inventory item.
     */
    private void buildDepotWithBothProducts() {
        Product.withNewTransaction {
            String suffix = UUID.randomUUID().toString().take(8)
            Location depot = new LocationTestBuilder().asFacility().name("Merge depot ${suffix}").build(true)
            Category category = new CategoryTestBuilder().name("Merge category ${suffix}").build(true)
            Product primary = new ProductTestBuilder().randomizedName().category(category).build(true)
            Product obsolete = new ProductTestBuilder().randomizedName().category(category).build(true)
            InventoryItem primaryLotA = InventoryItem.build(product: primary, lotNumber: "A-${suffix}", expirationDate: null)
            InventoryItem obsoleteLotA = InventoryItem.build(product: obsolete, lotNumber: "A-${suffix}", expirationDate: null)
            InventoryItem obsoleteLotB = InventoryItem.build(product: obsolete, lotNumber: "B-${suffix}", expirationDate: null)

            Transaction baseline = new Transaction(
                    inventory: depot.inventory,
                    transactionType: TransactionType.get(Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID),
                    transactionDate: new Date() - 1,
                    transactionNumber: "MERGE-${suffix}",
                    disableRefresh: true,
            )
            baseline.addToTransactionEntries(new TransactionEntry(product: primary, inventoryItem: primaryLotA, quantity: 5))
            baseline.addToTransactionEntries(new TransactionEntry(product: obsolete, inventoryItem: obsoleteLotA, quantity: 3))
            baseline.addToTransactionEntries(new TransactionEntry(product: null, inventoryItem: obsoleteLotB, quantity: 2))
            baseline.save(failOnError: true, flush: true)

            new ProductSupplier(name: "Supplier ${suffix}", code: "SUP-${suffix}", product: obsolete).save(failOnError: true)

            ids = [category    : category.id, depot: depot.id, primary: primary.id, obsolete: obsolete.id, primaryLotA: primaryLotA.id,
                   obsoleteLotA: obsoleteLotA.id, obsoleteLotB: obsoleteLotB.id, baseline: baseline.id]
        }
    }

    void 'getRelatedObjectsForProduct should find entries of the product directly and through their inventory item'() {
        given:
        buildDepotWithBothProducts()

        when:
        List<String> obsoleteLots = Product.withTransaction {
            productMergeService.getRelatedObjectsForProduct(TransactionEntry.createCriteria(), Product.get(ids.obsolete))
                    .collect { it.inventoryItem.id }
        }
        List<String> primaryLots = Product.withTransaction {
            productMergeService.getRelatedObjectsForProduct(TransactionEntry.createCriteria(), Product.get(ids.primary))
                    .collect { it.inventoryItem.id }
        }

        then:
        obsoleteLots as Set == [ids.obsoleteLotA, ids.obsoleteLotB] as Set
        primaryLots == [ids.primaryLotA]
    }

}
