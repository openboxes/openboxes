package org.pih.warehouse.allocation

import grails.gorm.transactions.Transactional
import org.pih.warehouse.api.PickTaskStatus
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.picking.PickTaskService
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentService

@Transactional
class OutboundOrderService {

    private static final List<String> RELEASE_ORDER = PickTaskStatus.values()*.name()

    AllocationService allocationService
    PickTaskService pickTaskService
    StockMovementService stockMovementService
    ProductAvailabilityService productAvailabilityService
    ShipmentService shipmentService

    /**
     * Changes the quantity of a line on an order that is already being picked, picked or staged. A decrease
     * releases the allocation, unpicked first, without moving any stock (the surplus is returned to storage
     * by hand), an increase raises a pick task for the difference only.
     */
    Map amend(String requisitionId, String requisitionItemId, Integer quantity, String reasonCode,
              Boolean crossDock, List<AllocationSourceStrategy> strategies) {
        RequisitionItem requisitionItem = RequisitionItem.get(requisitionItemId)
        Requisition requisition = requisitionItem?.requisition
        validate(requisitionId, requisition, requisitionItem, quantity, reasonCode)

        if (quantity != requisitionItem.quantity - (requisitionItem.quantityCanceled ?: 0)) {
            boolean isCrossDock = crossDock != null ? crossDock : requisitionItem.isBackordered()
            updateQuantity(requisitionItem, quantity, reasonCode)
            releaseExcessAllocation(requisitionItem, quantity)

            // A cross-dock line waits for its inbound for whatever is not allocated yet
            Integer quantityBackordered = isCrossDock ? Math.max(0, quantity - requisitionItem.calculateQuantityAllocated()) : 0
            requisitionItem.quantityBackordered = quantityBackordered ?: null
            requisitionItem.backorderedReasonCode = quantityBackordered ? ReasonCode.BACKORDER.toString() : null

            allocateOutstanding(requisitionItem, strategies)
            requisitionItem.save(flush: true, failOnError: true)

            if (requisition.shipment) {
                stockMovementService.refreshShipmentItems(requisitionItem, requisition.shipment)
            }
            refreshStatus(requisition)
            requisition.save(flush: true, failOnError: true)
        }

        return [
                requisitionStatus  : requisition.status?.name(),
                quantityUnallocated: Math.max(0, requisitionItem.calculateQuantityRequired() - requisitionItem.calculateQuantityAllocated()),
        ]
    }

