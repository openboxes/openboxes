package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.allocation.AllocationMode
import org.pih.warehouse.allocation.AllocationRequest
import org.pih.warehouse.allocation.AllocationResult
import org.pih.warehouse.allocation.AllocationSourceStrategy
import org.pih.warehouse.allocation.BackorderMatch
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.quartz.JobExecutionContext

class AutomaticBackorderReallocationJob {

    def shipmentService
    def allocationService
    def stockMovementService
    def putawayTaskService
    def backorderMatchingService

    def sessionRequired = false

    static triggers = {
        cron name: JobUtils.getCronName(AutomaticBackorderReallocationJob),
        cronExpression: JobUtils.getCronExpression(AutomaticBackorderReallocationJob)
    }

    def execute(JobExecutionContext context) {
        if (!Holders.config.openboxes.jobs.automaticBackorderReallocationJob.enabled) {
            log.info"Backorder re-allocation job is disabled"
            return
        }

        String shipmentId = context.mergedJobDataMap.get('shipmentId')
        if (shipmentId) {
            try {
                Shipment shipment = shipmentService.getShipmentInstance(shipmentId)
                if (!shipment) {
                    log.warn("Shipment ${shipmentId} not found, skipping")
                    return
                }
                if (shipment.shipmentItems?.any { it.backorderItem || it.backorderReference }) {
                    if (putawayTaskService.hasOpenCrossDockTask(shipment)) {
                        log.info("Shipment ${shipmentId} has an open cross-dock putaway, " +
                                "allocation is deferred until the stock reaches the cross-dock zone")
                        return
                    }
                    log.info("Handle backorder re-allocation for shipment ${shipmentId}")
                    def backorderItems = shipment.shipmentItems?.findAll { it.backorderReference || it.backorderItem }

                    List<AllocationSourceStrategy> strategies = []
                    Set<Requisition> backorders = []
                    backorderItems.forEach { ShipmentItem it ->
                        def backorderedRequisition = it.backorderItem?.requisition
                        if (!backorderedRequisition) {
                            backorderedRequisition = Requisition.findByRequestNumber(it.backorderReference)
                        }
                        if (backorderedRequisition) {
                            backorders.add(backorderedRequisition)
                        }
                    }

                    backorders.forEach { Requisition requisition ->
                        if (requisition.autoAllocationRequested) {
                            releaseCrossDockDemand(shipment, requisition, strategies)
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error processing shipment ${shipmentId}", e)
            }
        }
    }

    /**
     * Allocates a backorder once its cross-dock stock has landed in the cross-dock zone. The
     * quantity covered by the sales link comes from that zone, anything still missing may be taken
     * from ordinary stock, and whatever is left over stays backordered so the order waits for the
     * next delivery.
     */
    private void releaseCrossDockDemand(Shipment shipment, Requisition requisition, List<AllocationSourceStrategy> strategies) {
        boolean allocatedAnything = false

        List<BackorderMatch> matches = backorderMatchingService.match(
                requisition, backorderMatchingService.findInboundItemsForRequisition(shipment, requisition))
        Set<String> demandItemIds = matches.collect { it.demand?.id }.findAll() as Set

        if (!demandItemIds) {
            log.info("No demand covered by shipment ${shipment.shipmentNumber} on ${requisition.requestNumber}")
            return
        }

        requisition?.requisitionItems?.each { RequisitionItem requisitionItem ->
            if (!demandItemIds.contains(requisitionItem.id)) {
                return
            }

            Integer quantityOutstanding = backorderMatchingService.remainingDemand(requisitionItem)
            if (quantityOutstanding <= 0) {
                log.info("Requisition item ${requisitionItem.id} has no demand left, skipping")
                return
            }

            AllocationRequest allocationRequest = new AllocationRequest(
                    quantityRequired: quantityOutstanding,
                    requisitionItem: requisitionItem,
                    allocationMode: AllocationMode.AUTO,
                    allocationStrategies: strategies,
                    crossDockRelease: true)
            AllocationResult result = allocationService.allocate(allocationRequest)

            Integer quantityAllocated = result?.suggestedItems?.sum { it.quantityPicked } ?: 0
            if (quantityAllocated > 0) {
                allocatedAnything = true
            }
            updateQuantityBackordered(requisitionItem, quantityOutstanding, quantityAllocated)
            log.info("Cross-dock release allocated ${quantityAllocated} for requisition item ${requisitionItem.id}")
        }

        if (allocatedAnything) {
            stockMovementService.updateRequisitionStatus(requisition.id, RequisitionStatus.PICKING)
        }
    }

    private void updateQuantityBackordered(RequisitionItem requisitionItem, Integer quantityOutstanding,
                                           Integer quantityAllocated) {
        Integer quantityRemaining = Math.max(0, (quantityOutstanding ?: 0) - (quantityAllocated ?: 0))

        if (quantityRemaining > 0) {
            requisitionItem.quantityBackordered = quantityRemaining
            requisitionItem.backorderedReasonCode = ReasonCode.BACKORDER.toString()
        } else {
            requisitionItem.quantityBackordered = null
            requisitionItem.backorderedReasonCode = null
        }
        requisitionItem.save(failOnError: true)
    }
}
