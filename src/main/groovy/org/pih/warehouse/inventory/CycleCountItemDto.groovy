package org.pih.warehouse.inventory

import org.pih.warehouse.product.Product

class CycleCountItemDto {

    String id

    Map facility

    Product product

    InventoryItem inventoryItem

    Integer countIndex

    CycleCountItemStatus status

    Integer quantityOnHand

    Integer quantityCounted

    DiscrepancyReasonCode discrepancyReasonCode

    String comment

    Boolean custom

    Date dateCounted
}
