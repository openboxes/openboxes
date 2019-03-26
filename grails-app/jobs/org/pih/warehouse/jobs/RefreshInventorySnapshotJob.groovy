package org.pih.warehouse.jobs


import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.product.Product
import org.quartz.JobExecutionContext
import util.LiquibaseUtil

class RefreshInventorySnapshotJob {

    def inventorySnapshotService

    // Should never be triggered on a schedule - should only be triggered by persistence event listener
    static triggers = { }

    def execute(JobExecutionContext context) {
        if (LiquibaseUtil.isRunningMigrations()) {
            log.info "Postponing job execution until liquibase migrations are complete"
            return
        }

        def startTime = System.currentTimeMillis()
        def transactionEntryId = context.mergedJobDataMap.get('transactionEntryId')
        if (transactionEntryId) {
            TransactionEntry transactionEntry = TransactionEntry.get(transactionEntryId)
            if (transactionEntry) {
                Product product = transactionEntry.product
                Location location = Location.findByInventory(transactionEntry.transaction.inventory)
                Date transactionDate = transactionEntry.transaction.transactionDate
                log.info "Updating location ${location}, date ${transactionDate}, product ${product}"
                inventorySnapshotService.populateInventorySnapshots(transactionDate, location, product)
            }
        }
        log.info "Refreshed inventory snapshot table for transaction transactionEntryId ${transactionEntryId}: ${(System.currentTimeMillis() - startTime)} ms"
    }
}