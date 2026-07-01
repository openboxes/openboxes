package org.pih.warehouse.core

import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.product.Product
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.event.TransactionalEventListener

class TransactionCreatedEventService {

    WebhookPublisherService webhookPublisherService

    @Async
    @TransactionalEventListener
    void onTransactionCreatedEvent(TransactionCreatedEvent event) {
        log.info "Transaction created event published for transaction with id: $event.source"

        Transaction.withNewTransaction {
            String transactionId = event.source as String
            Transaction transaction = Transaction.get(transactionId)

            if (!transaction) {
                log.warn "Transaction with id ${event.source} not found, cannot process transaction created event"
                return
            }

            switch (transaction.transactionType.transactionCode) {
                case TransactionCode.DEBIT:
                case TransactionCode.CREDIT:
                    if (transaction.cycleCount || transaction.transactionSource?.cycleCount) {
                        // This case is handled by the CycleCountCreatedEvent, so we can skip it here
                        break
                    }

                    if (transaction.transactionType.id in [Constants.TRANSFER_IN_TRANSACTION_TYPE_ID,
                                                           Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID]) {
                        // Currently no-op
                        break
                    }

                    // Each product from adjustment transaction should trigger its own notification
                    transaction.associatedProducts?.each { String productId ->
                        Product product = Product.get(productId)
                        webhookPublisherService.publishInventoryAdjustmentEvent(
                                product,
                                transaction.inventory.warehouse,
                                transaction
                        )
                    }
                    break
                case TransactionCode.INVENTORY:
                case TransactionCode.PRODUCT_INVENTORY:
                    // Currently no-op
                    break
                default:
                    break
            }
        }
    }
}
