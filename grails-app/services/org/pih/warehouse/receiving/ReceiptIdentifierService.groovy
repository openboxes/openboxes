package org.pih.warehouse.receiving

import grails.gorm.transactions.Transactional

import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.identification.BlankIdentifierResolver

@Transactional
class ReceiptIdentifierService extends IdentifierService<Receipt> implements BlankIdentifierResolver<Receipt> {

    @Override
    String getIdentifierName() {
        return "receipt"
    }

    @Override
    protected Integer countByIdentifier(String id) {
        return Receipt.countByReceiptNumber(id)
    }

    @Override
    List<Receipt> getAllUnassignedEntities() {
        return Receipt.findAll("from Receipt as s where receiptNumber is null or receiptNumber = ''")
    }

    @Override
    void setIdentifierOnEntity(String id, Receipt receipt) {
        receipt.receiptNumber = id
    }
}
