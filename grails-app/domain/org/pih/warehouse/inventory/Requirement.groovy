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
    Integer totalQuantityAvailableToPromise // Total QATP for this product in Depot
    Integer quantityAvailable // quantityAvailable = quantity available to replenish = total qatp - qatp in bin
    InventoryLevelStatus status

    static mapping = {
        id generator: 'uuid'
        version false
        cache usage: "read-only"
    }

    static constraints = {
        id(nullable: true)
        product(nullable: true)
        location(nullable: true)
        binLocation(nullable: true)
        quantityInBin(nullable: true)
        minQuantity(nullable: true)
        maxQuantity(nullable: true)
        reorderQuantity(nullable: true)
        totalQuantityAvailableToPromise(nullable: true)
        quantityAvailable(nullable: true)
        status(nullable: true)
    }


    def getQuantityNeeded() {
        def qtyNeeded = getQuantityAvailable() > maxQuantity - quantityInBin ? maxQuantity - quantityInBin : getQuantityAvailable()
        return qtyNeeded > 0 ? qtyNeeded : 0
    }

    static transients = ['quantityNeeded']

    Map toJson() {
        return [
            id                                  : id,
            "product.id"                        : product?.id,
            "product.productCode"               : product?.productCode,
            "product.name"                      : product?.name,
            "binLocation.id"                    : binLocation?.id,
            "binLocation.name"                  : binLocation?.name,
            "zone"                              : binLocation?.zone?.name,
            quantityInBin                       : quantityInBin,
            minQuantity                         : minQuantity,
            maxQuantity                         : maxQuantity,
            quantityNeeded                      : quantityNeeded,
            quantityAvailable                   : quantityAvailable
        ]
    }
}
