package org.pih.warehouse.inventory

import org.pih.warehouse.product.Product

class CycleCountItemBasicDto {

    String id

    Map facility

    Product product

    InventoryItem inventoryItem

    Integer countIndex

    CycleCountItemStatus status
}
