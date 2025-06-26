package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class InventoryAuditSummary implements Serializable {

    Product product
    Location facility
    String abcClass
    Integer quantityAdjusted = 0
    Integer countAdjustments = 0
    Integer countCycleCounts = 0
    BigDecimal amountAdjusted = 0
    Date lastCounted
    Integer quantityDemanded = 0
    Integer quantityOnHand = 0
    BigDecimal amountOnHand = 0

    static mapping = {
        version false
    }

    static constraints = {
        id composite: ['facility', 'product']
    }

    Map toJson() {
        return [
                facility           : [
                        id  : facility?.id,
                        name: facility?.name
                ],
                product            : [
                        id         : product?.id,
                        name       : product?.name,
                        productCode: product?.productCode,
                        category   : product?.category?.name,
                        abcClass   : abcClass,
                        tags       : product.tags.collect { [id: it.id, name: it.tag] },
                        catalogs   : product?.productCatalogs?.collect { [id: it.id, name: it.name] }
                ],
                lastCounted        : lastCounted,
                pricePerUnit       : product?.pricePerUnit,
                countCycleCounts   : countCycleCounts,
                countAdjustments   : countAdjustments,
                quantityAdjusted   : quantityAdjusted,
                amountAdjusted     : amountAdjusted,
                quantityDemanded   : quantityDemanded,
                monthsOfStockChange: quantityDemanded > 0 ? (quantityAdjusted / quantityDemanded) : 0,
                quantityOnHand     : quantityOnHand,
                amountOnHand       : amountOnHand
        ]
    }
}