    private static void validate(String requisitionId, Requisition requisition, RequisitionItem requisitionItem,
                                 Integer quantity, String reasonCode) {
        if (!requisition || requisition.id != requisitionId) {
            throw new IllegalArgumentException("Item ${requisitionItem?.id} not found on outbound order ${requisitionId}")
        }
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Quantity must be zero or greater")
        }
        if (!(requisition.status in [RequisitionStatus.PICKING, RequisitionStatus.PICKED, RequisitionStatus.STAGED])) {
            throw new IllegalStateException("Outbound order ${requisition.requestNumber} is ${requisition.status}, " +
                    "only an order being picked, picked or staged can be amended this way")
        }
        if (requisitionItem.modificationItem || requisitionItem.substitutionItems || requisitionItem.parentRequisitionItem) {
            throw new IllegalStateException("Item ${requisitionItem.product?.productCode} has been revised or substituted " +
                    "and cannot be amended")
        }
        if (quantity < requisitionItem.quantity && !reasonCode) {
            throw new IllegalArgumentException("A reason code is required to decrease the quantity")
        }
    }

    private static void updateQuantity(RequisitionItem requisitionItem, Integer quantity, String reasonCode) {
        if (quantity > requisitionItem.quantity) {
            requisitionItem.quantity = quantity
        }
        requisitionItem.quantityCanceled = (requisitionItem.quantity - quantity) ?: null
        requisitionItem.cancelReasonCode = requisitionItem.quantityCanceled ? reasonCode : null
        requisitionItem.quantityApproved = quantity
    }

    private void releaseExcessAllocation(RequisitionItem requisitionItem, Integer quantity) {
        Integer excess = requisitionItem.calculateQuantityAllocated() - quantity
        if (excess <= 0) {
            return
        }

        PicklistItem.findAllByRequisitionItem(requisitionItem)
                .sort { RELEASE_ORDER.indexOf(it.status) }
                .each { PicklistItem picklistItem ->
                    Integer unpicked = Math.min(excess, picklistItem.quantityRemaining)
                    Integer picked = Math.min(excess - unpicked, picklistItem.quantityPicked ?: 0)
                    if (!unpicked && !picked) {
                        return
                    }
                    excess -= unpicked + picked

                    picklistItem.disableRefresh = Boolean.TRUE
                    picklistItem.quantity -= unpicked
                    if (picked) {
                        picklistItem.quantityPicked -= picked
                        picklistItem.quantity = picklistItem.quantityPicked
                        picklistItem.reasonCode = null
                    }

                    if (!picklistItem.quantity) {
                        picklistItem.picklist?.removeFromPicklistItems(picklistItem)
                        requisitionItem.removeFromPicklistItems(picklistItem)
                        picklistItem.delete()
                    } else if (picklistItem.status == PickTaskStatus.PICKING.name() && !picklistItem.quantityRemaining) {
                        picklistItem.status = PickTaskStatus.PICKED.name()
                    }
                }

        requisitionItem.save(flush: true)
        productAvailabilityService.refreshProductsAvailability(requisitionItem.requisition.origin?.id,
                [requisitionItem.product?.id], false)
    }

    private void allocateOutstanding(RequisitionItem requisitionItem, List<AllocationSourceStrategy> strategies) {
        Integer quantityOutstanding = requisitionItem.calculateQuantityRequired() - requisitionItem.calculateQuantityAllocated()
        if (quantityOutstanding <= 0) {
            return
        }

        AllocationResult result = allocationService.allocate(new AllocationRequest(
                quantityRequired: quantityOutstanding,
                requisitionItem: requisitionItem,
                allocationMode: AllocationMode.AUTO,
                allocationStrategies: strategies,
                keepExistingAllocation: true), false)

        List<PicklistItem> picklistItems = PicklistItem.findAllByRequisitionItem(requisitionItem)
        result.suggestedItems.each { SuggestedItem suggestedItem ->
            // A task nobody has started yet is topped up rather than joined by a second one for the same bin
            PicklistItem pendingItem = picklistItems.find {
                (it.status ?: PickTaskStatus.PENDING.name()) == PickTaskStatus.PENDING.name() && !it.quantityPicked &&
                        it.inventoryItem?.id == suggestedItem.inventoryItem?.id && it.binLocation?.id == suggestedItem.binLocation?.id
            }
            if (pendingItem) {
                stockMovementService.createOrUpdatePicklistItem(requisitionItem, pendingItem, pendingItem.inventoryItem,
                        pendingItem.binLocation, 0, null, pendingItem.comment, true,
                        pendingItem.quantity + suggestedItem.quantityPicked.intValue())
            } else {
                stockMovementService.allocateSuggestedItems(requisitionItem, [suggestedItem])
            }
        }
        stockMovementService.allocatePrepickItems(requisitionItem, result.prepickItems)
    }

    private void refreshStatus(Requisition requisition) {
        if (requisition.requisitionItems.every { it.isCanceled() }) {
            requisition.shipments?.toArray()?.each { Shipment shipment ->
                requisition.removeFromShipments(shipment)
                shipmentService.deleteShipment(shipment)
            }
            requisition.status = RequisitionStatus.CANCELED
            return
        }

        Collection<PicklistItem> picklistItems = requisition.picklist?.picklistItems ?: []
        boolean waitsForBackorder = requisition.partialAllocationAllowed && requisition.requisitionItems.any { it.isBackordered() }

        if (waitsForBackorder || picklistItems.any { it.quantityRemaining > 0 }) {
            requisition.status = RequisitionStatus.PICKING
        } else if (picklistItems && picklistItems.every { it.status == PickTaskStatus.STAGED.name() }) {
            if (requisition.status != RequisitionStatus.STAGED) {
                pickTaskService.stageRequisition(requisition)
            }
        } else if (picklistItems && requisition.status == RequisitionStatus.PICKING &&
                picklistItems.every { it.status in [PickTaskStatus.PICKED.name(), PickTaskStatus.STAGED.name()] }) {
            requisition.status = RequisitionStatus.PICKED
        }
    }
}
