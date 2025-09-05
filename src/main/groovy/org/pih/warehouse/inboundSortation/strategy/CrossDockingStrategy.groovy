package org.pih.warehouse.inboundSortation.strategy

import org.pih.warehouse.core.Location
import org.pih.warehouse.inboundSortation.DemandTypeCode
import org.pih.warehouse.inboundSortation.OutboundDemand
import org.pih.warehouse.inboundSortation.PutawayContext
import org.pih.warehouse.inboundSortation.PutawayResult

class CrossDockingStrategy implements PutawayStrategy {

    def demandService

    @Override
    List<PutawayResult> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining) {
        int availableQuantity = quantityRemaining
        Map<DemandTypeCode, List<OutboundDemand>> outboundDemandsMap = demandService.getDemandMap()

        List<PutawayResult> putawayTasks = []

        if (availableQuantity <= 0) {
            return putawayTasks
        }

        List<OutboundDemand> relevantDemands = outboundDemandsMap.values()
            .flatten()
            .findAll { it.product == context.product } as List<OutboundDemand>

        for (OutboundDemand demand in relevantDemands) {
            Location putawayLocation = locations.find {
                it.name.toLowerCase().startsWith(demand.demandTypeCode.code.toLowerCase())
            }
            if (putawayLocation) {
                int quantityForDemand = Math.min(demand.quantity, availableQuantity)
                putawayTasks << new PutawayResult(
                        facility: context.facility,
                        product: context.product,
                        inventoryItem: context.inventoryItem,
                        location: context.currentBinLocation,
                        destination: putawayLocation,
                        quantity: quantityForDemand
                )

                availableQuantity -= quantityForDemand

                if (availableQuantity <= 0) {
                    break
                }
            }
        }

        return putawayTasks
    }
}
