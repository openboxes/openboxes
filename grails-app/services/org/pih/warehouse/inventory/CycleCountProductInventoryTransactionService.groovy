package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException

/**
 * Responsible for managing product inventory transactions for the Cycle Count feature.
 */
@Transactional
class CycleCountProductInventoryTransactionService extends ProductInventoryTransactionService<CycleCount> {

    TransactionSource createCycleCountTransactionSource(CycleCount cycleCount) {
        TransactionSource transactionSource = new TransactionSource(
                transactionAction: TransactionAction.CYCLE_COUNT,
                cycleCount: cycleCount,
                origin: cycleCount.facility
        )
        if (!transactionSource.validate()) {
            throw new ValidationException("Invalid transaction source", transactionSource.errors)
        }
        // Flush is needed for setSourceObject method to be able to find the newly created TransactionSource in the same session
        return transactionSource.save(flush: true)
    }

    @Override
    void setSourceObject(Transaction transaction, CycleCount sourceObject) {
        transaction.cycleCount = sourceObject
        TransactionSource transactionSource = TransactionSource.findByCycleCount(sourceObject)
        transaction.transactionSource = transactionSource ?: createCycleCountTransactionSource(sourceObject)
    }
}
