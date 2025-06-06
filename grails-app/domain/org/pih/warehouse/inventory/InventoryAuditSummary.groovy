package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class InventoryAuditSummary implements Serializable {

    Product product
    Location facility

    String abcClass

    Integer quantityAdjusted

    static mapping = {
        version false
    }

    static constraints = {
        id composite: ['facility', 'product']
    }

    Map toJson() {
        return [
                facility           : [id: facility?.id, name: facility?.name],
                product            : [id: product?.id, name: product?.name],
                category           : product?.category?.name,
                tags               : product.tags.collect { [id: it.id, name: it.tag] },
                catalogs           : product?.productCatalogs?.collect { [id: it.id, name: it.name] },
                abcClass           : abcClass,
                lastCounted        : new Date(),
                quantityTotal      : quantityAdjusted,
                numberOfCounts     : 0,
                numberOfAdjustments: 0,
                quantityAdjusted   : 0,
                valueAdjusted      : 0,
                monthsOfStockChange: 0,
                quantityOnHand     : 0,
                valueOnHand        : 0,

        ]
    }


}
