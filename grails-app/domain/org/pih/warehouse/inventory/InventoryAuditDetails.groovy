package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class InventoryAuditDetails implements Serializable {

    Product product
    InventoryItem inventoryItem
    Location facility
    Location location

    Date transactionDate
    TransactionType transactionType
    String transactionNumber

    String abcClass
    Integer quantity
    Integer quantityOnHand


    static mapping = {
        version false
        id composite: ['facility', 'inventoryItem', 'location']
    }


    static constraints = {

    }

    Map toJson() {
        return [
                facility         : [id: facility.id, name: facility.name],
                inventoryItem    : inventoryItem,
                location         : [id: location.id, name: location.name],
                transactionType  : transactionType,
                transactionNumber: transactionNumber,
                transactionDate  : transactionType,
                quantityAdjusted : quantity?:0,
                amountAdjusted   : quantity?:0 * (product?.pricePerUnit ?: 0),
                quantityOnHand   : quantityOnHand?:0,
                amountOnHand     : quantityOnHand?:0 * (product?.pricePerUnit ?: 0),
                tags             : product.tags.collect { [id: it.id, name: it.tag ]},
                category         : product?.category?.name,
                catalogs         : product?.productCatalogs.collect { [id: it.id, name: it.name ]},
                abcClass         : abcClass,
        ]
    }

}
