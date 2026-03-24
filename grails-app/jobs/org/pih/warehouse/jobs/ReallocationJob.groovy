package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.allocation.AllocationMode
import org.pih.warehouse.allocation.AllocationRequest
import org.pih.warehouse.allocation.AllocationResult
import org.pih.warehouse.allocation.AllocationStrategy
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.quartz.JobExecutionContext

class ReallocationJob {

    def shipmentService
    def allocationService
    def stockMovementService

    def sessionRequired = false

    static triggers = {
        cron name: JobUtils.getCronName(ReallocationJob),
        cronExpression: JobUtils.getCronExpression(ReallocationJob)
    }

    def execute(JobExecutionContext context) {
        if (!Holders.config.openboxes.jobs.reallocationJob.enabled) {
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
                    log.info("Handle backorder re-allocation for shipment ${shipmentId}")
                    def backorderItems = shipment.shipmentItems?.findAll { it.backorderReference || it.backorderItem }

                    List<AllocationStrategy> strategies = [AllocationStrategy.WAREHOUSE_FIRST]
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
                        if (requisition.autoAllocationEnabled) {
                            List<AllocationResult> result = []
                            requisition?.requisitionItems?.each { requisitionItem ->
                                def picklistItems = requisitionItem.picklistItems
                                if (!picklistItems || picklistItems.isEmpty()) {
                                    AllocationRequest allocationRequest = new AllocationRequest(requisitionItem: requisitionItem, allocationMode: AllocationMode.AUTO, allocationStrategies: strategies)
                                    AllocationResult singleResult = allocationService.allocate(allocationRequest)
                                    result.add(singleResult)
                                }
                            }
                            if (result && !result.empty) {
                                requisition.requisitionItems.forEach {
                                    it.quantityBackordered = null
                                    it.backorderedReasonCode = null
                                }
                                stockMovementService.updateRequisitionStatus(requisition.id, RequisitionStatus.PICKING)
                            }
                            log.info("Re-allocate ${result}")
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error processing shipment ${shipmentId}", e)
            }
        }
    }
}
