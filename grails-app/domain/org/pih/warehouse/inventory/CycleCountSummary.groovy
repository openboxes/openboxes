package org.pih.warehouse.inventory

import java.time.Instant
import java.time.LocalDate

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.VarianceTypeCode
import org.pih.warehouse.product.Product

class CycleCountSummary implements Serializable {

    Location facility
    Location location
    Product product

    // Transaction details
    String transactionNumber
    LocalDate dateRequested
    Person requestedBy
    Instant dateInitiated
    Person initiatedBy
    Instant dateRecorded
    Person recordedBy

    // Blind count details
    LocalDate blindCountDateCounted
    Person blindCountAssignee
    Integer blindCountQuantityOnHand
    Integer blindCountQuantityCounted
    Integer blindCountQuantityVariance
    String blindCountVarianceReasonCode
    String blindCountVarianceComment

    // Verification count details
    LocalDate verificationCountDateCounted
    Person verificationCountAssignee
    Integer verificationCountQuantityOnHand
    Integer verificationCountQuantityCounted
    Integer verificationCountQuantityVariance
    String verificationCountVarianceReasonCode
    String verificationCountVarianceComment

    // Additional details
    CycleCount cycleCount

    static mapping = {
        version false
        table "cycle_count_summary"
    }

    VarianceTypeCode getVarianceTypeCode(Integer quantityVariance) {
        if (quantityVariance > 0) return VarianceTypeCode.MORE
        else if (quantityVariance < 0) return VarianceTypeCode.LESS
        else return VarianceTypeCode.EQUAL
    }

    VarianceTypeCode getBlindCountVarianceTypeCode() {
        return getVarianceTypeCode(blindCountQuantityVariance)
    }

    VarianceTypeCode getVerificationCountVarianceTypeCode() {
        return getVarianceTypeCode(verificationCountQuantityVariance)
    }


    Map toJson() {
        return [
                id: id,
                cycleCount       : [
                        id               : cycleCount?.id,
                        transactionNumber: transactionNumber,
                        transactionType  : "Cycle Count",
                        dateRequested    : dateRequested,
                        dateInitiated    : dateInitiated,
                        dateRecorded     : dateRecorded,
                        requestedBy      : [id: requestedBy?.id, name: requestedBy?.name],
                        initiatedBy      : [id: initiatedBy?.id, name: initiatedBy?.name],
                        recordedBy       : [id: recordedBy?.id, name: recordedBy?.name],

                ],
                product         : [
                        id         : product?.id,
                        name       : product?.name,
                        productCode: product?.productCode
                ],
                blindCount       : [
                        assignee          : [id: blindCountAssignee?.id, name: blindCountAssignee?.name],
                        dateCounted       : blindCountDateCounted,
                        quantityOnHand    : blindCountQuantityOnHand,
                        quantityCounted   : blindCountQuantityCounted,
                        quantityVariance  : blindCountQuantityVariance,
                        varianceTypeCode  : blindCountVarianceTypeCode?.name(),
                        varianceReasonCode: blindCountVarianceReasonCode,
                        varianceComment   : blindCountVarianceComment,

                ],
                verificationCount: [
                        assignee          : [id: verificationCountAssignee?.id, name: verificationCountAssignee?.name],
                        dateCounted       : verificationCountDateCounted,
                        quantityOnHand    : verificationCountQuantityOnHand,
                        quantityCounted   : verificationCountQuantityCounted,
                        quantityVariance  : verificationCountQuantityVariance,
                        varianceTypeCode  : verificationCountVarianceTypeCode?.name(),
                        varianceReasonCode: verificationCountVarianceReasonCode,
                        varianceComment   : verificationCountVarianceComment,
                ],
        ]
    }
}
