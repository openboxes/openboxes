package org.pih.warehouse.reporting

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class CycleCountProductSummary implements Serializable {

    String cycleCountId
    Date dateCounted
    Location facility
    Product product
    Integer quantityVariance
    Date lastUpdated

    static mapping = {
        version false
    }

    static constraints = {
        id composite: ['facility', 'product', 'cycleCountId']
    }

    Map toJson() {
        return [
                cycleCountId     : cycleCountId,
                dateCounted      : dateCounted,
                facility         : [
                        id  : facility?.id,
                        name: facility?.name
                ],
                product          : [
                        id  : product?.id,
                        name: product?.name
                ],
                quantityVariance : quantityVariance,
                lastUpdated      : lastUpdated
        ]
    }
}
