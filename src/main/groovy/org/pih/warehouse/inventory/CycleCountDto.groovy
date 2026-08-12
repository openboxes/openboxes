package org.pih.warehouse.inventory

import com.fasterxml.jackson.annotation.JsonProperty

import org.pih.warehouse.core.Person

import java.time.LocalDate

import org.pih.warehouse.core.dtos.IdentifiableDto

class CycleCountDto implements IdentifiableDto {

    String status

    String requestId

    List<CycleCountItemDto> cycleCountItems

    CountDto initialCount

    CountDto verificationCount

    static CycleCountDto toDto(CycleCount cycleCount) {
        CycleCountRequest cycleCountRequest = cycleCount?.cycleCountRequest

        return new CycleCountDto(
                id: cycleCount.id,
                requestId: cycleCountRequest?.id,
                initialCount: new CountDto(
                        assignee: cycleCountRequest?.countAssignee,
                        deadline: cycleCountRequest?.countDeadline
                ),
                verificationCount: new CountDto(
                        assignee: cycleCountRequest?.recountAssignee,
                        deadline: cycleCountRequest?.recountDeadline
                ),
                status: cycleCount.status.toString(),
                cycleCountItems: cycleCount.cycleCountItems.collect { CycleCountItemDto.from(it) }
        )
    }

    /**
     * @return The largest count index of all the cycle count items. Helps determine what count we're on.
     */
    @JsonProperty("maxCountIndex")
    Integer getMaxCountIndex() {
        return cycleCountItems.max{ it.countIndex }?.countIndex
    }
}

class CountDto {
    Person assignee
    LocalDate deadline
}
