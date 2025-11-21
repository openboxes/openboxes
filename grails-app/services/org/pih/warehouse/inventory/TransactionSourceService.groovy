package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location

@Transactional
class TransactionSourceService {

    TransactionSource createRecordStockTransactionSource(Location location) {
        TransactionSource transactionSource = new TransactionSource(
                transactionAction: TransactionAction.RECORD_STOCK,
                origin: location,
                destination: location,
                createdBy: AuthService.currentUser,
                updatedBy: AuthService.currentUser
        )
        if (!transactionSource.validate()) {
            throw new ValidationException("Invalid transaction source", transactionSource.errors)
        }
        return transactionSource.save()
    }
}
