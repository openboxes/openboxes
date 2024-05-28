package org.pih.warehouse.picklist

import grails.validation.Validateable
import org.apache.commons.lang.math.NumberUtils

class ImportPickCommand implements Validateable {

    String id
    String code
    String name
    String lotNumber
    Date expirationDate
    String binLocation
    Integer quantity

    static constraints = {
        id(nullable: false, blank: false)
        code(nullable: false, blank: false)
        name(nullable: true)
        lotNumber(nullable: true, validator: { val, obj ->
            if (val && val.toUpperCase().contains("E") && NumberUtils.isNumber(val)) {
                return ['scientificNotation.error']
            }
            return true
        })
        expirationDate(nullable: true)
        binLocation(nullable: true)
        quantity(nullable: false, min: 0)
    }
}
