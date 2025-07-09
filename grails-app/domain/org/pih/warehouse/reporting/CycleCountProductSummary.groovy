package org.pih.warehouse.reporting

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class CycleCountProductSummary implements Serializable {

    String cycleCountId
    Date transactionDate
    Location facility
    Product product
    Integer quantityVariance

    static mapping = {
        version false
    }

    static constraints = {
        id composite: ['facility', 'product', 'cycleCountId']
    }

    Map toJson() {
        return [
                cycleCountId     : cycleCountId,
                transactionDate  : transactionDate,
                facility         : [
                        id  : facility?.id,
                        name: facility?.name
                ],
                product          : [
                        id  : product?.id,
                        name: product?.name
                ],
                quantityVariance : quantityVariance,
        ]
    }
}
