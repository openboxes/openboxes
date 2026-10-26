package org.pih.warehouse.inventory

/**
 * Represents a single row of the Auto-Issuance Transactions report: the quantity of one product
 * reduced from one bin location by one outbound (TRANSFER_OUT) stock movement, caused by a requisition
 * that was auto-issued (autoIssuanceRequested). All fields are plain scalars read straight off the
 * report's native SQL row, since only display values are needed here.
 */
class AutoIssuanceTransactionDto {

    String productId
    String productCode
    String productName
    String binLocationId
    String binLocationName
    String transactionId
    String transactionNumber
    Date transactionDate
    String requisitionId
    String requisitionNumber
    Integer quantityReduced
    Date dateLastCounted
    Integer quantityOnHand

    Map asResponseBody() {
        return [
                product        : [
                        id         : productId,
                        productCode: productCode,
                        name       : productName,
                ],
                binLocation    : binLocationId ? [
                        id  : binLocationId,
                        name: binLocationName,
                ] : null,
                transaction    : [
                        id               : transactionId,
                        transactionNumber: transactionNumber,
                        transactionDate  : transactionDate,
                ],
                requisition    : [
                        id           : requisitionId,
                        requestNumber: requisitionNumber,
                ],
                quantityReduced: quantityReduced,
                dateLastCounted: dateLastCounted,
                quantityOnHand : quantityOnHand,
        ]
    }
}
