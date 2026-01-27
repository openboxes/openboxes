package org.pih.warehouse.data

import grails.gorm.transactions.Transactional
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.CycleCount
import org.pih.warehouse.inventory.CycleCountProductInventoryTransactionService
import org.pih.warehouse.inventory.InventoryCount
import org.pih.warehouse.inventory.InventoryImportProductInventoryTransactionService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionSource

@Transactional
class TransactionSourceMigrationService {

    InventoryImportProductInventoryTransactionService inventoryImportProductInventoryTransactionService
    CycleCountProductInventoryTransactionService cycleCountProductInventoryTransactionService
    HibernateSessionService hibernateSessionService

    List<InventoryCount> getInventoryImportTransactionsWithMissingTransactionSource(Location location) {
        List<InventoryCount> inventoryCounts = InventoryCount.createCriteria().list {
            transaction {
                isNull("transactionSource")
                // There is no better way to identify inventory import transactions other than checking the comment
                ilike("comment", "%Imported from%")
            }
            eq("facility", location)
        } as List<InventoryCount>
        return inventoryCounts
    }

    /**
     * Method to check if there are any missing cycle count associations (OBS-1930)
     * Without that step first, creating missing transaction sources does not make sense, as the data would still not be accurate
     * @return true/false
     */
    boolean hasMissingCycleCountAssociations() {
        List<InventoryCount> inventoryCountsWithBaselineAndAdjustment = InventoryCount.createCriteria().list {
            transaction {
                isNull("cycleCount")
            }
            associatedTransaction {
                isNotNull("cycleCount")
            }
            eq("facility", AuthService.currentLocation)
        }
        List<Map<String, Object>> aloneBaselinesMissingCycleCount = hibernateSessionService.list("""
            SELECT
                cc.id AS cycleCountId,
                t.id AS transactionId
            FROM cycle_count cc
            JOIN transaction t ON TIMESTAMPDIFF(SECOND, t.transaction_date, cc.last_updated) = 1
            JOIN transaction_type tt ON tt.id = t.transaction_type_id
            WHERE tt.id = 12
            AND cc.facility_id = :facilityId    
            AND t.cycle_count_id IS NULL
            ${!inventoryCountsWithBaselineAndAdjustment.isEmpty() ? "AND t.id NOT IN (:inventoryCountsWithBaselineAndAdjustment)" : ""}
            AND EXISTS (
                SELECT 1
                FROM transaction_entry te
                         JOIN inventory_item ii ON ii.id = te.inventory_item_id
                         JOIN cycle_count_item cci ON cci.cycle_count_id = cc.id 
                            AND cci.product_id = ii.product_id
                WHERE te.transaction_id = t.id
            )
        """, !inventoryCountsWithBaselineAndAdjustment.isEmpty()
                ? [inventoryCountsWithBaselineAndAdjustment: inventoryCountsWithBaselineAndAdjustment.transaction.id, facilityId: AuthService.currentLocation.id]
                : [facilityId: AuthService.currentLocation.id]).collect { Map<String, String> row ->
            [
                    cycleCount: CycleCount.read(row.cycleCountId),
                    transaction: Transaction.get(row.transactionId)
            ]
        }
        return inventoryCountsWithBaselineAndAdjustment.size() + aloneBaselinesMissingCycleCount.size() > 0
    }

    List<InventoryCount> getCycleCountTransactionsWithMissingTransactionSource(Location location) {
        List<InventoryCount> inventoryCounts = InventoryCount.createCriteria().list {
            transaction {
                isNull("transactionSource")
                isNotNull("cycleCount")
            }
            eq("facility", location)
        } as List<InventoryCount>
        return inventoryCounts
    }

    Integer getAmountOfMissingCycleCountTransactionSources() {
        List<InventoryCount> inventoryCounts = getCycleCountTransactionsWithMissingTransactionSource(AuthService.currentLocation)

        Set<List<Transaction>> transactionPairs = inventoryCounts
                .groupBy { [it.transaction, it.associatedTransaction] }
                .keySet()

        return transactionPairs.size()
    }

