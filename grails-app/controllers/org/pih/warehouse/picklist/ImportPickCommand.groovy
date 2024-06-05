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
    String quantity

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
        quantity(nullable: false, validator: { val, obj ->
            if (!NumberUtils.isNumber(val)) {
                return ['notANumber.error']
            }
            if (Integer.parseInt(val) < 0) {
                return ['negative.error']
            }
            return true
        })
        quantityAsNumber(nullable: true)
    }

    void setQuantity(String quantity) {
        this.quantity = quantity
    }

    void setQuantity(Integer quantity) {
        this.quantity = String.valueOf(quantity)
    }

    Integer getQuantityAsNumber() {
        return NumberUtils.isNumber(this.quantity) ? Integer.parseInt(this.quantity) : null
    }
}
