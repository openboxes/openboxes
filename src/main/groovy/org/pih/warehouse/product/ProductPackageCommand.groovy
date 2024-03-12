package org.pih.warehouse.product

import grails.validation.Validateable
import org.pih.warehouse.core.UnitOfMeasure
import util.ConfigHelper

class ProductPackageCommand implements Validateable {

    ProductSupplier productSupplier

    UnitOfMeasure uom

    // Package size
    Integer productPackageQuantity

    BigDecimal minOrderQuantity

    // Package price
    BigDecimal productPackagePrice

    BigDecimal contractPricePrice

    Date contractPriceValidUntil

    Boolean tieredPricing = false

    static constraints = {
        productPackageQuantity(min: 0)
        minOrderQuantity(nullable: true, min: 0.0)
        productPackagePrice(nullable: true, min: 0.0)
        contractPricePrice(nullable: true, min: 0.0)
        contractPriceValidUntil(nullable: true, validator: { Date date ->
            if (date) {
                Date minDate = ConfigHelper.getMinimumExpirationDate()
                return date > minDate
            }
            return true
        })
    }
}
