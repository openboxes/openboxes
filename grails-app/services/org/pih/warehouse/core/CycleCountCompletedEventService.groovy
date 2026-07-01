package org.pih.warehouse.core

import org.pih.warehouse.inventory.CycleCount
import org.pih.warehouse.inventory.CycleCountService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.event.TransactionalEventListener

class CycleCountCompletedEventService {
    CycleCountService cycleCountService
    WebhookPublisherService webhookPublisherService

    @Async
    @TransactionalEventListener
    void onCycleCountEvent(CycleCountCompletedEvent event) {
        CycleCount.withNewTransaction {
            log.info "Application event $event has been published! " + event.properties
            CycleCount cycleCount = CycleCount.get(event.source)
            if (!cycleCount) {
                log.warn "CycleCount with id ${event.source} not found, cannot process cycle count event"
                return
            }

            if (!cycleCount.status.isClosed()) {
                log.warn "CycleCount with id ${event.source} is not closed yet, cannot process cycle count completed event"
                return
            }

            // There are multiple baseline transactions for a cycle count, one for each product.
            TransactionType baselineType = TransactionType.read(Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID)
            List<Transaction> baselineTransactions = cycleCountService.getCycleCountTransactions(cycleCount, baselineType)
            // There should be only one adjustment transaction for a cycle count, which contains all products adjustments.
            TransactionType adjustmentType = TransactionType.read(Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID)
            List<Transaction> adjustmentTransactions = cycleCountService.getCycleCountTransactions(cycleCount, adjustmentType)

            cycleCount?.products?.each { Product product ->
                Transaction baselineTransaction = baselineTransactions.find { it?.transactionEntries?.any { it?.inventoryItem?.product == product } }
                Transaction adjustmentTransaction = adjustmentTransactions.find { it?.transactionEntries?.any { it?.inventoryItem?.product == product } }
                webhookPublisherService.publishCycleCountCompletedEvent(product, cycleCount.facility, baselineTransaction, adjustmentTransaction)
            }
        }
    }
}
