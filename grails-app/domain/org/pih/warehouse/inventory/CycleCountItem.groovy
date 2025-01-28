package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

class CycleCountItem {

    String id

    Location facility

    CycleCount cycleCount

    InventoryItem inventoryItem

    Product product

    User assignee

    Integer countIndex

    CycleCountItemStatus status

    Integer quantityOnHand

    Integer quantityCounted

    DiscrepancyCause discrepancyCause

    String comment

    Boolean draft

    Boolean custom

    Date dateCounted

    Date dateCreated

    Date lastUpdated

    User createdBy

    User updatedBy

    CycleCountItemBasicDto toBasicDto() {
        return new CycleCountItemBasicDto(
                id: id,
                facility: facility.toBaseJson(),
                product: product,
                inventoryItem: inventoryItem,
                countIndex: countIndex,
                status: status,
        )
    }

    static constraints = {
        dateCounted(nullable: true)
        comment(nullable: true)
        assignee(nullable: true)
        quantityCounted(nullable: true)
        discrepancyCause(nullable: true)
    }
}
