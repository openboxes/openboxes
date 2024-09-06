package org.pih.warehouse.outbound

import grails.util.Holders
import grails.validation.Validateable
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.pih.warehouse.CommandUtils

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


    ImportPackingListErrors getErrorMessages() {
        ImportPackingListErrors errors = new ImportPackingListErrors()
        buildErrors(errors)
        return errors
    }

    void buildErrors(ImportPackingListErrors errorsInstance) {
        // Build errors instance for each sub-step (fulfillmentDetails, sendingOptions, list of items)
        // Errors will be grouped by the sub-step and by fields, e.g.: { fulfillmentDetails: { origin: ["Cannot be null"] } }
        errorsInstance.fulfillmentDetails = CommandUtils.buildErrorsGroupedByField(fulfillmentDetails.errors)
        errorsInstance.sendingOptions = CommandUtils.buildErrorsGroupedByField(sendingOptions.errors)
        // If the packing list is empty and validator has detected it, return generalErrors that will contain the info of packingList being empty
        if (packingList.empty && errors.hasFieldErrors("packingList")) {
            ApplicationTagLib g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
            errorsInstance.packingList["generalErrors"] = errors.getFieldErrors("packingList").collect { g.message(error: it) }
            return
        }
        packingList.each { ImportPackingListItem item ->
            if (item.hasErrors()) {
                // Build errors for each row separately. Row id is the key.
                // e.g. { 2: { binLocation: ["Bin location cannot be null"] } }
                errorsInstance.packingList[item.rowId] = CommandUtils.buildErrorsGroupedByField(item.errors)
            }
        }
    }
}


class ImportPackingListErrors {

    Map<String, List<String>> fulfillmentDetails

    Map<String, List<String>> sendingOptions

    Map<String, Map<String, Map<String, List<String>>>> packingList = new HashMap<>()
}
