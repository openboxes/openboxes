package org.pih.warehouse.product.lot

import org.apache.commons.lang.builder.HashCodeBuilder

import org.pih.warehouse.product.Product

/**
 * Represents a specific lot number of a product. Lot numbers represent a group of a product instances that
 * were all made under the same conditions. As such, the lot is typically controlled by the product supplier.
 * Note that product lot has no association with an inventory. It is tied only to the product itself.
 *
 * This class acts like a simplified InventoryItem. We'd like to one day refactor InventoryItem to either use
 * or be more like this class, but for now this class is a simple POJO.
 */
class ProductLot {

    // Primary Key
    Product product
    String lotNumber

    Date expirationDate

    @Override
    boolean equals(Object o) {
        if (!(o instanceof ProductLot)) {
            return false
        }
        ProductLot that = (ProductLot) o

        // Only product and lotNumber determine uniqueness. We shouldn't have two ProductLots with the same
        // product and lotNumber but a different expirationDate, but we let that validation be checked elsewhere.
        return this.product == that.product && this.lotNumber == that.lotNumber
    }

    @Override
    int hashCode() {
        return new HashCodeBuilder()
                .append(product?.id)
                .append(lotNumber)
                .toHashCode()
    }
}
