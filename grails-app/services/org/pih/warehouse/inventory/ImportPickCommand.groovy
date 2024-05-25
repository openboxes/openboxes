package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.apache.commons.lang.math.NumberUtils

class ImportPickCommand implements Validateable {

    String id
    String code
    String name
    String lot
    String expiration
    String binLocation
    String quantity

    static constraints = {
        id(nullable: false, blank: false)
        code(nullable: false, blank: false)
        name(nullable: true)
        lot(nullable: true, validator: { val, obj ->
            if (val && val.toUpperCase().contains("E") &&  NumberUtils.isNumber(val)) {
                return ['scientificNotation.error']
            }
            return true
        })
        expiration(nullable: true)
        binLocation(nullable: true)
        quantity(nullable: false, blank: false)
    }
}
