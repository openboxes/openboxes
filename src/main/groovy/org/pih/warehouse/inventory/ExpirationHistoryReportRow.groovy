package org.pih.warehouse.inventory

import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product


class ExpirationHistoryReportRow {
    String transactionNumber
    Date transactionDate
    String productCode
    String productName
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
                transactionDate: transactionEntry.transaction.transactionDate,
                productCode: product.productCode,
                productName: product.displayNameOrDefaultName,
                category: product.category,
                lotNumber: transactionEntry.inventoryItem.lotNumber,
                expirationDate: transactionEntry.inventoryItem.expirationDate,
                quantityLostToExpiry: transactionEntry.quantity,
                unitPrice: product.pricePerUnit,
                valueLostToExpiry: product.pricePerUnit ? product.pricePerUnit * transactionEntry.quantity : null,
        )
    }
}
