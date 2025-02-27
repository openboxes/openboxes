package org.pih.warehouse.inventory

import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

class CycleCountItemDto {

    String id

    Map facility

    Map binLocation

    Product product

    InventoryItem inventoryItem

    Integer countIndex

    CycleCountItemStatus status

    Integer quantityOnHand

    Integer quantityCounted

    Integer quantityVariance

    DiscrepancyReasonCode discrepancyReasonCode

    String comment

    Boolean custom

    Date dateCounted

    User assignee
}
