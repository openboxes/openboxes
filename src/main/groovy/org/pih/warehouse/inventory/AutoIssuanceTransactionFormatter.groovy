package org.pih.warehouse.inventory

import org.springframework.stereotype.Component

import org.pih.warehouse.core.date.JavaUtilDateFormatter

/**
 * Formats an AutoIssuanceTransactionDto for use in API responses.
 */
@Component
class AutoIssuanceTransactionFormatter {

    List<Map> toCsv(Collection<AutoIssuanceTransactionDto> objectList) {
        return objectList.collect { toCsv(it) }
    }

    Map toCsv(AutoIssuanceTransactionDto object) {
        return [
                productCode      : object.productCode,
                productName      : object.productName,
                binLocation      : object.binLocationName,
                transactionNumber: object.transactionNumber,
                transactionDate  : object.transactionDate ? JavaUtilDateFormatter.formatAsDate(object.transactionDate) : "",
                requisitionNumber: object.requisitionNumber,
                quantityReduced  : object.quantityReduced,
                dateLastCounted  : object.dateLastCounted ? JavaUtilDateFormatter.formatAsDate(object.dateLastCounted) : "",
                quantityOnHand   : object.quantityOnHand,
        ]
    }
}
