package org.pih.warehouse.inventory

class CycleCountDto {

    String id

    String status

    String requestId

    List<CycleCountItemDto> cycleCountItems

    static CycleCountDto toDto(CycleCount cycleCount) {
        return new CycleCountDto(
                id: cycleCount.id,
                requestId: cycleCount?.cycleCountRequest?.id,
                status: cycleCount.status.toString(),
                cycleCountItems: cycleCount.cycleCountItems.collect { it.toDto() }
        )
    }
}
