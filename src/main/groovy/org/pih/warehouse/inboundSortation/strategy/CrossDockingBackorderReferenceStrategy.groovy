package org.pih.warehouse.inboundSortation.strategy

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inboundSortation.PutawayContext
import org.pih.warehouse.inboundSortation.PutawayResult
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem

import javax.xml.bind.ValidationException

class CrossDockingBackorderReferenceStrategy implements PutawayStrategy {

    @Override
    List<PutawayResult> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining, List<PutawayResult> putawayResults) {

        List<PutawayResult> putawayTasks = []
        if (!context.backorderReference && !context.backorderItem) {
            return putawayTasks
        }

        def requisitionItem = context.backorderItem
        if (!requisitionItem) {
            Requisition backorder = Requisition.findByRequestNumber(context.backorderReference)
            if (!backorder) {
                throw new ValidationException("No backorder found for '${context.backorderReference}' backorderReference")
            }
            requisitionItem = backorder.requisitionItems.find {
                it.product == context.product && it.quantity <= context.quantity && !it.isAllocated()
            } as RequisitionItem
        }
        if (!requisitionItem) {
            throw new ValidationException("No match for product '${context.product}' and quantity '${context.quantity}'")
        }

        // delivery type is only known once the backorder/requisition has been resolved, so populate it on the context
        // before delegating the container assignment to the facility-configured strategy
        context.deliveryTypeCode = requisitionItem.requisition.deliveryTypeCode
        ActivityCode deliveryActivityCode = context.deliveryTypeCode?.activityCode
        Location destination = locations.find { (deliveryActivityCode && it.supports(deliveryActivityCode)) && it.supports(ActivityCode.STAGING_LOCATION) }
        if (destination) {
            putawayTasks << new PutawayResult(
                    facility: context.facility,
                    product: context.product,
                    inventoryItem: context.inventoryItem,
                    location: context.currentBinLocation,
                    destination: destination,
                    container: resolvePutawayContainer(context, locations, destination),
                    quantity: requisitionItem.quantity,
            )
        }
        return putawayTasks
    }
}
