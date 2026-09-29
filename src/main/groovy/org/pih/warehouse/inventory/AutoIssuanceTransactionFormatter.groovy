package org.pih.warehouse.inventory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.date.JavaUtilDateFormatter
import org.pih.warehouse.core.localization.MessageLocalizer

/**
 * Formats an AutoIssuanceTransactionDto for use in API responses.
 */
@Component
class AutoIssuanceTransactionFormatter {

    @Autowired
    MessageLocalizer messageLocalizer

    List<Map> toCsv(Collection<AutoIssuanceTransactionDto> objectList) {
        return objectList.collect { toCsv(it) }
    }

    Map toCsv(AutoIssuanceTransactionDto object) {
        return [
                productCode      : object.productCode,
                productName      : object.productName,
                binLocation      : object.binLocationName ?: messageLocalizer.localize("react.cycleCount.table.autoIssuance.defaultBinLocation.label"),
                transactionNumber: object.transactionNumber,
                transactionDate  : object.transactionDate ? JavaUtilDateFormatter.formatAsDate(object.transactionDate) : "",
                requisitionNumber: object.requisitionNumber,
                quantityReduced  : object.quantityReduced,
                dateLastCounted  : object.dateLastCounted ? JavaUtilDateFormatter.formatAsDate(object.dateLastCounted) : "",
                quantityOnHand   : object.quantityOnHand,
        ]
    }
}
