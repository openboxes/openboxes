package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.pih.warehouse.core.Location

@Transactional
class AdjustInventoryService {

    TransactionSource createAdjustInventoryTransactionSource(Location location, boolean accurate = true) {
        TransactionSource transactionSource = new TransactionSource(
                transactionAction: TransactionAction.INVENTORY_ADJUSTMENT,
                origin: location,
                destination: location,
                accurate: accurate
        )
        if (!transactionSource.validate()) {
            throw new ValidationException("Invalid transaction source", transactionSource.errors)
        }
        return transactionSource.save()
    }

    TransactionSource createMissingAdjustInventoryTransactionSource(Location location) {
        return createAdjustInventoryTransactionSource(location, false)
    }
}
