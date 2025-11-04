package org.pih.warehouse.inventory

import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product


class ExpirationHistoryReportRow {
    String transactionNumber
    String transactionId
    Date transactionDate
    String productCode
    String productName
    String productId
    Category category
    String lotNumber
    Date expirationDate
    Integer quantityLostToExpiry
    BigDecimal unitPrice
    BigDecimal valueLostToExpiry

    static fromTransactionEntry(TransactionEntry transactionEntry) {
        Product product = transactionEntry.inventoryItem.product
        return new ExpirationHistoryReportRow(
                transactionNumber: transactionEntry.transaction.transactionNumber,
                transactionId: transactionEntry.transaction.id,
                transactionDate: transactionEntry.transaction.transactionDate,
                productCode: product.productCode,
                productName: product.displayNameOrDefaultName,
                productId: product.id,
                category: product.category,
                lotNumber: transactionEntry.inventoryItem.lotNumber,
                expirationDate: transactionEntry.inventoryItem.expirationDate,
                quantityLostToExpiry: transactionEntry.quantity,
                unitPrice: product.pricePerUnit,
                valueLostToExpiry: product.pricePerUnit ? product.pricePerUnit * transactionEntry.quantity : null,
        )
    }
}
