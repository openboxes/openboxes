package org.pih.warehouse.outbound

import grails.databinding.BindUsing
import grails.validation.Validateable
import org.pih.warehouse.core.Constants
import org.pih.warehouse.shipping.ShipmentType

class ShippingRequest implements Validateable {

    @BindUsing({ obj, source -> Constants.DELIVERY_DATE_FORMATTER.parse(source['expectedShippingDate']) })
    Date expectedShippingDate

    ShipmentType shipmentType

    String trackingNumber

    Date expectedDeliveryDate

    static constraints = {
        expectedShippingDate(validator: { value, obj ->
            // Check if shipping date happens before the expected delivery date (if delivery date specified)
            if (obj.expectedDeliveryDate && value.after(obj.expectedDeliveryDate + 1)) {
                return ['shippingDateAfterDelivery']
            }
            return true
        })
        trackingNumber(nullable: true, maxSize: 255)
        expectedDeliveryDate(blank: false)
    }
}
