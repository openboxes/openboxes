package org.pih.warehouse.inboundSortation

import org.pih.warehouse.core.Location
import org.pih.warehouse.requisition.DemandTypeCode
import org.pih.warehouse.requisition.OutboundDemand

class CrossDockingStrategy implements PutawayStrategy {

    def demandService

    @Override
    List<PutawayResult> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining) {
        Map<DemandTypeCode, List<OutboundDemand>> outboundDemandsMap = demandService.getDemandMap()

        List<PutawayResult> putawayTasks = []
        for (entry in outboundDemandsMap.entrySet()) {
            DemandTypeCode demandTypeCode = entry.key
            List<OutboundDemand> demandsForType = entry.value

            boolean isProductDemanded = demandsForType.any { it.product == context.product }

            if (isProductDemanded) {
                Location putawayLocation = locations.find { it.locationType?.name?.equalsIgnoreCase(demandTypeCode.code) }
                if (putawayLocation) {
                    putawayTasks << new PutawayResult(
                            facility: context.facility,
                            product: context.product,
                            inventoryItem: context.inventoryItem,
                            currentBinLocation: context.currentBinLocation,
                            putawayLocation: putawayLocation,
                            quantity: quantityRemaining
                    )
                }
            }
        }

        return putawayTasks
    }
}
