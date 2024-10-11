package org.pih.warehouse.receiving

import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.identification.BlankIdentifierResolver

class ReceiptIdentifierService extends IdentifierService implements BlankIdentifierResolver<Receipt> {

    @Override
    String getEntityKey() {
        return "receipt"
    }

    @Override
    protected Integer countDuplicates(String receiptNumber) {
        return Receipt.countByReceiptNumber(receiptNumber)
    }

    @Override
    List<Receipt> getAllUnassignedEntities() {
        return Receipt.findAll("from Receipt as s where receiptNumber is null or receiptNumber = ''")
    }

    @Override
    void setIdentifierOnEntity(String receiptNumber, Receipt receipt) {
        receipt.receiptNumber = receiptNumber
    }
}
