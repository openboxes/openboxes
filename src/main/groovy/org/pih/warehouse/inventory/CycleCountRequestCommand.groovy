package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class CycleCountRequestCommand implements Validateable {

    Location facility

    Product product

    Boolean blindCount

    static constraints = {
        product(nullable: true, validator: { Product product, CycleCountRequestCommand obj ->
            // TODO: Most probably, this will have to be modified not to include completed cycle counts
            CycleCountRequest cycleCountRequest = CycleCountRequest.findByProductAndFacility(product, obj.facility)
            if (cycleCountRequest) {
                return ['duplicateExists', product.productCode]
            }
            return true
        })
    }
}
