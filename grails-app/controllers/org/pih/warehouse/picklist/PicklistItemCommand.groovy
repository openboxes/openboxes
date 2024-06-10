package org.pih.warehouse.picklist

import grails.validation.Validateable
import org.apache.commons.lang.math.NumberUtils
import org.pih.warehouse.core.Constants

class PicklistItemCommand implements Validateable {

    String id
    String code
    String name
    String lotNumber
    Date expirationDate
    String binLocation
    String quantityAsText

    static transients = ['quantity']

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
        quantityAsText(nullable: false, validator: { val, obj ->
            if (!NumberUtils.isNumber(val)) {
                return ['invalid.error']
            }
            if (Integer.parseInt(val) < 0) {
                return ['negative.error']
            }
            return true
        })
    }

    boolean getIsDefaultBinLocation() {
        return !binLocation || binLocation?.equalsIgnoreCase(Constants.DEFAULT_BIN_LOCATION_NAME)
    }

    Integer getQuantity() {
        return NumberUtils.isNumber(this.quantityAsText) ? Integer.parseInt(this.quantityAsText) : null
    }
}
