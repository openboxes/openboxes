package org.pih.warehouse.inventory.product.availability

import org.pih.warehouse.product.Product

class InventoryByProduct {
    Integer quantityOnHand
    Integer finalQuantityAvailableToPromise
    Integer expiredQuantityAvailableToPromise
    Product product
}
