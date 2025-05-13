package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.VarianceTypeCode
import org.pih.warehouse.product.Product

class CycleCountFinalCountDetails implements Serializable {

    Location facility
    Location location
    Product product
    InventoryItem inventoryItem

    // Transaction details
    String transactionNumber
    Date transactionDate
    Date dateRequested
    Person requestedBy
    Date dateStarted
    Person startedBy
    Date dateRecorded
    Person recordedBy

    // Final count details
    Date dateCounted
    Person countAssignee
    Integer quantityOnHand
    Integer quantityCounted
    Integer quantityVariance

    // FIXME Should eventually be a list of enums
    String varianceReasonCode
    String varianceComments

    // Additional details
    CycleCount cycleCount
    CycleCountItem cycleCountItem


    static transients = ["varianceTypeCode"]

    static constraints = {
        version false
        // FIXME - this is not the correct approach, but i needed a temporary solution to get this domain working
        id composite: ['cycleCount', 'product', 'inventoryItem', 'location']
        table "cycle_count_final_count_details"
    }

    VarianceTypeCode getVarianceTypeCode() {
        if (quantityVariance > 0) return VarianceTypeCode.MORE
        else if (quantityVariance < 0) return VarianceTypeCode.LESS
        else return VarianceTypeCode.EQUAL
    }

    Map toJson() {
        return [
                transactionDetails: [
                        id               : "TBD",
                        transactionNumber: transactionNumber,
                        transactionType  : "Cycle Count",
                        transactionDate  : transactionDate,
                        dateRequested    : dateRequested,
                        requestedBy      : [id: requestedBy?.id, name: requestedBy?.name],
                        dateStarted      : dateStarted,
                        startedBy        : [id: startedBy?.id, name: startedBy?.name],
                        dateRecorded     : dateRecorded,
                        recordedBy       : [id: recordedBy?.id, name: recordedBy?.name],
                ],
                inventoryItem     : [
                        product         : [
                                id         : product.id,
                                name       : product.name,
                                productCode: product.productCode
                        ],
                        lotNumber       : inventoryItem.lotNumber,
                        expirationDate  : inventoryItem.expirationDate,
                        internalLocation: location.toJson(LocationTypeCode.INTERNAL),
                ],
                initialCount      : [
                        assignee          : [id: countAssignee?.id, name: countAssignee?.name],
                        dateCounted       : dateCounted,
                        quantityOnHand    : quantityOnHand,
                        quantityCounted   : quantityCounted,
                        quantityVariance  : quantityVariance,
                        varianceTypeCode  : varianceTypeCode.name(),
                        varianceReasonCode: varianceReasonCode,
                        varianceComments  : varianceComments,

                ],
                finalCount        : [
                        assignee          : [id: countAssignee?.id, name: countAssignee?.name],
                        dateCounted       : dateCounted,
                        quantityOnHand    : quantityOnHand,
                        quantityCounted   : quantityCounted,
                        quantityVariance  : quantityVariance,
                        varianceTypeCode  : varianceTypeCode.name(),
                        varianceReasonCode: varianceReasonCode,
                        varianceComments  : varianceComments,
                ],
        ]
    }
}
