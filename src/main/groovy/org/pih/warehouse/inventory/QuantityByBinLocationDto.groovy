package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

class QuantityByBinLocationDto implements Comparable<QuantityByBinLocationDto> {
    String id

    String status

    String value

    Category category

    Product product

    InventoryItem inventoryItem

    Location binLocation

    Integer quantity

    Boolean isOnHold

    @Override
    int compareTo(QuantityByBinLocationDto obj) {
        inventoryItem?.expirationDate <=> obj?.inventoryItem?.expirationDate
                ?: binLocation?.name <=> obj?.binLocation?.name
    }
}
