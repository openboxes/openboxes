package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class CycleCountRequestCommand implements Validateable {

    Location facility

    List<Product> products

    Boolean blindCount

    static constraints = {
        products(nullable: true, validator: { List<Product> products, CycleCountRequestCommand obj ->
            String productCode = null
            boolean duplicateExists = products.any { Product product ->
                // FIXME: Most probably, this will have to be modified not to include completed cycle counts
                CycleCountRequest cycleCountRequest = CycleCountRequest.findByProductAndFacility(product, obj.facility)
                if (cycleCountRequest) {
                    productCode = product.productCode
                    return true
                }
                return false
            }
            if (duplicateExists) {
                return ['duplicateExists', productCode]
            }
            return true
        })
    }
}
