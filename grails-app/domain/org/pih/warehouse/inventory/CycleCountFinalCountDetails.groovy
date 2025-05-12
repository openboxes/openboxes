package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.User
import org.pih.warehouse.core.VarianceTypeCode
import org.pih.warehouse.product.Product

class CycleCountFinalCountDetails implements Serializable {

    Location facility
    Location location
    Product product
    InventoryItem inventoryItem

    CycleCount cycleCount
    CycleCountItem cycleCountItem
    Date dateCounted

    User countAssignee
    User recountAssignee

    String transactionNumber
    Date transactionDate

    Integer quantityOnHand
    Integer quantityCounted
    Integer quantityVariance

    String comments

    // FIXME Should eventually be a list of enums
    String rootCause

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
                transactionDetails: [
                        id               : "TBD",
                        transactionNumber: transactionNumber,
                        transactionType  : "Cycle Count",
                        transactionDate  : transactionDate,
                ],
                initialCount      : [
                        dateCounted     : dateCounted,
                        quantityOnHand  : quantityOnHand,
                        quantityCounted : quantityCounted,
                        quantityVariance: quantityVariance,
                        varianceTypeCode: varianceTypeCode.name(),
                        assignee        : [id: countAssignee?.id, name: countAssignee?.name],
                ],
                finalCount        : [
                        dateCounted     : dateCounted,
                        quantityOnHand  : quantityOnHand,
                        quantityCounted : quantityCounted,
                        quantityVariance: quantityVariance,
                        varianceTypeCode: varianceTypeCode.name(),
                        assignee        : [id: recountAssignee?.id, name: recountAssignee?.name],
                ],
                comments          : comments,
                rootCause         : rootCause,
        ]
    }

}
