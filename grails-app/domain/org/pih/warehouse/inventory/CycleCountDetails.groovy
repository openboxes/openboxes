package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.VarianceTypeCode
import org.pih.warehouse.product.Product

class CycleCountDetails implements Serializable {

    Location facility
    Location location
    Product product
    InventoryItem inventoryItem

    // Transaction details
    String transactionNumber
    Date dateRequested
    Person requestedBy
    Date dateInitiated
    Person initiatedBy
    Date dateRecorded
    Person recordedBy

    // Blind count details
    Date blindCountDateCounted
    Person blindCountAssignee
    Integer blindCountQuantityOnHand
    Integer blindCountQuantityCounted
    Integer blindCountQuantityVariance
    String blindCountVarianceReasonCode
    String blindCountVarianceComment

    // Verification count details
    Date verificationCountDateCounted
    Person verificationCountAssignee
    Integer verificationCountQuantityOnHand
    Integer verificationCountQuantityCounted
    Integer verificationCountQuantityVariance
    String verificationCountVarianceReasonCode
    String verificationCountVarianceComment

    // Additional details
    CycleCount cycleCount


    static transients = ["varianceTypeCode"]

    static constraints = {
        version false
        table "cycle_count_details"
    }

    VarianceTypeCode getVarianceTypeCode(Integer quantityVariance) {
        if (quantityVariance > 0) return VarianceTypeCode.MORE
        else if (quantityVariance < 0) return VarianceTypeCode.LESS
        else return VarianceTypeCode.EQUAL
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
                inventoryItem    : [
                        product         : [
                                id         : product?.id,
                                name       : product?.name,
                                productCode: product?.productCode
                        ],
                        lotNumber       : inventoryItem?.lotNumber,
                        expirationDate  : inventoryItem?.expirationDate,
                        internalLocation: location?.toJson(LocationTypeCode.INTERNAL)
                ],
                blindCount       : [
                        assignee          : [id: blindCountAssignee?.id, name: blindCountAssignee?.name],
                        dateCounted       : blindCountDateCounted,
                        quantityOnHand    : blindCountQuantityOnHand,
                        quantityCounted   : blindCountQuantityCounted,
                        quantityVariance  : blindCountQuantityVariance,
                        varianceTypeCode  : getVarianceTypeCode(blindCountQuantityVariance)?.name(),
                        varianceReasonCode: blindCountVarianceReasonCode,
                        varianceComment   : blindCountVarianceComment,

                ],
                verificationCount: [
                        assignee          : [id: verificationCountAssignee?.id, name: verificationCountAssignee?.name],
                        dateCounted       : verificationCountDateCounted,
                        quantityOnHand    : verificationCountQuantityOnHand,
                        quantityCounted   : verificationCountQuantityCounted,
                        quantityVariance  : verificationCountQuantityVariance,
                        varianceTypeCode  : getVarianceTypeCode(verificationCountQuantityVariance)?.name(),
                        varianceReasonCode: verificationCountVarianceReasonCode,
                        varianceComment   : verificationCountVarianceComment,
                ],
        ]
    }
}
