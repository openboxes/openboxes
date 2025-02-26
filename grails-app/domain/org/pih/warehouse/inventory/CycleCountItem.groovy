package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
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

    User assignee

    Integer countIndex

    CycleCountItemStatus status

    Integer quantityOnHand

    Integer quantityCounted

    DiscrepancyReasonCode discrepancyReasonCode

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
                discrepancyReasonCode: discrepancyReasonCode,
                dateCounted: dateCounted,
                comment: comment,
                custom: custom
        )
    }

    static constraints = {
        dateCounted(nullable: true)
        comment(nullable: true)
        assignee(nullable: true)
        quantityCounted(nullable: true)
        discrepancyReasonCode(nullable: true)
        location(nullable: true)
    }

    @Override
    int compareTo(Object that) {
        return inventoryItem?.expirationDate <=> that.inventoryItem?.expirationDate ?: inventoryItem?.lotNumber <=> that.inventoryItem?.lotNumber
    }
}
