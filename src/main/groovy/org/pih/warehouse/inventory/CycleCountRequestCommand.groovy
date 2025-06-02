package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability
import org.springframework.web.context.request.RequestContextHolder

class CycleCountRequestCommand implements Validateable {

    Location facility

    Product product

    Boolean blindCount

    def beforeValidate() {
        String locationId = RequestContextHolder.getRequestAttributes().params?.facilityId
        facility = Location.findById(locationId)
    }

    static constraints = {
        product(nullable: true, validator: { Product product, CycleCountRequestCommand obj ->
            CycleCountRequest cycleCountRequest = CycleCountRequest.findByProductAndFacilityAndStatusNotInList(product, obj.facility, [CycleCountRequestStatus.COMPLETED, CycleCountRequestStatus.CANCELED])
            if (cycleCountRequest) {
                return ['duplicateExists', product.productCode]
            }

            // If a product is in a pending outbound, we don't allow you to proceed with the count. There is too much
            // stock uncertainty in that scenario so to avoid confusion we require the outbound to be resolved first.
            int quantityAllocatedForProduct = ProductAvailability.findAllByLocationAndProduct(obj.facility, product)
                    ?.sum{ it.quantityAllocated ?: 0 } as Integer ?: 0
            if (quantityAllocatedForProduct > 0) {
                return ['hasQuantityAllocated', product.productCode]
            }

            return true
        })
    }
}
