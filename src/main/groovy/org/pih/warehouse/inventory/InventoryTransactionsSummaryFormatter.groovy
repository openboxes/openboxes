package org.pih.warehouse.inventory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.date.JavaUtilDateFormatter
import org.pih.warehouse.core.localization.MessageLocalizer

/**
 * Formats a InventoryTransactionsSummary for use in API responses.
 */
@Component
class InventoryTransactionsSummaryFormatter {

    @Autowired
    MessageLocalizer messageLocalizer

    List<Map> toCsv(Collection<InventoryTransactionsSummary> objectList) {
        return objectList.collect { toCsv(it) }
    }

    Map toCsv(InventoryTransactionsSummary object) {
        return [
                alignment: object.varianceTypeCode,
                name: object.product?.name,
                productCode: object.product?.productCode,
                type: object.transactionAction,
                dateRecorded: JavaUtilDateFormatter.formatAsDate(object.dateRecorded),
                recordedBy: object.recordedBy.name,
                transactionId: object.transaction.transactionNumber,
                quantityBefore: object.quantityBefore,
                quantityAfter: object.quantityAfter,
                quantityDifference: object.quantityDifference,
                rootCauses: messageLocalizer.localizeEnumValues(object.rootCauses).join(", "),
                comments: object.comments?.join(", ") ?: "",
        ]
    }
}
