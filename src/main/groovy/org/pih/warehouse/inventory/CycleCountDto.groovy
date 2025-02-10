package org.pih.warehouse.inventory

class CycleCountDto {

    String id

    String status

    List<CycleCountItemDto> cycleCountItems

    static CycleCountDto asDto(CycleCount cycleCount) {
        return new CycleCountDto(
                id: cycleCount.id,
                status: cycleCount.status.toString(),
                cycleCountItems: cycleCount.cycleCountItems.collect { it.toDto() }
        )
    }
}