    Map<String, Integer> createMissingCycleCountTransactionSources(Location location) {
        boolean hasMissingCycleCountAssociations = hasMissingCycleCountAssociations()
        if (hasMissingCycleCountAssociations) {
            throw new IllegalStateException("Cannot create missing cycle count transaction sources when there are missing cycle count associations.")
        }
        List<InventoryCount> inventoryCounts = getCycleCountTransactionsWithMissingTransactionSource(location)

        // We base the candidates on the inventory count view as it has the transactions (e.g. baseline + adjustment pair) associated as one record
        Set<List<Transaction>> transactionPairs = inventoryCounts
        // We group inventory count records by transaction and associated transaction (if both exists (baseline+adjustment), associated transaction is the adjustment)
                .groupBy { [it.transaction, it.associatedTransaction] }
                .keySet()
        int transactionSourcesCreated = 0
        transactionPairs.each { List<Transaction> transactionPair ->
            // If both exists, the associated transaction is an adjustment, and the mainTransaction is baseline
            // If only main transaction exists, this might be either baseline or adjustment transaction alone
            Transaction mainTransaction = transactionPair[0]
            Transaction associatedTransaction = transactionPair[1]
            CycleCount cycleCount = mainTransaction.cycleCount
            TransactionSource transactionSource =
                    cycleCountProductInventoryTransactionService.createMissingCycleCountTransactionSource(cycleCount)
            if (!transactionSource) {
                return
            }
            transactionSourcesCreated++
            mainTransaction.transactionSource = transactionSource
            if (associatedTransaction) {
                associatedTransaction.transactionSource = transactionSource
            }
        }
        return [inventoryCounts: inventoryCounts.size(), transactionSourcesCreated: transactionSourcesCreated]
    }

    /**
     * This returns maximum amount of missing inventory import transaction sources that need to be created
     * @return
     */
    Integer getAmountOfMissingInventoryImportTransactionSources() {
        List<InventoryCount> inventoryCounts = getInventoryImportTransactionsWithMissingTransactionSource(AuthService.currentLocation)

        Set<List<Transaction>> transactionPairs = inventoryCounts
                .groupBy { [it.transaction, it.associatedTransaction] }
                .keySet()

        int missingTransactionSources = 0
        transactionPairs.each { List<Transaction> transactionPair ->
            Transaction mainTransaction = transactionPair[0]
            Transaction associatedTransaction = transactionPair[1]

            TransactionSource existingTransactionSource = mainTransaction.transactionSource ?: associatedTransaction?.transactionSource
            if (!existingTransactionSource) {
                missingTransactionSources++
            }
        }
        return missingTransactionSources
    }


    /**
     * Create missing transaction sources for inventory import transactions that don't have one.
     *
     * @param location The location for which to create missing transaction sources.
     * @return A map with the number of inventory counts processed and the number of transaction sources created.
     */
    Map<String, Integer> createMissingInventoryImportTransactionSources(Location location) {
        List<InventoryCount> inventoryCounts = getInventoryImportTransactionsWithMissingTransactionSource(location)

        // We base the candidates on the inventory count view as it has the transactions (e.g. baseline + adjustment pair) associated as one record
        Set<List<Transaction>> transactionPairs = inventoryCounts
        // We group inventory count records by transaction and associated transaction (if both exists (baseline+adjustment), associated transaction is the adjustment)
                .groupBy { [it.transaction, it.associatedTransaction] }
                .keySet()

        int transactionSourcesCreated = 0
        transactionPairs.each { List<Transaction> transactionPair ->
            // If both exists, the associated transaction is an adjustment, and the mainTransaction is baseline
            // If only main transaction exists, this might be either baseline or adjustment transaction alone
            Transaction mainTransaction = transactionPair[0]
            Transaction associatedTransaction = transactionPair[1]

            // There might be a case in import, where we might have a few inventory count records for the same transaction.
            // This might be the case when there are multiple products in the import file for the same transaction
            // and for one of them we would create only a baseline, for another one only adjustment, and for another both baseline + adjustment
            // So if while looping we identify that for one transaction we already have the source, use it, as it represents the same "system action".
            TransactionSource existingTransactionSource = mainTransaction.transactionSource ?: associatedTransaction?.transactionSource
            if (existingTransactionSource) {
                mainTransaction.transactionSource = existingTransactionSource
                associatedTransaction?.transactionSource = existingTransactionSource
                return
            }
            TransactionSource transactionSource =
                    inventoryImportProductInventoryTransactionService.createMissingInventoryImportTransactionSource(AuthService.currentLocation)
            mainTransaction.transactionSource = transactionSource
            if (associatedTransaction) {
                associatedTransaction.transactionSource = transactionSource
            }
            transactionSourcesCreated++
        }
        return [inventoryCounts: inventoryCounts.size(), transactionSourcesCreated: transactionSourcesCreated]
    }
}
