package org.pih.warehouse.inventory

class CycleCountDto {

    String id

    String status

    List<CycleCountItemDto> cycleCountItems

    static CycleCountDto createFromCycleCountItems(List<CycleCountItem> items) {
        // Cycle count items have the same cycle count associated, so we can just look at first item in order to get the cycle count properties
        CycleCountItem item = items ? items.first() : null
        return new CycleCountDto(
                id: item?.cycleCount?.id,
                status: item?.cycleCount?.status?.name(),
                cycleCountItems: items.collect { it.toDto() }
        )
    }
}
