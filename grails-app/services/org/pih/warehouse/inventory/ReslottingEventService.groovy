package org.pih.warehouse.inventory

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.Putaway
import org.pih.warehouse.api.PutawayItem
import org.pih.warehouse.api.PutawayStatus
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.User
import org.pih.warehouse.inboundSortation.PutawayContext
import org.pih.warehouse.inboundSortation.PutawayResult
import org.pih.warehouse.inboundSortation.SlottingService
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderIdentifierService
import org.pih.warehouse.product.Product
import org.pih.warehouse.putaway.PutawayService
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

import javax.transaction.Transactional

@Transactional
class ReslottingEventService {

    SlottingService slottingService
    PutawayService putawayService
    OrderIdentifierService orderIdentifierService
    ProductAvailabilityService productAvailabilityService
    LocationService locationService

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onReslottingEvent(ReslottingEvent event) {
        log.info "Application event $event has been published! " + event.properties

        InventoryLevel inventoryLevel = InventoryLevel.get(event.source)
        if (!inventoryLevel) {
            log.warn "InventoryLevel with id ${event.source} not found, cannot trigger reslotting"
            return
        }

        if (inventoryLevel.internalLocation?.supports(ActivityCode.UNDEFINED_LOCATION)) {
            // the update hasn't changed internalLocation to anything specific; no reslotting
            return
        }

        List<Location> binLocations = locationService.getLocationsSupportingActivity(ActivityCode.UNDEFINED_LOCATION)
        binLocations.each { Location bin ->
            List<AvailableItem> availableItems = productAvailabilityService.getAvailableItems(bin).findAll {
                it?.inventoryItem?.product == inventoryLevel.product
            }
            availableItems.each { AvailableItem availableItem ->
                ValuesPutawayContext valuesPutawayContext = createValuesPutawayContext(inventoryLevel, availableItem)
                executeSlotting(valuesPutawayContext, event.updatedByUserId)
            }
        }
    }

    @grails.gorm.transactions.Transactional(propagation = Propagation.REQUIRES_NEW)
    private void executeSlotting(ValuesPutawayContext valuesPutawayContext, String updatedByUserId) {
        PutawayContext putawayContext = createPutawayContext(valuesPutawayContext)
        List<PutawayResult> results = slottingService.execute(putawayContext)
        User user = User.load(updatedByUserId)
        results.each { PutawayResult result ->
            if (result.quantity > 0) {
                Putaway putaway = createPutaway(putawayContext, user)
                PutawayItem putawayItem = createPutawayItem(result)
                putaway.putawayItems.add(putawayItem)
                putawayService.savePutaway(putaway)
            }
        }
    }

    private PutawayContext createPutawayContext(ValuesPutawayContext valuesPutawayContext) {
        new PutawayContext(
                facility: Location.get(valuesPutawayContext.facilityId),
                product: Product.get(valuesPutawayContext.productId),
                inventoryItem: InventoryItem.get(valuesPutawayContext.inventoryItemId),
                lotNumber: valuesPutawayContext.lotNumber,
                expirationDate: valuesPutawayContext.expirationDate,
                currentBinLocation: Location.get(valuesPutawayContext.currentBinLocationId),
                preferredBin: Location.get(valuesPutawayContext.preferredBinId),
                internalLocation: Location.get(valuesPutawayContext.internalLocationId),
                quantity: valuesPutawayContext.quantity,
        )
    }

    private ValuesPutawayContext createValuesPutawayContext(InventoryLevel inventoryLevel, AvailableItem availableItem) {
        new ValuesPutawayContext(
                facilityId: availableItem.binLocation?.parentLocation?.id,
                productId: inventoryLevel.product?.id,
                inventoryItemId: availableItem.inventoryItem?.id,
                lotNumber: availableItem.inventoryItem.lotNumber,
                expirationDate: availableItem.inventoryItem.expirationDate,
                currentBinLocationId: availableItem.binLocation?.id,
                preferredBinId: inventoryLevel.preferredBinLocation?.id,
                internalLocationId: inventoryLevel.internalLocation?.id,
                quantity: availableItem.quantityOnHand,
        )
    }

    private Putaway createPutaway(PutawayContext putawayContext, User createdBy) {
        new Putaway(
                origin: putawayContext.facility,
                destination: putawayContext.facility,
                putawayNumber: orderIdentifierService.generate(new Order()),
                putawayAssignee: createdBy,
                putawayStatus: PutawayStatus.PENDING
        )
    }

    private PutawayItem createPutawayItem(PutawayResult task) {
        new PutawayItem(
                product: task.product,
                inventoryItem: task.inventoryItem,
                quantity: task.quantity,
                currentFacility: task.facility,
                currentLocation: task.location,
                putawayLocation: task.destination,
                containerLocation: task.container,
                putawayStatus: PutawayStatus.PENDING,
                comment: task.comment
        )
    }

    private class ValuesPutawayContext {
        String facilityId
        String productId
        String inventoryItemId
        String lotNumber
        Date expirationDate
        String currentBinLocationId
        String preferredBinId
        String internalLocationId
        Integer quantity
    }
}
