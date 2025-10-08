package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class InventoryAuditDetails implements Serializable {

    // Inventory item dimensions
    Product product
    InventoryItem inventoryItem
    Location facility
    Location location
    String abcClass

    // Transaction dimensions
    CycleCount cycleCount
    Date transactionDate
    TransactionType transactionType
    String transactionNumber

    // Facts
    Integer quantityAdjusted
    Integer quantityOnHand
    BigDecimal pricePerUnit

    static mapping = {
        version false
        id composite: ['facility', 'inventoryItem', 'location']
    }


    static constraints = {

    }

    Map toJson() {
        return [
                facility         : [
                        id            : facility.id,
                        locationNumber: facility.locationNumber,
                        name          : facility.name],
                inventoryItem    : [
                        id            : inventoryItem?.id,
                        location      : [id: location.id, name: location.name],
                        product       : [
                                id         : product?.id,
                                productCode: product?.productCode,
                                name       : product.name,
                                abcClass   : abcClass,
                                category   : product?.category?.name,
                                tags       : product.tags.collect { [id: it.id, name: it.tag] },
                                catalogs   : product?.productCatalogs.collect { [id: it.id, name: it.name] }
                        ],
                        lotNumber     : inventoryItem?.lotNumber,
                        expirationDate: inventoryItem?.expirationDate
                ],
                transactionType  : [id: transactionType?.id, name: transactionType?.name, operation: transactionType?.transactionCode?.name()],
                transactionNumber: transactionNumber,
                transactionDate  : transactionDate,
                cycleCount       : [id: cycleCount?.id, status: cycleCount?.status?.name()],
                quantityAdjusted : quantityAdjusted ?: 0,
                amountAdjusted   : quantityAdjusted ?: 0 * (product?.pricePerUnit ?: 0),
                quantityOnHand   : quantityOnHand ?: 0,
                amountOnHand     : quantityOnHand ?: 0 * (product?.pricePerUnit ?: 0)
        ]
    }

}
