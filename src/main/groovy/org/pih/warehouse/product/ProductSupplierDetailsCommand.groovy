package org.pih.warehouse.product

import grails.validation.Validateable
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.RatingTypeCode

class ProductSupplierDetailsCommand implements Validateable {

    Product product

    Organization supplier

    Organization manufacturer

    String code

    String name

    String supplierCode

    String manufacturerCode

    Boolean active

    String description

    String brandName

    RatingTypeCode ratingTypeCode

    String productCode

    static constraints = {
        importFrom ProductSupplier
        supplier(nullable: false)
        supplierCode(nullable: true, blank: false)
        name(nullable: false, blank: false)
    }
}
