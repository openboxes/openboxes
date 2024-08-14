package org.pih.warehouse.outbound

import grails.validation.Validateable

class ImportPackingListCommand implements Validateable {

    FulfillmentRequest fulfillmentDetails

    ShippingRequest sendingOptions

    List<ImportPackingListItem> packingList

    static constraints = {
        fulfillmentDetails(validator: { FulfillmentRequest fulfillmentDetails ->
            return fulfillmentDetails.validate()
        })
        sendingOptions(validator: { ShippingRequest sendingOptions ->
            return sendingOptions.validate()
        })
        packingList(validator: { List<ImportPackingListItem> packingList ->
            if (packingList.empty) {
                return ['empty']
            }
            // Loop through every item and validate it
            packingList.each { ImportPackingListItem item -> item.validate() }
            // Check if every item passed the validation
            return packingList.every { ImportPackingListItem item -> !item.hasErrors() }
        })
    }
}
