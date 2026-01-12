package org.pih.warehouse.inventory

import org.pih.warehouse.core.date.JavaUtilDateFormatter
import org.pih.warehouse.core.localization.MessageLocalizer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Formats a InventoryAuditSummary for use in API responses.
 */
@Component
class InventoryAuditSummaryFormatter {

    @Autowired
    MessageLocalizer messageLocalizer

    List<Map> toCsv(Collection<InventoryAuditSummary> objectList) {
        return objectList.collect { toCsv(it) }
    }

    Map toCsv(InventoryAuditSummary object) {
        return [
                product: object.product.name,
                category: object.product.category?.name,
                tag: object.product.tagsToString(),
                'product catalogue': object.product.productCatalogsToString(),
                'abc class': object.abcClass ?: '',
                'number of counts': object.countCycleCounts,
                'number of adjustments': object.countAdjustments,
                'total of adjustments': object.quantityAdjusted,
                'adjustments value': object.amountAdjusted,
                'months of stock change': object.monthsOfStockChange,
                'currently in stock': object.quantityOnHand,
                'value in stock': object.amountOnHand,
        ]
    }
}
