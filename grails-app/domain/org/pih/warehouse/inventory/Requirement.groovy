package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class Requirement implements Serializable {
    String id
    Product product
    Location location
    Location binLocation
    Integer quantityInBin // QoH in bin
    Integer minQuantity
    Integer maxQuantity
    Integer reorderQuantity
    Integer totalQuantityOnHand // Total QoH for this product in Depot
    InventoryLevelStatus status

    static constraints = {
        id(nullable: true)
        product(nullable: true)
        location(nullable: true)
        binLocation(nullable: true)
        quantityInBin(nullable: true)
        minQuantity(nullable: true)
        maxQuantity(nullable: true)
        reorderQuantity(nullable: true)
        totalQuantityOnHand(nullable: true)
        status(nullable: true)
    }

    Map toJson() {
        return [
            id                      : id,
            "product.id"            : product?.id,
            "product.productCode"   : product?.productCode,
            "product.name"          : product?.name,
            "binLocation.id"        : binLocation?.id,
            "binLocation.name"      : binLocation?.name,
            "zone"                  : binLocation?.zone?.name,
            quantityInBin           : quantityInBin,
            minQuantity             : minQuantity,
            maxQuantity             : maxQuantity,
            totalQuantityOnHand     : totalQuantityOnHand
        ]
    }
}
