package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional

import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.identification.BlankIdentifierResolver

@Transactional
class TransactionIdentifierService extends IdentifierService<Transaction> implements BlankIdentifierResolver<Transaction> {

    @Override
    String getIdentifierName() {
        return "transaction"
    }

    @Override
    protected Integer countByIdentifier(String id) {
        return Transaction.countByTransactionNumber(id)
    }

    @Override
    List<Transaction> getAllUnassignedEntities() {
        return Transaction.findAll("from Transaction as t where transactionNumber is null or transactionNumber = ''")
    }

    @Override
    void setIdentifierOnEntity(String id, Transaction entity) {
        entity.transactionNumber = id
    }
}
