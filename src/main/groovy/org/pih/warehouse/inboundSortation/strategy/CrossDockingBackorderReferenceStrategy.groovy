package org.pih.warehouse.inboundSortation.strategy

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inboundSortation.PutawayContext
import org.pih.warehouse.inboundSortation.PutawayResult
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem

import javax.xml.bind.ValidationException

class CrossDockingBackorderReferenceStrategy implements PutawayStrategy {

    @Override
    List<PutawayResult> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining) {

        List<PutawayResult> putawayTasks = []

        Requisition backorder = Requisition.findByRequestNumber(context.backorderReference)
        if (!backorder) {
            throw new ValidationException("No backorder found for '${context.backorderReference}' backorderReference")
        }

        RequisitionItem requisitionItem = context.backorderItem
        if (!requisitionItem) {
            requisitionItem = backorder.requisitionItems.find {
                it.product == context.product && it.quantity == context.quantity && !it.isAllocated()
            }
        }
        if (!requisitionItem) {
            throw new ValidationException("No match for product '${context.product}' and quantity '${context.quantity}'")
        }

        ActivityCode deliveryTypeCode = backorder.deliveryTypeCode.activityCode
        Location putawayLocation = locations.find { (deliveryTypeCode && it.supports(deliveryTypeCode)) && it.supports(ActivityCode.STAGING_LOCATION) }
        if (putawayLocation) {
            putawayTasks << new PutawayResult(
                    facility: context.facility,
                    product: context.product,
                    inventoryItem: context.inventoryItem,
                    location: context.currentBinLocation,
                    destination: putawayLocation,
                    quantity: context.quantity,
            )
        }
        return putawayTasks
    }
}
