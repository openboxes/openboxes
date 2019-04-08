package org.pih.warehouse.jobs


import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.product.Product
import org.quartz.JobExecutionContext
import util.LiquibaseUtil

class RefreshInventorySnapshotJob {

    def inventorySnapshotService

    // Should never be triggered on a schedule - should only be triggered by persistence event listener
    static triggers = { }

    def execute(JobExecutionContext context) {

        def startTime = System.currentTimeMillis()
        def transactionId = context.mergedJobDataMap.get('transactionId')
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
        else if (transactionId) {
            Transaction transaction = Transaction.get(transactionId)
            if (transaction) {
                Location location = Location.findByInventory(transaction.inventory)
                if (location) {
                    Date transactionDate = transaction.transactionDate
                    log.info "Updating location ${location} from date ${transactionDate}"
                    inventorySnapshotService.populateInventorySnapshots(transactionDate, location)
                }
            }
        }


        log.info "Refreshed inventory snapshot table for transaction transactionEntryId ${transactionEntryId}: ${(System.currentTimeMillis() - startTime)} ms"
    }
}