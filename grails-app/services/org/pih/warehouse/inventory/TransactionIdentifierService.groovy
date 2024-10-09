package org.pih.warehouse.inventory

import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.identification.BlankIdentifierResolver

class TransactionIdentifierService extends IdentifierService implements BlankIdentifierResolver<Transaction> {

    @Override
    String getPropertyKey() {
        return "transaction"
    }

    @Override
    protected Integer countDuplicates(String transactionNumber) {
        return Transaction.countByTransactionNumber(transactionNumber)
    }

    @Override
    List<Transaction> getAllUnassignedEntities() {
        return Transaction.findAll("from Transaction as t where transactionNumber is null or transactionNumber = ''")
    }

    @Override
    void setIdentifierOnEntity(String transactionNumber, Transaction transaction) {
        transaction.transactionNumber = transactionNumber
    }
}
