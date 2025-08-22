package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.core.VarianceTypeCode
import org.pih.warehouse.product.Product

class InventoryTransactionsSummary implements Serializable {

    String id

    Transaction transaction

    Product product

    Location facility

    Integer quantityBefore

    Integer quantityAfter

    Integer quantityDifference

    Date dateRecorded

    User recordedBy

    Transaction baselineTransaction


    static constraints = {
        table "inventory_transactions_summary"
        version false
    }

    /**
     * Root causes are only captured for the cycle count
     */
    Set<String> getRootCauses() {
        if (transaction.cycleCount) {
            return transaction
                    .cycleCount
                    .cycleCountItems
                    .discrepancyReasonCode
                    // filter out null/empty
                    .findAll { it }
                    .collect { it.name() }
                    .toSet()
        }
        return null
    }

    List<String> getComments() {
        List<String> entriesComments = transaction.transactionEntries
                .findAll { it.product?.id == product?.id && it.comments }
                .collect { it.comments }
        String transactionComment = transaction.comment
        // Concat "global" transaction comment with entries comments and filter out empty comments
        return [transactionComment, entriesComments].findAll { it }
    }

    VarianceTypeCode getVarianceTypeCode() {
        if (quantityDifference > 0) {
            return VarianceTypeCode.MORE
        }
        if (quantityDifference < 0) {
            return VarianceTypeCode.LESS
        }
        return VarianceTypeCode.EQUAL
    }

    /**
     * Transaction type display name used in the "Inventory Transactions" table
     * possible options are: CC (Cycle count transaction), IA (Inventory adjustment), RS (Record stock), II (Inventory import)
     */
    TransactionAction getTransactionAction() {
        if (transaction.cycleCount) {
            return TransactionAction.CYCLE_COUNT
        }
        // FIXME: For now we don't have any clear way to distinguish inventory import vs record stock
        if (transaction.comment?.contains("Imported from")) {
            return TransactionAction.INVENTORY_IMPORT
        }
        if (!baselineTransaction) {
            return TransactionAction.INVENTORY_ADJUSTMENT
        }
        return TransactionAction.RECORD_STOCK
    }

    Map toJson() {
        [
                id: id,
                transaction: [
                    transactionNumber: transaction.transactionNumber
                ],
                product: [
                    id: product.id,
                    name: product.name,
                    productCode: product.productCode,
                    displayName: product.displayNameOrDefaultName,
                ],
                facility: facility.toBaseJson(),
                quantityBefore: quantityBefore,
                quantityAfter: quantityAfter,
                quantityDifference: quantityDifference,
                dateRecorded: dateRecorded,
                recordedBy: [
                    name: recordedBy.name,
                ],
                rootCauses: rootCauses,
                comments: comments,
                varianceTypeCode: varianceTypeCode?.name(),
                transactionAction: transactionAction.name(),
        ]
    }
}
