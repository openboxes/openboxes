package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
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
            // TODO: Most probably, this will have to be modified not to include completed cycle counts
            CycleCountRequest cycleCountRequest = CycleCountRequest.findByProductAndFacilityAndStatusNotInList(product, obj.facility, [CycleCountRequestStatus.COMPLETED, CycleCountRequestStatus.CANCELED])
            if (cycleCountRequest) {
                return ['duplicateExists', product.productCode]
            }
            return true
        })
    }
}
