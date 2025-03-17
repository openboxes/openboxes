package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

class CycleCountItem implements Comparable {

    String id

    Location facility

    CycleCount cycleCount

    InventoryItem inventoryItem

    // Bin location
    Location location

    Product product

    Person assignee

    Integer countIndex

    CycleCountItemStatus status

    Integer quantityOnHand

    Integer quantityCounted

    ReasonCode discrepancyReasonCode

    String comment

    Boolean custom

    Date dateCounted

    Date dateCreated

    Date lastUpdated

    User createdBy

    User updatedBy

    CycleCountItemDto toDto() {
        return new CycleCountItemDto(
                id: id,
                facility: facility.toBaseJson(),
                product: product,
                inventoryItem: inventoryItem,
                binLocation: location?.toBaseJson(),
                countIndex: countIndex,
                status: status,
                quantityOnHand: quantityOnHand,
                quantityCounted: quantityCounted,
                quantityVariance: quantityVariance,
                discrepancyReasonCode: discrepancyReasonCode,
                dateCounted: dateCounted,
                comment: comment,
                custom: custom,
                assignee: assignee,
        )
    }

    static constraints = {
        dateCounted(nullable: true)
        comment(nullable: true)
        assignee(nullable: true)
        quantityCounted(nullable: true)
        discrepancyReasonCode(nullable: true, validator: { ReasonCode discrepancyReasonCode ->
            if (!discrepancyReasonCode) {
                return true
            }

            return ReasonCode.listInventoryAdjustmentReasonCodes().contains(discrepancyReasonCode) ?
                    true :
                    ['cycleCountItem.discrepancyReasonCode.invalid']
        })
        location(nullable: true)
    }

    Integer getQuantityVariance() {
        if (quantityCounted != null && quantityOnHand != null) {
            return quantityCounted - quantityOnHand
        }
        return null
    }

    @Override
    int compareTo(Object that) {
        int diff = inventoryItem?.expirationDate <=> that.inventoryItem?.expirationDate
                ?: location?.name <=> that.location?.name
        if (diff == 0) {
            return -1
        }
        return diff
    }
}
