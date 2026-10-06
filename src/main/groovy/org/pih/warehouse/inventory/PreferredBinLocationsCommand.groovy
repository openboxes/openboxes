package org.pih.warehouse.inventory

import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.validation.ObjectValidatable
import org.pih.warehouse.product.Product

/**
 * Fetches the preferred bin locations that are configured on the inventory levels of the given products
 * at a given facility.
 */
class PreferredBinLocationsCommand implements ObjectValidatable {

    Location facility

    List<Product> products

    def beforeValidate() {
        String facilityId = RequestContextHolder.getRequestAttributes().params?.facilityId
        facility = Location.read(facilityId)
    }

    static constraints = {
        // Collections are exempt from the default nullable: false, so it has to be declared explicitly
        products(nullable: false, minSize: 1)
    }
}
