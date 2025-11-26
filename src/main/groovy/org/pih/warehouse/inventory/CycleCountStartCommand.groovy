package org.pih.warehouse.inventory

import grails.validation.Validateable

import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability

class CycleCountStartCommand implements Validateable {

    CycleCountRequest cycleCountRequest

    static constraints = {
        cycleCountRequest(validator: { CycleCountRequest cycleCountRequest ->
            CycleCount cycleCount = cycleCountRequest.cycleCount

            // If a product is in a pending outbound, we don't allow you to proceed with the count unless it has
            // already been started (ie the status is past 'REQUESTED'). There is too much uncertainty when some of
            // the stock allocated, so to avoid confusion we require the outbound to be resolved first.
            if (!cycleCount || cycleCount.status == CycleCountStatus.REQUESTED) {
                Product product = cycleCountRequest.product
                int quantityAllocatedForProduct = ProductAvailability.findAllByLocationAndProduct(
                        cycleCountRequest.facility, product)?.sum{ it.quantityAllocated ?: 0 } as Integer ?: 0
                if (quantityAllocatedForProduct > 0) {
                    return ['hasQuantityAllocated', product.productCode]
                }
            }
            if (!cycleCount) {
                // When first starting a count, the cycle count object won't exist yet, so this is valid.
                return true
            }
            if (!cycleCount.status.isCounting()) {
                return ['invalidCycleCountStatus', cycleCount.status]
            }
        })
    }
}
