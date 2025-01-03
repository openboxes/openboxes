package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.product.Product

class CycleCountRequestBatchCommand implements Validateable {

    List<CycleCountRequestCommand> requests

    static constraints = {
        requests(validator: { List<CycleCountRequestCommand> requests ->
            // First catch if any duplicates have been provided (e.g. 10006, 10006 in the payload)
            // This doesn't check though if any duplicate is already persisted, this is checked in the CCR command below (.each + .validate() clause)
            Map<String, List<Product>> duplicates = requests.product
                    .groupBy { it.productCode }
                    .findAll { it.value.size() > 1 }
            if (!duplicates.isEmpty()) {
                return ['duplicateExists', duplicates.keySet().toString()]
            }
            // Elements of a list are not validated by default, so proceed manual validation of every element in the list
            requests.each { CycleCountRequestCommand command -> command.validate() }
            // If any of elements have validation errors, throw an exception
            if (requests.any { it.hasErrors() }) {
                return false
            }
        })
    }
}
