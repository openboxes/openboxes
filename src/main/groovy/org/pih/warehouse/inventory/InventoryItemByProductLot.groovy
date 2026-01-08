package org.pih.warehouse.inventory


import org.pih.warehouse.product.Product
import org.pih.warehouse.product.lot.ProductLot

/**
 * A simple convenience wrapper on hashmap representing a map of inventory items keyed on product lot.
 */
class InventoryItemByProductLot extends HashMap<ProductLot, InventoryItem> {

    /**
     * Fetch an InventoryItem by product and lot number. ProductLot is unique on these two fields alone,
     * so we don't need to bother passing any other fields (such as expirationDate).
     */
    InventoryItem get(Product product, String lotNumber) {
        //InventoryItem x = get(new ProductLot(product: product, lotNumber: lotNumber))
        return get(new ProductLot(product: product, lotNumber: lotNumber))
    }
}
