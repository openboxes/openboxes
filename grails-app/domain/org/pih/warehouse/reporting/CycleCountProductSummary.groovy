package org.pih.warehouse.reporting

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class CycleCountProductSummary implements Serializable {

    String cycleCountId
    Date dateCounted
    Location facility
    Product product
    Boolean hasVariance

    static mapping = {
        version false
        id composite: ['facility', 'product']
    }

    static constraints = {

    }

    Map toJson() {
        return [
                cycleCountId: cycleCountId,
                dateCounted : dateCounted,
                facility    : [id: facility?.id, name: facility?.name],
                product     : [id: product?.id, name: product?.name],
                hasVariance : hasVariance
        ]
    }
}
